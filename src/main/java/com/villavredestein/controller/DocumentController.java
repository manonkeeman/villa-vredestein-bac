package com.villavredestein.controller;

import com.villavredestein.dto.UploadResponseDTO;
import com.villavredestein.service.DocumentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;

@RestController
@RequestMapping("/api/documents")
public class DocumentController {

    private final DocumentService documentService;

    @Autowired
    public DocumentController(DocumentService documentService) {
        this.documentService = documentService;
    }

    @GetMapping("/ping")
    public String ping() {
        return "documents-ok";
    }

    @PreAuthorize("hasAnyRole('ADMIN','CLEANER','STUDENT')")
    @PostMapping(path = "/{uploaderUserId}/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UploadResponseDTO> uploadDocument(
            @PathVariable Long uploaderUserId,
            @RequestParam("file") MultipartFile file) {
        try {
            UploadResponseDTO response = documentService.upload(uploaderUserId, file);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @PreAuthorize("hasAnyRole('ADMIN','CLEANER','STUDENT')")
    @GetMapping("/{id}/download")
    public ResponseEntity<FileSystemResource> downloadDocument(@PathVariable Long id) {
        try {
            FileSystemResource resource = documentService.download(id);
            if (resource == null || !resource.exists()) {
                return ResponseEntity.notFound().build();
            }

            String filename = resource.getFilename();
            MediaType mediaType = MediaType.APPLICATION_OCTET_STREAM;

            String detectedType = Files.probeContentType(resource.getFile().toPath());
            if (detectedType != null) {
                mediaType = MediaType.parseMediaType(detectedType);
            }

            return ResponseEntity.ok()
                    .contentType(mediaType)
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            ContentDisposition.attachment().filename(filename).build().toString())
                    .body(resource);

        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}