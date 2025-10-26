package com.villavredestein.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.Instant;

@Entity
@Table(name = "documents")
public class Document {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Bestandsnaam mag niet leeg zijn")
    @Size(max = 255, message = "Bestandsnaam te lang (maximaal 255 tekens)")
    @Column(name = "file_name", nullable = false, length = 255)
    private String fileName;

    @NotBlank(message = "Content type is verplicht")
    @Size(max = 255, message = "Content type te lang (maximaal 255 tekens)")
    @Column(name = "content_type", nullable = false, length = 255)
    private String contentType;

    @Positive(message = "Bestandsgrootte moet positief zijn")
    @Column(nullable = false)
    private long size;

    @NotBlank(message = "Opslagpad mag niet leeg zijn")
    @Size(max = 255, message = "Opslagpad te lang (maximaal 255 tekens)")
    @Column(name = "storage_path", nullable = false, length = 255)
    private String storagePath;

    @NotNull(message = "Uploadmoment is verplicht")
    @Column(name = "uploaded_at", nullable = false)
    private Instant uploadedAt = Instant.now();

    @NotNull(message = "Uploader is verplicht")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "uploader_id", nullable = false)
    private User uploader;

    public Document() {
    }

    public Document(String fileName, String contentType, long size, String storagePath, Instant uploadedAt, User uploader) {
        this.fileName = fileName;
        this.contentType = contentType;
        this.size = size;
        this.storagePath = storagePath;
        this.uploadedAt = (uploadedAt != null) ? uploadedAt : Instant.now();
        this.uploader = uploader;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public String getStoragePath() {
        return storagePath;
    }

    public void setStoragePath(String storagePath) {
        this.storagePath = storagePath;
    }

    public Instant getUploadedAt() {
        return uploadedAt;
    }

    public void setUploadedAt(Instant uploadedAt) {
        this.uploadedAt = uploadedAt;
    }

    public User getUploader() {
        return uploader;
    }

    public void setUploader(User uploader) {
        this.uploader = uploader;
    }
}