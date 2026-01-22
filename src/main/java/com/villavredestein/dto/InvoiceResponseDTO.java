package com.villavredestein.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class InvoiceResponseDTO {

    private final Long id;
    private final String title;
    private final String description;
    private final BigDecimal amount;
    private final LocalDate issueDate;
    private final LocalDate dueDate;
    private final String status;

    private final int reminderCount;
    private final LocalDateTime lastReminderSentAt;

    private final String studentName;
    private final String studentEmail;

    public InvoiceResponseDTO(
            Long id,
            String title,
            String description,
            BigDecimal amount,
            LocalDate issueDate,
            LocalDate dueDate,
            String status,
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
        this.status = status;
        this.reminderCount = reminderCount;
        this.lastReminderSentAt = lastReminderSentAt;
        this.studentName = studentName;
        this.studentEmail = studentEmail;
    }

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