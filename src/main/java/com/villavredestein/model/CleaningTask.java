package com.villavredestein.model;

import jakarta.persistence.*;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "cleaning_tasks")
public class CleaningTask {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int weekNumber;
    private String name;
    private String description;
    private boolean completed;

    private String comment;           // voor notities van cleaner
    private String incidentReport;    // voor defecten of materiaal-aanvragen

    @UpdateTimestamp
    private LocalDateTime lastUpdated;

    @ManyToOne
    @JoinColumn(name = "assigned_to_id")
    private User assignedTo;

    public CleaningTask() {}

    public CleaningTask(int weekNumber, String name, String description,
                        boolean completed, String comment, String incidentReport, User assignedTo) {
        this.weekNumber = weekNumber;
        this.name = name;
        this.description = description;
        this.completed = completed;
        this.comment = comment;
        this.incidentReport = incidentReport;
        this.assignedTo = assignedTo;
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

    public LocalDateTime getLastUpdated() { return lastUpdated; }

    public User getAssignedTo() { return assignedTo; }
    public void setAssignedTo(User assignedTo) { this.assignedTo = assignedTo; }
}