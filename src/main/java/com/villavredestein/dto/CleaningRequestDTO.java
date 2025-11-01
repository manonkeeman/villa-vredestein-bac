package com.villavredestein.dto;

import java.time.LocalDate;

public class CleaningRequestDTO {

    private Long id;
    private String name;
    private String description;
    private LocalDate dueDate;
    private boolean completed;
    private String assignedTo;

    public CleaningRequestDTO() {}

    public CleaningRequestDTO(Long id, String name, String description,
                              LocalDate dueDate, boolean completed, String assignedTo) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.dueDate = dueDate;
        this.completed = completed;
        this.assignedTo = assignedTo;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

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
}