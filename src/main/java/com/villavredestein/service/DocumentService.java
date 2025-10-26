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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
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

    public UploadResponseDTO upload(Long uploaderUserId, MultipartFile file) throws IOException {
        User uploader = userRepository.findById(uploaderUserId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Uploader met id " + uploaderUserId + " niet gevonden"
                ));

        // Uploadmap creëren indien nodig
        Path uploadDir = Paths.get(uploadDirPath).toAbsolutePath().normalize();
        Files.createDirectories(uploadDir);

        // Veilige bestandsnaam genereren
        String originalFileName = Optional.ofNullable(file.getOriginalFilename())
                .map(name -> name.replaceAll("[^a-zA-Z0-9._-]", "_"))
                .orElse("unnamed");
        String safeFileName = System.currentTimeMillis() + "_" + originalFileName;

        Path targetPath = uploadDir.resolve(safeFileName);

        // Bestand kopiëren naar map
        Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

        // Document entity opslaan
        Document document = new Document();
        document.setUploader(uploader);
        document.setFileName(originalFileName);
        document.setContentType(file.getContentType());
        document.setStoragePath(targetPath.toString());
        document.setSize(file.getSize());
        document.setUploadedAt(Instant.now());

        Document saved = documentRepository.save(document);
        return new UploadResponseDTO(saved.getId(), saved.getFileName(), saved.getUploadedAt().toString());
    }

    public FileSystemResource download(Long id) {
        return documentRepository.findById(id)
                .map(doc -> {
                    Path path = Paths.get(doc.getStoragePath());
                    return Files.exists(path) ? new FileSystemResource(path) : null;
                })
                .orElse(null);
    }
}