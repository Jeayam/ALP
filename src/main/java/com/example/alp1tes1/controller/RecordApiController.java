package com.example.alp1tes1.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.alp1tes1.model.Record;
import com.example.alp1tes1.service.RecordService;

@RestController
@RequestMapping("/api/records")
public class RecordApiController {

    @Autowired
    private RecordService recordService;

    // API Mendapatkan semua data (READ) -> GET /api/records
    @GetMapping
    public List<Record> getAll() {
        return recordService.getAllRecords();
    }

    // API Mendapatkan satu data berdasarkan ID (READ khusus edit) -> GET /api/records/{id}
    @GetMapping("/{id}")
    public ResponseEntity<Record> getById(@PathVariable Long id) {
        Optional<Record> record = recordService.getRecordById(id);
        return record.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    // API Menyimpan data baru (CREATE) -> POST /api/records
    @PostMapping
    public Record create(@RequestBody Record record) {
        return recordService.saveOrUpdateRecord(record);
    }

    // API Mengubah data lama (UPDATE) -> PUT /api/records/{id}
    @PutMapping("/{id}")
    public ResponseEntity<Record> update(@PathVariable Long id, @RequestBody Record updatedData) {
        return recordService.getRecordById(id).map(record -> {
            record.setTanggal(updatedData.getTanggal());
            record.setListrik(updatedData.getListrik());
            record.setBbm(updatedData.getBbm());
            record.setAir(updatedData.getAir());
            
            Record hasilUbah = recordService.saveOrUpdateRecord(record);
            return ResponseEntity.ok(hasilUbah);
        }).orElseGet(() -> ResponseEntity.notFound().build());
    }

    // API Menghapus data (DELETE) -> DELETE /api/records/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (recordService.getRecordById(id).isPresent()) {
            recordService.deleteRecord(id);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }

    // API Mendapatkan Pesan Edukasi Otomatis -> GET /api/records/{id}/edukasi
    @GetMapping("/{id}/edukasi")
    public ResponseEntity<String> getEdukasi(@PathVariable Long id) {
        return recordService.getRecordById(id)
                .map(record -> ResponseEntity.ok(recordService.dapatkanEdukasiHarian(record)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}

  