package com.villavredestein.controller;

import com.villavredestein.dto.DocumentRequestDTO;
import com.villavredestein.dto.DocumentResponseDTO;
import com.villavredestein.dto.UploadResponseDTO;
import com.villavredestein.service.DocumentService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.security.Principal;
import java.util.List;

// =====================================================================
// # DocumentController
// =====================================================================
@Validated
@RestController
@RequestMapping(value = "/api/documents", produces = MediaType.APPLICATION_JSON_VALUE)
@CrossOrigin
public class DocumentController {

    private final DocumentService documentService;

    public DocumentController(DocumentService documentService) {
        this.documentService = documentService;
    }

    // =====================================================================
    // # READ
    // =====================================================================

    @PreAuthorize("hasAnyRole('ADMIN','STUDENT','CLEANER')")
    @GetMapping
    public ResponseEntity<List<DocumentResponseDTO>> getDocuments(Authentication authentication) {
        String role = resolveRole(authentication);

        if ("ADMIN".equals(role)) {
            return ResponseEntity.ok(documentService.listAll());
        }

        return ResponseEntity.ok(documentService.listAccessibleDocuments(role));
    }

    // =====================================================================
    // # CREATE
    // =====================================================================

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping(
            value = "/upload",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<UploadResponseDTO> uploadDocument(
            Principal principal,
            @RequestPart("file") MultipartFile file,
            @Valid @RequestPart("meta") DocumentRequestDTO meta
    ) {
        String uploaderEmail = principal == null ? "unknown" : principal.getName();

        UploadResponseDTO created = documentService.upload(
                uploaderEmail,
                file,
                meta.getRoleAccess()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    // =====================================================================
    // # DOWNLOAD
    // =====================================================================

    @PreAuthorize("hasAnyRole('ADMIN','STUDENT','CLEANER')")
    @GetMapping(value = "/{id}/download", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<FileSystemResource> downloadDocument(
            Authentication authentication,
            @PathVariable @Positive Long id
    ) {
        String role = resolveRole(authentication);

        if (!"ADMIN".equals(role)) {
            boolean allowed = documentService.listAccessibleDocuments(role)
                    .stream()
                    .anyMatch(d -> d.id() != null && d.id().equals(id));

            if (!allowed) {
                throw new EntityNotFoundException("Document not found: " + id);
            }
        }

        FileSystemResource resource = documentService.download(id);

        String filename = (resource.getFilename() == null || resource.getFilename().isBlank())
                ? "document"
                : resource.getFilename();

        ContentDisposition contentDisposition = ContentDisposition
                .attachment()
                .filename(filename, StandardCharsets.UTF_8)
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition.toString())
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }

    // =====================================================================
    // # DELETE
    // =====================================================================

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDocument(@PathVariable @Positive Long id) {
        documentService.delete(id);
        return ResponseEntity.noContent().build();
    }

    // =====================================================================
    // # Helpers
    // =====================================================================

    private String resolveRole(Authentication authentication) {
        if (authentication == null || authentication.getAuthorities() == null) {
            return "STUDENT";
        }

        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
        if (isAdmin) return "ADMIN";

        boolean isCleaner = authentication.getAuthorities().stream()
                .anyMatch(a -> "ROLE_CLEANER".equals(a.getAuthority()));
        if (isCleaner) return "CLEANER";

        return "STUDENT";
    }
}