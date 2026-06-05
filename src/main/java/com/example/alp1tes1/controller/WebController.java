package com.example.alp1tes1.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired; // Menggunakan model Record milikmu
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model; // Menggunakan repository milikmu
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.alp1tes1.model.Record;
import com.example.alp1tes1.model.User;
import com.example.alp1tes1.repository.RecordRepository;

import jakarta.servlet.http.HttpSession;

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