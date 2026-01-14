package com.villavredestein.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;

public class InvoiceRequestDTO {

    @NotBlank(message = "Titel is verplicht")
    @Size(max = 120, message = "Titel mag maximaal 120 tekens bevatten")
    private String title;

    @Size(max = 1000, message = "Omschrijving mag maximaal 1000 tekens bevatten")
    private String description;

    @NotNull(message = "Bedrag is verplicht")
    @DecimalMin(value = "0.01", message = "Bedrag moet groter zijn dan 0")
    @Digits(integer = 8, fraction = 2, message = "Bedrag mag maximaal 8 cijfers en 2 decimalen bevatten")
    private BigDecimal amount;

    @NotNull(message = "Factuurdatum is verplicht")
    private LocalDate issueDate;

    @NotNull(message = "Vervaldatum is verplicht")
    @FutureOrPresent(message = "Vervaldatum mag niet in het verleden liggen")
    private LocalDate dueDate;

    @NotBlank(message = "Student e-mailadres is verplicht")
    @Email(message = "Ongeldig e-mailadres")
    private String studentEmail;

    protected InvoiceRequestDTO() {
    }

    public InvoiceRequestDTO(String title,
                             String description,
                             BigDecimal amount,
                             LocalDate issueDate,
                             LocalDate dueDate,
                             String studentEmail) {
        this.title = title;
        this.description = description;
        this.amount = amount;
        this.issueDate = issueDate;
        this.dueDate = dueDate;
        this.studentEmail = studentEmail;
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

    public String getStudentEmail() {
        return studentEmail;
    }
}