package com.villavredestein.controller;

import com.villavredestein.dto.AnnouncementResponseDTO;
import com.villavredestein.model.Announcement;
import com.villavredestein.repository.AnnouncementRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Validated
@RestController
@RequestMapping(value = "/api/announcements", produces = MediaType.APPLICATION_JSON_VALUE)
public class AnnouncementController {

    private final AnnouncementRepository announcementRepository;

    public AnnouncementController(AnnouncementRepository announcementRepository) {
        this.announcementRepository = announcementRepository;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STUDENT')")
    public ResponseEntity<List<AnnouncementResponseDTO>> getAll() {
        List<AnnouncementResponseDTO> result = announcementRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(this::toDTO)
                .toList();
        return ResponseEntity.ok(result);
    }

    public record CreateRequest(
            @Pattern(regexp = "mededeling|onderhoud|evenement",
                    flags = Pattern.Flag.CASE_INSENSITIVE,
                    message = "type moet mededeling, onderhoud of evenement zijn")
            String type,

            @NotBlank(message = "title is verplicht")
            @Size(max = 120, message = "title mag maximaal 120 tekens zijn")
            String title,

            @NotBlank(message = "body is verplicht")
            @Size(max = 2000, message = "body mag maximaal 2000 tekens zijn")
            String body,

            @Size(max = 80, message = "author mag maximaal 80 tekens zijn")
            String author
    ) {}

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AnnouncementResponseDTO> create(@Valid @RequestBody CreateRequest req) {
        Announcement ann = new Announcement();
        ann.setTitle(req.title() != null ? req.title().trim() : "");
        ann.setBody(req.body() != null ? req.body().trim() : "");
        ann.setAuthor(req.author() != null ? req.author().trim() : "Beheerder");
        ann.setCreatedAt(LocalDateTime.now());
        try {
            ann.setType(Announcement.AnnouncementType.valueOf(
                    req.type() != null ? req.type().trim().toLowerCase() : "mededeling"));
        } catch (IllegalArgumentException e) {
            ann.setType(Announcement.AnnouncementType.mededeling);
        }
        Announcement saved = announcementRepository.save(ann);
        return ResponseEntity.ok(toDTO(saved));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> delete(@PathVariable Long id) {
        if (!announcementRepository.existsById(id)) {
            throw new EntityNotFoundException("Aankondiging niet gevonden: id=" + id);
        }
        announcementRepository.deleteById(id);
        return ResponseEntity.ok(Map.of("message", "Verwijderd"));
    }

    private AnnouncementResponseDTO toDTO(Announcement a) {
        return new AnnouncementResponseDTO(
                a.getId(),
                a.getType() != null ? a.getType().name() : "mededeling",
                a.getTitle(),
                a.getBody(),
                a.getAuthor(),
                a.getCreatedAt()
        );
    }
}