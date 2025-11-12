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

/**
 * {@code StudentController} beheert alle API-endpoints die betrekking hebben
 * op studentenfunctionaliteiten binnen de Villa Vredestein webapplicatie.
 *
 * <p>De controller biedt studenten toegang tot hun eigen gegevens zoals
 * facturen, documenten en schoonmaaktaken. Daarnaast is toegang ook toegestaan
 * voor ADMIN-gebruikers, zodat zij studentgerelateerde data kunnen beheren of inzien.</p>
 *
 * <p>De controller werkt samen met {@link InvoiceService}, {@link DocumentService}
 * en {@link CleaningService} om data uit de onderliggende lagen op te halen.</p>
 */
@RestController
@RequestMapping("/api/student")
@CrossOrigin(origins = "*")
@PreAuthorize("hasAnyRole('STUDENT','ADMIN')")
public class StudentController {

    private final InvoiceService invoiceService;
    private final DocumentService documentService;
    private final CleaningService cleaningService;

    /**
     * Constructor voor {@link StudentController}.
     *
     * @param invoiceService service voor factuurbeheer
     * @param documentService service voor documentbeheer
     * @param cleaningService service voor schoonmaaktaken
     */
    public StudentController(InvoiceService invoiceService,
                             DocumentService documentService,
                             CleaningService cleaningService) {
        this.invoiceService = invoiceService;
        this.documentService = documentService;
        this.cleaningService = cleaningService;
    }

    /**
     * Haalt alle facturen op die relevant zijn voor de ingelogde student.
     *
     * <p>Beschikbaar voor gebruikers met de rollen STUDENT en ADMIN.</p>
     *
     * @return lijst van {@link InvoiceResponseDTO} objecten
     */
    @GetMapping("/invoices")
    public ResponseEntity<List<InvoiceResponseDTO>> getMyInvoices() {
        return ResponseEntity.ok(invoiceService.getAllInvoices());
    }

    /**
     * Haalt alle documenten op die zichtbaar zijn voor de student.
     *
     * <p>Beschikbaar voor gebruikers met de rollen STUDENT en ADMIN.</p>
     *
     * @return lijst van {@link Document} objecten
     */
    @GetMapping("/documents")
    public ResponseEntity<List<Document>> getMyDocuments() {
        return ResponseEntity.ok(documentService.listAll());
    }

    /**
     * Haalt de schoonmaaktaken op voor de student.
     *
     * <p>Indien een weeknummer wordt meegegeven, worden enkel de taken voor die week getoond.
     * Zonder parameter worden alle taken opgehaald.</p>
     *
     * @param weekNumber optioneel weeknummer om op te filteren
     * @return lijst van {@link CleaningRequestDTO} objecten
     */
    @GetMapping("/cleaning/tasks")
    public ResponseEntity<List<CleaningRequestDTO>> getCleaningTasks(
            @RequestParam(required = false) Integer weekNumber) {
        if (weekNumber != null) {
            return ResponseEntity.ok(cleaningService.getTasksByWeek(weekNumber));
        }
        return ResponseEntity.ok(cleaningService.getAllTasks());
    }
}