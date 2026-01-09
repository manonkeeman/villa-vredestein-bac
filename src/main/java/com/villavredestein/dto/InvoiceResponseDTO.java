package com.villavredestein.dto;

import com.villavredestein.model.Invoice;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class InvoiceResponseDTO {

    private Long id;
    private String title;
    private String description;
    private BigDecimal amount;
    private LocalDate issueDate;
    private LocalDate dueDate;
    private String status;

    private int reminderCount;
    private LocalDateTime lastReminderSentAt;

    private String studentName;
    private String studentEmail;

    protected InvoiceResponseDTO() {
    }

    public InvoiceResponseDTO(
            Long id,
            String title,
            String description,
            BigDecimal amount,
            LocalDate issueDate,
            LocalDate dueDate,
            Invoice.InvoiceStatus status,
            int reminderCount,
            LocalDateTime lastReminderSentAt,
            String studentName,
            String studentEmail
    ) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.amount = amount;
        this.issueDate = issueDate;
        this.dueDate = dueDate;
        this.status = status != null ? status.name() : null;
        this.reminderCount = reminderCount;
        this.lastReminderSentAt = lastReminderSentAt;
        this.studentName = studentName;
        this.studentEmail = studentEmail;
    }

    // =========================
    // Getters (read-only DTO)
    // =========================

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public LocalDate getIssueDate() {
        return issueDate;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public String getStatus() {
        return status;
    }

    public int getReminderCount() {
        return reminderCount;
    }

    public LocalDateTime getLastReminderSentAt() {
        return lastReminderSentAt;
    }

    public String getStudentName() {
        return studentName;
    }

    public String getStudentEmail() {
        return studentEmail;
    }
}