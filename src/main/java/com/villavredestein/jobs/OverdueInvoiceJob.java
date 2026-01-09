package com.villavredestein.jobs;

import com.villavredestein.model.Invoice;
import com.villavredestein.model.Invoice.InvoiceStatus;
import com.villavredestein.model.User;
import com.villavredestein.service.InvoiceService;
import com.villavredestein.service.MailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Locale;

@Component
@ConditionalOnProperty(value = "spring.task.scheduling.enabled", havingValue = "true", matchIfMissing = true)
public class OverdueInvoiceJob {

    private static final Logger log = LoggerFactory.getLogger(OverdueInvoiceJob.class);

    private static final Locale NL = Locale.forLanguageTag("nl-NL");
    private static final NumberFormat EUR = NumberFormat.getCurrencyInstance(NL);
    private static final DateTimeFormatter DATE_NL = DateTimeFormatter.ofPattern("d MMMM yyyy", NL);

    @Value("${app.invoice.overdue.max-reminders:5}")
    private int maxReminders;

    @Value("${app.invoice.overdue.min-hours-between:24}")
    private int minHoursBetween;

    private final InvoiceService invoiceService;
    private final MailService mailService;

    public OverdueInvoiceJob(InvoiceService invoiceService, MailService mailService) {
        this.invoiceService = invoiceService;
        this.mailService = mailService;
    }

    @Scheduled(cron = "${app.invoice.overdue.cron:0 15 9 * * *}", zone = "Europe/Amsterdam")
    public void sendOverdueReminders() {
        LocalDate today = LocalDate.now();
        LocalDateTime now = LocalDateTime.now();

        List<Invoice> candidates = invoiceService.getAllOpenInvoices();
        log.info("Start overdue invoice job (candidates={}, maxReminders={}, minHoursBetween={})",
                candidates.size(), maxReminders, minHoursBetween);

        for (Invoice invoice : candidates) {
            processInvoice(invoice, today, now);
        }

        log.info("Overdue invoice job klaar");
    }

    private void processInvoice(Invoice invoice, LocalDate today, LocalDateTime now) {
        if (invoice == null) {
            log.warn("Skip: invoice is null");
            return;
        }

        Long invoiceId = invoice.getId();

        User student = invoice.getStudent();
        if (student == null) {
            log.warn("Skip: student ontbreekt (invoiceId={})", invoiceId);
            return;
        }

        if (invoice.getDueDate() == null) {
            log.warn("Skip: dueDate ontbreekt (invoiceId={})", invoiceId);
            return;
        }

        if (!invoice.getDueDate().isBefore(today)) {
            return;
        }

        if (invoice.getStatus() != InvoiceStatus.OPEN && invoice.getStatus() != InvoiceStatus.OVERDUE) {
            return;
        }

        if (!passesAntiSpamRules(invoice, now)) {
            return;
        }

        String to = student.getEmail();
        if (to == null || to.isBlank()) {
            log.warn("Skip: geen geldig e-mailadres (invoiceId={}, student={})", invoiceId, safeName(student));
            return;
        }

        if (invoice.getStatus() != InvoiceStatus.OVERDUE) {
            invoice.setStatus(InvoiceStatus.OVERDUE);
        }

        sendOverdueMail(invoice, student, to);
    }

    private boolean passesAntiSpamRules(Invoice invoice, LocalDateTime now) {
        Long invoiceId = invoice.getId();

        if (invoice.getReminderCount() >= maxReminders) {
            log.info("Skip overdue: maxReminders bereikt (invoiceId={}, reminderCount={})",
                    invoiceId, invoice.getReminderCount());
            return false;
        }

        LocalDateTime last = invoice.getLastReminderSentAt();
        if (last == null) {
            return true;
        }

        long hoursSinceLast = ChronoUnit.HOURS.between(last, now);
        if (hoursSinceLast < minHoursBetween) {
            log.info("Skip overdue: laatste reminder {} uur geleden (invoiceId={})", hoursSinceLast, invoiceId);
            return false;
        }

        return true;
    }

    private void sendOverdueMail(Invoice invoice, User student, String to) {
        Long invoiceId = invoice.getId();

        String amount = EUR.format(invoice.getAmount());
        String due = invoice.getDueDate().format(DATE_NL);

        String subject = "Huurbetaling verlopen – actie nodig";
        String body = String.format("""
                Beste %s,

                Onze administratie geeft aan dat de betaling van je huur (%s) met vervaldatum %s nog openstaat.
                Wil je dit z.s.m. voldoen? Als je al betaald hebt, kun je deze mail negeren.

                Met vriendelijke groet,
                Villa Vredestein
                """, safeName(student), amount, due);

        try {
            mailService.sendInvoiceReminderMail(to, subject, body);
            invoice.markReminderSentNow();
            invoiceService.saveReminderMeta(invoice);

            log.info("Overdue mail verzonden (invoiceId={}, to={}, reminderCount={})",
                    invoiceId, maskEmail(to), invoice.getReminderCount());
        } catch (Exception e) {
            log.error("Verzenden overdue-mail mislukt (invoiceId={}, to={}): {}",
                    invoiceId, maskEmail(to), e.getMessage());
        }
    }

    private String safeName(User user) {
        if (user == null) return "student";
        String u = user.getUsername();
        return (u == null || u.isBlank()) ? "student" : u.trim();
    }

    private String maskEmail(String email) {
        if (email == null || email.isBlank()) return "(no-email)";
        int at = email.indexOf('@');
        if (at <= 1) return "***";
        return email.charAt(0) + "***" + email.substring(at);
    }
}