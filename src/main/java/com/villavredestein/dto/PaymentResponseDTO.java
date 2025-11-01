package com.villavredestein.dto;

import java.time.LocalDate;

public class PaymentResponseDTO {
    private Long id;
    private double amount;
    private LocalDate date;
    private String status;

    public PaymentResponseDTO(Long id, double amount, LocalDate date, String status) {
        this.id = id;
        this.amount = amount;
        this.date = date;
        this.status = status;
    }

    public Long getId() { return id; }
    public double getAmount() { return amount; }
    public LocalDate getDate() { return date; }
    public String getStatus() { return status; }
}