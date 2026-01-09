package com.villavredestein.service;

import com.villavredestein.dto.DocumentResponseDTO;
import com.villavredestein.dto.UploadResponseDTO;
import com.villavredestein.model.Document;
import com.villavredestein.model.User;
import com.villavredestein.repository.DocumentRepository;
import com.villavredestein.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import org.springframework.security.access.prepost.PreAuthorize;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class DocumentService {

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
    public UploadResponseDTO upload(String uploaderPrincipalName, MultipartFile file, String roleAccess) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Bestand is verplicht");
        }

        User uploader = userRepository.findByEmail(uploaderPrincipalName)
                .orElseThrow(() -> new IllegalArgumentException("Uploader niet gevonden: " + uploaderPrincipalName));

        Path uploadDir = Paths.get(uploadDirPath).toAbsolutePath().normalize();
        Files.createDirectories(uploadDir);

        String originalName = Optional.ofNullable(file.getOriginalFilename())
                .map(this::sanitizeFilename)
                .orElse("document");

        String storageKey = UUID.randomUUID() + "_" + originalName;
        Path targetPath = uploadDir.resolve(storageKey);

        try (InputStream in = file.getInputStream()) {
            Files.copy(in, targetPath, StandardCopyOption.REPLACE_EXISTING);
        }

        Document document = new Document();
        document.setTitle(originalName);
        document.setDescription("Geüpload door " + uploader.getUsername());
        document.setStoragePath(targetPath.toString());
        document.setRoleAccess(normalizeRoleAccess(roleAccess));
        document.setUploadedBy(uploader);

        Document saved = documentRepository.save(document);

        String downloadUrl = "/api/documents/" + saved.getId() + "/download";
        return new UploadResponseDTO(saved.getId(), saved.getTitle(), downloadUrl);
    }

    public UploadResponseDTO upload(Long uploaderUserId, MultipartFile file, String roleAccess) throws IOException {
        User uploader = userRepository.findById(uploaderUserId)
                .orElseThrow(() -> new IllegalArgumentException("Uploader niet gevonden: " + uploaderUserId));

        // Delegate naar principal-based methode op basis van email
        return upload(uploader.getEmail(), file, roleAccess);
    }

    /**
     * Haalt alle documenten op als veilige response-DTO’s.
     */
    public List<DocumentResponseDTO> listAll() {
        List<Document> docs;
        try {
            docs = documentRepository.findAllByOrderByIdDesc();
        } catch (Exception ex) {
            docs = documentRepository.findAll();
        }

        return docs.stream()
                .map(this::toResponseDTO)
                .toList();
    }

    /**
     * Haalt documenten op die zichtbaar zijn voor een rol.
     */
    public List<DocumentResponseDTO> listAccessibleDocuments(String role) {
        String normalizedRole = normalizeRoleAccess(role);

        return documentRepository.findAll().stream()
                .filter(doc -> "ADMIN".equalsIgnoreCase(normalizedRole)
                        || Document.ROLE_ALL.equalsIgnoreCase(doc.getRoleAccess())
                        || normalizedRole.equalsIgnoreCase(doc.getRoleAccess()))
                .map(this::toResponseDTO)
                .toList();
    }

    /**
     * Downloadt het fysieke bestand bij een document.
     */
    public FileSystemResource download(Long id) {
        return documentRepository.findById(id)
                .map(doc -> {
                    Path path = Paths.get(doc.getStoragePath());
                    return Files.exists(path) ? new FileSystemResource(path) : null;
                })
                .orElse(null);
    }

    /**
     * Verwijdert een document en het bijbehorende fysieke bestand.
     */
    @PreAuthorize("hasRole('ADMIN')")
    public void delete(Long id) {
        Document doc = documentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Document niet gevonden: " + id));

        try {
            Files.deleteIfExists(Paths.get(doc.getStoragePath()));
        } catch (IOException ignored) {
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
        return roleAccess.trim().toUpperCase();
    }

    private String sanitizeFilename(String filename) {
        String cleaned = filename.replaceAll("[^a-zA-Z0-9._-]", "_").trim();
        if (cleaned.isBlank()) {
            return "document";
        }
        return cleaned.length() > 120 ? cleaned.substring(0, 120) : cleaned;
    }
}