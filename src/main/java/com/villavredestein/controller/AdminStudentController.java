package com.villavredestein.controller;

import com.villavredestein.dto.InvoiceRequestDTO;
import com.villavredestein.dto.InvoiceResponseDTO;
import com.villavredestein.dto.UserResponseDTO;
import com.villavredestein.model.Invoice;
import com.villavredestein.model.Room;
import com.villavredestein.model.User;
import com.villavredestein.repository.RoomRepository;
import com.villavredestein.repository.UserRepository;
import com.villavredestein.service.CleaningScheduleService;
import com.villavredestein.service.InvoiceService;
import com.villavredestein.service.MailService;
import com.villavredestein.service.UserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Validated
@RestController
@RequestMapping(value = "/api/admin", produces = MediaType.APPLICATION_JSON_VALUE)
@PreAuthorize("hasRole('ADMIN')")
public class AdminStudentController {

    private static final Logger log = LoggerFactory.getLogger(AdminStudentController.class);

    private static final DateTimeFormatter MONTH_NL =
            DateTimeFormatter.ofPattern("MMMM yyyy", Locale.forLanguageTag("nl-NL"));

    private final UserService userService;
    private final UserRepository userRepository;
    private final RoomRepository roomRepository;
    private final InvoiceService invoiceService;
    private final MailService mailService;
    private final CleaningScheduleService cleaningScheduleService;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.frontend-url:https://villa-vredestein.netlify.app}")
    private String frontendUrl;

    @Value("${app.instagram:@villavredestein}")
    private String instagram;

    public AdminStudentController(UserService userService,
                                  UserRepository userRepository,
                                  RoomRepository roomRepository,
                                  InvoiceService invoiceService,
                                  MailService mailService,
                                  CleaningScheduleService cleaningScheduleService,
                                  PasswordEncoder passwordEncoder) {
        this.userService              = userService;
        this.userRepository           = userRepository;
        this.roomRepository           = roomRepository;
        this.invoiceService           = invoiceService;
        this.mailService              = mailService;
        this.cleaningScheduleService  = cleaningScheduleService;
        this.passwordEncoder          = passwordEncoder;
    }

    @GetMapping("/rooms/available")
    public ResponseEntity<List<String>> availableRooms() {
        List<String> names = roomRepository.findByOccupantIsNullOrderByNameAsc()
                .stream()
                .map(Room::getName)
                .toList();
        return ResponseEntity.ok(names);
    }

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

        @NotNull(message = "Huurbedrag is verplicht")
        @DecimalMin(value = "1.00", message = "Huurbedrag moet minimaal €1 zijn")
        private BigDecimal rentAmount;

        private boolean sendWelcomeEmail = true;

