package com.villavredestein.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "payments",
        indexes = {
                @Index(name = "idx_payment_student", columnList = "student_id"),
                @Index(name = "idx_payment_status", columnList = "status"),
                @Index(name = "idx_payment_paid_at", columnList = "paid_at")
        }
)
public class Payment {

    public enum PaymentStatus {
        OPEN,
        PENDING,
        PAID,
        FAILED,
        CANCELLED
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Bedrag is verplicht")
    @DecimalMin(value = "0.01", message = "Bedrag moet groter zijn dan 0")
    @Digits(integer = 8, fraction = 2, message = "Bedrag mag maximaal 8 cijfers en 2 decimalen bevatten")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    @NotNull(message = "Status is verplicht")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PaymentStatus status = PaymentStatus.OPEN;

    @Size(max = 500, message = "Omschrijving mag maximaal 500 tekens bevatten")
    @Column(length = 500)
    private String description;

    @NotNull(message = "Student is verplicht")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    private User student;

    public Payment() {
    }

    public Payment(BigDecimal amount,
                   LocalDateTime paidAt,
                   PaymentStatus status,
                   String description,
                   User student) {
        this.amount = amount;
        this.paidAt = paidAt;
        this.status = (status == null) ? PaymentStatus.OPEN : status;
        this.description = description;
        this.student = student;
    }

    @PrePersist
    private void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        normalize();
    }

    @PreUpdate
    private void onUpdate() {
        normalize();
    }

    private void normalize() {
        if (description != null) {
            description = description.trim();
        }
        if (status == null) {
            status = PaymentStatus.OPEN;
        }
    }

    @AssertTrue(message = "paidAt mag niet voor createdAt liggen")
    private boolean isPaidAtAfterOrEqualCreatedAt() {
        if (createdAt == null || paidAt == null) {
            return true;
        }
        return !paidAt.isBefore(createdAt);
    }

    // =========================
    // Getters
    // =========================

    public Long getId() {
        return id;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getPaidAt() {
        return paidAt;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public String getDescription() {
        return description;
    }

    public User getStudent() {
        return student;
    }

    // =========================
    // Setters
    // =========================

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public void setPaidAt(LocalDateTime paidAt) {
        this.paidAt = paidAt;
    }

    public void setStatus(PaymentStatus status) {
        this.status = (status == null) ? PaymentStatus.OPEN : status;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setStudent(User student) {
        this.student = student;
    }

    // =========================
    // Convenience
    // =========================

    public void markPaidNow() {
        this.status = PaymentStatus.PAID;
        this.paidAt = LocalDateTime.now();
    }

    public boolean isPaid() {
        return this.status == PaymentStatus.PAID;
    }

    @Override
    public String toString() {
        return "Payment{" +
                "id=" + id +
                ", amount=" + amount +
                ", createdAt=" + createdAt +
                ", paidAt=" + paidAt +
                ", status=" + status +
                ", student=" + (student != null ? student.getUsername() : "null") +
                '}';
    }
}