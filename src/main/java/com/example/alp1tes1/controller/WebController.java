package com.example.alp1tes1.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class WebController {

    @GetMapping({"/", "/index"})
    public String dashboard() {
        return "index";
    }

    @GetMapping("/input")
    public String inputHalaman() {
        return "input";
    }

    @GetMapping("/history")
    public String historyHalaman() {
        return "history";
    }

    @GetMapping("/target")
    public String targetHalaman() {
        return "target";
    }

    @GetMapping("/edukasi")
    public String edukasiHalaman() {
        return "edukasi";
    }

    @GetMapping("/pengaturan")
    public String pengaturanHalaman() {
        return "pengaturan";
    }
}