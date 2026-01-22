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

    public static final String ROLE_ALL = "ROLE_ALL";

    public static final String LEGACY_ALL = "ALL";

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

    @Column(name = "role_access", nullable = false, length = 20)
    private String roleAccess = ROLE_ALL;

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
        this.roleAccess = normalizeRoleAccess(roleAccess);
        this.uploadedBy = uploadedBy;
        normalize();
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

        roleAccess = normalizeRoleAccess(roleAccess);
    }

    private static String normalizeRoleAccess(String roleAccess) {
        if (roleAccess == null || roleAccess.isBlank()) {
            return ROLE_ALL;
        }

        String normalized = roleAccess.trim().toUpperCase(Locale.ROOT);

        if (normalized.startsWith("ROLE_")) {
            normalized = normalized.substring("ROLE_".length());
        }

        if (LEGACY_ALL.equals(normalized)) {
            return ROLE_ALL;
        }

        if ("ADMIN".equals(normalized) || "STUDENT".equals(normalized) || "CLEANER".equals(normalized)) {
            return normalized;
        }

        return normalized;
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = (title == null) ? null : title.trim();
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = (description == null) ? null : description.trim();
    }

    public String getStoragePath() {
        return storagePath;
    }

    public void setStoragePath(String storagePath) {
        this.storagePath = (storagePath == null) ? null : storagePath.trim();
    }

    public String getRoleAccess() {
        return roleAccess;
    }

    public void setRoleAccess(String roleAccess) {
        this.roleAccess = normalizeRoleAccess(roleAccess);
    }

    public User getUploadedBy() {
        return uploadedBy;
    }

    public void setUploadedBy(User uploadedBy) {
        this.uploadedBy = uploadedBy;
    }
}