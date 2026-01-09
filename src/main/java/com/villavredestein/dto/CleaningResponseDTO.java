package com.villavredestein.dto;

public class CleaningResponseDTO {

    private final Long id;
    private final int weekNumber;
    private final String name;
    private final String description;
    private final boolean completed;
    private final String assignedTo;
    private final String comment;
    private final String incidentReport;

    public CleaningResponseDTO(Long id,
                               int weekNumber,
                               String name,
                               String description,
                               boolean completed,
                               String assignedTo,
                               String comment,
                               String incidentReport) {
        this.id = id;
        this.weekNumber = weekNumber;
        this.name = name;
        this.description = description;
        this.completed = completed;
        this.assignedTo = assignedTo;
        this.comment = comment;
        this.incidentReport = incidentReport;
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
}