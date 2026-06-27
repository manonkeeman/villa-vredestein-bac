package com.villavredestein.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "task_photos",
        indexes = @Index(name = "idx_task_photos_task", columnList = "task_id"))
public class TaskPhoto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer"})
    private CleaningTask task;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uploaded_by_id", nullable = false)
    @JsonIgnoreProperties({"password", "invoices", "hibernateLazyInitializer"})
    private User uploadedBy;

    @Column(name = "photo_path", nullable = false, length = 500)
    private String photoPath;

    @Column(name = "uploaded_at", nullable = false, updatable = false)
    private Instant uploadedAt = Instant.now();

    public Long getId() { return id; }
    public CleaningTask getTask() { return task; }
    public void setTask(CleaningTask task) { this.task = task; }
    public User getUploadedBy() { return uploadedBy; }
    public void setUploadedBy(User uploadedBy) { this.uploadedBy = uploadedBy; }
    public String getPhotoPath() { return photoPath; }
    public void setPhotoPath(String photoPath) { this.photoPath = photoPath; }
    public Instant getUploadedAt() { return uploadedAt; }
}
