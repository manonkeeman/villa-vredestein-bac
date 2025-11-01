package com.villavredestein.controller;

import com.villavredestein.model.CleaningSchedule;
import com.villavredestein.repository.CleaningScheduleRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/schedules")
public class CleaningScheduleController {

    private final CleaningScheduleRepository repo;

    public CleaningScheduleController(CleaningScheduleRepository repo) {
        this.repo = repo;
    }

    // Haal alle weekschema’s op
    @GetMapping
    public ResponseEntity<List<CleaningSchedule>> getAllSchedules() {
        return ResponseEntity.ok(repo.findAll());
    }

    // Haal een specifiek weekschema op
    @GetMapping("/week/{week}")
    public ResponseEntity<CleaningSchedule> getScheduleByWeek(@PathVariable int week) {
        CleaningSchedule schedule = repo.findByWeekNumber(week);
        if (schedule == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(schedule);
    }

    // Alleen admin mag nieuwe weekschema's toevoegen
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<CleaningSchedule> createSchedule(@RequestBody CleaningSchedule schedule) {
        CleaningSchedule saved = repo.save(schedule);
        return ResponseEntity.ok(saved);
    }

    // Alleen admin mag een weekschema bijwerken (bijv. weeknummer wijzigen)
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<CleaningSchedule> updateSchedule(@PathVariable Long id, @RequestBody CleaningSchedule updated) {
        return repo.findById(id)
                .map(existing -> {
                    existing.setWeekNumber(updated.getWeekNumber());
                    return ResponseEntity.ok(repo.save(existing));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // Alleen admin mag een weekschema verwijderen
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSchedule(@PathVariable Long id) {
        if (!repo.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        repo.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}