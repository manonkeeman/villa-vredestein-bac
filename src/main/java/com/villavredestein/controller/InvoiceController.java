package com.villavredestein.controller;

import com.villavredestein.dto.InvoiceRequestDTO;
import com.villavredestein.dto.InvoiceResponseDTO;
import com.villavredestein.model.Invoice;
import com.villavredestein.service.InvoiceService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/invoices")
@CrossOrigin
public class InvoiceController {

    private final InvoiceService invoiceService;

    public InvoiceController(InvoiceService invoiceService) {
        this.invoiceService = invoiceService;
    }

    /**
     * ADMIN: alle facturen
     * STUDENT: alleen eigen facturen
     */
    @PreAuthorize("hasAnyRole('ADMIN','STUDENT')")
    @GetMapping
    public ResponseEntity<List<InvoiceResponseDTO>> getInvoices(Authentication authentication) {

        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (isAdmin) {
            return ResponseEntity.ok(invoiceService.getAllInvoices());
        }

        // JWT subject / username / email (afhankelijk van jouw implementatie)
        String currentUsernameOrEmail = authentication.getName();
        return ResponseEntity.ok(invoiceService.getInvoicesForStudent(currentUsernameOrEmail));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<InvoiceResponseDTO> createInvoice(@Valid @RequestBody InvoiceRequestDTO request) {
        InvoiceResponseDTO created = invoiceService.createInvoice(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * Voorbeeld: PUT /api/invoices/{id}/status?status=PAID
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}/status")
    public ResponseEntity<InvoiceResponseDTO> updateInvoiceStatus(
            @PathVariable Long id,
            @RequestParam Invoice.InvoiceStatus status
    ) {
        return ResponseEntity.ok(invoiceService.updateStatus(id, status));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteInvoice(@PathVariable Long id) {
        invoiceService.deleteInvoice(id);
        return ResponseEntity.noContent().build();
    }
}