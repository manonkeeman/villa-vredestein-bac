package com.villavredestein.controller;

import com.villavredestein.dto.InvoiceRequestDTO;
import com.villavredestein.dto.InvoiceResponseDTO;
import com.villavredestein.service.InvoiceService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// =====================================================================
// # InvoiceController
// =====================================================================
@Validated
@RestController
@RequestMapping(value = "/api/invoices", produces = MediaType.APPLICATION_JSON_VALUE)
@CrossOrigin
public class InvoiceController {

    private final InvoiceService invoiceService;

    public InvoiceController(InvoiceService invoiceService) {
        this.invoiceService = invoiceService;
    }

    // =====================================================================
    // # READ – admin listing
    // =====================================================================
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<List<InvoiceResponseDTO>> getAllInvoices() {
        return ResponseEntity.ok(invoiceService.getAllInvoices());
    }

    // =====================================================================
    // # READ – current user listing
    // =====================================================================
    @PreAuthorize("hasAnyRole('STUDENT','ADMIN')")
    @GetMapping("/me")
    public ResponseEntity<List<InvoiceResponseDTO>> getMyInvoices(Authentication authentication) {
        return ResponseEntity.ok(invoiceService.getInvoicesForStudent(authentication.getName()));
    }

    // =====================================================================
    // # READ – by id
    // =====================================================================
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<InvoiceResponseDTO> getInvoiceById(@PathVariable @Positive Long id) {
        return ResponseEntity.ok(invoiceService.getInvoiceById(id));
    }

    // =====================================================================
    // # CREATE
    // =====================================================================
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<InvoiceResponseDTO> createInvoice(@Valid @RequestBody InvoiceRequestDTO request) {
        InvoiceResponseDTO created = invoiceService.createInvoice(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    // =====================================================================
    // # UPDATE – status
    // =====================================================================
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}/status")
    public ResponseEntity<InvoiceResponseDTO> updateInvoiceStatus(
            @PathVariable @Positive Long id,
            @RequestParam @NotBlank String status
    ) {
        String normalizedStatus = status.trim().toUpperCase();
        return ResponseEntity.ok(invoiceService.updateStatus(id, normalizedStatus));
    }

    // =====================================================================
    // # DELETE
    // =====================================================================
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteInvoice(@PathVariable @Positive Long id) {
        invoiceService.deleteInvoice(id);
        return ResponseEntity.noContent().build();
    }
}