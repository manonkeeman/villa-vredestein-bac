package com.villavredestein.controller;

import com.villavredestein.dto.UploadResponseDTO;
import com.villavredestein.model.Document;
import com.villavredestein.service.DocumentService;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

/**
 * {@code DocumentController} beheert alle documentgerelateerde API-endpoints
 * binnen de Villa Vredestein webapplicatie.
 *
 * <p>De controller maakt het mogelijk om documenten te uploaden, downloaden,
 * op te vragen en te verwijderen. Toegangsrechten zijn gebaseerd op rollen
 * (ADMIN, STUDENT of CLEANER), zodat documenten veilig en doelgericht gedeeld worden.</p>
 *
 * <p>Deze controller werkt samen met {@link DocumentService} voor de opslag en
 * het ophalen van bestanden in het lokale bestandssysteem.</p>
 */
@RestController
@RequestMapping("/api/documents")
@CrossOrigin
public class DocumentController {

    private final DocumentService documentService;

    /**
     * Constructor voor {@link DocumentController}.
     *
     * @param documentService service die documentbeheer verzorgt
     */
    public DocumentController(DocumentService documentService) {
        this.documentService = documentService;
    }

    /**
     * Haalt een lijst op van alle beschikbare documenten.
     *
     * <p>Beschikbaar voor gebruikers met de rollen ADMIN, STUDENT en CLEANER.</p>
     *
     * @return lijst van {@link Document} objecten
     */
    @PreAuthorize("hasAnyRole('ADMIN','STUDENT','CLEANER')")
    @GetMapping
    public ResponseEntity<List<Document>> getAllDocuments() {
        return ResponseEntity.ok(documentService.listAll());
    }

    /**
     * Uploadt een nieuw document en slaat dit veilig op in de serveromgeving.
     *
     * <p>Alleen toegankelijk voor gebruikers met de rol ADMIN.</p>
     *
     * @param uploaderId ID van de gebruiker die het bestand uploadt
     * @param file het geüploade bestand
     * @param roleAccess toegangsrechten voor dit document (standaard: ALL)
     * @return {@link UploadResponseDTO} met uploadstatus en documentinformatie
     * @throws IOException als het bestand niet kan worden opgeslagen
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/upload")
    public ResponseEntity<UploadResponseDTO> uploadDocument(
            @RequestParam("uploaderId") Long uploaderId,
            @RequestParam("file") MultipartFile file,
            @RequestParam(defaultValue = "ALL") String roleAccess) throws IOException {
        return ResponseEntity.ok(documentService.upload(uploaderId, file, roleAccess));
    }

    /**
     * Downloadt een document op basis van het ID.
     *
     * <p>Beschikbaar voor gebruikers met de rollen ADMIN, STUDENT en CLEANER.</p>
     *
     * @param id het unieke ID van het document
     * @return {@link FileSystemResource} met het gedownloade bestand of 404 Not Found als het document niet bestaat
     */
    @PreAuthorize("hasAnyRole('ADMIN','STUDENT','CLEANER')")
    @GetMapping("/{id}/download")
    public ResponseEntity<FileSystemResource> downloadDocument(@PathVariable Long id) {
        FileSystemResource resource = documentService.download(id);
        if (resource == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + resource.getFilename())
                .contentType(MediaType.APPLICATION_PDF)
                .body(resource);
    }

    /**
     * Verwijdert een document op basis van ID.
     *
     * <p>Alleen toegankelijk voor gebruikers met de rol ADMIN.</p>
     *
     * @param id het unieke ID van het te verwijderen document
     * @return bevestigingsbericht bij succesvolle verwijdering
     */
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteDocument(@PathVariable Long id) {
        documentService.delete(id);
        return ResponseEntity.ok("🗑️ Document verwijderd.");
    }
}