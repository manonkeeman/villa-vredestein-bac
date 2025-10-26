package com.villavredestein.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.Instant;

@Entity
@Table(name = "tasks")
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Taaknaam is verplicht")
    @Size(min = 3, max = 100, message = "Taaknaam moet tussen 3 en 100 tekens bevatten")
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @NotBlank(message = "Omschrijving is verplicht")
    @Size(max = 255, message = "Omschrijving mag maximaal 255 tekens bevatten")
    @Column(name = "description", nullable = false, length = 255)
    private String description;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "completed", nullable = false)
    private boolean completed = false;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "evidence_document_id")
    private Document evidenceDocument;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_user_id")
    private User assignedUser;

    public Task() {
    }

    public Task(String name, String description, Document evidenceDocument, User assignedUser, Instant createdAt, boolean completed) {
        this.name = name;
        this.description = description;
        this.evidenceDocument = evidenceDocument;
        this.assignedUser = assignedUser;
        this.createdAt = (createdAt != null) ? createdAt : Instant.now();
        this.completed = completed;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public Document getEvidenceDocument() {
        return evidenceDocument;
    }

    public void setEvidenceDocument(Document evidenceDocument) {
        this.evidenceDocument = evidenceDocument;
    }

    public User getAssignedUser() {
        return assignedUser;
    }

    public void setAssignedUser(User assignedUser) {
        this.assignedUser = assignedUser;
    }
}