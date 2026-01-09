package com.villavredestein.dto;

import com.villavredestein.model.Payment.PaymentStatus;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * PaymentRequestDTO bevat alle velden die de client mag aanleveren bij het aanmaken of bijwerken
 * van een betaling.
 *
 * NOVI-proof:
 * - DTO is losgekoppeld van de JPA-entity (geen entity-imports in de DTO)
 * - Validatie gebeurt met Bean Validation annotaties
 */
public class PaymentRequestDTO {

    @NotNull(message = "Bedrag is verplicht")
    @DecimalMin(value = "0.01", message = "Bedrag moet groter zijn dan 0")
    @Digits(integer = 8, fraction = 2, message = "Bedrag mag max 8 cijfers en 2 decimalen bevatten")
    private BigDecimal amount;

    @Size(max = 500, message = "Omschrijving mag maximaal 500 tekens bevatten")
    private String description;

    /**
     * Status is optioneel: als deze niet wordt meegegeven, kan de service een default zetten (bijv. OPEN).
     */
    private PaymentStatus status;

    /**
     * Betaaldatum is optioneel. Als deze wordt meegegeven, moet dit een datum in het verleden of nu zijn.
     */
    @PastOrPresent(message = "Betaaldatum mag niet in de toekomst liggen")
    private LocalDateTime paidAt;

    /**
     * E-mailadres van de student waarvoor de betaling geldt.
     */
    @NotBlank(message = "Student e-mailadres is verplicht")
    @Email(message = "Ongeldig e-mailadres")
    private String studentEmail;

    // =========================
    // Getters & setters
    // =========================

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
        this.description = description;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public void setStatus(PaymentStatus status) {
        this.status = status;
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
        this.studentEmail = studentEmail;
    }
}