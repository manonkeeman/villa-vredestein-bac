package com.villavredestein.controller;

import com.villavredestein.dto.CleaningResponseDTO;
import com.villavredestein.dto.DocumentResponseDTO;
import com.villavredestein.dto.InvoiceResponseDTO;
import com.villavredestein.service.CleaningService;
import com.villavredestein.service.DocumentService;
import com.villavredestein.service.InvoiceService;
import jakarta.validation.constraints.Positive;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Validated
@RestController
@RequestMapping("/api/student")
@CrossOrigin
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

    @GetMapping("/dashboard")
    public ResponseEntity<String> studentDashboardTest() {
        return ResponseEntity.ok("STUDENT OK");
    }

    @GetMapping("/invoices")
    public ResponseEntity<List<InvoiceResponseDTO>> getMyInvoices(Authentication authentication) {
        return ResponseEntity.ok(invoiceService.getInvoicesForStudent(authentication.getName()));
    }

    @GetMapping("/documents")
    public ResponseEntity<List<DocumentResponseDTO>> getMyDocuments() {
        return ResponseEntity.ok(documentService.listAll());
    }

    @GetMapping("/cleaning/tasks")
    public ResponseEntity<List<CleaningResponseDTO>> getCleaningTasks(
            @RequestParam(required = false) @Positive Integer weekNumber) {

        if (weekNumber != null) {
            return ResponseEntity.ok(cleaningService.getTasksByWeek(weekNumber));
        }
        return ResponseEntity.ok(cleaningService.getAllTasks());
    }
}