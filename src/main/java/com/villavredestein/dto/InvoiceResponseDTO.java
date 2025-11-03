package com.villavredestein.dto;

import java.time.LocalDate;

public class InvoiceResponseDTO {

    private Long id;
    private String title;
    private double amount;
    private LocalDate dueDate;
    private String status;
    private boolean reminderSent;
    private String studentName;
    private String studentEmail;

    public InvoiceResponseDTO(Long id, String title, double amount, LocalDate dueDate, String status,
                              boolean reminderSent, String studentName, String studentEmail) {
        this.id = id;
        this.title = title;
        this.amount = amount;
        this.dueDate = dueDate;
        this.status = status;
        this.reminderSent = reminderSent;
        this.studentName = studentName;
        this.studentEmail = studentEmail;
    }

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public double getAmount() { return amount; }
    public LocalDate getDueDate() { return dueDate; }
    public String getStatus() { return status; }
    public boolean isReminderSent() { return reminderSent; }
    public String getStudentName() { return studentName; }
    public String getStudentEmail() { return studentEmail; }
}