package com.villavredestein.controller;

import com.villavredestein.dto.UserResponseDTO;
import com.villavredestein.model.Room;
import com.villavredestein.model.User;
import com.villavredestein.repository.RoomRepository;
import com.villavredestein.repository.UserRepository;
import com.villavredestein.service.MailService;
import com.villavredestein.service.UserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Admin endpoints for student creation with room assignment and welcome email.
 */
@Validated
@RestController
@RequestMapping(value = "/api/admin", produces = MediaType.APPLICATION_JSON_VALUE)
@PreAuthorize("hasRole('ADMIN')")
public class AdminStudentController {

    private static final Logger log = LoggerFactory.getLogger(AdminStudentController.class);

    private final UserService userService;
    private final UserRepository userRepository;
    private final RoomRepository roomRepository;
    private final MailService mailService;

    @Value("${app.frontend-url:https://villa-vredestein.netlify.app}")
    private String frontendUrl;

    public AdminStudentController(UserService userService,
                                  UserRepository userRepository,
                                  RoomRepository roomRepository,
                                  MailService mailService) {
        this.userService    = userService;
        this.userRepository = userRepository;
        this.roomRepository = roomRepository;
        this.mailService    = mailService;
    }

    // ── GET /api/admin/rooms/available ────────────────────────────────────
    @GetMapping("/rooms/available")
    public ResponseEntity<List<String>> availableRooms() {
        List<String> names = roomRepository.findByOccupantIsNullOrderByNameAsc()
                .stream()
                .map(Room::getName)
                .toList();
        return ResponseEntity.ok(names);
    }

    // ── POST /api/admin/students ──────────────────────────────────────────
    public static class CreateStudentRequest {

        @NotBlank(message = "Naam is verplicht")
        @Size(min = 2, max = 50, message = "Naam moet tussen 2 en 50 tekens zijn")
        private String username;

        @NotBlank(message = "E-mail is verplicht")
        @Email(message = "E-mail moet geldig zijn")
        @Size(max = 100)
        private String email;

        @NotBlank(message = "Wachtwoord is verplicht")
        @Size(min = 8, max = 72, message = "Wachtwoord moet minimaal 8 tekens zijn")
        private String password;

        private String room;
        private boolean sendWelcomeEmail = true;

        public String getUsername()      { return username; }
        public void setUsername(String v){ this.username = v; }
        public String getEmail()         { return email; }
        public void setEmail(String v)   { this.email = v; }
        public String getPassword()      { return password; }
        public void setPassword(String v){ this.password = v; }
        public String getRoom()          { return room; }
        public void setRoom(String v)    { this.room = v; }
        public boolean isSendWelcomeEmail()         { return sendWelcomeEmail; }
        public void setSendWelcomeEmail(boolean v)  { this.sendWelcomeEmail = v; }
    }

    @PostMapping(value = "/students", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UserResponseDTO> createStudent(@Valid @RequestBody CreateStudentRequest req) {

        // 1. Create student account
        UserResponseDTO created = userService.createUserWithRole(
                req.getUsername().trim(),
                req.getEmail().trim().toLowerCase(),
                req.getPassword(),
                "STUDENT"
        );

        // 2. Assign room if provided
        String kamerNaam = req.getRoom() != null ? req.getRoom().trim() : "";
        if (!kamerNaam.isEmpty()) {
            roomRepository.findByNameIgnoreCase(kamerNaam).ifPresentOrElse(room -> {
                User student = userRepository.findById(created.getId()).orElseThrow();
                room.assignOccupant(student);
                roomRepository.save(room);
                log.info("Room '{}' assigned to student id={}", kamerNaam, created.getId());
            }, () -> log.warn("Room '{}' not found — skipping assignment", kamerNaam));
        }

        // 3. Send welcome email
        if (req.isSendWelcomeEmail()) {
            try {
                String loginUrl = frontendUrl + "/login";
                String kamerTekst = kamerNaam.isEmpty() ? "nog toe te wijzen" : kamerNaam;
                mailService.sendWelcomeMail(
                        req.getEmail().trim().toLowerCase(),
                        req.getUsername().trim(),
                        kamerTekst,
                        loginUrl,
                        req.getPassword()
                );
            } catch (Exception e) {
                log.error("Welcome mail failed for {}: {}", maskEmail(req.getEmail()), e.getMessage());
                // Don't fail the request — student is already created
            }
        }

        return ResponseEntity.ok(created);
    }

    private String maskEmail(String email) {
        if (email == null || email.isBlank()) return "(no-email)";
        int at = email.indexOf('@');
        if (at <= 1) return "***";
        return email.charAt(0) + "***" + email.substring(at);
    }
}
