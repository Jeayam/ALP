package com.example.alp1tes1.model;

import jakarta.persistence.*;

@Entity
@Table(name = "tb_edukasi")
public class Edukasi {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String judul;
    
    @Column(columnDefinition = "TEXT")
    private String kontenTips;
    
    private String kategori; // Contoh: "Listrik", "Air", "BBM"

    // Getter dan Setter
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getJudul() { return judul; }
    public void setJudul(String judul) { this.judul = judul; }
    public String getKontenTips() { return kontenTips; }
    public void setKontenTips(String kontenTips) { this.kontenTips = kontenTips; }
    public String getKategori() { return kategori; }
    public void setKategori(String kategori) { this.kategori = kategori; }
}