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
 * {@code AdminController} beheert alle beheerfunctionaliteiten binnen
 * de Villa Vredestein web-API. Alleen toegankelijk voor gebruikers met
 * de rol {@code ADMIN}.
 *
 * <p>Bevat functionaliteit voor:</p>
 * <ul>
 *     <li>Gebruikersbeheer</li>
 *     <li>Factuurbeheer</li>
 *     <li>Schoonmaaktaken beheren</li>
 *     <li>Versturen van huurherinneringen</li>
 * </ul>
 *
 * <p>Fouten worden centraal afgehandeld via {@link com.villavredestein.config.GlobalExceptionHandler}.</p>
 */
@RestController
@RequestMapping("/api/admin")
@CrossOrigin
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final UserService userService;
    private final InvoiceService invoiceService;
    private final CleaningService cleaningService;
    private final InvoiceReminderJob invoiceReminderJob;
    private final OverdueInvoiceJob overdueInvoiceJob;

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

    // ============================================================
    // TEST ENDPOINT
    // ============================================================

    /**
     * Test om admin toegang te verifiëren.
     */
    @GetMapping("/panel")
    public ResponseEntity<String> adminPanelTest() {
        return ResponseEntity.ok("ADMIN OK");
    }

    // ============================================================
    // FACTUREN
    // ============================================================

    /**
     * Haalt één factuur op basis van ID.
     *
     * @param id factuur-ID
     */
    @GetMapping("/invoices/{id}")
    public ResponseEntity<InvoiceResponseDTO> getInvoiceById(@PathVariable Long id) {
        return ResponseEntity.ok(invoiceService.getInvoiceById(id));
    }

    /**
     * Haalt alle facturen op.
     */
    @GetMapping("/invoices")
    public ResponseEntity<List<InvoiceResponseDTO>> getAllInvoices() {
        return ResponseEntity.ok(invoiceService.getAllInvoices());
    }

    /**
     * Maakt een nieuwe factuur aan.
     */
    @PostMapping("/invoices")
    public ResponseEntity<InvoiceResponseDTO> createInvoice(@RequestBody InvoiceRequestDTO dto) {
        return ResponseEntity.ok(invoiceService.createInvoice(dto));
    }

    /**
     * Wijzigt de status van een factuur (OPEN → PAID, etc.)
     */
    @PutMapping("/invoices/{id}/status")
    public ResponseEntity<InvoiceResponseDTO> updateInvoiceStatus(
            @PathVariable Long id,
            @RequestParam String status
    ) {
        return ResponseEntity.ok(invoiceService.updateStatus(id, status));
    }

    /**
     * Verwijdert een factuur.
     */
    @DeleteMapping("/invoices/{id}")
    public ResponseEntity<Void> deleteInvoice(@PathVariable Long id) {
        invoiceService.deleteInvoice(id);
        return ResponseEntity.noContent().build();
    }

    // ============================================================
    // REMINDERS
    // ============================================================

    /**
     * Verstuur handmatige betalingsherinneringen.
     */
    @PostMapping("/invoices/remind")
    public ResponseEntity<String> sendManualReminders() {
        invoiceReminderJob.sendReminders();
        return ResponseEntity.ok("📬 Handmatige huurherinneringen verstuurd.");
    }

    /**
     * Verstuur handmatige herinneringen voor achterstallige facturen.
     */
    @PostMapping("/invoices/remind-overdue")
    public ResponseEntity<String> sendManualOverdueReminders() {
        overdueInvoiceJob.sendOverdueReminders();
        return ResponseEntity.ok("📬 Handmatige vervallen herinneringen verstuurd.");
    }

    // ============================================================
    // GEBRUIKERS
    // ============================================================

    /**
     * Haalt alle gebruikers op.
     */
    @GetMapping("/users")
    public ResponseEntity<List<UserResponseDTO>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    /**
     * Verwijdert een gebruiker.
     */
    @DeleteMapping("/users/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Wijzigt de rol van een gebruiker.
     */
    @PutMapping("/users/{id}/role")
    public ResponseEntity<UserResponseDTO> changeRole(
            @PathVariable Long id,
            @RequestParam String newRole
    ) {
        return ResponseEntity.ok(userService.changeRole(id, newRole));
    }

    // ============================================================
    // SCHOONMAAK TAKEN
    // ============================================================

    /**
     * Haalt schoonmaaktaken op (optioneel per weeknummer).
     */
    @GetMapping("/cleaning/tasks")
    public ResponseEntity<List<CleaningRequestDTO>> getCleaningTasks(
            @RequestParam(required = false) Integer weekNumber
    ) {
        if (weekNumber != null) {
            return ResponseEntity.ok(cleaningService.getTasksByWeek(weekNumber));
        }
        return ResponseEntity.ok(cleaningService.getAllTasks());
    }

    /**
     * Maakt een nieuwe schoonmaaktaak aan.
     */
    @PostMapping("/cleaning/tasks")
    public ResponseEntity<CleaningRequestDTO> createTask(@RequestBody CleaningRequestDTO dto) {
        return ResponseEntity.ok(cleaningService.addTask(dto));
    }

    /**
     * Wijzigt een bestaande schoonmaaktaak.
     */
    @PutMapping("/cleaning/tasks/{taskId}")
    public ResponseEntity<CleaningRequestDTO> updateTask(
            @PathVariable Long taskId,
            @RequestBody CleaningRequestDTO dto
    ) {
        return ResponseEntity.ok(cleaningService.updateTask(taskId, dto));
    }

    /**
     * Toggle taak-status (OPEN ↔ DONE).
     */
    @PutMapping("/cleaning/tasks/{taskId}/toggle")
    public ResponseEntity<CleaningRequestDTO> toggleTask(@PathVariable Long taskId) {
        return ResponseEntity.ok(cleaningService.toggleTask(taskId));
    }

    /**
     * Verwijdert een schoonmaaktaak.
     */
    @DeleteMapping("/cleaning/tasks/{taskId}")
    public ResponseEntity<Void> deleteTask(@PathVariable Long taskId) {
        cleaningService.deleteTask(taskId);
        return ResponseEntity.noContent().build();
    }

    // ============================================================
    // INTERNAL SERVER ERROR 500 (testing)
    // ============================================================

    /**
     * Geforceerde server error voor Postman testing.
     * Wordt opgevangen door GlobalExceptionHandler → 500.
     */
    @GetMapping("/force-500")
    public void forceInternalError() {
        throw new RuntimeException("500 testfout");
    }

    @GetMapping("/boom")
    public void boom() {
        throw new RuntimeException("500 testfout");
    }

    @GetMapping("/trigger-500")
    public void trigger500() {
        throw new RuntimeException("Testfout");
    }
}