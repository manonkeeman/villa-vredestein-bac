package com.villavredestein.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "cleaning_tasks")
public class CleaningTask {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String description;
    private LocalDate dueDate;
    private boolean completed;

    @ManyToOne
    private User assignedTo;

    @ManyToOne
    private CleaningSchedule cleaningSchedule;

    // ✅ correcte constructor (zelfde naam als class)
    public CleaningTask() {}

    public CleaningTask(String name, String description, LocalDate dueDate, boolean completed, User assignedTo, CleaningSchedule cleaningSchedule) {
        this.name = name;
        this.description = description;
        this.dueDate = dueDate;
        this.completed = completed;
        this.assignedTo = assignedTo;
        this.cleaningSchedule = cleaningSchedule;
    }

    // ✅ Getters en Setters
    public Long getId() { return id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }

    public boolean isCompleted() { return completed; }
    public void setCompleted(boolean completed) { this.completed = completed; }

    public User getAssignedTo() { return assignedTo; }
    public void setAssignedTo(User assignedTo) { this.assignedTo = assignedTo; }

    public CleaningSchedule getCleaningSchedule() { return cleaningSchedule; }
    public void setCleaningSchedule(CleaningSchedule cleaningSchedule) { this.cleaningSchedule = cleaningSchedule; }
}