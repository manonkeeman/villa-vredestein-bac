package com.villavredestein.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/assignments")
public class AssignmentController {

    @GetMapping("/ping")
    public String ping() {
        return "assignments-ok";
    }
}