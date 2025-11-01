package com.villavredestein.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "invoices")
public class Invoice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private Double amount;
    private LocalDate issueDate = LocalDate.now();
    private LocalDate dueDate;
    private String description;
    private String status = "OPEN";
    private boolean reminderSent = false;

    @Column(name = "invoice_month")
    private int month;

    @Column(name = "invoice_year")
    private int year;

    @ManyToOne
    private User student;

    public Invoice() {}

    public Invoice(String title, Double amount, LocalDate issueDate, LocalDate dueDate,
                   String description, String status, boolean reminderSent,
                   Integer month, Integer year, User student) {
        this.title = title;
        this.amount = amount;
        this.issueDate = issueDate;
        this.dueDate = dueDate;
        this.description = description;
        this.status = status;
        this.reminderSent = reminderSent;
        this.month = month;
        this.year = year;
        this.student = student;
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public LocalDate getIssueDate() {
        return issueDate;
    }

    public void setIssueDate(LocalDate issueDate) {
        this.issueDate = issueDate;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public boolean getReminderSent() {
        return reminderSent;
    }

    public void setReminderSent(boolean reminderSent) {
        this.reminderSent = reminderSent;
    }

    public Integer getMonth() {
        return month;
    }

    public void setMonth(Integer month) {
        this.month = month;
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public User getStudent() {
        return student;
    }

    public void setStudent(User student) {
        this.student = student;
    }
}