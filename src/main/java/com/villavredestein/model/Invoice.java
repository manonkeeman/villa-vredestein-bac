package com.villavredestein.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "invoices",
        indexes = {
                @Index(name = "idx_invoice_student", columnList = "student_id"),
                @Index(name = "idx_invoice_status", columnList = "status"),
                @Index(name = "idx_invoice_due_date", columnList = "due_date"),
                @Index(name = "idx_invoice_year_month", columnList = "invoice_year, invoice_month")
        }
)
public class Invoice {

    public enum InvoiceStatus {
        OPEN,
        PAID,
        OVERDUE,
        CANCELLED
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Titel is verplicht")
    @Size(max = 120, message = "Titel mag maximaal 120 tekens bevatten")
    @Column(nullable = false, length = 120)
    private String title;

    @Size(max = 1000, message = "Beschrijving mag maximaal 1000 tekens bevatten")
    @Column(length = 1000)
    private String description;

    @NotNull(message = "Bedrag is verplicht")
    @DecimalMin(value = "0.00", inclusive = false, message = "Bedrag moet groter zijn dan 0")
    @Digits(integer = 8, fraction = 2, message = "Bedrag mag maximaal 8 cijfers en 2 decimalen bevatten")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @NotNull(message = "Issue date is verplicht")
    @Column(name = "issue_date", nullable = false)
    private LocalDate issueDate;

    @NotNull(message = "Due date is verplicht")
    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;

    @Min(value = 1, message = "Invoice month moet tussen 1 en 12 liggen")
    @Max(value = 12, message = "Invoice month moet tussen 1 en 12 liggen")
    @Column(name = "invoice_month", nullable = false)
    private int invoiceMonth;

    @Min(value = 2000, message = "Invoice year moet realistisch zijn")
    @Max(value = 2100, message = "Invoice year moet realistisch zijn")
    @Column(name = "invoice_year", nullable = false)
    private int invoiceYear;

    @Column(name = "last_reminder_sent_at")
    private LocalDateTime lastReminderSentAt;

    @Min(value = 0, message = "Reminder count mag niet negatief zijn")
    @Column(name = "reminder_count", nullable = false)
    private int reminderCount = 0;

    @NotNull(message = "Status is verplicht")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private InvoiceStatus status = InvoiceStatus.OPEN;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    private User student;

    public Invoice() {
    }

    public Invoice(String title,
                   String description,
                   BigDecimal amount,
                   LocalDate issueDate,
                   LocalDate dueDate,
                   int invoiceMonth,
                   int invoiceYear,
                   InvoiceStatus status,
                   User student) {
        this.title = title;
        this.description = description;
        this.amount = amount;
        this.issueDate = issueDate;
        this.dueDate = dueDate;
        this.invoiceMonth = invoiceMonth;
        this.invoiceYear = invoiceYear;
        this.status = (status == null) ? InvoiceStatus.OPEN : status;
        this.student = student;
    }

    @PrePersist
    @PreUpdate
    private void normalize() {
        if (title != null) {
            title = title.trim();
        }
        if (description != null) {
            description = description.trim();
        }
        if (status == null) {
            status = InvoiceStatus.OPEN;
        }
        if (issueDate != null) {
            if (invoiceMonth <= 0 || invoiceMonth > 12) {
                invoiceMonth = issueDate.getMonthValue();
            }
            if (invoiceYear < 2000 || invoiceYear > 2100) {
                invoiceYear = issueDate.getYear();
            }
        }
    }

    @AssertTrue(message = "Due date mag niet voor issue date liggen")
    private boolean isDueDateAfterOrEqualIssueDate() {
        if (issueDate == null || dueDate == null) {
            return true;
        }
        return !dueDate.isBefore(issueDate);
    }

    // =========================
    // Getters
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

    public int getInvoiceMonth() {
        return invoiceMonth;
    }

    public int getInvoiceYear() {
        return invoiceYear;
    }

    public LocalDateTime getLastReminderSentAt() {
        return lastReminderSentAt;
    }

    public int getReminderCount() {
        return reminderCount;
    }

    public InvoiceStatus getStatus() {
        return status;
    }

    public User getStudent() {
        return student;
    }

    // =========================
    // Setters
    // =========================

    public void setId(long id) {
        this.id = id;
    }

    public void setAmount(double amount) {
        this.amount = BigDecimal.valueOf(amount);
    }

    public void setStatus(String status) {
        if (status == null) {
            this.status = null;
            return;
        }
        this.status = InvoiceStatus.valueOf(status.trim().toUpperCase());
    }

    public void setReminderSent(boolean reminderSent) {
        if (reminderSent) {
            markReminderSentNow();
        } else {
            this.lastReminderSentAt = null;
        }
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public void setIssueDate(LocalDate issueDate) {
        this.issueDate = issueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public void setInvoiceMonth(int invoiceMonth) {
        this.invoiceMonth = invoiceMonth;
    }

    public void setInvoiceYear(int invoiceYear) {
        this.invoiceYear = invoiceYear;
    }

    public void setLastReminderSentAt(LocalDateTime lastReminderSentAt) {
        this.lastReminderSentAt = lastReminderSentAt;
    }

    public void setReminderCount(int reminderCount) {
        this.reminderCount = (reminderCount < 0) ? 0 : reminderCount;
    }

    public void setStatus(InvoiceStatus status) {
        this.status = (status == null) ? InvoiceStatus.OPEN : status;
    }

    public void setStudent(User student) {
        this.student = student;
    }

    // =========================
    // Convenience
    // =========================

    public void markReminderSentNow() {
        this.lastReminderSentAt = LocalDateTime.now();
        this.reminderCount = this.reminderCount + 1;
    }

    @Override
    public String toString() {
        return "Invoice{" +
                "id=" + id +
                ", student=" + (student != null ? student.getUsername() : "null") +
                ", amount=" + amount +
                ", month=" + invoiceMonth +
                ", year=" + invoiceYear +
                ", status=" + status +
                ", reminderCount=" + reminderCount +
                ", lastReminderSentAt=" + lastReminderSentAt +
                '}';
    }
}