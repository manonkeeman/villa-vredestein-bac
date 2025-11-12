package com.villavredestein.controller;

import com.villavredestein.dto.InvoiceRequestDTO;
import com.villavredestein.dto.InvoiceResponseDTO;
import com.villavredestein.service.InvoiceService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * {@code InvoiceController} beheert alle API-endpoints voor het aanmaken, ophalen,
 * bijwerken en verwijderen van facturen binnen de Villa Vredestein webapplicatie.
 *
 * <p>De controller maakt gebruik van {@link InvoiceService} om de businesslogica
 * rondom facturatie te verwerken. Toegang tot deze endpoints is beperkt tot
 * gebruikers met de juiste rol (ADMIN of STUDENT).</p>
 *
 * <p>ADMIN-gebruikers kunnen facturen aanmaken, wijzigen en verwijderen;
 * STUDENT-gebruikers kunnen hun facturen enkel bekijken.</p>
 */
@RestController
@RequestMapping("/api/invoices")
@CrossOrigin(origins = "*")
public class InvoiceController {

    private final InvoiceService invoiceService;

    /** Constructor voor {@link InvoiceController}. */
    public InvoiceController(InvoiceService invoiceService) {
        this.invoiceService = invoiceService;
    }

    /** Haalt een lijst op van alle facturen. */
    @PreAuthorize("hasAnyRole('ADMIN','STUDENT')")
    @GetMapping
    public ResponseEntity<List<InvoiceResponseDTO>> getAllInvoices() {
        return ResponseEntity.ok(invoiceService.getAllInvoices());
    }

    /** Maakt een nieuwe factuur aan (alleen ADMIN). */
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<InvoiceResponseDTO> createInvoice(@RequestBody InvoiceRequestDTO request) {
        return ResponseEntity.ok(invoiceService.createInvoice(request));
    }

    /** Wijzigt de status van een factuur (alleen ADMIN). */
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}/status")
    public ResponseEntity<InvoiceResponseDTO> updateStatus(
            @PathVariable Long id,
            @RequestParam String status) {
        return ResponseEntity.ok(invoiceService.updateStatus(id, status));
    }

    /** Verwijdert een factuur uit de database (alleen ADMIN). */
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteInvoice(@PathVariable Long id) {
        invoiceService.deleteInvoice(id);
        return ResponseEntity.noContent().build();
    }
}