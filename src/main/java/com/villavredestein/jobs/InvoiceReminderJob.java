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

import java.math.BigDecimal;
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

        log.info("InvoiceReminderJob started (daysBeforeDue={}, maxReminders={}, minHoursBetween={})",
                daysBeforeDue, maxReminders, minHoursBetween);

        List<Invoice> candidates = invoiceService.getUpcomingInvoices();
        log.info("Candidates received: {} invoices", candidates.size());

        for (Invoice invoice : candidates) {
            processInvoice(invoice, today, now);
        }

        log.info("InvoiceReminderJob finished");
    }

    private void processInvoice(Invoice invoice, LocalDate today, LocalDateTime now) {
        if (invoice == null) {
            log.warn("Skip: invoice is null");
            return;
        }

        Long invoiceId = invoice.getId();

        if (invoice.getStatus() != null && invoice.getStatus() != Invoice.InvoiceStatus.OPEN) {
            log.info("Skip: invoice not OPEN (invoiceId={}, status={})", invoiceId, invoice.getStatus());
            return;
        }

        User student = invoice.getStudent();
        if (student == null) {
            log.warn("Skip: student missing (invoiceId={})", invoiceId);
            return;
        }

        if (invoice.getDueDate() == null) {
            log.warn("Skip: dueDate missing (invoiceId={})", invoiceId);
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
            log.warn("Skip: invalid email (invoiceId={}, student={})", invoiceId, safeName(student));
            return;
        }

        sendReminderMail(invoice, student, to, now);
    }

    private boolean passesAntiSpamRules(Invoice invoice, LocalDateTime now) {
        Long invoiceId = invoice.getId();

        if (invoice.getReminderCount() >= maxReminders) {
            log.info("Skip: maxReminders reached (invoiceId={}, reminderCount={})",
                    invoiceId, invoice.getReminderCount());
            return false;
        }

        LocalDateTime last = invoice.getLastReminderSentAt();
        if (last == null) {
            return true;
        }

        long hoursSinceLast = ChronoUnit.HOURS.between(last, now);
        if (hoursSinceLast < minHoursBetween) {
            log.info("Skip: last reminder {} hours ago (invoiceId={})", hoursSinceLast, invoiceId);
            return false;
        }

        return true;
    }

    private boolean withinDueWindow(Invoice invoice, LocalDate today) {
        Long invoiceId = invoice.getId();

        long daysToDue = ChronoUnit.DAYS.between(today, invoice.getDueDate());
        if (daysToDue < 0 || daysToDue > daysBeforeDue) {
            log.info("Skip: dueDate outside window (invoiceId={}, daysToDue={}, window={})",
                    invoiceId, daysToDue, daysBeforeDue);
            return false;
        }

        return true;
    }

    private void sendReminderMail(Invoice invoice, User student, String to, LocalDateTime now) {
        Long invoiceId = invoice.getId();

        String amount = formatAmount(invoice.getAmount());
        String due = invoice.getDueDate().format(DATE_NL);
        String description = normalizeDescription(invoice.getDescription());

        String subject = "Herinnering huurbetaling Villa Vredestein – betaal vóór " + due;

        String body = String.format("""
                Beste %s,

                Dit is een vriendelijke herinnering om je huur van %s te betalen vóór %s.

                Beschrijving: %s

                Met vriendelijke groet,
                Villa Vredestein
                """, safeName(student), amount, due, description);

        log.info("Reminder built (invoiceId={}, to={}, student={}, amount={}, due={})",
                invoiceId, maskEmail(to), safeName(student), amount, due);

        try {
            mailService.sendInvoiceReminderMail(to, subject, body);

            invoice.markReminderSentNow();
            invoiceService.saveReminderMeta(invoice);

            log.info("Reminder sent (invoiceId={}, to={}, reminderCount={})",
                    invoiceId, maskEmail(to), invoice.getReminderCount());

        } catch (Exception e) {
            log.error("Sending failed (invoiceId={}, to={}): {}", invoiceId, maskEmail(to), e.getMessage());
        }
    }

    private String normalizeDescription(String description) {
        if (description == null || description.isBlank()) {
            return "Huur";
        }
        return description.trim();
    }

    private String formatAmount(BigDecimal amount) {
        if (amount == null) {
            return EUR.format(0);
        }
        return EUR.format(amount);
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