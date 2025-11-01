package com.villavredestein.controller;

import com.villavredestein.dto.CleaningRequestDTO;
import com.villavredestein.dto.CleaningResponseDTO;
import com.villavredestein.service.CleaningService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cleaner")
@PreAuthorize("hasRole('CLEANER')")
public class CleanerController {

    private final CleaningService cleaningService;

    public CleanerController(CleaningService cleaningService) {
        this.cleaningService = cleaningService;
    }


    @GetMapping("/schedules")
    public ResponseEntity<List<CleaningResponseDTO>> getAllSchedules() {
        return ResponseEntity.ok(cleaningService.getAllSchedules());
    }

    @GetMapping("/schedules/week/{week}")
    public ResponseEntity<CleaningResponseDTO> getScheduleByWeek(@PathVariable int week) {
        return ResponseEntity.ok(cleaningService.getByWeek(week));
    }


    @PutMapping("/tasks/{id}/toggle")
    public ResponseEntity<CleaningRequestDTO> toggleTask(@PathVariable Long id,
                                                         @AuthenticationPrincipal UserDetails userDetails) {
        String email = userDetails.getUsername();
        CleaningRequestDTO updated = cleaningService.toggleTaskForCleaner(id, email);
        return ResponseEntity.ok(updated);
    }

    @PutMapping("/tasks/{id}/note")
    public ResponseEntity<CleaningRequestDTO> updateTaskNote(@PathVariable Long id,
                                                             @RequestParam String note,
                                                             @AuthenticationPrincipal UserDetails userDetails) {
        String email = userDetails.getUsername();
        CleaningRequestDTO updated = cleaningService.updateTaskNoteForCleaner(id, note, email);
        return ResponseEntity.ok(updated);
    }
}