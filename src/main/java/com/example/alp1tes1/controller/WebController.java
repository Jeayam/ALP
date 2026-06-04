package com.example.alp1tes1.controller;

import com.example.alp1tes1.model.User;
import com.example.alp1tes1.model.Edukasi;
import com.example.alp1tes1.model.Record; // Menggunakan model Record milikmu
import com.example.alp1tes1.repository.EdukasiRepository;
import com.example.alp1tes1.repository.RecordRepository; // Menggunakan repository milikmu
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class WebController {

    // 1. HUBUNGKAN KE REPOSITORY KAMU
    @Autowired
    private RecordRepository recordRepository;

    private static List<User> userDatabase = new ArrayList<>();
    // MOCK DATA ENERGYLOG SUDAH DIHAPUS TOTAL DISINI

    static {
        // Akun default untuk login tetap dipertahankan
        userDatabase.add(new User("admin", "admin@energiku.com", "password123"));
    }

    // ==========================================
    // AUTHENTICATION SYSTEM
    // ==========================================
    @GetMapping("/login")
    public String loginPage(HttpSession session) {
        if (session.getAttribute("userLoggedIn") != null) { return "redirect:/index"; }
        return "login";
    }

    @PostMapping("/do-login")
    public String doLogin(@RequestParam String username, @RequestParam String password, HttpSession session, Model model) {
        for (User user : userDatabase) {
            if (user.getUsername().equalsIgnoreCase(username) && user.getPassword().equals(password)) {
                session.setAttribute("userLoggedIn", user.getUsername());
                return "redirect:/index";
            }
        }
        model.addAttribute("errorMsg", "Username atau Password salah!");
        return "login";
    }

    @GetMapping("/register")
    public String registerPage() { return "register"; }

    @PostMapping("/do-register")
    public String doRegister(@RequestParam String username, @RequestParam String email, @RequestParam String password, Model model) {
        for (User user : userDatabase) {
            if (user.getUsername().equalsIgnoreCase(username)) {
                model.addAttribute("errorMsg", "Username sudah terdaftar!");
                return "register";
            }
        }
        userDatabase.add(new User(username, email, password));
        model.addAttribute("successMsg", "Pendaftaran berhasil! Silakan login.");
        return "login";
    }

    @GetMapping("/logout")
    public String doLogout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }

    // ==========================================
    // SIDEBAR PAGES ROUTING (PROTECTED)
    // ==========================================
    
    @GetMapping({"/", "/index"})
