package com.villavredestein.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

import java.util.Locale;

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
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "uploaded_by_id", nullable = false)
    @JsonIgnoreProperties({"password", "hibernateLazyInitializer", "handler"})
    private User uploadedBy;

    protected Document() {
    }

    public Document(String title,
                    String description,
                    String storagePath,
                    String roleAccess,
                    User uploadedBy) {
        this.title = title;
        this.description = description;
        this.storagePath = storagePath;
        this.roleAccess = (roleAccess == null || roleAccess.isBlank())
                ? ROLE_ALL
                : roleAccess.trim().toUpperCase(Locale.ROOT);
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
                : roleAccess.trim().toUpperCase(Locale.ROOT);
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