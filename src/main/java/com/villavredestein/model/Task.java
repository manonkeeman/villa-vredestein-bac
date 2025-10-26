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

    @NotBlank(message = "Taaknaam mag niet leeg zijn")
    @Size(min = 3, max = 100, message = "Taaknaam moet tussen 3 en 100 tekens bevatten")
    @Column(nullable = false)
    private String name;

    @NotBlank(message = "Omschrijving mag niet leeg zijn")
    @Size(max = 255, message = "Omschrijving mag maximaal 255 tekens bevatten")
    @Column(nullable = false)
    private String description;

    @OneToOne
    @JoinColumn(name = "evidence_document_id")
    private Document evidenceDocument;

    @ManyToOne
    @JoinColumn(name = "assigned_user_id")
    private User assignedUser;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(nullable = false)
    private boolean completed = false;

    public Task() {}

    public Task(String name, String description, Document evidenceDocument, User assignedUser, Instant createdAt, boolean completed) {
        this.name = name;
        this.description = description;
        this.evidenceDocument = evidenceDocument;
        this.assignedUser = assignedUser;
        this.createdAt = createdAt;
        this.completed = completed;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Document getEvidenceDocument() { return evidenceDocument; }
    public void setEvidenceDocument(Document evidenceDocument) { this.evidenceDocument = evidenceDocument; }

    public User getAssignedUser() { return assignedUser; }
    public void setAssignedUser(User assignedUser) { this.assignedUser = assignedUser; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public boolean isCompleted() { return completed; }
    public void setCompleted(boolean completed) { this.completed = completed; }
}