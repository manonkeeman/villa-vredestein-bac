package com.villavredestein.controller;

import com.villavredestein.dto.CleaningRequestDTO;
import com.villavredestein.dto.InvoiceResponseDTO;
import com.villavredestein.model.Document;
import com.villavredestein.service.CleaningService;
import com.villavredestein.service.DocumentService;
import com.villavredestein.service.InvoiceService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/student")
@CrossOrigin(origins = "*")
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
    public ResponseEntity<List<InvoiceResponseDTO>> getMyInvoices(
            @AuthenticationPrincipal UserDetails userDetails) {
        String email = userDetails.getUsername();
        return ResponseEntity.ok(invoiceService.getInvoicesByStudentEmail(email));
    }

    @GetMapping("/documents")
    public ResponseEntity<List<Document>> getMyDocuments(
            @AuthenticationPrincipal UserDetails userDetails) {
        String email = userDetails.getUsername();
        return ResponseEntity.ok(documentService.getDocumentsByOwnerEmail(email));
    }

    @GetMapping("/documents/{id}")
    public ResponseEntity<Document> getMyDocument(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        String email = userDetails.getUsername();
        return ResponseEntity.ok(documentService.getDocumentForUser(id, email));
    }

    @GetMapping("/cleaning/tasks")
    public ResponseEntity<List<CleaningRequestDTO>> getMyCleaningTasks(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false) Integer weekNumber) {
        String email = userDetails.getUsername();
        if (weekNumber != null) {
            return ResponseEntity.ok(cleaningService.getTasksForStudentByWeek(email, weekNumber));
        }
        return ResponseEntity.ok(cleaningService.getTasksForStudent(email));
    }

    @PutMapping("/cleaning/tasks/{taskId}/toggle")
    public ResponseEntity<CleaningRequestDTO> toggleMyTask(
            @PathVariable Long taskId,
            @AuthenticationPrincipal UserDetails userDetails) {
        String email = userDetails.getUsername();
        return ResponseEntity.ok(cleaningService.toggleTaskForStudent(taskId, email));
    }
}