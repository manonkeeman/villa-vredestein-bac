package com.villavredestein.controller;

import com.villavredestein.dto.CleaningResponseDTO;
import com.villavredestein.dto.CleaningRequestDTO;
import com.villavredestein.service.CleaningService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cleaning")
public class CleaningController {

    private final CleaningService cleaningService;

    public CleaningController(CleaningService cleaningService) {
        this.cleaningService = cleaningService;
    }

    @GetMapping("/schedules")
    public List<CleaningResponseDTO> getAllSchedules() {
        return cleaningService.getAllSchedules();
    }

    @PostMapping("/schedules")
    public CleaningResponseDTO createSchedule(@RequestParam int weekNumber) {
        return cleaningService.createSchedule(weekNumber);
    }

    @PostMapping("/schedules/{scheduleId}/tasks")
    public CleaningRequestDTO addTask(@PathVariable Long scheduleId, @RequestBody CleaningRequestDTO dto) {
        return cleaningService.addTask(scheduleId, dto);
    }

    @PutMapping("/tasks/{taskId}/toggle")
    public CleaningRequestDTO toggleCompleted(@PathVariable Long taskId) {
        return cleaningService.toggleTask(taskId); // ✅ juiste methode in service
    }

    @DeleteMapping("/tasks/{taskId}")
    public void deleteTask(@PathVariable Long taskId) {
        cleaningService.deleteTask(taskId);
    }
}