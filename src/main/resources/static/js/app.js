/**
 * EnergiKu — Core Application Script
 * Mengatur AJAX Fetch API, manipulasi DOM, kalkulasi emisi, dan grafik Chart.js
 */

// Simpan instance chart secara global agar bisa di-update nantinya
let dashboardChart = null;

/**
 * Fungsi Utama: Menginisialisasi seluruh komponen Dashboard dengan mengambil data asli dari Database
 */
function initDashboard() {
    console.log("EnergiKu: Mengambil data dari phpMyAdmin...");
    
    // Panggil API Spring Boot menggunakan Fetch
    fetch('/api/logs')
        .then(response => response.json())
        .then(dataFromDb => {
            if (dataFromDb.length === 0) {
                console.log("Database kosong, belum ada data laporan.");
                // Jika data kosong, panggil visualisasi dengan array kosong agar widget bernilai 0
                kalkulasiDanUpdateDashboard([]);
                renderEnergyChart([]);
                return;
            }
            
            // Jalankan kalkulasi & gambar grafik berdasarkan data asli database
            kalkulasiDanUpdateDashboard(dataFromDb);
            renderEnergyChart(dataFromDb);
        })
        .catch(error => console.error("Gagal memuat data dari database:", error));
}

/**
 * Fungsi Perhitungan Akumulasi Akhir & Logika Warning Dampak Lingkungan
 */
function kalkulasiDanUpdateDashboard(dataLog) {
    let totalBiaya = 0;
    let totalCarbon = 0;
    let totalHari = dataLog.length;
    
    let hasListrik = false;
    let hasBbm = false;
    let hasAir = false;

    dataLog.forEach((log, index) => {
        totalBiaya += log.biaya || 0;
        totalCarbon += log.co2 || 0;

        if (index === dataLog.length - 1) {
            if (log.listrik > 0) hasListrik = true;
            if (log.bbm > 0) hasBbm = true;
            if (log.air > 0) hasAir = true;
        }
    });

    // --- 1. UPDATE WIDGET ---
    document.getElementById("widgetBiaya").innerText = "Rp " + totalBiaya.toLocaleString('id-ID');
    document.getElementById("widgetCarbon").innerText = totalCarbon.toFixed(1) + " kg";
    document.getElementById("widgetLog").innerText = totalHari + " Hari";

    // --- 2. LOGIKA ECO-SCORE GRADE (TAMBAHAN) ---
    const widgetScore = document.getElementById("widgetScore");
    const widgetScoreText = document.getElementById("widgetScoreText");
    let grade = "A+", desc = "Sangat Ramah", color = "#516631";

    if (totalCarbon > 200) { grade = "D"; desc = "Boros & Berbahaya"; color = "#e74c3c"; }
    else if (totalCarbon > 150) { grade = "C"; desc = "Perlu Perbaikan"; color = "#e67e22"; }
    else if (totalCarbon > 100) { grade = "B"; desc = "Cukup Baik"; color = "#cda97e"; }

    widgetScore.innerText = grade;
    widgetScore.style.color = color;
    widgetScoreText.innerText = desc;

    // --- 3. SISTEM PERINGATAN (WARNING) OTOMATIS ---
    const warningContainer = document.getElementById("warningContainer");
    if (totalCarbon > 150) {
        warningContainer.innerHTML = `
            <div style="background: #fff5f5; border: 1px solid #feb2b2; color: #c53030; padding: 20px; border-radius: 12px; margin-bottom: 24px; animation: fadeIn 0.5s;">
                <h4 style="margin: 0 0 8px 0;"><i class="fas fa-exclamation-triangle"></i> PERINGATAN BAHAYA</h4>
                <p style="margin: 0; font-size: 14px;">Total jejak karbon Anda <strong>${totalCarbon.toFixed(1)} kg CO2</strong>. Anda telah melampaui ambang batas aman (150 kg). Mohon kurangi penggunaan energi!</p>
            </div>
        `;
    } else {
        warningContainer.innerHTML = "";
    }

    // --- 4. UPDATE BARIS PROGRESS BAR ---
    const barFill = document.getElementById("targetBarFill");
    const maxTarget = 150;
    let percentage = Math.min((totalCarbon / maxTarget) * 100, 100);
    
    barFill.style.width = percentage + "%";
    barFill.style.backgroundColor = (totalCarbon > 150) ? "#e74c3c" : "#516631";
    document.getElementById("targetBarText").innerText = totalCarbon.toFixed(1) + " kg CO2";

    // --- STRATEGI 2: HITUNG KONVERSI POHON SECARA DINAMIS ---
    let butuhPohon = Math.ceil(totalCarbon / 22); // 1 pohon dewasa menyerap 22 kg CO2/tahun
    if (totalCarbon === 0) butuhPohon = 0;
    
    const textJumlahPohon = document.getElementById("textJumlahPohon");
    if (textJumlahPohon) textJumlahPohon.innerText = butuhPohon;

    const boxPohon = document.getElementById("boxKonversiPohon");
    if (boxPohon) {
        if (totalCarbon > 150) {
            boxPohon.style.background = "#fffaf0";
            boxPohon.style.borderColor = "#feebc8";
        } else {
            boxPohon.style.background = "#f4f7f1";
            boxPohon.style.borderColor = "#e2ebd9";
        }
    }

    // --- 5. UPDATE LAMPU STATUS (Fungsi yang sudah ada) ---
    updateLampuIndikator("indListrik", hasListrik);
    updateLampuIndikator("indBbm", hasBbm);
    updateLampuIndikator("indAir", hasAir);
}

