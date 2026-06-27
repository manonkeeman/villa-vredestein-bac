package com.villavredestein.controller;

import com.villavredestein.model.CleaningTask;
import com.villavredestein.model.TaskPhoto;
import com.villavredestein.model.User;
import com.villavredestein.repository.CleaningTaskRepository;
import com.villavredestein.repository.TaskPhotoRepository;
import com.villavredestein.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(value = "/api/task-photos", produces = MediaType.APPLICATION_JSON_VALUE)
public class TaskPhotosController {

    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024;
    private static final List<String> ALLOWED_TYPES = List.of("image/jpeg", "image/png", "image/webp");

    private final TaskPhotoRepository taskPhotoRepository;
    private final CleaningTaskRepository cleaningTaskRepository;
    private final UserRepository userRepository;
    private final Path uploadDir;

    public TaskPhotosController(TaskPhotoRepository taskPhotoRepository,
                                CleaningTaskRepository cleaningTaskRepository,
                                UserRepository userRepository,
                                @Value("${app.upload-dir:uploads}") String uploadDir) {
        this.taskPhotoRepository = taskPhotoRepository;
        this.cleaningTaskRepository = cleaningTaskRepository;
        this.userRepository = userRepository;
        this.uploadDir = Paths.get(uploadDir, "task-photos").toAbsolutePath().normalize();
    }

    @GetMapping("/task/{taskId}")
    @PreAuthorize("hasAnyRole('ADMIN','CLEANER')")
    public ResponseEntity<List<TaskPhoto>> getByTask(@PathVariable Long taskId) {
        CleaningTask task = cleaningTaskRepository.findById(taskId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Taak niet gevonden."));
        return ResponseEntity.ok(taskPhotoRepository.findByTaskOrderByUploadedAtDesc(task));
    }

    @PostMapping(value = "/task/{taskId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN','CLEANER')")
    public ResponseEntity<TaskPhoto> upload(@PathVariable Long taskId,
                                            @RequestParam("photo") MultipartFile photo,
                                            Authentication auth) throws IOException {
        if (photo.isEmpty()) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Geen bestand.");
        if (photo.getSize() > MAX_FILE_SIZE) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Bestand te groot (max 5MB).");
        String contentType = photo.getContentType();
        if (contentType == null || !ALLOWED_TYPES.contains(contentType))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Alleen JPEG, PNG en WebP zijn toegestaan.");

        CleaningTask task = cleaningTaskRepository.findById(taskId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Taak niet gevonden."));
        User uploader = resolveUser(auth.getName());

        Files.createDirectories(uploadDir);
        String ext = contentType.contains("png") ? ".png" : contentType.contains("webp") ? ".webp" : ".jpg";
        String filename = "task-" + taskId + "-" + UUID.randomUUID() + ext;
        Files.copy(photo.getInputStream(), uploadDir.resolve(filename));

        TaskPhoto taskPhoto = new TaskPhoto();
        taskPhoto.setTask(task);
        taskPhoto.setUploadedBy(uploader);
        taskPhoto.setPhotoPath("task-photos/" + filename);

        return ResponseEntity.ok(taskPhotoRepository.save(taskPhoto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) throws IOException {
        TaskPhoto photo = taskPhotoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Foto niet gevonden."));
        Path file = Paths.get(uploadDir.getParent().toString(), photo.getPhotoPath()).normalize();
        Files.deleteIfExists(file);
        taskPhotoRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    private User resolveUser(String email) {
        return userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Gebruiker niet gevonden."));
    }
}
