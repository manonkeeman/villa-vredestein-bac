package com.villavredestein.model;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "tasks")
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name; // bv. Keuken, Badkamer, Vuilnis

    @Column(nullable = false)
    private String description;

    // Optioneel bewijsdocument (foto/pdf van uitgevoerde taak)
    @OneToOne
    @JoinColumn(name = "evidence_document_id")
    private Document evidenceDocument;

    // Optioneel: gebruiker die verantwoordelijk is voor de taak
    @ManyToOne
    @JoinColumn(name = "assigned_user_id")
    private User assignedUser;

    // Automatische timestamps
    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(nullable = false)
    private boolean completed = false;

    // --- Constructors ---
    public Task() {
    }

    public Task(String name, String description, Document evidenceDocument, User assignedUser, Instant createdAt, boolean completed) {
        this.name = name;
        this.description = description;
        this.evidenceDocument = evidenceDocument;
        this.assignedUser = assignedUser;
        this.createdAt = createdAt;
        this.completed = completed;
    }

    // --- Getters en Setters ---
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
}