function updateLampuIndikator(elementId, isLogged) {
    const el = document.getElementById(elementId);
    if (!el) return;
    if (isLogged) {
        el.innerText = "Logged";
        el.className = "status-dot status-green";
    } else {
        el.innerText = "Off-Log";
        el.className = "status-dot status-gray";
    }
}

/**
 * Merender Grafik Tren Menggunakan Properti 'co2' dan 'biaya' dari MySQL
 */
function renderEnergyChart(dataLog) {
    const ctx = document.getElementById('energyChart');
    if (!ctx) return;

    const labelTanggal = dataLog.map(item => {
        const dateObj = new Date(item.tanggal);
        return dateObj.toLocaleDateString('id-ID', { day: '2-digit', month: 'short' });
    });
    
    const dataCarbon = dataLog.map(item => item.co2 || 0);
    const dataBiaya = dataLog.map(item => (item.biaya || 0) / 1000);

    if (dashboardChart !== null) {
        dashboardChart.destroy();
    }

    dashboardChart = new Chart(ctx, {
        type: 'line',
        data: {
            labels: labelTanggal,
            datasets: [
                {
                    label: 'Jejak Karbon (kg CO2)',
                    data: dataCarbon,
                    borderColor: '#72695e',
                    backgroundColor: 'rgba(114, 105, 94, 0.1)',
                    borderWidth: 3,
                    tension: 0.3,
                    fill: true,
                    yAxisID: 'y'
                },
                {
                    label: 'Biaya Energi (x1.000 Rp)',
                    data: dataBiaya,
                    type: 'bar',
                    backgroundColor: '#516631',
                    borderRadius: 5,
                    barThickness: 20,
                    yAxisID: 'y1'
                }
            ]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            scales: {
                y: { type: 'linear', display: true, position: 'left', grid: { color: '#efebe4' } },
                y1: { type: 'linear', display: true, position: 'right', grid: { drawOnChartArea: false } },
                x: { grid: { display: false } }
            }
        }
    });
}

/**
 * ========================================================
 * FUNGSI CRUD AJAX (TAMBAH / UPDATE / HAPUS) DATA DATABASE
 * ========================================================
 */

