package com.villavredestein.model;

import java.util.Locale;

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

    // Canonical public access value we store going forward
    public static final String ROLE_ALL = "ROLE_ALL";

    // Backwards compatibility: older rows / inputs may still contain "ALL"
    public static final String LEGACY_ALL = "ALL";

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

    @Column(name = "role_access", nullable = false, length = 20)
    private String roleAccess = ROLE_ALL;

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
        this.roleAccess = normalizeRoleAccess(roleAccess);
    }

    private static String normalizeRoleAccess(String roleAccess) {
        if (roleAccess == null || roleAccess.isBlank()) {
            return ROLE_ALL;
        }

        String normalized = roleAccess.trim().toUpperCase(Locale.ROOT);

        // Convert Spring Security authority format (ROLE_STUDENT -> STUDENT)
        if (normalized.startsWith("ROLE_")) {
            normalized = normalized.substring("ROLE_".length());
        }

        // Legacy support
        if (LEGACY_ALL.equals(normalized)) {
            return ROLE_ALL;
        }

        // Canonical values
        if ("ADMIN".equals(normalized) || "STUDENT".equals(normalized) || "CLEANER".equals(normalized)) {
            return normalized;
        }

        // Keep as-is (uppercased). Validation/service layer can reject if needed.
        return normalized;
    }

    @PrePersist
    @PreUpdate
    private void normalize() {
        roleAccess = normalizeRoleAccess(roleAccess);

        if (name != null) {
            name = name.trim();
        }
        if (description != null) {
            description = description.trim();
        }
    }

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


    public void setWeekNumber(int weekNumber) {
        this.weekNumber = weekNumber;
    }

    public void setName(String name) {
        this.name = (name == null) ? null : name.trim();
    }

    public void setDescription(String description) {
        this.description = (description == null) ? null : description.trim();
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
        this.roleAccess = normalizeRoleAccess(roleAccess);
    }

    public void setAssignedTo(User assignedTo) {
        this.assignedTo = assignedTo;
    }
}