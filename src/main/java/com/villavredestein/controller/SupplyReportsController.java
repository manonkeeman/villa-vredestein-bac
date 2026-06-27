package com.villavredestein.controller;

import com.villavredestein.model.SupplyReport;
import com.villavredestein.model.User;
import com.villavredestein.repository.SupplyReportRepository;
import com.villavredestein.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(value = "/api/supply-reports", produces = MediaType.APPLICATION_JSON_VALUE)
public class SupplyReportsController {

    private final SupplyReportRepository supplyReportRepository;
    private final UserRepository userRepository;

    public SupplyReportsController(SupplyReportRepository supplyReportRepository,
                                   UserRepository userRepository) {
        this.supplyReportRepository = supplyReportRepository;
        this.userRepository = userRepository;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','CLEANER')")
    public ResponseEntity<List<SupplyReport>> getAll(Authentication auth) {
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        if (isAdmin) return ResponseEntity.ok(supplyReportRepository.findAllByOrderByReportedAtDesc());
        User cleaner = resolveUser(auth.getName());
        return ResponseEntity.ok(supplyReportRepository.findByReportedByOrderByReportedAtDesc(cleaner));
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN','CLEANER')")
    public ResponseEntity<SupplyReport> create(@RequestBody Map<String, String> body, Authentication auth) {
        String itemName = body.get("itemName");
        if (itemName == null || itemName.isBlank())
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "itemName is verplicht.");

        User reporter = resolveUser(auth.getName());
        SupplyReport report = new SupplyReport();
        report.setReportedBy(reporter);
        report.setItemName(itemName.trim());
        report.setNotes(body.get("notes"));
        if (body.get("urgency") != null) {
            try { report.setUrgency(SupplyReport.Urgency.valueOf(body.get("urgency").toUpperCase())); }
            catch (IllegalArgumentException ignored) {}
        }
        return ResponseEntity.ok(supplyReportRepository.save(report));
    }

    @PatchMapping(value = "/{id}/status", consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SupplyReport> updateStatus(@PathVariable Long id,
                                                     @RequestBody Map<String, String> body) {
        SupplyReport report = supplyReportRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Rapport niet gevonden."));
        try {
            report.setStatus(SupplyReport.Status.valueOf(body.get("status").toUpperCase()));
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ongeldige status.");
        }
        report.setUpdatedAt(Instant.now());
        return ResponseEntity.ok(supplyReportRepository.save(report));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (!supplyReportRepository.existsById(id)) return ResponseEntity.notFound().build();
        supplyReportRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    private User resolveUser(String email) {
        return userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Gebruiker niet gevonden."));
    }
}
