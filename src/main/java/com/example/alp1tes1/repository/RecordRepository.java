package com.example.alp1tes1.repository;

import com.example.alp1tes1.model.Record;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RecordRepository extends JpaRepository<Record, Long> {
    // Semua fungsi CRUD (save, findAll, deleteById) otomatis disediakan oleh Spring Data JPA
}