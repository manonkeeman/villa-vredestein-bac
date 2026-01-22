package com.villavredestein.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class PaymentRequestDTO {

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    @Digits(integer = 8, fraction = 2, message = "Amount may contain up to 8 digits and 2 decimals")
    private BigDecimal amount;

    @Size(max = 500, message = "Description may contain at most 500 characters")
    private String description;

    @NotBlank(message = "Status is required")
    private String status;

    @PastOrPresent(message = "Paid date may not be in the future")
    private LocalDateTime paidAt;

    @NotBlank(message = "Student email is required")
    @Email(message = "Invalid email address")
    private String studentEmail;

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description == null ? null : description.trim();
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status == null ? null : status.trim().toUpperCase();
    }

    public LocalDateTime getPaidAt() {
        return paidAt;
    }

    public void setPaidAt(LocalDateTime paidAt) {
        this.paidAt = paidAt;
    }

    public String getStudentEmail() {
        return studentEmail;
    }

    public void setStudentEmail(String studentEmail) {
        this.studentEmail = studentEmail == null ? null : studentEmail.trim().toLowerCase();
    }
}