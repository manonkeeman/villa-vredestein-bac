package com.villavredestein.jobs;

import com.villavredestein.model.EmailTemplate;
import com.villavredestein.model.Invoice;
import com.villavredestein.service.EmailTemplateService;
import com.villavredestein.service.InvoiceService;
import com.villavredestein.service.MailService;
import com.villavredestein.service.MollieService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

/**
 * Sends payment reminders for unpaid invoices:
 *  - PAYMENT_REMINDER_1 on the 3rd of the month at 09:00
 *  - PAYMENT_REMINDER_2 on the 7th of the month at 09:00
 */
@Component
@ConditionalOnProperty(value = "spring.task.scheduling.enabled", havingValue = "true", matchIfMissing = true)
public class PaymentReminderJob {

    private static final Logger log = LoggerFactory.getLogger(PaymentReminderJob.class);
    private static final Locale NL = Locale.forLanguageTag("nl-NL");
    private static final DateTimeFormatter MONTH_NL = DateTimeFormatter.ofPattern("MMMM yyyy", NL);
    private static final DateTimeFormatter DATE_NL = DateTimeFormatter.ofPattern("d MMMM yyyy", NL);

    private final InvoiceService invoiceService;
    private final MailService mailService;
    private final EmailTemplateService emailTemplateService;
    private final MollieService mollieService;

    public PaymentReminderJob(InvoiceService invoiceService,
                              MailService mailService,
                              EmailTemplateService emailTemplateService,
                              MollieService mollieService) {
        this.invoiceService = invoiceService;
        this.mailService = mailService;
        this.emailTemplateService = emailTemplateService;
        this.mollieService = mollieService;
    }

    // =====================================================================
    // # First reminder — 3rd of the month
    // =====================================================================

    @Scheduled(cron = "0 0 9 3 * *", zone = "Europe/Amsterdam")
    public void sendFirstReminders() {
        sendReminders(EmailTemplate.TemplateType.PAYMENT_REMINDER_1, 1);
    }

    public void triggerFirstReminder() {
        sendReminders(EmailTemplate.TemplateType.PAYMENT_REMINDER_1, 1);
    }

    // =====================================================================
    // # Second reminder — 7th of the month
    // =====================================================================

    @Scheduled(cron = "0 0 9 7 * *", zone = "Europe/Amsterdam")
    public void sendSecondReminders() {
        sendReminders(EmailTemplate.TemplateType.PAYMENT_REMINDER_2, 2);
    }

    public void triggerSecondReminder() {
        sendReminders(EmailTemplate.TemplateType.PAYMENT_REMINDER_2, 2);
    }

    // =====================================================================
    // # Core logic
    // =====================================================================

    private void sendReminders(EmailTemplate.TemplateType templateType, int reminderNumber) {
        LocalDate today = LocalDate.now();
        int month = today.getMonthValue();
        int year = today.getYear();

        log.info("PaymentReminderJob [{}] started (month={}/{})", templateType, month, year);

        List<Invoice> unpaid = invoiceService.getUnpaidForMonth(month, year);
        log.info("Found {} unpaid invoices for {}/{}", unpaid.size(), month, year);

        EmailTemplate template = loadTemplate(templateType);

        for (Invoice invoice : unpaid) {
            sendReminder(invoice, template, reminderNumber);
        }

        log.info("PaymentReminderJob [{}] finished ({} invoices processed)", templateType, unpaid.size());
    }

    private void sendReminder(Invoice invoice, EmailTemplate template, int reminderNumber) {
        try {
            String email = invoice.getStudent().getEmail();
            String naam = invoice.getStudent().getUsername();
            String maand = LocalDate.of(invoice.getInvoiceYear(), invoice.getInvoiceMonth(), 1).format(MONTH_NL);
            String bedrag = formatBedrag(invoice.getAmount());
            String betaalLink = refreshCheckoutUrl(invoice, maand);
            String vervaldatum = invoice.getDueDate() != null ? invoice.getDueDate().format(DATE_NL) : "";

            String subject, body;
            if (template != null) {
                subject = template.renderSubject(naam, bedrag, maand, betaalLink, vervaldatum);
                body = template.renderBody(naam, bedrag, maand, betaalLink, vervaldatum);
            } else {
                subject = "Herinnering huur " + maand + " – Villa Vredestein";
                body = "Beste " + naam + ",\n\nJe huur van " + bedrag + " voor " + maand + " is nog niet betaald.\n\nMet vriendelijke groet,\nVilla Vredestein";
            }

            mailService.sendInvoiceReminderMail(email, subject, body);

            // Track reminder metadata
            invoice.setReminderCount(invoice.getReminderCount() + 1);
            invoice.setLastReminderSentAt(LocalDateTime.now());
            invoiceService.saveReminderMeta(invoice);

            log.info("Reminder {} sent for invoiceId={} to {}", reminderNumber, invoice.getId(), maskEmail(email));

        } catch (Exception e) {
            log.error("Failed to send reminder {} for invoiceId={}: {}", reminderNumber, invoice.getId(), e.getMessage());
        }
    }

    // =====================================================================
    // # Helpers
    // =====================================================================

    /**
     * Creates a fresh Mollie iDEAL payment for the invoice so the reminder
     * email always contains a valid, non-expired checkout URL.
     * Falls back to the stored URL (or empty string) if Mollie is unavailable.
     */
    private String refreshCheckoutUrl(Invoice invoice, String maand) {
        String description = "Huur " + maand + " – Villa Vredestein";
        try {
            MollieService.MolliePaymentResult result =
                    mollieService.createPayment(invoice.getAmount(), description, invoice.getId());
            if (result != null && result.checkoutUrl() != null) {
                invoiceService.attachMolliePayment(invoice, result.molliePaymentId(), result.checkoutUrl());
                log.info("Fresh Mollie payment created for reminder (invoiceId={}, mollieId={})",
                        invoice.getId(), result.molliePaymentId());
                return result.checkoutUrl();
            }
        } catch (Exception e) {
            log.warn("Could not refresh Mollie link for invoiceId={}: {}", invoice.getId(), e.getMessage());
        }
        // Fallback to stored URL (may be expired but better than nothing)
        return invoice.getCheckoutUrl() != null ? invoice.getCheckoutUrl() : "";
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