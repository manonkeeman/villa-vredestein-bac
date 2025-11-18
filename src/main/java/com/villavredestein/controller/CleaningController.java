package com.villavredestein.controller;

import com.villavredestein.dto.CleaningRequestDTO;
import com.villavredestein.service.CleaningService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * {@code CleaningController} beheert alle API-endpoints rondom schoonmaaktaken
 * binnen de Villa Vredestein webapplicatie.
 *
 * <p>De controller maakt het mogelijk om schoonmaaktaken op te vragen, aan te maken,
 * te toggelen, van opmerkingen of incidenten te voorzien, en te verwijderen.
 * Afhankelijk van de gebruikersrol (ADMIN, CLEANER, STUDENT) zijn verschillende acties toegestaan.</p>
 *
 * <p>Deze controller maakt gebruik van {@link CleaningService} voor alle
 * businesslogica en database-interacties.</p>
 */
@RestController
@RequestMapping("/api/cleaning")
@CrossOrigin
public class CleaningController {

    private final CleaningService cleaningService;

    /**
     * Constructor voor {@link CleaningController}.
     *
     * @param cleaningService service die schoonmaaktaken beheert
     */
    public CleaningController(CleaningService cleaningService) {
        this.cleaningService = cleaningService;
    }

    /**
     * Test-endpoint voor CLEANER toegangscontrole.
     */
    @GetMapping("/tasks-test-cleaner")
    @PreAuthorize("hasRole('CLEANER')")
    public ResponseEntity<String> cleanerTestTasks() {
        return ResponseEntity.ok("CLEANER OK");
    }

    @GetMapping("/../cleaner/tasks")
    @PreAuthorize("hasRole('CLEANER')")
    public ResponseEntity<String> cleanerTasksAlias() {
        return ResponseEntity.ok("CLEANER OK");
    }


    /**
     * Haalt alle schoonmaaktaken op, eventueel gefilterd op weeknummer.
     *
     * <p>Beschikbaar voor gebruikers met de rollen ADMIN, STUDENT of CLEANER.</p>
     *
     * @param weekNumber optioneel weeknummer om te filteren
     * @return lijst van {@link CleaningRequestDTO} met schoonmaaktaken
     */
    @GetMapping("/tasks")
    @PreAuthorize("hasAnyRole('ADMIN','STUDENT','CLEANER')")
    public ResponseEntity<List<CleaningRequestDTO>> getTasks(
            @RequestParam(required = false) Integer weekNumber) {
        if (weekNumber != null) {
            return ResponseEntity.ok(cleaningService.getTasksByWeek(weekNumber));
        }
        return ResponseEntity.ok(cleaningService.getAllTasks());
    }

    /**
     * Maakt een nieuwe schoonmaaktaak aan.
     *
     * <p>Beschikbaar voor gebruikers met de rollen ADMIN of CLEANER.</p>
     *
     * @param dto gegevens van de nieuwe taak
     * @return aangemaakte {@link CleaningRequestDTO}
     */
    @PostMapping("/tasks")
    @PreAuthorize("hasAnyRole('ADMIN','CLEANER')")
    public ResponseEntity<CleaningRequestDTO> createTask(@RequestBody CleaningRequestDTO dto) {
        return ResponseEntity.ok(cleaningService.addTask(dto));
    }

    /**
     * Wisselt de status van een taak (bijv. open ↔ afgerond).
     *
     * <p>Beschikbaar voor gebruikers met de rollen ADMIN, STUDENT of CLEANER.</p>
     *
     * @param taskId ID van de taak
     * @return bijgewerkte {@link CleaningRequestDTO}
     */
    @PutMapping("/tasks/{taskId}/toggle")
    @PreAuthorize("hasAnyRole('ADMIN','STUDENT','CLEANER')")
    public ResponseEntity<CleaningRequestDTO> toggleTask(@PathVariable Long taskId) {
        return ResponseEntity.ok(cleaningService.toggleTask(taskId));
    }

    /**
     * Voegt een opmerking toe aan een bestaande schoonmaaktaak.
     *
     * <p>Beschikbaar voor gebruikers met de rollen ADMIN of CLEANER.</p>
     *
     * @param taskId ID van de taak
     * @param comment opmerking die aan de taak toegevoegd wordt
     * @return bijgewerkte {@link CleaningRequestDTO}
     */
    @PutMapping("/tasks/{taskId}/comment")
    @PreAuthorize("hasAnyRole('ADMIN','CLEANER')")
    public ResponseEntity<CleaningRequestDTO> addComment(
            @PathVariable Long taskId,
            @RequestParam String comment) {
        return ResponseEntity.ok(cleaningService.addComment(taskId, comment));
    }

    /**
     * Registreert een incident dat tijdens het schoonmaken is opgetreden.
     *
     * <p>Beschikbaar voor gebruikers met de rollen ADMIN of CLEANER.</p>
     *
     * @param taskId ID van de taak
     * @param incident omschrijving van het incident
     * @return bijgewerkte {@link CleaningRequestDTO}
     */
    @PutMapping("/tasks/{taskId}/incident")
    @PreAuthorize("hasAnyRole('ADMIN','CLEANER')")
    public ResponseEntity<CleaningRequestDTO> addIncident(
            @PathVariable Long taskId,
            @RequestParam String incident) {
        return ResponseEntity.ok(cleaningService.addIncident(taskId, incident));
    }

    /**
     * Verwijdert een schoonmaaktaak.
     *
     * <p>Alleen toegankelijk voor gebruikers met de rol ADMIN.</p>
     *
     * @param taskId ID van de taak
     * @return HTTP 204 No Content bij succes
     */
    @DeleteMapping("/tasks/{taskId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteTask(@PathVariable Long taskId) {
        cleaningService.deleteTask(taskId);
        return ResponseEntity.noContent().build();
    }
}