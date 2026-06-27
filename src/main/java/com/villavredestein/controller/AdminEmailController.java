package com.villavredestein.controller;

import com.villavredestein.model.EmailTemplate;
import com.villavredestein.model.Invoice;
import com.villavredestein.model.User;
import com.villavredestein.repository.InvoiceRepository;
import com.villavredestein.repository.UserRepository;
import com.villavredestein.service.EmailTemplateService;
import com.villavredestein.service.InvoiceService;
import com.villavredestein.service.MailService;
import com.villavredestein.service.WhatsAppService;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@RestController
@RequestMapping(value = "/api/admin/email", produces = MediaType.APPLICATION_JSON_VALUE)
@PreAuthorize("hasRole('ADMIN')")
public class AdminEmailController {

    private static final Logger log = LoggerFactory.getLogger(AdminEmailController.class);
    private static final Locale NL = Locale.forLanguageTag("nl-NL");
    private static final DateTimeFormatter MONTH_NL = DateTimeFormatter.ofPattern("MMMM yyyy", NL);
    private static final DateTimeFormatter DATE_NL   = DateTimeFormatter.ofPattern("d MMMM yyyy", NL);

    private final UserRepository userRepository;
    private final InvoiceRepository invoiceRepository;
    private final InvoiceService invoiceService;
    private final MailService mailService;
    private final EmailTemplateService emailTemplateService;
    private final WhatsAppService whatsAppService;

    public AdminEmailController(UserRepository userRepository,
                                InvoiceRepository invoiceRepository,
                                InvoiceService invoiceService,
                                MailService mailService,
                                EmailTemplateService emailTemplateService,
                                WhatsAppService whatsAppService) {
        this.userRepository      = userRepository;
        this.invoiceRepository   = invoiceRepository;
        this.invoiceService      = invoiceService;
        this.mailService         = mailService;
        this.emailTemplateService = emailTemplateService;
        this.whatsAppService     = whatsAppService;
    }

    // POST /api/admin/email/send

    public record SendReminderRequest(Long userId, String templateType) {}

    @PostMapping(value = "/send", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, String>> sendReminder(@RequestBody SendReminderRequest request) {

        User student = userRepository.findById(request.userId())
                .orElseThrow(() -> new EntityNotFoundException("Student niet gevonden: id=" + request.userId()));

        LocalDate today = LocalDate.now();
        int month = today.getMonthValue();
        int year  = today.getYear();

        List<Invoice> invoices = invoiceRepository.findByStudentAndInvoiceMonthAndInvoiceYear(student, month, year);
        Invoice invoice = invoices.stream()
                .filter(i -> i.getStatus() != Invoice.InvoiceStatus.PAID
                          && i.getStatus() != Invoice.InvoiceStatus.CANCELLED)
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException(
                        "Geen openstaande factuur gevonden voor " + student.getUsername()
                        + " in " + month + "/" + year));

        String maand       = LocalDate.of(year, month, 1).format(MONTH_NL);
        String vervaldatum = invoice.getDueDate() != null ? invoice.getDueDate().format(DATE_NL) : "";
        String bedrag      = formatBedrag(invoice.getAmount());
        String betaalLink  = "";

        EmailTemplate.TemplateType templateType = resolveTemplateType(request.templateType());
        EmailTemplate template = loadTemplate(templateType);

        String naam = student.getUsername();
        String subject, body;
        if (template != null) {
            subject = template.renderSubject(naam, bedrag, maand, betaalLink, vervaldatum);
            body    = template.renderBody(naam, bedrag, maand, betaalLink, vervaldatum);
        } else {
            subject = "Herinnering huur " + maand + " – Villa Vredestein";
            body    = "Beste " + naam + ",\n\nJe huur van " + bedrag + " voor " + maand
                    + " is nog niet betaald.\n\nBetaal via: " + betaalLink
                    + "\n\nMet vriendelijke groet,\nVilla Vredestein";
        }

        mailService.sendInvoiceReminderMail(student.getEmail(), subject, body);

        String phone = student.getPhoneNumber();
        if (phone != null && !phone.isBlank()) {
            String waMsg = String.format(
                    "⚠️ Huurherinnering – Villa Vredestein\n\nHallo %s, je huur van %s voor %s is nog niet betaald.\n" +
                    "Betaal vóór %s via NL94 INGB 0660 8510 83 t.n.v. M. Staal.",
                    naam, bedrag, maand, vervaldatum);
            whatsAppService.send(phone, waMsg);
        }

        invoice.setReminderCount(invoice.getReminderCount() + 1);
        invoice.setLastReminderSentAt(LocalDateTime.now());
        invoiceService.saveReminderMeta(invoice);

        log.info("Admin manual reminder ({}) sent to {} for invoiceId={}",
                templateType, maskEmail(student.getEmail()), invoice.getId());

        return ResponseEntity.ok(Map.of(
                "message", "Herinnering verstuurd naar " + naam + " (" + student.getEmail() + ")",
                "invoiceId", String.valueOf(invoice.getId()),
                "betaalLink", betaalLink
        ));
    }

    // Helpers

    private EmailTemplate.TemplateType resolveTemplateType(String raw) {
        if (raw == null) return EmailTemplate.TemplateType.PAYMENT_REMINDER_1;
        try {
            return EmailTemplate.TemplateType.valueOf(raw.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            log.warn("Unknown templateType '{}', defaulting to PAYMENT_REMINDER_1", raw);
            return EmailTemplate.TemplateType.PAYMENT_REMINDER_1;
        }
    }

    private EmailTemplate loadTemplate(EmailTemplate.TemplateType type) {
        try {
            return emailTemplateService.getByType(type);
        } catch (Exception e) {
            log.error("Could not load template {}: {}", type, e.getMessage());
            return null;
        }
    }

    private String formatBedrag(java.math.BigDecimal amount) {
        return NumberFormat.getCurrencyInstance(NL).format(amount);
    }

    private String maskEmail(String email) {
        if (email == null || email.isBlank()) return "(no-email)";
        int at = email.indexOf('@');
        if (at <= 1) return "***";
        return email.charAt(0) + "***" + email.substring(at);
    }
}
