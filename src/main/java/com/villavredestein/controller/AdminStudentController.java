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
import com.villavredestein.service.MollieService;
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
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

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
    private final MollieService mollieService;
    private final MailService mailService;
    private final CleaningScheduleService cleaningScheduleService;

    @Value("${app.frontend-url:https://villa-vredestein.netlify.app}")
    private String frontendUrl;

    @Value("${app.instagram:@villavredestein}")
    private String instagram;

    public AdminStudentController(UserService userService,
                                  UserRepository userRepository,
                                  RoomRepository roomRepository,
                                  InvoiceService invoiceService,
                                  MollieService mollieService,
                                  MailService mailService,
                                  CleaningScheduleService cleaningScheduleService) {
        this.userService              = userService;
        this.userRepository           = userRepository;
        this.roomRepository           = roomRepository;
        this.invoiceService           = invoiceService;
        this.mollieService            = mollieService;
        this.mailService              = mailService;
        this.cleaningScheduleService  = cleaningScheduleService;
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

            try {
                String description = "Huur " + maand + " – Villa Vredestein";
                MollieService.MolliePaymentResult mollie =
                        mollieService.createPayment(req.getRentAmount(), description, invoice.getId());
                if (mollie != null && mollie.checkoutUrl() != null) {
                    Invoice raw = invoiceService.getRawById(invoice.getId());
                    invoiceService.attachMolliePayment(raw, mollie.molliePaymentId(), mollie.checkoutUrl());
                    log.info("Mollie payment created for new student invoiceId={}", invoice.getId());
                }
            } catch (Exception e) {
                log.warn("Mollie payment failed for new student invoiceId={}: {}", invoice.getId(), e.getMessage());
            }
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

    private String maskEmail(String email) {
        if (email == null || email.isBlank()) return "(no-email)";
        int at = email.indexOf('@');
        if (at <= 1) return "***";
        return email.charAt(0) + "***" + email.substring(at);
    }
}
