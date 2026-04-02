package com.villavredestein.dto;

import java.time.LocalDate;

public class CleaningTaskResponseDTO {

    private final Long id;
    private final int weekNumber;
    private final String name;
    private final String description;
    private final boolean completed;
    private final String assignedTo;
    private final String comment;
    private final String incidentReport;
    private final LocalDate deadline;

    public CleaningTaskResponseDTO(Long id,
                                   int weekNumber,
                                   String name,
                                   String description,
                                   boolean completed,
                                   String assignedTo,
                                   String comment,
                                   String incidentReport,
                                   LocalDate deadline) {
        this.id = id;
        this.weekNumber = weekNumber;
        this.name = name;
        this.description = description;
        this.completed = completed;
        this.assignedTo = assignedTo;
        this.comment = comment;
        this.incidentReport = incidentReport;
        this.deadline = deadline;
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

    public String getAssignedTo() {
        return assignedTo;
    }

    public String getComment() {
        return comment;
    }

    public String getIncidentReport() {
        return incidentReport;
    }

    public LocalDate getDeadline() {
        return deadline;
    }
}