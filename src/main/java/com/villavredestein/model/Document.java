package com.villavredestein.model;

import com.villavredestein.model.User;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
public class Document {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String fileName;

    @Column(nullable = false)
    private String contentType;

    @Column(nullable = false)
    private long size;

    @Column(nullable = false)
    private String storagePath;

    @Column(nullable = false)
    private Instant uploadedAt;

    @ManyToOne(optional = false)
    @JoinColumn(name = "uploader_id")
    private User uploader;

    // --- Constructors ---
    public Document() {}

    public Document(String fileName, String contentType, long size, String storagePath, Instant uploadedAt) {
        this.fileName = fileName;
        this.contentType = contentType;
        this.size = size;
        this.storagePath = storagePath;
        this.uploadedAt = uploadedAt;
    }

    // --- Getters en Setters ---
    public Long getId() { return id; }

    public void setId(Long id) { this.id = id; }

    public String getFileName() { return fileName; }

    public void setFileName(String fileName) { this.fileName = fileName; }

    public String getContentType() { return contentType; }

    public void setContentType(String contentType) { this.contentType = contentType; }

    public long getSize() { return size; }

    public void setSize(long size) { this.size = size; }

    public String getStoragePath() { return storagePath; }

    public void setStoragePath(String storagePath) { this.storagePath = storagePath; }

    public Instant getUploadedAt() { return uploadedAt; }

    public void setUploadedAt(Instant uploadedAt) { this.uploadedAt = uploadedAt; }

    public User getUploader() {
        return uploader;
    }

    public void setUploader(User uploader) {
        this.uploader = uploader;
    }
}