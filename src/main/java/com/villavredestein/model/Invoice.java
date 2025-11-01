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
    private String description;
    private double amount;

    @Column(name = "issue_date")
    private LocalDate issueDate;

    @Column(name = "due_date")
    private LocalDate dueDate;

    @Column(name = "invoice_month")
    private int invoiceMonth;

    @Column(name = "invoice_year")
    private int invoiceYear;

    @Column(name = "reminder_sent")
    private boolean reminderSent = false;

    private String status;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private User student;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

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

    public int getInvoiceMonth() { return invoiceMonth; }
    public void setInvoiceMonth(int invoiceMonth) { this.invoiceMonth = invoiceMonth; }

    public int getInvoiceYear() { return invoiceYear; }
    public void setInvoiceYear(int invoiceYear) { this.invoiceYear = invoiceYear; }

    public boolean isReminderSent() { return reminderSent; }
    public void setReminderSent(boolean reminderSent) { this.reminderSent = reminderSent; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public User getStudent() { return student; }
    public void setStudent(User student) { this.student = student; }

    public int getMonth() {
        return getInvoiceMonth();
    }

    public int getYear() {
        return getInvoiceYear();
    }

    public boolean getReminderSent() {
        return isReminderSent();
    }

    @Override
    public String toString() {
        return "Invoice{" +
                "id=" + id +
                ", student=" + (student != null ? student.getUsername() : "null") +
                ", amount=" + amount +
                ", month=" + invoiceMonth +
                ", year=" + invoiceYear +
                ", status='" + status + '\'' +
                ", reminderSent=" + reminderSent +
                '}';
    }
}