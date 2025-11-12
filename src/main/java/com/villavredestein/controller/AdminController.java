package com.villavredestein.controller;

import com.villavredestein.dto.*;
import com.villavredestein.jobs.InvoiceReminderJob;
import com.villavredestein.jobs.OverdueInvoiceJob;
import com.villavredestein.service.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * {@code AdminController} beheert alle beheerfunctionaliteiten binnen de Villa Vredestein web-API.
 * Deze controller is uitsluitend toegankelijk voor gebruikers met de rol {@code ADMIN}.
 *
 * <p>De controller biedt endpoints voor het beheren van gebruikers, facturen en schoonmaaktaken,
 * evenals het handmatig versturen van betalingsherinneringen. Alle acties verlopen via
 * de service-laag, waardoor de businesslogica gescheiden blijft van de API-laag.</p>
 */
@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "*")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final UserService userService;
    private final InvoiceService invoiceService;
    private final CleaningService cleaningService;
    private final InvoiceReminderJob invoiceReminderJob;
    private final OverdueInvoiceJob overdueInvoiceJob;

    /**
     * Constructor voor {@link AdminController}.
     *
     * @param userService service voor gebruikersbeheer
     * @param invoiceService service voor factuurbeheer
     * @param cleaningService service voor schoonmaaktaken
     * @param invoiceReminderJob geplande taak voor huurherinneringen
     * @param overdueInvoiceJob geplande taak voor vervallen herinneringen
     */
    public AdminController(UserService userService,
                           InvoiceService invoiceService,
                           CleaningService cleaningService,
                           InvoiceReminderJob invoiceReminderJob,
                           OverdueInvoiceJob overdueInvoiceJob) {
        this.userService = userService;
        this.invoiceService = invoiceService;
        this.cleaningService = cleaningService;
        this.invoiceReminderJob = invoiceReminderJob;
        this.overdueInvoiceJob = overdueInvoiceJob;
    }

    // === USERS ===

    /** Haalt een lijst van alle geregistreerde gebruikers op. */
    @GetMapping("/users")
    public ResponseEntity<List<UserResponseDTO>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    /** Verwijdert een gebruiker op basis van ID. */
    @DeleteMapping("/users/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    /** Wijzigt de rol van een gebruiker (bijv. STUDENT → ADMIN). */
    @PutMapping("/users/{id}/role")
    public ResponseEntity<UserResponseDTO> changeRole(
            @PathVariable Long id,
            @RequestParam String newRole) {
        return ResponseEntity.ok(userService.changeRole(id, newRole));
    }

    // === INVOICES ===

    /** Haalt een overzicht van alle facturen op. */
    @GetMapping("/invoices")
    public ResponseEntity<List<InvoiceResponseDTO>> getAllInvoices() {
        return ResponseEntity.ok(invoiceService.getAllInvoices());
    }

    /** Maakt een nieuwe factuur aan. */
    @PostMapping("/invoices")
    public ResponseEntity<InvoiceResponseDTO> createInvoice(@RequestBody InvoiceRequestDTO dto) {
        return ResponseEntity.ok(invoiceService.createInvoice(dto));
    }

    /** Wijzigt de status van een bestaande factuur (bijv. OPEN → BETAALD). */
    @PutMapping("/invoices/{id}/status")
    public ResponseEntity<InvoiceResponseDTO> updateInvoiceStatus(
            @PathVariable Long id,
            @RequestParam String status) {
        return ResponseEntity.ok(invoiceService.updateStatus(id, status));
    }

    /** Verwijdert een factuur. */
    @DeleteMapping("/invoices/{id}")
    public ResponseEntity<Void> deleteInvoice(@PathVariable Long id) {
        invoiceService.deleteInvoice(id);
        return ResponseEntity.noContent().build();
    }

    // === MAIL REMINDERS ===

    /** Verstuur handmatig huurherinneringen via e-mail. */
    @PostMapping("/invoices/remind")
    public ResponseEntity<String> sendManualReminders() {
        invoiceReminderJob.sendReminders();
        return ResponseEntity.ok("📬 Handmatige huurherinneringen verstuurd.");
    }

    /** Verstuur handmatig herinneringen voor achterstallige betalingen. */
    @PostMapping("/invoices/remind-overdue")
    public ResponseEntity<String> sendManualOverdueReminders() {
        overdueInvoiceJob.sendOverdueReminders();
        return ResponseEntity.ok("📬 Handmatige vervallen herinneringen verstuurd.");
    }

    // === CLEANING ===

    /** Haalt alle schoonmaaktaken op, eventueel gefilterd per weeknummer. */
    @GetMapping("/cleaning/tasks")
    public ResponseEntity<List<CleaningRequestDTO>> getCleaningTasks(
            @RequestParam(required = false) Integer weekNumber) {
        if (weekNumber != null) {
            return ResponseEntity.ok(cleaningService.getTasksByWeek(weekNumber));
        }
        return ResponseEntity.ok(cleaningService.getAllTasks());
    }

    /** Maakt een nieuwe schoonmaaktaak aan. */
    @PostMapping("/cleaning/tasks")
    public ResponseEntity<CleaningRequestDTO> createTask(@RequestBody CleaningRequestDTO dto) {
        return ResponseEntity.ok(cleaningService.addTask(dto));
    }

    /** Wijzigt een bestaande schoonmaaktaak. */
    @PutMapping("/cleaning/tasks/{taskId}")
    public ResponseEntity<CleaningRequestDTO> updateTask(
            @PathVariable Long taskId,
            @RequestBody CleaningRequestDTO dto) {
        return ResponseEntity.ok(cleaningService.updateTask(taskId, dto));
    }

    /** Wisselt de status van een schoonmaaktaak (open ↔ afgerond). */
    @PutMapping("/cleaning/tasks/{taskId}/toggle")
    public ResponseEntity<CleaningRequestDTO> toggleTask(@PathVariable Long taskId) {
        return ResponseEntity.ok(cleaningService.toggleTask(taskId));
    }

    /** Verwijdert een schoonmaaktaak. */
    @DeleteMapping("/cleaning/tasks/{taskId}")
    public ResponseEntity<Void> deleteTask(@PathVariable Long taskId) {
        cleaningService.deleteTask(taskId);
        return ResponseEntity.noContent().build();
    }
}