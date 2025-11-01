package com.villavredestein.dto;

import java.util.List;

public class CleaningResponseDTO {

    private Long id;
    private int weekNumber;
    private List<CleaningRequestDTO> tasks;

    public CleaningResponseDTO() {}

    public CleaningResponseDTO(Long id, int weekNumber, List<CleaningRequestDTO> tasks) {
        this.id = id;
        this.weekNumber = weekNumber;
        this.tasks = tasks;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public int getWeekNumber() { return weekNumber; }
    public void setWeekNumber(int weekNumber) { this.weekNumber = weekNumber; }

    public List<CleaningRequestDTO> getTasks() { return tasks; }
    public void setTasks(List<CleaningRequestDTO> tasks) { this.tasks = tasks; }
}