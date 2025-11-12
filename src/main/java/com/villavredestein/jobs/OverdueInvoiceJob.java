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
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

/**
 * {@code OverdueInvoiceJob} is een geplande taak (cron job) die dagelijks controleert
 * of er huurbetalingen zijn waarvan de vervaldatum is verstreken. Voor iedere openstaande
 * factuur die over tijd is, wordt automatisch een herinneringsmail verstuurd naar de student.
 *
 * <p>De job draait standaard iedere dag om 09:15 uur (tijdzone {@code Europe/Amsterdam}).
 * Via de property {@code spring.task.scheduling.enabled} kan het in- of uitgeschakeld worden.</p>
 *
 * <p>De e-mails worden verzonden via de {@link MailService} met een standaard onderwerp
 * “Let op: huurbetaling is vervallen”. De bedragen en datums worden opgemaakt in Nederlandse notatie.</p>
 *
 * @see InvoiceService#getAllOpenInvoices()
 * @see MailService#sendMailWithRole(String, String, String, String)
 */
@Component
@ConditionalOnProperty(value = "spring.task.scheduling.enabled", havingValue = "true", matchIfMissing = true)
public class OverdueInvoiceJob {

    private static final Logger log = LoggerFactory.getLogger(OverdueInvoiceJob.class);

    /** Nederlandse locale voor valuta- en datumopmaak. */
    private static final Locale NL = new Locale("nl", "NL");

    /** Formatter voor valuta in euro’s, bijv. €1.200,00. */
    private static final NumberFormat EUR = NumberFormat.getCurrencyInstance(NL);

    /** Formatter voor datumnotatie, bijv. 5 oktober 2025. */
    private static final DateTimeFormatter DATE_NL = DateTimeFormatter.ofPattern("d MMMM yyyy", NL);

    private final InvoiceService invoiceService;
    private final MailService mailService;

    /**
     * Constructor voor {@link OverdueInvoiceJob}.
     *
     * @param invoiceService service voor factuurbeheer
     * @param mailService service voor e-mailcommunicatie
     */
    public OverdueInvoiceJob(InvoiceService invoiceService, MailService mailService) {
        this.invoiceService = invoiceService;
        this.mailService = mailService;
    }

    /**
     * Controleert dagelijks om 09:15 op openstaande facturen die over tijd zijn.
     * Voor iedere factuur met een vervaldatum vóór vandaag wordt een herinnering verstuurd.
     */
    @Scheduled(cron = "0 15 9 * * *", zone = "Europe/Amsterdam")
    public void sendOverdueReminders() {
        List<Invoice> candidates = invoiceService.getAllOpenInvoices();
        LocalDate today = LocalDate.now();
        long total = candidates.size();

        log.info("📅 Controle vervallen facturen: {} open facturen in check", total);

        candidates.stream()
                .filter(inv -> inv != null && inv.getDueDate() != null && inv.getDueDate().isBefore(today))
                .forEach(this::sendOverdue);
    }

    /**
     * Verstuurt een waarschuwing per e-mail voor een specifieke factuur die over tijd is.
     * De mail bevat bedrag, vervaldatum en beschrijving van de factuur.
     *
     * @param invoice de openstaande factuur die vervallen is
     */
    private void sendOverdue(Invoice invoice) {
        User student = invoice.getStudent();
        if (student == null || student.getEmail() == null || student.getEmail().isBlank()) {
            log.warn("⛔ Factuur {} heeft geen (geldige) student/e-mail. Overdue-mail overgeslagen.", invoice.getId());
            return;
        }

        String amount = EUR.format(invoice.getAmount());
        String due = invoice.getDueDate() != null ? invoice.getDueDate().format(DATE_NL) : "(onbekend)";

        String subject = "Let op: huurbetaling is vervallen";
        String body = String.format("""
                Beste %s,

                Onze administratie geeft aan dat de betaling van je huur (%s) met vervaldatum %s nog openstaat.
                Wil je dit z.s.m. voldoen? Als je al betaald hebt, kun je deze mail negeren.

                Met vriendelijke groet,

                Maxim Staal
                Villa Vredestein
                """, safe(student.getUsername()), amount, due);

        try {
            mailService.sendMailWithRole("ADMIN", student.getEmail(), subject, body);
            log.info("📨 Overdue-mail verzonden voor factuur {} aan {}", invoice.getId(), student.getEmail());
        } catch (Exception e) {
            log.error("⚠️ Verzenden overdue-mail mislukt voor factuur {}: {}", invoice.getId(), e.getMessage());
        }
    }

    /**
     * Helpermethode om te voorkomen dat {@code null} of lege gebruikersnamen
     * worden gebruikt in e-mails.
     *
     * @param s de originele gebruikersnaam
     * @return de originele naam of het woord "student" als fallback
     */
    private String safe(String s) {
        return (s == null || s.isBlank()) ? "student" : s;
    }
}