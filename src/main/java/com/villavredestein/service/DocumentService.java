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
import java.util.UUID;

@Service
public class DocumentService {

    private static final Logger log = LoggerFactory.getLogger(DocumentService.class);

    private final DocumentRepository documentRepository;
    private final UserRepository userRepository;

    @Value("${app.upload-dir}")
    private String uploadDirPath;

    public DocumentService(DocumentRepository documentRepository, UserRepository userRepository) {
        this.documentRepository = documentRepository;
        this.userRepository = userRepository;
    }

    /**
     * Uploadt een document namens de ingelogde gebruiker.
     */
    public UploadResponseDTO upload(String uploaderPrincipalName, MultipartFile file, String roleAccess) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Bestand is verplicht");
        }

        if (uploadDirPath == null || uploadDirPath.isBlank()) {
            throw new IllegalStateException("app.upload-dir is niet geconfigureerd");
        }

        User uploader = userRepository.findByEmailIgnoreCase(uploaderPrincipalName)
                .orElseThrow(() -> new EntityNotFoundException("Uploader niet gevonden: " + uploaderPrincipalName));

        Path uploadDir = Paths.get(uploadDirPath).toAbsolutePath().normalize();
        try {
            Files.createDirectories(uploadDir);
        } catch (IOException e) {
            log.error("Could not create upload directory {}", uploadDir, e);
            throw new RuntimeException("Upload directory kan niet worden aangemaakt");
        }

        String originalName = Optional.ofNullable(file.getOriginalFilename())
                .map(this::sanitizeFilename)
                .orElse("document");

        String storageKey = UUID.randomUUID() + "_" + originalName;
        Path targetPath = uploadDir.resolve(storageKey);

        try (InputStream in = file.getInputStream()) {
            Files.copy(in, targetPath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            log.error("Upload failed for {} -> {}", uploaderPrincipalName, targetPath, e);
            throw new RuntimeException("Upload mislukt. Probeer het opnieuw");
        }

        String normalizedRoleAccess = normalizeRoleAccess(roleAccess);

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
        User uploader = userRepository.findById(uploaderUserId)
                .orElseThrow(() -> new EntityNotFoundException("Uploader niet gevonden: " + uploaderUserId));

        // Delegate naar principal-based methode op basis van email
        return upload(uploader.getEmail(), file, roleAccess);
    }

    /**
     * Haalt alle documenten op als veilige response-DTO’s.
     */
    public List<DocumentResponseDTO> listAll() {
        return documentRepository.findAllByOrderByIdDesc()
                .stream()
                .map(this::toResponseDTO)
                .toList();
    }

    /**
     * Haalt documenten op die zichtbaar zijn voor een rol.
     */
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

    /**
     * Downloadt het fysieke bestand bij een document.
     */
    public FileSystemResource download(Long id) {
        Document doc = documentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Document niet gevonden: " + id));

        Path path = Paths.get(doc.getStoragePath());
        if (!Files.exists(path)) {
            throw new EntityNotFoundException("Bestand voor document " + id + " niet gevonden");
        }

        return new FileSystemResource(path);
    }

    /**
     * Verwijdert een document en het bijbehorende fysieke bestand.
     */
    public void delete(Long id) {
        Document doc = documentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Document niet gevonden: " + id));

        try {
            Files.deleteIfExists(Paths.get(doc.getStoragePath()));
        } catch (IOException e) {
            log.warn("Could not delete file for document {} at {}", id, doc.getStoragePath(), e);
        }

        documentRepository.delete(doc);
    }

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

    private String normalizeRoleAccess(String roleAccess) {
        if (roleAccess == null || roleAccess.isBlank()) {
            return Document.ROLE_ALL;
        }
        String normalized = roleAccess.trim().toUpperCase(Locale.ROOT);
        if (normalized.startsWith("ROLE_")) {
            normalized = normalized.substring("ROLE_".length());
        }
        return normalized;
    }

    private String sanitizeFilename(String filename) {
        String cleaned = filename.replaceAll("[^a-zA-Z0-9._-]", "_").trim();
        if (cleaned.isBlank()) {
            return "document";
        }
        return cleaned.length() > 120 ? cleaned.substring(0, 120) : cleaned;
    }
}