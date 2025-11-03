package com.villavredestein.controller;

import com.villavredestein.dto.CleaningRequestDTO;
import com.villavredestein.dto.InvoiceResponseDTO;
import com.villavredestein.model.Document;
import com.villavredestein.service.CleaningService;
import com.villavredestein.service.DocumentService;
import com.villavredestein.service.InvoiceService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/student")
@CrossOrigin(origins = "*")
@PreAuthorize("hasAnyRole('STUDENT','ADMIN')")
public class StudentController {

    private final InvoiceService invoiceService;
    private final DocumentService documentService;
    private final CleaningService cleaningService;

    public StudentController(InvoiceService invoiceService,
                             DocumentService documentService,
                             CleaningService cleaningService) {
        this.invoiceService = invoiceService;
        this.documentService = documentService;
        this.cleaningService = cleaningService;
    }

    @GetMapping("/invoices")
    public ResponseEntity<List<InvoiceResponseDTO>> getMyInvoices() {
        return ResponseEntity.ok(invoiceService.getAllInvoices());
    }

    @GetMapping("/documents")
    public ResponseEntity<List<Document>> getMyDocuments() {
        return ResponseEntity.ok(documentService.listAll());
    }

    @GetMapping("/cleaning/tasks")
    public ResponseEntity<List<CleaningRequestDTO>> getCleaningTasks(
            @RequestParam(required = false) Integer weekNumber) {
        if (weekNumber != null) {
            return ResponseEntity.ok(cleaningService.getTasksByWeek(weekNumber));
        }
        return ResponseEntity.ok(cleaningService.getAllTasks());
    }
}