        public String getUsername()      { return username; }
        public void setUsername(String v){ this.username = v; }
        public String getEmail()         { return email; }
        public void setEmail(String v)   { this.email = v; }
        public String getPassword()      { return password; }
        public void setPassword(String v){ this.password = v; }
        public String getRoom()          { return room; }
        public void setRoom(String v)    { this.room = v; }
        public BigDecimal getRentAmount()         { return rentAmount; }
        public void setRentAmount(BigDecimal v)   { this.rentAmount = v; }
        public boolean isSendWelcomeEmail()        { return sendWelcomeEmail; }
        public void setSendWelcomeEmail(boolean v) { this.sendWelcomeEmail = v; }
    }

    @PostMapping(value = "/students", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UserResponseDTO> createStudent(@Valid @RequestBody CreateStudentRequest req) {

        String email     = req.getEmail().trim().toLowerCase();
        String naam      = req.getUsername().trim();
        String kamerNaam = req.getRoom() != null ? req.getRoom().trim() : "";

        Room room = null;
        if (!kamerNaam.isEmpty()) {
            room = roomRepository.findByNameIgnoreCase(kamerNaam)
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.BAD_REQUEST, "Kamer '" + kamerNaam + "' bestaat niet."));
            if (room.isOccupied()) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "Kamer '" + kamerNaam + "' is al bezet. Kies een lege kamer.");
            }
        }

        UserResponseDTO created = userService.createUserWithRole(naam, email, req.getPassword(), "STUDENT");

        User student = userRepository.findById(created.id()).orElseThrow();
        student.setRentAmount(req.getRentAmount());
        userRepository.save(student);

        if (room != null) {
            room.assignOccupant(student);
            roomRepository.save(room);
            log.info("Room '{}' assigned to student id={}", kamerNaam, created.id());
        }

        try {
            LocalDate today    = LocalDate.now();
            LocalDate dueDate  = today.plusDays(7);
            String maand       = today.withDayOfMonth(1).format(MONTH_NL);

            InvoiceRequestDTO invoiceDto = new InvoiceRequestDTO();
            invoiceDto.setTitle("Huur " + maand + " - " + naam);
            invoiceDto.setDescription("Maandelijkse huur - " + naam);
            invoiceDto.setAmount(req.getRentAmount());
            invoiceDto.setIssueDate(today);
            invoiceDto.setDueDate(dueDate);
            invoiceDto.setStudentEmail(email);

            InvoiceResponseDTO invoice = invoiceService.createInvoice(invoiceDto);

        } catch (Exception e) {
            log.error("Invoice creation failed for new student {}: {}", maskEmail(email), e.getMessage());
        }

        try {
            cleaningScheduleService.reseedNow();
            log.info("Cleaning schedule reseeded after new student id={}", created.id());
        } catch (Exception e) {
            log.warn("Cleaning reseed failed after student creation: {}", e.getMessage());
        }

        if (req.isSendWelcomeEmail()) {
            try {
                String kamerTekst = kamerNaam.isEmpty() ? "nog toe te wijzen" : kamerNaam;
                mailService.sendWelcomeMail(email, naam, kamerTekst, frontendUrl + "/login",
                        req.getPassword(), frontendUrl, instagram);
            } catch (Exception e) {
                log.error("Welcome mail failed for {}: {}", maskEmail(email), e.getMessage());
            }
        }

        UserResponseDTO finalDto = userService.getUserById(created.id()).orElse(created);
        return ResponseEntity.ok(finalDto);
    }

    @PatchMapping(value = "/students/{id}/password", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> resetStudentPassword(
            @PathVariable @NotNull Long id,
            @RequestBody Map<String, String> body) {

        String newPassword = body.get("newPassword");
        if (newPassword == null || newPassword.length() < 8) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Wachtwoord moet minimaal 8 tekens zijn.");
        }

        User student = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Gebruiker niet gevonden."));

        student.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(student);

        log.info("Admin reset password for userId={}", id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping(value = "/students/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UserResponseDTO> updateStudent(
            @PathVariable @NotNull Long id,
            @RequestBody Map<String, Object> body) {

        User student = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Gebruiker niet gevonden."));

        if (body.containsKey("rentAmount") && body.get("rentAmount") != null) {
            student.setRentAmount(new BigDecimal(body.get("rentAmount").toString()));
        }
        if (body.containsKey("phoneNumber")) {
            student.setPhoneNumber(body.get("phoneNumber") != null ? body.get("phoneNumber").toString() : null);
        }
        if (body.containsKey("username") && body.get("username") != null) {
            String newName = body.get("username").toString().trim();
            if (!newName.isEmpty()) student.setUsername(newName);
        }

        userRepository.save(student);
        log.info("Admin updated student id={}", id);
        return ResponseEntity.ok(userService.getUserById(id).orElseThrow());
    }

    private String maskEmail(String email) {
        if (email == null || email.isBlank()) return "(no-email)";
        int at = email.indexOf('@');
        if (at <= 1) return "***";
        return email.charAt(0) + "***" + email.substring(at);
    }
}
