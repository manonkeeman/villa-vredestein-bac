package com.villavredestein.controller;

import com.villavredestein.dto.CleaningTaskRequestDTO;
import com.villavredestein.dto.CleaningTaskResponseDTO;
import com.villavredestein.service.CleaningTaskService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// =====================================================================
// # CleaningTaskController
// =====================================================================
@Validated
@RestController
@RequestMapping("/api/cleaning")
@CrossOrigin
public class CleaningTaskController {

    private final CleaningTaskService cleaningService;

    public CleaningTaskController(CleaningTaskService cleaningService) {
        this.cleaningService = cleaningService;
    }

    // =====================================================================
    // # Access check
    // =====================================================================
    @GetMapping("/tasks/test-cleaner")
    @PreAuthorize("hasRole('CLEANER')")
    public ResponseEntity<String> cleanerAccessCheck() {
        return ResponseEntity.ok("CLEANER OK");
    }

    // =====================================================================
    // # READ – tasks
    // =====================================================================
    @GetMapping("/tasks")
    @PreAuthorize("hasAnyRole('ADMIN','STUDENT','CLEANER')")
    public ResponseEntity<List<CleaningTaskResponseDTO>> getTasks(
            @RequestParam(required = false) @Positive Integer weekNumber,
            Authentication authentication
    ) {
        String role = authentication.getAuthorities()
                .stream()
                .findFirst()
                .map(a -> a.getAuthority())
                .orElse("STUDENT");

        List<CleaningTaskResponseDTO> result = (weekNumber == null)
                ? cleaningService.getAllTasksForRole(role)
                : cleaningService.getTasksByWeekForRole(role, weekNumber);

        return ResponseEntity.ok(result);
    }

    // =====================================================================
    // # CREATE
    // =====================================================================
    @PostMapping("/tasks")
    @PreAuthorize("hasAnyRole('ADMIN','CLEANER')")
    public ResponseEntity<CleaningTaskResponseDTO> createTask(
            @Valid @RequestBody CleaningTaskRequestDTO dto
    ) {
        CleaningTaskResponseDTO created = cleaningService.addTask(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    // =====================================================================
    // # UPDATE
    // =====================================================================
    @PutMapping("/tasks/{taskId}/toggle")
    @PreAuthorize("hasAnyRole('ADMIN','STUDENT','CLEANER')")
    public ResponseEntity<CleaningTaskResponseDTO> toggleTask(
            @PathVariable @Positive Long taskId
    ) {
        return ResponseEntity.ok(cleaningService.toggleTask(taskId));
    }

    @PutMapping("/tasks/{taskId}/comment")
    @PreAuthorize("hasAnyRole('ADMIN','CLEANER')")
    public ResponseEntity<CleaningTaskResponseDTO> addComment(
            @PathVariable @Positive Long taskId,
            @RequestParam @NotBlank String comment
    ) {
        return ResponseEntity.ok(cleaningService.addComment(taskId, comment));
    }

    @PutMapping("/tasks/{taskId}/incident")
    @PreAuthorize("hasAnyRole('ADMIN','CLEANER')")
    public ResponseEntity<CleaningTaskResponseDTO> addIncident(
            @PathVariable @Positive Long taskId,
            @RequestParam @NotBlank String incident
    ) {
        return ResponseEntity.ok(cleaningService.addIncident(taskId, incident));
    }

    // =====================================================================
    // # DELETE
    // =====================================================================
    @DeleteMapping("/tasks/{taskId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteTask(@PathVariable @Positive Long taskId) {
        cleaningService.deleteTask(taskId);
        return ResponseEntity.noContent().build();
    }
}