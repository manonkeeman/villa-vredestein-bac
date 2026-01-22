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
        log.info("OverdueInvoiceJob started (candidates={}, maxReminders={}, minHoursBetween={})",
                candidates.size(), maxReminders, minHoursBetween);

        for (Invoice invoice : candidates) {
            processInvoice(invoice, today, now);
        }

        log.info("OverdueInvoiceJob finished");
    }

    private void processInvoice(Invoice invoice, LocalDate today, LocalDateTime now) {
        if (invoice == null) {
            log.warn("Skip: invoice is null");
            return;
        }

        Long invoiceId = invoice.getId();

        User student = invoice.getStudent();
        if (student == null) {
            log.warn("Skip: student missing (invoiceId={})", invoiceId);
            return;
        }

        if (invoice.getDueDate() == null) {
            log.warn("Skip: dueDate missing (invoiceId={})", invoiceId);
            return;
        }

        if (!invoice.getDueDate().isBefore(today)) {
            return;
        }

        Invoice.InvoiceStatus status = invoice.getStatus();
        if (status != Invoice.InvoiceStatus.OPEN && status != Invoice.InvoiceStatus.OVERDUE) {
            return;
        }

        if (!passesAntiSpamRules(invoice, now)) {
            return;
        }

        String to = student.getEmail();
        if (to == null || to.isBlank()) {
            log.warn("Skip: invalid email (invoiceId={}, student={})", invoiceId, safeName(student));
            return;
        }

        if (status != Invoice.InvoiceStatus.OVERDUE) {
            invoice.setStatus(Invoice.InvoiceStatus.OVERDUE);
        }

        sendOverdueMail(invoice, student, to);
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

    private void sendOverdueMail(Invoice invoice, User student, String to) {
        Long invoiceId = invoice.getId();

        String amount = formatAmount(invoice.getAmount());
        String due = invoice.getDueDate().format(DATE_NL);

        String subject = "Rent payment overdue – action required";
        String body = String.format("""
                Dear %s,

                Our records show that your rent payment (%s) with due date %s is still unpaid.
                Please pay as soon as possible. If you already paid, you can ignore this message.

                Kind regards,
                Villa Vredestein
                """, safeName(student), amount, due);

        try {
            mailService.sendInvoiceReminderMail(to, subject, body);

            invoice.markReminderSentNow();
            invoiceService.saveReminderMeta(invoice);

            log.info("Overdue reminder sent (invoiceId={}, to={}, reminderCount={})",
                    invoiceId, maskEmail(to), invoice.getReminderCount());

        } catch (Exception e) {
            log.error("Sending failed (invoiceId={}, to={}): {}",
                    invoiceId, maskEmail(to), e.getMessage());
        }
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