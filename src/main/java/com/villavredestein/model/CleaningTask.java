package com.villavredestein.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

@Entity
@Table(
        name = "cleaning_tasks",
        indexes = {
                @Index(name = "idx_cleaning_task_week", columnList = "week_number"),
                @Index(name = "idx_cleaning_task_completed", columnList = "completed")
        }
)
public class CleaningTask {

    public static final String ROLE_ALL = "ALL";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Min(value = 1, message = "Weeknummer moet minimaal 1 zijn")
    @Max(value = 53, message = "Weeknummer mag maximaal 53 zijn")
    @Column(name = "week_number", nullable = false)
    private int weekNumber;

    @NotBlank(message = "Naam van de taak is verplicht")
    @Column(nullable = false, length = 120)
    private String name;

    @Column(length = 1000)
    private String description;

    @Column(nullable = false)
    private boolean completed = false;

    @Column(length = 1000)
    private String comment;

    @Column(length = 1000)
    private String incidentReport;

    /**
     * Bepaalt voor welke rol(len) deze taak zichtbaar of uitvoerbaar is.
     * Voorbeelden: ALL, ADMIN, STUDENT, CLEANER.
     */
    @Column(name = "role_access", nullable = false, length = 20)
    private String roleAccess = ROLE_ALL;

    /**
     * De gebruiker aan wie deze taak is toegewezen.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_to_id")
    @JsonIgnoreProperties({"password", "hibernateLazyInitializer", "handler"})
    private User assignedTo;

    public CleaningTask() {
    }

    public CleaningTask(int weekNumber, String name, String description, String roleAccess) {
        this.weekNumber = weekNumber;
        this.name = name;
        this.description = description;
        this.roleAccess = (roleAccess == null || roleAccess.isBlank())
                ? ROLE_ALL
                : roleAccess;
    }

    @PrePersist
    @PreUpdate
    private void normalize() {
        roleAccess = (roleAccess == null || roleAccess.isBlank())
                ? ROLE_ALL
                : roleAccess.trim().toUpperCase();

        if (name != null) {
            name = name.trim();
        }
    }

    // =========================
    // Getters
    // =========================

    public Long getId() {
        return id;
    }

    public int getWeekNumber() {
        return weekNumber;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public boolean isCompleted() {
        return completed;
    }

    public String getComment() {
        return comment;
    }

    public String getIncidentReport() {
        return incidentReport;
    }

    public String getRoleAccess() {
        return roleAccess;
    }

    public User getAssignedTo() {
        return assignedTo;
    }

    // =========================
    // Setters
    // =========================

    public void setWeekNumber(int weekNumber) {
        this.weekNumber = weekNumber;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public void setIncidentReport(String incidentReport) {
        this.incidentReport = incidentReport;
    }

    public void setRoleAccess(String roleAccess) {
        this.roleAccess = roleAccess;
    }

    public void setAssignedTo(User assignedTo) {
        this.assignedTo = assignedTo;
    }
}