public String dashboardPage(HttpSession session, Model model) {
    if (session.getAttribute("userLoggedIn") == null) { return "redirect:/login"; }
    
    List<Record> allRecords = recordRepository.findAll();
    double totalEmisi = allRecords.stream().mapToDouble(Record::getCo2).sum();
    
    model.addAttribute("usernameAktif", session.getAttribute("userLoggedIn"));
    // Mengirim data ke HTML
    model.addAttribute("totalEmisi", totalEmisi);
    model.addAttribute("ecoScore", hitungGrade(totalEmisi));
    model.addAttribute("pesanWarning", hitungPesanWarning(totalEmisi));
    
    return "index";
}


    @GetMapping("/input")
    public String inputPage(HttpSession session, Model model) {
        if (session.getAttribute("userLoggedIn") == null) { return "redirect:/login"; }
        model.addAttribute("usernameAktif", session.getAttribute("userLoggedIn"));
        return "input";
    }

    @GetMapping("/history")
    public String historyPage(HttpSession session, Model model) {
        if (session.getAttribute("userLoggedIn") == null) { return "redirect:/login"; }
        model.addAttribute("usernameAktif", session.getAttribute("userLoggedIn"));
        
        // AMBIL DATA ASLI DARI DATABASE UNTUK TABEL RIWAYAT
        model.addAttribute("logs", recordRepository.findAll()); 
        return "history";
    }

    @GetMapping("/target")
    public String targetPage(HttpSession session, Model model) {
        if (session.getAttribute("userLoggedIn") == null) { return "redirect:/login"; }
        model.addAttribute("usernameAktif", session.getAttribute("userLoggedIn"));
        return "target";
    }

    @GetMapping("/edukasi")
    public String edukasiPage(HttpSession session, Model model) {
        if (session.getAttribute("userLoggedIn") == null) { return "redirect:/login"; }
        model.addAttribute("usernameAktif", session.getAttribute("userLoggedIn"));
        return "edukasi";
    }

    @GetMapping("/pengaturan")
    public String pengaturanPage(HttpSession session, Model model) {
        if (session.getAttribute("userLoggedIn") == null) { return "redirect:/login"; }
        model.addAttribute("usernameAktif", session.getAttribute("userLoggedIn"));
        return "pengaturan";
    }

    // ==========================================
    // DATABASE CRUD API FOR JAVASCRIPT (APP.JS)
    // ==========================================

    // A. READ: Mengambil data dari phpMyAdmin untuk Chart & Widget Dashboard
    @GetMapping("/api/logs")
    @ResponseBody
    public List<Record> getEnergyLogsApi() {
        return recordRepository.findAll(); 
    }

    // B. CREATE & UPDATE: Menyimpan data baru atau memperbarui data lama via AJAX
    @PostMapping("/api/logs")
    @ResponseBody
    public Record saveRecordApi(@RequestBody Record record) {
        // Kalkulasi otomatis Biaya & CO2 di backend jika frontend belum menghitungnya
        if (record.getBiaya() == null || record.getBiaya() == 0) {
            int biayaListrik = (int) (record.getListrik() * 1444);
            int biayaBbm = (int) (record.getBbm() * 10000);
            int biayaAir = (int) (record.getAir() * 7500);
            record.setBiaya(biayaListrik + biayaBbm + biayaAir);
        }
        if (record.getCo2() == null || record.getCo2() == 0.0) {
            double co2Listrik = record.getListrik() * 0.85;
            double co2Bbm = record.getBbm() * 2.3;
            double co2Air = record.getAir() * 0.5;
            record.setCo2(co2Listrik + co2Bbm + co2Air);
        }
        
        // .save() otomatis melakukan INSERT jika ID kosong, dan UPDATE jika ID sudah ada
        return recordRepository.save(record); 
    }

    // C. DELETE: Menghapus record berdasarkan ID dari database phpMyAdmin
    @DeleteMapping("/api/logs/{id}")
    @ResponseBody
    public String deleteRecordApi(@PathVariable Long id) {
        recordRepository.deleteById(id);
        return "Data berhasil dihapus dari database!";
    }
    // Tambahkan di WebController.java
    @Autowired
    private EdukasiRepository edukasiRepository; // Inject repository baru

    @GetMapping("/api/edukasi")
    @ResponseBody
    public List<Edukasi> getEdukasiApi() {
    return edukasiRepository.findAll();
}
@GetMapping("/api/tarif")
@ResponseBody
public Map<String, Double> getTarif() {
    Map<String, Double> tarif = new HashMap<>();
    tarif.put("listrik", 1444.0);
    tarif.put("bbm", 10000.0);
    tarif.put("air", 7500.0);
    return tarif;
}
// Method untuk menentukan Grade
private String hitungGrade(double totalEmisi) {
    if (totalEmisi > 200) return "D"; // Buruk
    if (totalEmisi > 150) return "C"; // Waspada
    if (totalEmisi > 100) return "B"; // Baik
    return "A+"; // Sangat Ramah
}

private String hitungPesanWarning(double totalEmisi) {
    if (totalEmisi > 150) return "BAHAYA: Akumulasi jejak karbon Anda melebihi batas aman!";
    if (totalEmisi > 100) return "WASPADA: Penggunaan energi Anda mulai tinggi.";
    return ""; // Tidak ada peringatan

}}