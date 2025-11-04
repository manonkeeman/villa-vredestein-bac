package com.villavredestein.jobs;

import com.villavredestein.model.Invoice;
import com.villavredestein.model.User;
import com.villavredestein.service.InvoiceService;
import com.villavredestein.service.MailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

@Component
@ConditionalOnProperty(value = "spring.task.scheduling.enabled", havingValue = "true", matchIfMissing = true)
public class InvoiceReminderJob {

    private static final Logger log = LoggerFactory.getLogger(InvoiceReminderJob.class);

    private static final Locale NL = new Locale("nl", "NL");
    private static final NumberFormat EUR = NumberFormat.getCurrencyInstance(NL);
    private static final DateTimeFormatter DATE_NL = DateTimeFormatter.ofPattern("d MMMM yyyy", NL);

    private final InvoiceService invoiceService;
    private final MailService mailService;

    public InvoiceReminderJob(InvoiceService invoiceService, MailService mailService) {
        this.invoiceService = invoiceService;
        this.mailService = mailService;
    }

    /**
     * Draait elke 28e van de maand om 09:00 (Europe/Amsterdam).
     * Pakt open facturen die binnen 4 dagen vervallen en stuurt een herinnering.
     */
    @Scheduled(cron = "0 0 9 28 * *", zone = "Europe/Amsterdam")
    public void sendReminders() {
        List<Invoice> upcoming = invoiceService.getUpcomingInvoices();
        log.info("📆 Herinneringen verwerken: {} open facturen binnen 4 dagen vervaldatum", upcoming.size());

        for (Invoice invoice : upcoming) {
            if (invoice == null || invoice.getStudent() == null) {
                log.warn("⛔ Overgeslagen: factuur of student ontbreekt (id: {}).", invoice != null ? invoice.getId() : null);
                continue;
            }
            sendReminder(invoice);
        }
    }

    private void sendReminder(Invoice invoice) {
        User student = invoice.getStudent();
        String to = student.getEmail();

        if (to == null || to.isBlank()) {
            log.warn("⛔ Overgeslagen: geen geldig e-mailadres voor factuur {} (student: {}).",
                    invoice.getId(), student.getUsername());
            return;
        }

        String amount = EUR.format(invoice.getAmount());
        String due = invoice.getDueDate() != null ? invoice.getDueDate().format(DATE_NL) : "(onbekend)";
        String beschrijving = invoice.getDescription() != null ? invoice.getDescription() : "Huur";

        String subject = "Herinnering: huurbetaling Villa Vredestein";
        String body = String.format("""
                Beste %s,

                Dit is een vriendelijke herinnering om je huur van %s te betalen vóór %s.

                Beschrijving: %s

                Met vriendelijke groet,

                Maxim Staal
                Villa Vredestein
                """,
                safe(student.getUsername()), amount, due, beschrijving
        );

        try {
            mailService.sendMailWithRole("ADMIN", to, subject, body);
            log.info("📨 Herinnering verzonden: factuur {} → {}", invoice.getId(), to);
        } catch (Exception e) {
            log.error("⚠️ Verzenden mislukt voor factuur {} → {}: {}", invoice.getId(), to, e.getMessage());
        }
    }

    private String safe(String s) {
        return (s == null || s.isBlank()) ? "student" : s;
    }
}