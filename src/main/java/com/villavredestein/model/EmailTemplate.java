package com.villavredestein.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

@Entity
@Table(name = "email_templates")
public class EmailTemplate {

    public enum TemplateType {
        PAYMENT_NEW,
        PAYMENT_REMINDER_1,
        PAYMENT_REMINDER_2,
        OVERDUE,
        MISSED_CLEANING
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = true, length = 30)
    private TemplateType type;

    @NotBlank
    @Size(max = 200)
    @Column(nullable = false, length = 200)
    private String subject;

    @NotBlank
    @Column(nullable = false, columnDefinition = "TEXT")
    private String body;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    protected EmailTemplate() {}

    public EmailTemplate(TemplateType type, String subject, String body) {
        this.type = type;
        this.subject = subject;
        this.body = body;
        this.updatedAt = LocalDateTime.now();
    }

    @PrePersist @PreUpdate
    private void touch() { this.updatedAt = LocalDateTime.now(); }

    public Long getId() { return id; }
    public TemplateType getType() { return type; }
    public String getSubject() { return subject; }
    public String getBody() { return body; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    public void setSubject(String subject) { this.subject = subject; }
    public void setBody(String body) { this.body = body; }

    public String renderBody(String naam, String bedrag, String maand, String betaalLink, String vervaldatum) {
        return body
                .replace("{{naam}}", safe(naam))
                .replace("{{bedrag}}", safe(bedrag))
                .replace("{{maand}}", safe(maand))
                .replace("{{betaalLink}}", safe(betaalLink))
                .replace("{{vervaldatum}}", safe(vervaldatum));
    }

    public String renderSubject(String naam, String bedrag, String maand, String betaalLink, String vervaldatum) {
        return subject
                .replace("{{naam}}", safe(naam))
                .replace("{{bedrag}}", safe(bedrag))
                .replace("{{maand}}", safe(maand))
                .replace("{{betaalLink}}", safe(betaalLink))
                .replace("{{vervaldatum}}", safe(vervaldatum));
    }

    private static String safe(String s) { return s != null ? s : ""; }
}
