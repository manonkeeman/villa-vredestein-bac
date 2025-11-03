package com.villavredestein.dto;

import java.time.LocalDate;

public class CleaningRequestDTO {
    private Long id;
    private int weekNumber;
    private String name;
    private String description;
    private LocalDate dueDate;
    private boolean completed;
    private String assignedTo;
    private String comment;
    private String incidentReport;

    public CleaningRequestDTO() {}

    public CleaningRequestDTO(Long id, int weekNumber, String name, String description,
                              LocalDate dueDate, boolean completed, String assignedTo,
                              String comment, String incidentReport) {
        this.id = id;
        this.weekNumber = weekNumber;
        this.name = name;
        this.description = description;
        this.dueDate = dueDate;
        this.completed = completed;
        this.assignedTo = assignedTo;
        this.comment = comment;
        this.incidentReport = incidentReport;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public int getWeekNumber() { return weekNumber; }
    public void setWeekNumber(int weekNumber) { this.weekNumber = weekNumber; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }

    public boolean isCompleted() { return completed; }
    public void setCompleted(boolean completed) { this.completed = completed; }

    public String getAssignedTo() { return assignedTo; }
    public void setAssignedTo(String assignedTo) { this.assignedTo = assignedTo; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }

    public String getIncidentReport() { return incidentReport; }
    public void setIncidentReport(String incidentReport) { this.incidentReport = incidentReport; }
}