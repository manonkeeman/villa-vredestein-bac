package com.villavredestein.controller;

import com.villavredestein.dto.CleaningRequestDTO;
import com.villavredestein.service.CleaningService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cleaning")
@CrossOrigin(origins = "*")
public class CleaningController {

    private final CleaningService cleaningService;

    public CleaningController(CleaningService cleaningService) {
        this.cleaningService = cleaningService;
    }

    @GetMapping("/tasks")
    @PreAuthorize("hasAnyRole('ADMIN','STUDENT','CLEANER')")
    public ResponseEntity<List<CleaningRequestDTO>> getTasks(
            @RequestParam(required = false) Integer weekNumber) {
        if (weekNumber != null) {
            return ResponseEntity.ok(cleaningService.getTasksByWeek(weekNumber));
        }
        return ResponseEntity.ok(cleaningService.getAllTasks());
    }

    @PostMapping("/tasks")
    @PreAuthorize("hasAnyRole('ADMIN','CLEANER')")
    public ResponseEntity<CleaningRequestDTO> createTask(@RequestBody CleaningRequestDTO dto) {
        return ResponseEntity.ok(cleaningService.addTask(dto));
    }

    @PutMapping("/tasks/{taskId}/toggle")
    @PreAuthorize("hasAnyRole('ADMIN','STUDENT','CLEANER')")
    public ResponseEntity<CleaningRequestDTO> toggleTask(@PathVariable Long taskId) {
        return ResponseEntity.ok(cleaningService.toggleTask(taskId));
    }

    @PutMapping("/tasks/{taskId}/comment")
    @PreAuthorize("hasAnyRole('ADMIN','CLEANER')")
    public ResponseEntity<CleaningRequestDTO> addComment(
            @PathVariable Long taskId,
            @RequestParam String comment) {
        return ResponseEntity.ok(cleaningService.addComment(taskId, comment));
    }

    @PutMapping("/tasks/{taskId}/incident")
    @PreAuthorize("hasAnyRole('ADMIN','CLEANER')")
    public ResponseEntity<CleaningRequestDTO> addIncident(
            @PathVariable Long taskId,
            @RequestParam String incident) {
        return ResponseEntity.ok(cleaningService.addIncident(taskId, incident));
    }

    @DeleteMapping("/tasks/{taskId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteTask(@PathVariable Long taskId) {
        cleaningService.deleteTask(taskId);
        return ResponseEntity.noContent().build();
    }
}