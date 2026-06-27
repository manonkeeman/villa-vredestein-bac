package com.villavredestein.jobs;

import com.villavredestein.model.Invoice;
import com.villavredestein.repository.InvoiceRepository;
import com.villavredestein.service.WhatsAppService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

@Component
@ConditionalOnProperty(value = "spring.task.scheduling.enabled", havingValue = "true", matchIfMissing = true)
public class BunqPaymentReminderJob {

    private static final Logger log = LoggerFactory.getLogger(BunqPaymentReminderJob.class);
    private static final Locale NL = Locale.forLanguageTag("nl-NL");
    private static final DateTimeFormatter MONTH_NL = DateTimeFormatter.ofPattern("MMMM yyyy", NL);
    private static final DateTimeFormatter DATE_NL = DateTimeFormatter.ofPattern("d MMMM yyyy", NL);

    private final InvoiceRepository invoiceRepository;
    private final WhatsAppService whatsAppService;

    @Value("${bunq.me.username:MaximStaal}")
    private String bunqMeUsername;

    public BunqPaymentReminderJob(InvoiceRepository invoiceRepository,
                                  WhatsAppService whatsAppService) {
        this.invoiceRepository = invoiceRepository;
        this.whatsAppService = whatsAppService;
    }

    @Scheduled(cron = "0 0 9 6 * *", zone = "Europe/Amsterdam")
    public void sendFirstReminder() {
        sendReminders(1);
    }

    @Scheduled(cron = "0 0 9 11 * *", zone = "Europe/Amsterdam")
    public void sendSecondReminder() {
        sendReminders(2);
    }

    private void sendReminders(int reminderNumber) {
        LocalDate today = LocalDate.now();
        int month = today.getMonthValue();
        int year = today.getYear();
        String maand = today.withDayOfMonth(1).format(MONTH_NL);

        List<Invoice> openInvoices = invoiceRepository.findByInvoiceMonthAndInvoiceYearAndStatusNotIn(
                month, year, List.of(Invoice.InvoiceStatus.PAID));

        log.info("BunqPaymentReminderJob reminder={} maand={} openInvoices={}", reminderNumber, maand, openInvoices.size());

        for (Invoice invoice : openInvoices) {
            try {
                var student = invoice.getStudent();
                if (student == null) continue;

                String phone = student.getPhoneNumber();
                if (phone == null || phone.isBlank()) continue;

                String naam = student.getUsername();
                String bedrag = formatBedrag(invoice.getAmount());
                String vervaldatum = invoice.getDueDate() != null
                        ? invoice.getDueDate().format(DATE_NL)
                        : "zo snel mogelijk";

                String bunqLink = buildBunqLink(invoice.getAmount(), maand);
                String waMsg = String.format(
                        "Hallo %s! Dit is herinnering %d voor je huur van %s voor %s. " +
                        "De betaling staat nog open. Maak het bedrag over vóór %s naar " +
                        "NL94 INGB 0660 8510 83 ten name van M. Staal.%s " +
                        "Heb je al betaald? Dan kun je dit bericht negeren.",
                        naam, reminderNumber, bedrag, maand, vervaldatum,
                        bunqLink.isEmpty() ? "" : " Of betaal direct via bunq: " + bunqLink + ".");

                whatsAppService.send(phone, waMsg);
                whatsAppService.sendToAdmins("Bunq herinnering " + reminderNumber + " verstuurd aan "
                        + naam + " voor huur " + maand + " (" + bedrag + ").");

                log.info("BunqPaymentReminderJob reminder={} sent to student={}", reminderNumber, student.getId());
            } catch (Exception e) {
                log.error("BunqPaymentReminderJob failed for invoiceId={}: {}", invoice.getId(), e.getMessage());
            }
        }
    }

    private String buildBunqLink(BigDecimal amount, String maand) {
        if (bunqMeUsername == null || bunqMeUsername.isBlank()) return "";
        try {
            String amountStr = amount.stripTrailingZeros().toPlainString();
            String desc = URLEncoder.encode("Huur " + maand + " Villa Vredestein", StandardCharsets.UTF_8)
                    .replace("+", "%20");
            return "https://bunq.me/" + bunqMeUsername + "/" + amountStr + "/" + desc;
        } catch (Exception e) {
            return "";
        }
    }

    private String formatBedrag(BigDecimal amount) {
        NumberFormat nf = NumberFormat.getCurrencyInstance(NL);
        return nf.format(amount);
    }
}
