package com.villavredestein.controller;

import com.villavredestein.dto.CleaningResponseDTO;
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
    public ResponseEntity<List<InvoiceResponseDTO>> getMyInvoices(@AuthenticationPrincipal UserDetails userDetails) {
        String email = userDetails.getUsername();
        List<InvoiceResponseDTO> invoices = invoiceService.getInvoicesByStudentEmail(email)
                .stream()
                .map(i -> new InvoiceResponseDTO(
                        i.getId(),
                        i.getTitle(),
                        i.getAmount(),
                        i.getDueDate(),
                        i.getStatus(),
                        i.isReminderSent()
                ))
                .toList();

        return ResponseEntity.ok(invoices);
    }

    @GetMapping("/documents")
    public ResponseEntity<List<Document>> getMyDocuments(@AuthenticationPrincipal UserDetails userDetails) {
        String email = userDetails.getUsername();
        return ResponseEntity.ok(documentService.getDocumentsByOwnerEmail(email));
    }

    @GetMapping("/documents/{id}")
    public ResponseEntity<Document> getMyDocument(@PathVariable Long id,
                                                  @AuthenticationPrincipal UserDetails userDetails) {
        String email = userDetails.getUsername();
        Document doc = documentService.getDocumentForUser(id, email);
        return ResponseEntity.ok(doc);
    }

    @GetMapping("/cleaning/week/{week}")
    public ResponseEntity<CleaningResponseDTO> getCleaningSchedule(@PathVariable int week) {
        return ResponseEntity.ok(cleaningService.getByWeek(week));
    }
}