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

@RestController
@RequestMapping("/api/documents")
public class DocumentController {

    private final DocumentService documentService;

    public DocumentController(DocumentService documentService) {
        this.documentService = documentService;
    }

    // 👀 Studenten en Admins mogen lijst zien
    @PreAuthorize("hasAnyRole('ADMIN','STUDENT')")
    @GetMapping
    public ResponseEntity<List<Document>> getAllDocuments() {
        return ResponseEntity.ok(documentService.listAll());
    }

    // 📥 Alleen Admin mag uploaden
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/upload")
    public ResponseEntity<UploadResponseDTO> uploadDocument(
            @RequestParam("uploaderId") Long uploaderId,
            @RequestParam("file") MultipartFile file,
            @RequestParam(defaultValue = "ALL") String roleAccess) throws IOException {
        return ResponseEntity.ok(documentService.upload(uploaderId, file, roleAccess));
    }

    // 📄 Studenten & Admins mogen downloaden
    @PreAuthorize("hasAnyRole('ADMIN','STUDENT')")
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

    // ❌ Alleen Admin mag verwijderen
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteDocument(@PathVariable Long id) {
        documentService.delete(id);
        return ResponseEntity.ok("🗑️ Document verwijderd.");
    }
}