package com.villavredestein.service;

import com.villavredestein.dto.DocumentResponseDTO;
import com.villavredestein.dto.UploadResponseDTO;
import com.villavredestein.model.Document;
import com.villavredestein.model.User;
import com.villavredestein.repository.DocumentRepository;
import com.villavredestein.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
public class DocumentService {

    private static final Logger log = LoggerFactory.getLogger(DocumentService.class);

    private static final Set<String> ALLOWED_ROLE_ACCESS = Set.of(
            Document.ROLE_ALL, // expected: "ROLE_ALL"
            "ADMIN",
            "STUDENT",
            "CLEANER"
    );

    private final DocumentRepository documentRepository;
    private final UserRepository userRepository;

    @Value("${app.upload-dir}")
    private String uploadDirPath;

    public DocumentService(DocumentRepository documentRepository, UserRepository userRepository) {
        this.documentRepository = documentRepository;
        this.userRepository = userRepository;
    }

    // ============================================================
    // UPLOAD
    // ============================================================

    public UploadResponseDTO upload(String uploaderPrincipalName, MultipartFile file, String roleAccess) {
        if (uploaderPrincipalName == null || uploaderPrincipalName.isBlank()) {
            throw new IllegalArgumentException("Uploader principal is verplicht");
        }
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Bestand is verplicht");
        }
        if (uploadDirPath == null || uploadDirPath.isBlank()) {
            throw new IllegalStateException("app.upload-dir is niet geconfigureerd");
        }

        User uploader = userRepository.findByEmailIgnoreCase(uploaderPrincipalName)
                .orElseThrow(() -> new EntityNotFoundException("Uploader niet gevonden: " + uploaderPrincipalName));

        Path uploadDir = ensureUploadDirectory();

        String originalName = Optional.ofNullable(file.getOriginalFilename())
                .map(this::sanitizeFilename)
                .orElse("document");

        String storageKey = UUID.randomUUID() + "_" + originalName;
        Path targetPath = uploadDir.resolve(storageKey).normalize();

        if (!targetPath.startsWith(uploadDir)) {
            throw new IllegalArgumentException("Ongeldige bestandsnaam");
        }

        try (InputStream in = file.getInputStream()) {
            Files.copy(in, targetPath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            log.error("Upload failed for {} -> {}", uploaderPrincipalName, targetPath, e);
            throw new RuntimeException("Upload mislukt. Probeer het opnieuw");
        }

        String normalizedRoleAccess = normalizeRoleAccess(roleAccess);
        validateRoleAccess(normalizedRoleAccess);

        Document document = new Document(
                originalName,
                "Geüpload door " + uploader.getUsername(),
                targetPath.toString(),
                normalizedRoleAccess,
                uploader
        );

        Document saved = documentRepository.save(document);

        String downloadUrl = "/api/documents/" + saved.getId() + "/download";
        return new UploadResponseDTO(saved.getId(), saved.getTitle(), downloadUrl);
    }

    public UploadResponseDTO upload(Long uploaderUserId, MultipartFile file, String roleAccess) {
        if (uploaderUserId == null || uploaderUserId <= 0) {
            throw new IllegalArgumentException("Uploader userId is ongeldig");
        }

        User uploader = userRepository.findById(uploaderUserId)
                .orElseThrow(() -> new EntityNotFoundException("Uploader niet gevonden: " + uploaderUserId));

        return upload(uploader.getEmail(), file, roleAccess);
    }

    // ============================================================
    // LIST
    // ============================================================

    public List<DocumentResponseDTO> listAll() {
        return documentRepository.findAllByOrderByIdDesc()
                .stream()
                .map(this::toResponseDTO)
                .toList();
    }

    public List<DocumentResponseDTO> listAccessibleDocuments(String role) {
        String normalizedRole = normalizeRoleAccess(role);

        List<Document> docs;
        if ("ADMIN".equalsIgnoreCase(normalizedRole)) {
            docs = documentRepository.findAllByOrderByIdDesc();
        } else {
            docs = documentRepository.findAccessibleForRole(normalizedRole);
        }

        return docs.stream()
                .map(this::toResponseDTO)
                .toList();
    }

    // ============================================================
    // DOWNLOAD
    // ============================================================

    public FileSystemResource download(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Document id is ongeldig");
        }

        Document doc = documentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Document niet gevonden: " + id));

        Path path = Paths.get(doc.getStoragePath()).normalize();
        if (!Files.exists(path)) {
            throw new EntityNotFoundException("Bestand voor document " + id + " niet gevonden");
        }

        return new FileSystemResource(path);
    }

    // ============================================================
    // DELETE
    // ============================================================

    public void delete(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Document id is ongeldig");
        }

        Document doc = documentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Document niet gevonden: " + id));

        try {
            Files.deleteIfExists(Paths.get(doc.getStoragePath()));
        } catch (IOException e) {
            log.warn("Could not delete file for document {} at {}", id, doc.getStoragePath(), e);
        }

        documentRepository.delete(doc);
    }

    // ============================================================
    // HELPERS
    // ============================================================

    private DocumentResponseDTO toResponseDTO(Document doc) {
        String uploadedBy = doc.getUploadedBy() != null ? doc.getUploadedBy().getUsername() : null;

        return new DocumentResponseDTO(
                doc.getId(),
                doc.getTitle(),
                doc.getDescription(),
                doc.getRoleAccess(),
                uploadedBy
        );
    }

    private Path ensureUploadDirectory() {
        Path uploadDir = Paths.get(uploadDirPath).toAbsolutePath().normalize();
        try {
            Files.createDirectories(uploadDir);
        } catch (IOException e) {
            log.error("Could not create upload directory {}", uploadDir, e);
            throw new RuntimeException("Upload directory kan niet worden aangemaakt");
        }
        return uploadDir;
    }

    private String normalizeRoleAccess(String roleAccess) {
        if (roleAccess == null || roleAccess.isBlank()) {
            return Document.ROLE_ALL;
        }

        String normalized = roleAccess.trim().toUpperCase(Locale.ROOT);

        if (normalized.startsWith("ROLE_")) {
            normalized = normalized.substring("ROLE_".length());
        }

        if ("ALL".equals(normalized)) {
            return Document.ROLE_ALL;
        }

        if ("ADMIN".equals(normalized) || "STUDENT".equals(normalized) || "CLEANER".equals(normalized)) {
            return normalized;
        }

        return normalized;
    }

    private void validateRoleAccess(String roleAccess) {
        if (!ALLOWED_ROLE_ACCESS.contains(roleAccess)) {
            throw new IllegalArgumentException("roleAccess moet ROLE_ALL, ADMIN, STUDENT of CLEANER zijn");
        }
    }

    private String sanitizeFilename(String filename) {
        if (filename == null) {
            return "document";
        }

        String cleaned = filename
                .replace("\\", "_")
                .replace("/", "_")
                .trim();

        cleaned = cleaned.replaceAll("[^a-zA-Z0-9._-]", "_");

        while (cleaned.startsWith(".")) {
            cleaned = cleaned.substring(1);
        }

        cleaned = cleaned.replaceAll("_+", "_");

        if (cleaned.isBlank()) {
            return "document";
        }

        return cleaned.length() > 120 ? cleaned.substring(0, 120) : cleaned;
    }
}