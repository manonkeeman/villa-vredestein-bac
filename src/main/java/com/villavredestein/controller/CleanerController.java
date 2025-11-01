package com.villavredestein.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/cleaner")
public class CleanerController {

    @GetMapping("/dashboard")
    public String cleanerDashboard() {
        return "🧹 Welkom CLEANER — hier kun je het schoonmaakschema bekijken en voortgang bijhouden.";
    }
}