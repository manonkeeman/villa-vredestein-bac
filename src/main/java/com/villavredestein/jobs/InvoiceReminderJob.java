package com.villavredestein.jobs;

import com.villavredestein.model.Invoice;
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
public class InvoiceReminderJob {

    private static final Logger log = LoggerFactory.getLogger(InvoiceReminderJob.class);

    private static final Locale NL = Locale.forLanguageTag("nl-NL");
    private static final NumberFormat EUR = NumberFormat.getCurrencyInstance(NL);
    private static final DateTimeFormatter DATE_NL = DateTimeFormatter.ofPattern("d MMMM yyyy", NL);

    @Value("${app.invoice.reminder.days-before-due:4}")
    private int daysBeforeDue;

    @Value("${app.invoice.reminder.max-reminders:3}")
    private int maxReminders;

    @Value("${app.invoice.reminder.min-hours-between:24}")
    private int minHoursBetween;

    private final InvoiceService invoiceService;
    private final MailService mailService;

    public InvoiceReminderJob(InvoiceService invoiceService, MailService mailService) {
        this.invoiceService = invoiceService;
        this.mailService = mailService;
    }

    @Scheduled(cron = "0 0 9 * * *", zone = "Europe/Amsterdam")
    public void sendReminders() {
        LocalDate today = LocalDate.now();
        LocalDateTime now = LocalDateTime.now();

        log.info("Start invoice reminder job (daysBeforeDue={}, maxReminders={}, minHoursBetween={})",
                daysBeforeDue, maxReminders, minHoursBetween);

        List<Invoice> candidates = invoiceService.getUpcomingInvoices();
        log.info("📆 Kandidaten ontvangen: {} facturen", candidates.size());

        for (Invoice invoice : candidates) {
            processInvoice(invoice, today, now);
        }

        log.info("Invoice reminder job klaar");
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

        if (!passesAntiSpamRules(invoice, now)) {
            return;
        }

        if (!withinDueWindow(invoice, today)) {
            return;
        }

        String to = student.getEmail();
        if (to == null || to.isBlank()) {
            log.warn("Skip: geen geldig e-mailadres (invoiceId={}, student={})", invoiceId, safeName(student));
            return;
        }

        sendReminderMail(invoice, student, to, now);
    }

    private boolean passesAntiSpamRules(Invoice invoice, LocalDateTime now) {
        Long invoiceId = invoice.getId();

        if (invoice.getReminderCount() >= maxReminders) {
            log.info("Skip reminder: maxReminders bereikt (invoiceId={}, reminderCount={})",
                    invoiceId, invoice.getReminderCount());
            return false;
        }

        LocalDateTime last = invoice.getLastReminderSentAt();
        if (last == null) {
            return true;
        }

        long hoursSinceLast = ChronoUnit.HOURS.between(last, now);
        if (hoursSinceLast < minHoursBetween) {
            log.info("Skip reminder: laatste reminder {} uur geleden (invoiceId={})", hoursSinceLast, invoiceId);
            return false;
        }

        return true;
    }

    private boolean withinDueWindow(Invoice invoice, LocalDate today) {
        Long invoiceId = invoice.getId();

        long daysToDue = ChronoUnit.DAYS.between(today, invoice.getDueDate());
        if (daysToDue < 0 || daysToDue > daysBeforeDue) {
            log.info("Skip reminder: dueDate buiten venster (invoiceId={}, daysToDue={}, window={})",
                    invoiceId, daysToDue, daysBeforeDue);
            return false;
        }

        return true;
    }

    private void sendReminderMail(Invoice invoice, User student, String to, LocalDateTime now) {
        Long invoiceId = invoice.getId();

        String amount = EUR.format(invoice.getAmount());
        String due = invoice.getDueDate().format(DATE_NL);
        String description = (invoice.getDescription() == null || invoice.getDescription().isBlank())
                ? "Huur"
                : invoice.getDescription().trim();

        String subject = "Herinnering huurbetaling Villa Vredestein – betaal vóór " + due;

        String body = String.format("""
                Beste %s,

                Dit is een vriendelijke herinnering om je huur van %s te betalen vóór %s.

                Beschrijving: %s

                Met vriendelijke groet,
                Villa Vredestein
                """, safeName(student), amount, due, description);

        log.info("Reminder opgebouwd (invoiceId={}, to={}, student={}, amount={}, due={})",
                invoiceId, maskEmail(to), safeName(student), amount, due);

        try {
            mailService.sendInvoiceReminderMail(to, subject, body);

            invoice.markReminderSentNow();          // zet lastReminderSentAt + increment reminderCount
            invoiceService.saveReminderMeta(invoice); // persist reminder meta

            log.info("Reminder verzonden (invoiceId={}, to={}, reminderCount={})",
                    invoiceId, maskEmail(to), invoice.getReminderCount());

        } catch (Exception e) {
            log.error("Verzenden mislukt (invoiceId={}, to={}): {}", invoiceId, maskEmail(to), e.getMessage());
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