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
    private final int invoiceMonth;
    private final int invoiceYear;
    private final String status;
    private final int reminderCount;
    private final LocalDateTime lastReminderSentAt;
    private final String checkoutUrl;
    private final LocalDateTime paidAt;
    private final String studentName;
    private final String studentEmail;

    public InvoiceResponseDTO(
            Long id,
            String title,
            String description,
            BigDecimal amount,
            LocalDate issueDate,
            LocalDate dueDate,
            int invoiceMonth,
            int invoiceYear,
            String status,
            int reminderCount,
            LocalDateTime lastReminderSentAt,
            String checkoutUrl,
            LocalDateTime paidAt,
            String studentName,
            String studentEmail
    ) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.amount = amount;
        this.issueDate = issueDate;
        this.dueDate = dueDate;
        this.invoiceMonth = invoiceMonth;
        this.invoiceYear = invoiceYear;
        this.status = status;
        this.reminderCount = reminderCount;
        this.lastReminderSentAt = lastReminderSentAt;
        this.checkoutUrl = checkoutUrl;
        this.paidAt = paidAt;
        this.studentName = studentName;
        this.studentEmail = studentEmail;
    }

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public BigDecimal getAmount() { return amount; }
    public LocalDate getIssueDate() { return issueDate; }
    public LocalDate getDueDate() { return dueDate; }
    public int getInvoiceMonth() { return invoiceMonth; }
    public int getInvoiceYear() { return invoiceYear; }
    public String getStatus() { return status; }
    public int getReminderCount() { return reminderCount; }
    public LocalDateTime getLastReminderSentAt() { return lastReminderSentAt; }
    public String getCheckoutUrl() { return checkoutUrl; }
    public LocalDateTime getPaidAt() { return paidAt; }
    public String getStudentName() { return studentName; }
    public String getStudentEmail() { return studentEmail; }
}
