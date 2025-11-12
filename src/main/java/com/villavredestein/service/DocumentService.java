package com.villavredestein.service;

import com.villavredestein.dto.UploadResponseDTO;
import com.villavredestein.model.Document;
import com.villavredestein.model.User;
import com.villavredestein.repository.DocumentRepository;
import com.villavredestein.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * {@code DocumentService} verzorgt de businesslogica voor het beheren van documenten
 * binnen de Villa Vredestein webapplicatie.
 *
 * <p>Deze service is verantwoordelijk voor het uploaden, downloaden, ophalen en verwijderen
 * van documenten. Daarbij wordt rekening gehouden met toegangsrechten op basis van gebruikersrollen
 * (ADMIN, STUDENT, CLEANER of ALL).</p>
 *
 * <p>De bestanden worden fysiek opgeslagen in de map die is gedefinieerd in
 * <code>application.yml</code> via de property {@code app.upload-dir}.
 * De metadata wordt opgeslagen in de database via de {@link DocumentRepository}.</p>
 *
 * <p>Deze service vormt de brug tussen de {@link com.villavredestein.controller.DocumentController}
 * en de persistente opslaglaag.</p>
 */
@Service
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final UserRepository userRepository;

    @Value("${app.upload-dir}")
    private String uploadDirPath;

    /**
     * Constructor voor {@link DocumentService}.
     *
     * @param documentRepository repository voor documentopslag
     * @param userRepository repository voor gebruikersinformatie
     */
    public DocumentService(DocumentRepository documentRepository, UserRepository userRepository) {
        this.documentRepository = documentRepository;
        this.userRepository = userRepository;
    }

    /**
     * Uploadt een document, slaat het bestand op in het lokale bestandssysteem
     * en bewaart de metadata in de database.
     *
     * @param uploaderUserId ID van de gebruiker die het bestand uploadt
     * @param file het te uploaden bestand
     * @param roleAccess toegangsrechten (bijv. ADMIN, STUDENT, CLEANER, ALL)
     * @return {@link UploadResponseDTO} met ID, bestandsnaam en download-URL
     * @throws IOException als het bestand niet opgeslagen kan worden
     * @throws IllegalArgumentException als de uploader niet wordt gevonden
     */
    public UploadResponseDTO upload(Long uploaderUserId, MultipartFile file, String roleAccess) throws IOException {
        User uploader = userRepository.findById(uploaderUserId)
                .orElseThrow(() -> new IllegalArgumentException("Uploader niet gevonden"));

        Path uploadDir = Paths.get(uploadDirPath).toAbsolutePath().normalize();
        Files.createDirectories(uploadDir);

        String originalName = Optional.ofNullable(file.getOriginalFilename())
                .map(name -> name.replaceAll("[^a-zA-Z0-9._-]", "_"))
                .orElse("unnamed.pdf");

        String safeFileName = System.currentTimeMillis() + "_" + originalName;
        Path targetPath = uploadDir.resolve(safeFileName);
        Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

        Document document = new Document();
        document.setTitle(originalName);
        document.setDescription("Geüpload door " + uploader.getUsername());
        document.setStoragePath(targetPath.toString());
        document.setContentType(file.getContentType());
        document.setRoleAccess(roleAccess);
        document.setSize(file.getSize());
        document.setUploadedAt(LocalDateTime.now());
        document.setUploadedBy(uploader);

        Document saved = documentRepository.save(document);

        // Bouw RESTful download-URL
        String downloadUrl = "/api/documents/" + saved.getId() + "/download";

        return new UploadResponseDTO(saved.getId(), saved.getTitle(), downloadUrl);
    }

    /**
     * Haalt een lijst van documenten op die toegankelijk zijn voor een specifieke gebruikersrol.
     * ADMIN ziet alle documenten; STUDENT en CLEANER alleen de documenten met hun rol of "ALL".
     *
     * @param role de gebruikersrol
     * @return lijst van toegankelijke documenten
     */
    public List<Document> listAccessibleDocuments(String role) {
        return documentRepository.findAll().stream()
                .filter(doc -> "ADMIN".equalsIgnoreCase(role)
                        || "ALL".equalsIgnoreCase(doc.getRoleAccess())
                        || ("STUDENT".equalsIgnoreCase(role) && "STUDENT".equalsIgnoreCase(doc.getRoleAccess()))
                        || ("CLEANER".equalsIgnoreCase(role) && "CLEANER".equalsIgnoreCase(doc.getRoleAccess())))
                .peek(doc -> {
                    if (doc.getUploadedBy() != null) {
                        doc.setDescription("Geüpload door " + doc.getUploadedBy().getUsername());
                    }
                })
                .toList();
    }

    /**
     * Downloadt een document op basis van zijn ID.
     *
     * @param id het unieke ID van het document
     * @return {@link FileSystemResource} met het fysieke bestand, of {@code null} als het bestand niet bestaat
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
     *
     * @param id het unieke ID van het document
     */
    public void delete(Long id) {
        documentRepository.findById(id).ifPresent(doc -> {
            try {
                Files.deleteIfExists(Paths.get(doc.getStoragePath()));
            } catch (IOException ignored) {}
            documentRepository.delete(doc);
        });
    }

    /**
     * Haalt alle documenten op zonder filtering.
     *
     * @return lijst van alle documenten
     */
    public List<Document> listAll() {
        return documentRepository.findAll().stream()
                .peek(doc -> {
                    if (doc.getUploadedBy() != null) {
                        doc.setDescription("Geüpload door " + doc.getUploadedBy().getUsername());
                    }
                })
                .toList();
    }
}