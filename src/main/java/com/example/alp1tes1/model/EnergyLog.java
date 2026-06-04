package com.example.alp1tes1.model; // Sesuaikan nama package projectmu

public class EnergyLog {
    private String tanggal;
    private double listrikKwh;
    private double bbmLiter;
    private double airLiter;
    private double biaya;
    private double carbon;

    // Constructor Utama
    public EnergyLog(String tanggal, double listrikKwh, double bbmLiter, double airLiter, double biaya, double carbon) {
        this.tanggal = tanggal;
        this.listrikKwh = listrikKwh;
        this.bbmLiter = bbmLiter;
        this.airLiter = airLiter;
        this.biaya = biaya;
        this.carbon = carbon;
    }

    // Getter dan Setter (Wajib ada agar Spring Boot bisa mengubahnya jadi JSON otomatis)
    public String getTanggal() { return tanggal; }
    public void setTanggal(String tanggal) { this.tanggal = tanggal; }

    public double getListrikKwh() { return listrikKwh; }
    public void setListrikKwh(double listrikKwh) { this.listrikKwh = listrikKwh; }

    public double getBbmLiter() { return bbmLiter; }
    public void setBbmLiter(double bbmLiter) { this.bbmLiter = bbmLiter; }

    public double getAirLiter() { return airLiter; }
    public void setAirLiter(double airLiter) { this.airLiter = airLiter; }

    public double getBiaya() { return biaya; }
    public void setBiaya(double biaya) { this.biaya = biaya; }

    public double getCarbon() { return carbon; }
    public void setCarbon(double carbon) { this.carbon = carbon; }
}