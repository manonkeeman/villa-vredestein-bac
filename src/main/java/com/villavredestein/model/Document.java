package com.villavredestein.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

/**
 * {@code Document} representeert een opgeslagen document binnen Villa Vredestein.
 *
 * <p>Deze entity bevat uitsluitend metadata die nodig is om een bestand terug te vinden
 * in storage en om toegangscontrole toe te passen. De daadwerkelijke bestandsinhoud
 * wordt niet in de database opgeslagen.</p>
 *
 * <p>Let op: stuur entities niet direct naar de client. Gebruik DTO’s in de controllerlaag.</p>
 */
@Entity
@Table(
        name = "documents",
        indexes = {
                @Index(name = "idx_document_role_access", columnList = "role_access"),
                @Index(name = "idx_document_uploaded_by", columnList = "uploaded_by_id")
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_document_storage_path", columnNames = "storage_path")
        }
)
public class Document {

    public static final String ROLE_ALL = "ALL";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Titel is verplicht")
    @Column(nullable = false, length = 120)
    private String title;

    @Column(length = 1000)
    private String description;

    /**
     * Interne opslagreferentie naar het bestand (bijv. pad of storage key).
     * Deze waarde moet uniek zijn.
     */
    @NotBlank(message = "storagePath is verplicht")
    @Column(name = "storage_path", nullable = false, length = 255, unique = true)
    private String storagePath;

    /**
     * Bepaalt voor welke rol(len) dit document zichtbaar is.
     * Voorbeelden: ALL, ADMIN, STUDENT, CLEANER.
     */
    @Column(name = "role_access", nullable = false, length = 20)
    private String roleAccess = ROLE_ALL;

    /**
     * De gebruiker die het document heeft geüpload.
     *
     * <p>Lazy loading voorkomt onnodige database queries.
     * Gevoelige velden worden genegeerd bij serialisatie.</p>
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "uploaded_by_id", nullable = false)
    @JsonIgnoreProperties({"password", "hibernateLazyInitializer", "handler"})
    private User uploadedBy;

    /**
     * Publieke no-args constructor vereist door JPA.
     */
    public Document() {
    }

    public Document(String title, String description, String storagePath, String roleAccess, User uploadedBy) {
        this.title = title;
        this.description = description;
        this.storagePath = storagePath;
        this.roleAccess = (roleAccess == null || roleAccess.isBlank()) ? ROLE_ALL : roleAccess;
        this.uploadedBy = uploadedBy;
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

        if (storagePath != null) {
            storagePath = storagePath.trim();
        }

        roleAccess = (roleAccess == null || roleAccess.isBlank())
                ? ROLE_ALL
                : roleAccess.trim().toUpperCase();
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStoragePath() {
        return storagePath;
    }

    public void setStoragePath(String storagePath) {
        this.storagePath = storagePath;
    }

    public String getRoleAccess() {
        return roleAccess;
    }

    public void setRoleAccess(String roleAccess) {
        this.roleAccess = roleAccess;
    }

    public User getUploadedBy() {
        return uploadedBy;
    }

    public void setUploadedBy(User uploadedBy) {
        this.uploadedBy = uploadedBy;
    }
}