function simpanRecord(event) {
    event.preventDefault();

    const recordId = document.getElementById("recordId").value;
    const tanggal = document.getElementById("tanggal").value;
    const listrik = document.getElementById("listrik").value;
    const bbm = document.getElementById("bbm").value;
    const air = document.getElementById("air").value;

    const dataPayload = {
        tanggal: tanggal,
        listrik: parseFloat(listrik),
        bbm: parseFloat(bbm),
        air: parseFloat(air)
    };

    if (recordId) {
        dataPayload.id = parseInt(recordId);
    }

    fetch('/api/logs', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(dataPayload)
    })
    .then(response => {
        if (response.ok) {
            alert("Catatan energi berhasil disimpan ke database!");
            window.location.href = "/history";
        } else {
            alert("Gagal menyimpan data ke database, mohon cek kembali inputan.");
        }
    })
    .catch(error => {
        console.error("Error saat menyimpan data:", error);
        alert("Terjadi gangguan koneksi ke server localhost.");
    });
}

function hapusRecord(id) {
    if (confirm("Apakah Anda yakin ingin menghapus data laporan ini?")) {
        fetch(`/api/logs/${id}`, {
            method: 'DELETE'
        })
        .then(response => {
            if (response.ok) {
                alert("Data berhasil dihapus dari database!");
                window.location.reload();
            } else {
                alert("Gagal menghapus data.");
            }
        })
        .catch(error => console.error("Error saat menghapus data:", error));
    }
}

function editRecord(id) {
    fetch('/api/logs')
        .then(response => response.json())
        .then(dataLogs => {
            const dataTarget = dataLogs.find(log => log.id === id);
            if (dataTarget) {
                sessionStorage.setItem("edit_id", dataTarget.id);
                sessionStorage.setItem("edit_tanggal", dataTarget.tanggal);
                sessionStorage.setItem("edit_listrik", dataTarget.listrik);
                sessionStorage.setItem("edit_bbm", dataTarget.bbm);
                sessionStorage.setItem("edit_air", dataTarget.air);
                window.location.href = "/input";
            }
        })
        .catch(error => console.error("Gagal mengambil data untuk edit:", error));
}

function checkEditMode() {
    const editId = sessionStorage.getItem("edit_id");
    if (editId) {
        console.log("EnergiKu: Mendeteksi Mode Edit untuk ID " + editId);
        const pageTitle = document.getElementById("pageTitle");
        if (pageTitle) pageTitle.innerText = "Edit Catatan Penggunaan Energi";
        
        const submitBtn = document.querySelector("#energyForm button[type='submit']");
        if (submitBtn) submitBtn.innerHTML = `<i class="fas fa-edit"></i> Perbarui Catatan Pengeluaran`;

        document.getElementById("recordId").value = editId;
        document.getElementById("tanggal").value = sessionStorage.getItem("edit_tanggal");
        document.getElementById("listrik").value = sessionStorage.getItem("edit_listrik");
        document.getElementById("bbm").value = sessionStorage.getItem("edit_bbm");
        document.getElementById("air").value = sessionStorage.getItem("edit_air");

        sessionStorage.clear();
    }
}

/**
 * ========================================================
 * STRATEGI 1: OPERASI INTERAKTIF ECO-POINTS (LOCALSTORAGE)
 * ========================================================
 */
document.addEventListener("DOMContentLoaded", function() {
    const pointsElement = document.getElementById("sidebarPoints");
    if(pointsElement) {
        let currentPoints = localStorage.getItem("ecoPoints") || 0;
        pointsElement.innerText = currentPoints + " XP";
    }
});

function selesaikanMisi() {
    if(localStorage.getItem("misiSelesai") === "true") return;

    let currentPoints = parseInt(localStorage.getItem("ecoPoints") || 0);
    currentPoints += 50;
    localStorage.setItem("ecoPoints", currentPoints);
    
    const sidebarPoints = document.getElementById("sidebarPoints");
    if(sidebarPoints) sidebarPoints.innerText = currentPoints + " XP";
    
    localStorage.setItem("misiSelesai", "true");
    gantiStatusTombolMisi();
    
    alert("Selamat! Kamu mendapatkan +50 Eco-Points karena menjaga bumi hari ini. 🌱");
}

function gantiStatusTombolMisi() {
    const btn = document.getElementById("btnMisi");
    if(btn) {
        btn.innerHTML = `<i class="fas fa-calendar-check"></i> Misi Selesai Hari Ini`;
        btn.style.background = "#efebe4";
        btn.style.color = "#72695e";
        btn.style.cursor = "not-allowed";
        btn.disabled = true;
    }
}