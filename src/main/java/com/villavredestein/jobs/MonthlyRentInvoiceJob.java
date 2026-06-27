package com.villavredestein.jobs;

import com.villavredestein.model.EmailTemplate;
import com.villavredestein.model.User;
import com.villavredestein.repository.UserRepository;
import com.villavredestein.service.EmailTemplateService;
import com.villavredestein.service.InvoiceService;
import com.villavredestein.service.MailService;
import com.villavredestein.service.WhatsAppService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

@Component
@ConditionalOnProperty(value = "spring.task.scheduling.enabled", havingValue = "true", matchIfMissing = true)
public class MonthlyRentInvoiceJob {

    private static final Logger log = LoggerFactory.getLogger(MonthlyRentInvoiceJob.class);
    private static final Locale NL = Locale.forLanguageTag("nl-NL");
    private static final DateTimeFormatter MONTH_NL = DateTimeFormatter.ofPattern("MMMM yyyy", NL);
    private static final DateTimeFormatter DATE_NL = DateTimeFormatter.ofPattern("d MMMM yyyy", NL);

    private final UserRepository userRepository;
    private final InvoiceService invoiceService;
    private final MailService mailService;
    private final EmailTemplateService emailTemplateService;
    private final WhatsAppService whatsAppService;

    @Value("${app.rent.amount:350.00}")
    private BigDecimal rentAmount;

    public MonthlyRentInvoiceJob(UserRepository userRepository,
                                 InvoiceService invoiceService,
                                 MailService mailService,
                                 EmailTemplateService emailTemplateService,
                                 WhatsAppService whatsAppService) {
        this.userRepository = userRepository;
        this.invoiceService = invoiceService;
        this.mailService = mailService;
        this.emailTemplateService = emailTemplateService;
        this.whatsAppService = whatsAppService;
    }

    @Scheduled(cron = "0 0 8 1 * *", zone = "Europe/Amsterdam")
    public void createMonthlyInvoices() {
        LocalDate today = LocalDate.now();
        int month = today.getMonthValue();
        int year = today.getYear();
        LocalDate dueDate = today.plusDays(7); // due 8th of the month

        String maand = today.format(MONTH_NL);
        String vervaldatum = dueDate.format(DATE_NL);
        log.info("MonthlyRentInvoiceJob started (maand={})", maand);

        List<User> students = userRepository.findByRole(User.Role.STUDENT);
        log.info("Creating invoices for {} students", students.size());

        EmailTemplate template = loadTemplate();

        for (User student : students) {
            BigDecimal studentRent = student.getRentAmount() != null ? student.getRentAmount() : rentAmount;
            String studentBedrag = formatBedrag(studentRent);
            processStudent(student, month, year, dueDate, maand, vervaldatum, studentBedrag, studentRent, template);
        }

        log.info("MonthlyRentInvoiceJob finished");
    }

    public void run() {
        createMonthlyInvoices();
    }


    private void processStudent(User student, int month, int year, LocalDate dueDate,
                                String maand, String vervaldatum, String bedragFormatted,
                                BigDecimal studentRent, EmailTemplate template) {
        try {
            var dto = new com.villavredestein.dto.InvoiceRequestDTO();
            dto.setStudentEmail(student.getEmail());
            dto.setTitle("Huur " + maand);
            dto.setDescription("Maandelijkse huur voor " + maand);
            dto.setAmount(studentRent);
            dto.setIssueDate(LocalDate.now());
            dto.setDueDate(dueDate);

            com.villavredestein.dto.InvoiceResponseDTO invoiceDTO;
            try {
                invoiceDTO = invoiceService.createInvoice(dto);
            } catch (org.springframework.web.server.ResponseStatusException e) {
                if (e.getStatusCode().value() == 409) {
                    log.info("Invoice already exists for student={} month={}/{}", student.getEmail(), month, year);
                    return;
                }
                throw e;
            }

            Long invoiceId = invoiceDTO.getId();
            String betaalLink = "";

            String naam = student.getUsername();
            if (template != null) {
                String subject = template.renderSubject(naam, bedragFormatted, maand, betaalLink, vervaldatum);
                String body = template.renderBody(naam, bedragFormatted, maand, betaalLink, vervaldatum);
                mailService.sendInvoiceReminderMail(student.getEmail(), subject, body);
                log.info("PAYMENT_NEW email sent to {}", maskEmail(student.getEmail()));
            }

            String waMsg = String.format(
                    "Hallo %s! Je huurrekening van %s voor %s is aangemaakt. " +
                    "Betaal vóór %s via overboeking naar NL94 INGB 0660 8510 83 t.n.v. M. Staal. " +
                    "Vragen? Neem contact op via WhatsApp.",
                    naam, bedragFormatted, maand, vervaldatum);
            if (student.getPhoneNumber() != null && !student.getPhoneNumber().isBlank()) {
                whatsAppService.send(student.getPhoneNumber(), waMsg);
            }
            whatsAppService.sendToAdmins("📋 Huur " + maand + " factuur aangemaakt voor " + naam + " (" + bedragFormatted + ").");

        } catch (Exception e) {
            log.error("Error processing student {} for month={}/{}: {}", maskEmail(student.getEmail()), month, year, e.getMessage(), e);
        }
    }


    private EmailTemplate loadTemplate() {
        try {
            return emailTemplateService.getByType(EmailTemplate.TemplateType.PAYMENT_NEW);
        } catch (Exception e) {
            log.error("Could not load PAYMENT_NEW template: {}", e.getMessage());
            return null;
        }
    }

    private String formatBedrag(BigDecimal amount) {
        NumberFormat nf = NumberFormat.getCurrencyInstance(NL);
        return nf.format(amount);
    }

    private String maskEmail(String email) {
        if (email == null || email.isBlank()) return "(no-email)";
        int at = email.indexOf('@');
        if (at <= 1) return "***";
        return email.charAt(0) + "***" + email.substring(at);
    }
}