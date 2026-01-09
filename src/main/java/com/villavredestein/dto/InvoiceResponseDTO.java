package com.villavredestein.dto;

import com.villavredestein.model.Invoice;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Response DTO voor facturen.
 *
 * <p>Deze DTO wordt gebruikt om factuurgegevens veilig en gestructureerd
 * terug te sturen naar de client. Er worden geen JPA-entiteiten of interne
 * database-objecten blootgesteld.</p>
 */
public class InvoiceResponseDTO {

    private Long id;
    private String title;
    private String description;
    private BigDecimal amount;
    private LocalDate issueDate;
    private LocalDate dueDate;

    /**
     * Status wordt als enum-naam (String) teruggegeven om de API stabiel te houden.
     */
    private String status;

    /**
     * Reminder metadata (audit-vriendelijk).
     */
    private int reminderCount;
    private LocalDateTime lastReminderSentAt;

    // Student-context (read-only)
    private String studentName;
    private String studentEmail;

    protected InvoiceResponseDTO() {
        // for serialization
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