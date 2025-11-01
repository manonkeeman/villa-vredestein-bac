package com.villavredestein.model;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "cleaning_schedules")
public class CleaningSchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int weekNumber;

    @OneToMany(mappedBy = "cleaningSchedule", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CleaningTask> cleaningTasks = new ArrayList<>();

    public CleaningSchedule() {}

    public CleaningSchedule(int weekNumber) {
        this.weekNumber = weekNumber;
    }

    public Long getId() { return id; }

    public int getWeekNumber() { return weekNumber; }
    public void setWeekNumber(int weekNumber) { this.weekNumber = weekNumber; }

    public List<CleaningTask> getCleaningTasks() { return cleaningTasks; }
    public void setCleaningTasks(List<CleaningTask> cleaningTasks) { this.cleaningTasks = cleaningTasks; }

    public void addTask(CleaningTask task) {
        cleaningTasks.add(task);
        task.setCleaningSchedule(this);
    }

    public void removeTask(CleaningTask task) {
        cleaningTasks.remove(task);
        task.setCleaningSchedule(null);
    }
}