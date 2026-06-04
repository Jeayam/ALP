const API_URL = '/api/records';

// --- FUNGSI UTAMA ALL CRUD CRUD OPERATIONS ---

// 1. Fungsi Simpan atau Update Data (CREATE / UPDATE)
async function simpanRecord(event) {
    event.preventDefault();
    
    const recordData = {
        tanggal: document.getElementById('tanggal').value,
        listrik: parseFloat(document.getElementById('listrik').value) || 0,
        bbm: parseFloat(document.getElementById('bbm').value) || 0,
        air: parseFloat(document.getElementById('air').value) || 0
    };

    const id = document.getElementById('recordId').value;
    const method = id ? 'PUT' : 'POST';
    const url = id ? `${API_URL}/${id}` : API_URL;

    try {
        const response = await fetch(url, {
            method: method,
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(recordData)
        });

        if (response.ok) {
            alert(id ? 'Catatan energi berhasil diperbarui!' : 'Catatan energi baru berhasil disimpan!');
            window.location.href = '/history';
        } else {
            alert('Aksi gagal dilakukan. Periksa kembali inputan Anda.');
        }
    } catch (error) {
        console.error('Error saving data:', error);
    }
}

// 2. Fungsi Mengisi Form Saat Edit (Mode UPDATE)
async function checkEditMode() {
    const urlParams = new URLSearchParams(window.location.search);
    const editId = urlParams.get('editId');
    if (!editId) return;

    document.getElementById('pageTitle').innerText = "Edit Log Catatan Energi";
    document.getElementById('recordId').value = editId;

    try {
        const response = await fetch(`${API_URL}/${editId}`);
        if (response.ok) {
            const rec = await response.json();
            document.getElementById('tanggal').value = rec.tanggal;
            document.getElementById('listrik').value = rec.listrik;
            document.getElementById('bbm').value = rec.bbm;
            document.getElementById('air').value = rec.air;
        }
    } catch (e) {
        console.error('Error fetching one record:', e);
    }
}

// 3. Fungsi Load Halaman Riwayat & Edukasi (READ)
async function loadHistory() {
    const tableBody = document.getElementById('historyTableBody');
    if (!tableBody) return;

    try {
        const response = await fetch(API_URL);
        const data = await response.json();
        tableBody.innerHTML = '';

        if (data.length === 0) {
            tableBody.innerHTML = `<tr><td colspan="7" style="text-align:center; color: #72695e;">Belum ada log penggunaan energi terdaftar.</td></tr>`;
            document.getElementById('eduContent').innerText = "🌱 Belum ada data untuk dianalisis. Silakan masukkan log harian Anda terlebih dahulu pada menu Input.";
            return;
        }

        data.sort((a,b) => new Date(a.tanggal) - new Date(b.tanggal));

        data.forEach(rec => {
            tableBody.innerHTML += `
                <tr>
                    <td><strong>${formatTanggalIndo(rec.tanggal)}</strong></td>
                    <td>${rec.listrik} kWh</td>
                    <td>${rec.bbm} Liter</td>
                    <td>${rec.air} m³</td>
                    <td><span style="font-weight:600; color:#516631;">Rp ${rec.biaya.toLocaleString('id-ID')}</span></td>
                    <td><i class="fas fa-smog" style="color:#72695e; margin-right:5px;"></i>${rec.co2.toFixed(2)} kg</td>
                    <td>
                        <button class="action-btn btn-edit" onclick="window.location.href='/input?editId=${rec.id}'" title="Ubah Data"><i class="fas fa-edit"></i></button>
                        <button class="action-btn btn-delete" onclick="hapusRecord(${rec.id})" title="Hapus Data"><i class="fas fa-trash"></i></button>
                    </td>
                </tr>
            `;
        });

        const latestRecord = data[data.length - 1];
        fetchEdukasiOtomatis(latestRecord.id);

    } catch (error) {
        console.error('Error loading table history:', error);
    }
}

// 4. Ambil Konten Edukasi Dinamis dari Backend
async function fetchEdukasiOtomatis(id) {
    try {
        const response = await fetch(`${API_URL}/${id}/edukasi`);
        if (response.ok) {
            const text = await response.text();
            document.getElementById('eduContent').innerText = text;
        }
    } catch (e) {
        console.stringify(e);
    }
}

// 5. Fungsi Delete Data (DELETE)
async function hapusRecord(id) {
    if (confirm('Apakah Anda yakin ingin menghapus log pengeluaran energi ini dari database?')) {
        try {
            const response = await fetch(`${API_URL}/${id}`, { method: 'DELETE' });
            if (response.ok) {
                loadHistory();
            }
        } catch (error) {
            console.error('Error deleting record:', error);
        }
    }
}

