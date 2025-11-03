package com.villavredestein.model;

import jakarta.persistence.*;

@Entity
@Table(name = "cleaning_tasks")
public class CleaningTask {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private int weekNumber;

    @Column(nullable = false)
    private String name;

    @Column(length = 1000)
    private String description;

    private boolean completed;
    private String comment;
    private String incidentReport;

    @Column(nullable = false)
    private String roleAccess = "ALL";

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_to_id")
    private User assignedTo;

    public CleaningTask() {}

    public CleaningTask(int weekNumber, String name, String description, String roleAccess) {
        this.weekNumber = weekNumber;
        this.name = name;
        this.description = description;
        this.roleAccess = roleAccess != null ? roleAccess : "ALL";
    }

    public Long getId() { return id; }
    public int getWeekNumber() { return weekNumber; }
    public void setWeekNumber(int weekNumber) { this.weekNumber = weekNumber; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public boolean isCompleted() { return completed; }
    public void setCompleted(boolean completed) { this.completed = completed; }
    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }
    public String getIncidentReport() { return incidentReport; }
    public void setIncidentReport(String incidentReport) { this.incidentReport = incidentReport; }
    public String getRoleAccess() { return roleAccess; }
    public void setRoleAccess(String roleAccess) { this.roleAccess = roleAccess; }
    public User getAssignedTo() { return assignedTo; }
    public void setAssignedTo(User assignedTo) { this.assignedTo = assignedTo; }
}