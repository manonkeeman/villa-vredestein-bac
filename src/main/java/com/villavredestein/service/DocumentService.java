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
        return new UploadResponseDTO(saved.getId(), saved.getTitle(), saved.getUploadedAt().toString());
    }

    public List<Document> listAccessibleDocuments(String role) {
        return documentRepository.findAll().stream()
                .filter(doc -> "ADMIN".equalsIgnoreCase(role)
                        || "ALL".equalsIgnoreCase(doc.getRoleAccess())
                        || (("STUDENT".equalsIgnoreCase(role) && "STUDENT".equalsIgnoreCase(doc.getRoleAccess())))
                        || (("CLEANER".equalsIgnoreCase(role) && "CLEANER".equalsIgnoreCase(doc.getRoleAccess()))))
                .peek(doc -> {
                    if (doc.getUploadedBy() != null) {
                        doc.setDescription("Geüpload door " + doc.getUploadedBy().getUsername());
                    }
                })
                .toList();
    }

    public FileSystemResource download(Long id) {
        return documentRepository.findById(id)
                .map(doc -> {
                    Path path = Paths.get(doc.getStoragePath());
                    return Files.exists(path) ? new FileSystemResource(path) : null;
                })
                .orElse(null);
    }

    public void delete(Long id) {
        documentRepository.findById(id).ifPresent(doc -> {
            try {
                Files.deleteIfExists(Paths.get(doc.getStoragePath()));
            } catch (IOException ignored) {}
            documentRepository.delete(doc);
        });
    }
}