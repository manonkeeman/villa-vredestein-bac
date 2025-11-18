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

    public InvoiceResponseDTO() {
    }

    public InvoiceResponseDTO(Long id, String title, double amount,
                              LocalDate dueDate, String status,
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
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public boolean isReminderSent() { return reminderSent; }
    public void setReminderSent(boolean reminderSent) { this.reminderSent = reminderSent; }

    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }

    public String getStudentEmail() { return studentEmail; }
    public void setStudentEmail(String studentEmail) { this.studentEmail = studentEmail; }
}