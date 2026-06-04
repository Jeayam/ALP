package com.example.alp1tes1.model;

import jakarta.persistence.*;

@Entity
@Table(name = "tb_target")
public class Target {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String bulanTahun;
    private Double targetListrik;
    private Double targetBbm;
    private Double targetAir;
    private String catatan;

    // Getter dan Setter
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getBulanTahun() { return bulanTahun; }
    public void setBulanTahun(String bulanTahun) { this.bulanTahun = bulanTahun; }
    public Double getTargetListrik() { return targetListrik; }
    public void setTargetListrik(Double targetListrik) { this.targetListrik = targetListrik; }
    public Double getTargetBbm() { return targetBbm; }
    public void setTargetBbm(Double targetBbm) { this.targetBbm = targetBbm; }
    public Double getTargetAir() { return targetAir; }
    public void setTargetAir(Double targetAir) { this.targetAir = targetAir; }
    public String getCatatan() { return catatan; }
    public void setCatatan(String catatan) { this.catatan = catatan; }
}