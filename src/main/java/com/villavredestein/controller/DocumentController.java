package com.villavredestein.controller;

import com.villavredestein.dto.DocumentResponseDTO;
import com.villavredestein.dto.UploadResponseDTO;
import com.villavredestein.service.DocumentService;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.Principal;
import java.util.List;

/**
 * REST-controller voor documentbeheer binnen Villa Vredestein.
 */
@RestController
@RequestMapping("/api/documents")
@CrossOrigin
public class DocumentController {

    private final DocumentService documentService;

    public DocumentController(DocumentService documentService) {
        this.documentService = documentService;
    }

    /**
     * Haalt een lijst op met alle beschikbare document-metadata.
     */
    @PreAuthorize("hasAnyRole('ADMIN','STUDENT','CLEANER')")
    @GetMapping
    public ResponseEntity<List<DocumentResponseDTO>> getAllDocuments() {
        return ResponseEntity.ok(documentService.listAll());
    }

    /**
     * Uploadt een document en slaat dit op in de serveromgeving.
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UploadResponseDTO> uploadDocument(
            Principal principal,
            @RequestParam("file") MultipartFile file,
            @RequestParam(defaultValue = "ALL") String roleAccess
    ) throws IOException {
        UploadResponseDTO created = documentService.upload(principal.getName(), file, roleAccess);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * Downloadt een document op basis van het document-ID.
     */
    @PreAuthorize("hasAnyRole('ADMIN','STUDENT','CLEANER')")
    @GetMapping("/{id}/download")
    public ResponseEntity<FileSystemResource> downloadDocument(@PathVariable Long id) {
        FileSystemResource resource = documentService.download(id);
        if (resource == null || !resource.exists()) {
            return ResponseEntity.notFound().build();
        }

        String filename = resource.getFilename() == null ? "document" : resource.getFilename();

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }

    /**
     * Verwijdert een document op basis van ID.
     */
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDocument(@PathVariable Long id) {
        documentService.delete(id);
        return ResponseEntity.noContent().build();
    }
}