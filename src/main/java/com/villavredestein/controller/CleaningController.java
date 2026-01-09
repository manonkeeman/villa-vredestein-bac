package com.villavredestein.controller;

import com.villavredestein.dto.CleaningRequestDTO;
import com.villavredestein.dto.CleaningResponseDTO;
import com.villavredestein.service.CleaningService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST-controller voor het beheren van schoonmaaktaken binnen Villa Vredestein.
 */
@RestController
@RequestMapping("/api/cleaning")
@CrossOrigin
public class CleaningController {

    private final CleaningService cleaningService;

    public CleaningController(CleaningService cleaningService) {
        this.cleaningService = cleaningService;
    }

    /**
     * Gezondheidscheck voor rolgebaseerde toegang.
     */
    @GetMapping("/tasks/test-cleaner")
    @PreAuthorize("hasRole('CLEANER')")
    public ResponseEntity<String> cleanerAccessCheck() {
        return ResponseEntity.ok("CLEANER OK");
    }

    /**
     * Haalt alle schoonmaaktaken op, optioneel gefilterd op weeknummer.
     */
    @GetMapping("/tasks")
    @PreAuthorize("hasAnyRole('ADMIN','STUDENT','CLEANER')")
    public ResponseEntity<List<CleaningResponseDTO>> getTasks(
            @RequestParam(required = false) Integer weekNumber
    ) {
        List<CleaningResponseDTO> result = (weekNumber == null)
                ? cleaningService.getAllTasks()
                : cleaningService.getTasksByWeek(weekNumber);

        return ResponseEntity.ok(result);
    }

    /**
     * Maakt een nieuwe schoonmaaktaak aan.
     */
    @PostMapping("/tasks")
    @PreAuthorize("hasAnyRole('ADMIN','CLEANER')")
    public ResponseEntity<CleaningResponseDTO> createTask(
            @Valid @RequestBody CleaningRequestDTO dto
    ) {
        CleaningResponseDTO created = cleaningService.addTask(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * Zet een taak op afgerond of weer open.
     */
    @PutMapping("/tasks/{taskId}/toggle")
    @PreAuthorize("hasAnyRole('ADMIN','STUDENT','CLEANER')")
    public ResponseEntity<CleaningResponseDTO> toggleTask(@PathVariable Long taskId) {
        return ResponseEntity.ok(cleaningService.toggleTask(taskId));
    }

    /**
     * Voegt of wijzigt een opmerking bij een taak.
     */
    @PutMapping("/tasks/{taskId}/comment")
    @PreAuthorize("hasAnyRole('ADMIN','CLEANER')")
    public ResponseEntity<CleaningResponseDTO> addComment(
            @PathVariable Long taskId,
            @RequestParam String comment
    ) {
        return ResponseEntity.ok(cleaningService.addComment(taskId, comment));
    }

    /**
     * Registreert een incidentrapport bij een taak.
     */
    @PutMapping("/tasks/{taskId}/incident")
    @PreAuthorize("hasAnyRole('ADMIN','CLEANER')")
    public ResponseEntity<CleaningResponseDTO> addIncident(
            @PathVariable Long taskId,
            @RequestParam String incident
    ) {
        return ResponseEntity.ok(cleaningService.addIncident(taskId, incident));
    }

    /**
     * Verwijdert een schoonmaaktaak.
     */
    @DeleteMapping("/tasks/{taskId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteTask(@PathVariable Long taskId) {
        cleaningService.deleteTask(taskId);
        return ResponseEntity.noContent().build();
    }
}