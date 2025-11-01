package com.villavredestein.controller;

import com.villavredestein.model.Invoice;
import com.villavredestein.service.InvoiceService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/invoices")
@CrossOrigin(origins = "*")
public class InvoiceController {

    private final InvoiceService invoiceService;

    public InvoiceController(InvoiceService invoiceService) {
        this.invoiceService = invoiceService;
    }

    @PreAuthorize("hasAnyRole('STUDENT','ADMIN')")
    @GetMapping("/student/{email}")
    public ResponseEntity<List<Invoice>> getInvoicesForStudent(@PathVariable String email) {
        List<Invoice> invoices = invoiceService.getInvoicesByStudentEmail(email);
        return ResponseEntity.ok(invoices);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<List<Invoice>> getAllInvoices() {
        return ResponseEntity.ok(invoiceService.getAllInvoices());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<Invoice> createInvoice(@RequestBody Invoice invoice) {
        Invoice saved = invoiceService.saveInvoice(invoice);
        return ResponseEntity.ok(saved);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}/status")
    public ResponseEntity<Invoice> updateStatus(
            @PathVariable Long id,
            @RequestParam String status
    ) {
        Invoice updated = invoiceService.updateStatus(id, status);
        return ResponseEntity.ok(updated);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteInvoice(@PathVariable Long id) {
        invoiceService.deleteInvoice(id);
        return ResponseEntity.noContent().build();
    }
}