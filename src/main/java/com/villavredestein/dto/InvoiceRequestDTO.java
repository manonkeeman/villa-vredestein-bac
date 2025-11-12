package com.villavredestein.dto;

import java.time.LocalDate;

public class InvoiceRequestDTO {

    private String title;
    private String description;
    private double amount;
    private LocalDate issueDate;
    private LocalDate dueDate;
    private String studentEmail;

    public InvoiceRequestDTO() {
    }

    public InvoiceRequestDTO(String title, String description, double amount,
                             LocalDate issueDate, LocalDate dueDate, String studentEmail) {
        this.title = title;
        this.description = description;
        this.amount = amount;
        this.issueDate = issueDate;
        this.dueDate = dueDate;
        this.studentEmail = studentEmail;
    }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public LocalDate getIssueDate() { return issueDate; }
    public void setIssueDate(LocalDate issueDate) { this.issueDate = issueDate; }

    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }

    public String getStudentEmail() { return studentEmail; }
    public void setStudentEmail(String studentEmail) { this.studentEmail = studentEmail; }
}