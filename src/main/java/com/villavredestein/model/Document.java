package com.villavredestein.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "documents")
public class Document {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;           // "Huisregels"
    private String description;     // "Huisregels 2025.pdf"
    private String storagePath;     // Pad naar opgeslagen bestand op schijf
    private String contentType;     // "application/pdf"
    private String roleAccess;      // "STUDENT", "ADMIN" of "ALL"
    private long size;              // Bestandsgrootte in bytes
    private LocalDateTime uploadedAt = LocalDateTime.now();

    @ManyToOne
    private User uploadedBy;

    public Document() {}

    public Document(String title, String description, String storagePath, String contentType,
                    String roleAccess, long size, User uploadedBy) {
        this.title = title;
        this.description = description;
        this.storagePath = storagePath;
        this.contentType = contentType;
        this.roleAccess = roleAccess;
        this.size = size;
        this.uploadedBy = uploadedBy;
        this.uploadedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getStoragePath() { return storagePath; }
    public void setStoragePath(String storagePath) { this.storagePath = storagePath; }
    public String getContentType() { return contentType; }
    public void setContentType(String contentType) { this.contentType = contentType; }
    public String getRoleAccess() { return roleAccess; }
    public void setRoleAccess(String roleAccess) { this.roleAccess = roleAccess; }
    public long getSize() { return size; }
    public void setSize(long size) { this.size = size; }
    public LocalDateTime getUploadedAt() { return uploadedAt; }
    public void setUploadedAt(LocalDateTime uploadedAt) { this.uploadedAt = uploadedAt; }
    public User getUploadedBy() { return uploadedBy; }
    public void setUploadedBy(User uploadedBy) { this.uploadedBy = uploadedBy; }
}