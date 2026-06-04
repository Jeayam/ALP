package com.example.alp1tes1.model; // Sesuaikan dengan nama folder project utama kamu

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "tb_records")
public class Record {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate tanggal;
    private Double listrik; // dalam kWh
    private Double bbm;     // dalam Liter
    private Double air;     // dalam m³
    private Integer biaya;  // Total pengeluaran rupiah otomatis
    private Double co2;     // Total emisi CO2 otomatis

    // --- CONSTRUCTOR ---
    public Record() {}

    public Record(LocalDate tanggal, Double listrik, Double bbm, Double air, Integer biaya, Double co2) {
        this.tanggal = tanggal;
        this.listrik = listrik;
        this.bbm = bbm;
        this.air = air;
        this.biaya = biaya;
        this.co2 = co2;
    }

    // --- GETTER & SETTER ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public LocalDate getTanggal() { return tanggal; }
    public void setTanggal(LocalDate tanggal) { this.tanggal = tanggal; }

    public Double getListrik() { return listrik; }
    public void setListrik(Double listrik) { this.listrik = listrik; }

    public Double getBbm() { return bbm; }
    public void setBbm(Double bbm) { this.bbm = bbm; }

    public Double getAir() { return air; }
    public void setAir(Double air) { this.air = air; }

    public Integer getBiaya() { return biaya; }
    public void setBiaya(Integer biaya) { this.biaya = biaya; }

    public Double getCo2() { return co2; }
    public void setCo2(Double co2) { this.co2 = co2; }
}