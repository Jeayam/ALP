package com.example.alp1tes1.service;

import com.example.alp1tes1.model.Record;
import com.example.alp1tes1.repository.RecordRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class RecordService {

    @Autowired
    private RecordRepository recordRepository;

    // Standar harga acuan default jika user belum setel di pengaturan
    private final int TARIF_LISTRIK = 1444; 
    private final int HARGA_BBM = 10000;
    private final int TARIF_AIR = 7500;

    // 1. CREATE atau UPDATE (Menghitung biaya & CO2 sebelum disimpan ke DB)
    public Record saveOrUpdateRecord(Record record) {
        // Hitung estimasi total biaya rupiah secara otomatis di backend
        double biayaListrik = (record.getListrik() != null) ? record.getListrik() * TARIF_LISTRIK : 0;
        double biayaBbm = (record.getBbm() != null) ? record.getBbm() * HARGA_BBM : 0;
        double biayaAir = (record.getAir() != null) ? record.getAir() * TARIF_AIR : 0;
        record.setBiaya((int) (biayaListrik + biayaBbm + biayaAir));

        // Hitung perkiraan jejak karbon emisi CO2 (Rumus sains dasar kg CO2)
        double co2Listrik = (record.getListrik() != null) ? record.getListrik() * 0.85 : 0;
        double co2Bbm = (record.getBbm() != null) ? record.getBbm() * 2.35 : 0;
        double co2Air = (record.getAir() != null) ? record.getAir() * 0.5 : 0;
        record.setCo2(co2Listrik + co2Bbm + co2Air);

        return recordRepository.save(record);
    }

    // 2. READ ALL (Untuk memunculkan data di halaman History)
    public List<Record> getAllRecords() {
        return recordRepository.findAll();
    }

    // 3. READ ONE BY ID (Membantu proses edit agar angka lama muncul di form edit)
    public Optional<Record> getRecordById(Long id) {
        return recordRepository.findById(id);
    }

    // 4. DELETE (Menghapus log pengeluaran berdasarkan ID)
    public void deleteRecord(Long id) {
        recordRepository.deleteById(id);
    }

    // 5. LOGIKA EDUKASI OTOMATIS (Menganalisis apakah konsumsi di atas rata-rata)
    public String dapatkanEdukasiHarian(Record record) {
        StringBuilder edukasi = new StringBuilder();

        // Contoh batas wajar harian rumah tangga kecil: Listrik 10 kWh, BBM 3 Liter, Air 0.5 m³
        if (record.getListrik() > 10) {
            edukasi.append("⚠️ Konsumsi listrik Anda (").append(record.getListrik()).append(" kWh) di atas rata-rata! Cobalah mematikan AC jika ruangan kosong dan beralih ke lampu LED.\n");
        }
        if (record.getBbm() > 3) {
            edukasi.append("⚠️ Penggunaan BBM Anda cukup tinggi hari ini. Pertimbangkan untuk menggunakan transportasi umum atau menyatukan rute perjalanan guna menekan emisi CO2.\n");
        }
        if (record.getAir() > 0.5) {
            edukasi.append("⚠️ Penggunaan air bersih terdeteksi boros. Pastikan kran tertutup rapat dan tampung air sisa bilasan untuk menyiram tanaman.\n");
        }

        if (edukasi.length() == 0) {
            edukasi.append("🌱 Keren! Penggunaan energimu hari ini sangat efisien dan berada di bawah batas rata-rata. Pertahankan gaya hidup hijau ini!");
        }

        return edukasi.toString();
    }
}