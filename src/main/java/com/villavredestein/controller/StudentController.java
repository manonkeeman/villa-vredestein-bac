package com.villavredestein.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/student")
public class StudentController {

    @GetMapping("/dashboard")
    public String studentDashboard() {
        return "🎓 Welkom STUDENT — hier kun je jouw taken en betalingen beheren.";
    }
}