// --- LOGIKA UTAMA DASHBOARD & RENDER SKETSA GRAFIK KOSONG ---
async function initDashboard() {
    const ctx = document.getElementById('energyChart');
    if (!ctx) return;

    try {
        const response = await fetch(API_URL);
        const data = await response.json();

        // JIKA DATABASE KOSONG -> RENDER SKETSA GRAFIK KOSONG BIAR CANTIK & BERGARIS
        if (data.length === 0) {
            document.getElementById('widgetBiaya').innerText = "Rp 0";
            document.getElementById('widgetCarbon').innerText = "0.0 kg";
            document.getElementById('widgetLog').innerText = "0 Hari";
            document.getElementById('widgetScore').innerText = "-";
            document.getElementById('widgetScoreText').innerText = "Belum ada log";

            new Chart(ctx, {
                type: 'line',
                data: {
                    labels: ['Log Hari 1', 'Log Hari 2', 'Log Hari 3', 'Log Hari 4', 'Log Hari 5'],
                    datasets: [
                        { label: 'Sketsa Grid Listrik', data: [0, 0, 0, 0, 0], borderColor: 'rgba(230, 126, 34, 0.25)', borderDash: [6, 6], tension: 0.3, borderWidth: 2 },
                        { label: 'Sketsa Grid BBM', data: [0, 0, 0, 0, 0], borderColor: 'rgba(231, 76, 60, 0.25)', borderDash: [6, 6], tension: 0.3, borderWidth: 2 },
                        { label: 'Sketsa Grid Air', data: [0, 0, 0, 0, 0], borderColor: 'rgba(52, 152, 219, 0.25)', borderDash: [6, 6], tension: 0.3, borderWidth: 2 }
                    ]
                },
                options: {
                    responsive: true,
                    maintainAspectRatio: false,
                    plugins: {
                        legend: { display: true, labels: { font: { family: 'Plus Jakarta Sans', weight: 600 }, color: '#72695e' } },
                        title: {
                            display: true,
                            text: '⚠️ Belum ada entri data harian. Di atas adalah contoh sketsa grid template visualisasi.',
                            color: '#72695e',
                            position: 'bottom',
                            font: { style: 'italic', size: 13, family: 'Plus Jakarta Sans' }
                        }
                    },
                    scales: {
                        y: { min: 0, max: 10, grid: { color: '#e4ded4' }, ticks: { color: '#a0968a' } },
                        x: { grid: { color: '#e4ded4' }, ticks: { color: '#a0968a' } }
                    }
                }
            });
            return;
        }

        // --- PROSES JIKA DATA SUDAH ADA / ADA INPUT ---
        data.sort((a,b) => new Date(a.tanggal) - new Date(b.tanggal));

        let totalBiaya = 0;
        let totalCarbon = 0;
        data.forEach(r => {
            totalBiaya += r.biaya;
            totalCarbon += r.co2;
        });
        
        // Isi Nilai Utama
        document.getElementById('widgetBiaya').innerText = "Rp " + totalBiaya.toLocaleString('id-ID');
        document.getElementById('widgetCarbon').innerText = totalCarbon.toFixed(1) + " kg";
        document.getElementById('widgetLog').innerText = data.length + " Hari";

        // Logic Penghitungan Eco Score Hiasan
        let rerataCarbon = totalCarbon / data.length;
        if(rerataCarbon <= 5.0) {
            document.getElementById('widgetScore').innerText = "A+";
            document.getElementById('widgetScore').style.color = "#516631";
            document.getElementById('widgetScoreText').innerText = "Sangat Ramah Lingkungan!";
        } else if(rerataCarbon <= 10.0) {
            document.getElementById('widgetScore').innerText = "B";
            document.getElementById('widgetScore').style.color = "#e67e22";
            document.getElementById('widgetScoreText').innerText = "Konsumsi Normal Stabil";
        } else {
            document.getElementById('widgetScore').innerText = "C";
            document.getElementById('widgetScore').style.color = "#e74c3c";
            document.getElementById('widgetScoreText').innerText = "Butuh Penghematan Energi";
        }

        // Ganti Lampu Indikator Status Menjadi Aktif Terdata
        document.getElementById('indListrik').innerText = "Aktif";
        document.getElementById('indListrik').className = "status-dot status-green";
        document.getElementById('indBbm').innerText = "Aktif";
        document.getElementById('indBbm').className = "status-dot status-green";
        document.getElementById('indAir').innerText = "Aktif";
        document.getElementById('indAir').className = "status-dot status-green";

        // Update Progres Batas Karbon Bulanan Hiasan
        let persentaseTarget = (totalCarbon / 150) * 100;
        if(persentaseTarget > 100) persentaseTarget = 100;
        document.getElementById('targetBarFill').style.width = persentaseTarget + "%";
        document.getElementById('targetBarText').innerText = totalCarbon.toFixed(1) + " kg CO2";
        if(totalCarbon > 150) {
            document.getElementById('targetBarFill').style.backgroundColor = "#e74c3c";
        }

        // Susun Data Sumbu Grafik Riil
        const labelsTanggal = data.map(r => formatTanggalIndo(r.tanggal));
        const datasetListrik = data.map(r => r.listrik);
        const datasetBBM = data.map(r => r.bbm);
        const datasetAir = data.map(r => r.air);

        // Render Grafik Asli Riil
        new Chart(ctx, {
            type: 'line',
            data: {
                labels: labelsTanggal,
                datasets: [
                    { label: 'Listrik (kWh)', data: datasetListrik, borderColor: '#e67e22', backgroundColor: 'rgba(230, 126, 34, 0.08)', tension: 0.3, borderWidth: 3 },
                    { label: 'BBM (Liter)', data: datasetBBM, borderColor: '#e74c3c', backgroundColor: 'rgba(231, 76, 60, 0.08)', tension: 0.3, borderWidth: 3 },
                    { label: 'Air Bersih (m³)', data: datasetAir, borderColor: '#3498db', backgroundColor: 'rgba(52, 152, 219, 0.08)', tension: 0.3, borderWidth: 3 }
                ]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                    legend: { position: 'top', labels: { font: { family: 'Plus Jakarta Sans', weight: 600 } } }
                },
                scales: {
                    y: { beginAtZero: true, grid: { color: '#e4ded4' } },
                    x: { grid: { display: false } }
                }
            }
        });

    } catch (e) {
        console.error('Error rendering dashboard components:', e);
    }
}

function formatTanggalIndo(stringTanggal) {
    const opsi = { day: 'numeric', month: 'short' };
    return new Date(stringTanggal).toLocaleDateString('id-ID', opsi);
}