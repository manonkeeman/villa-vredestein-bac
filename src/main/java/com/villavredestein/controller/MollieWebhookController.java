package com.villavredestein.controller;

import com.villavredestein.model.Invoice;
import com.villavredestein.service.InvoiceService;
import com.villavredestein.service.MailService;
import com.villavredestein.service.MollieService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

/**
 * Receives payment status updates from Mollie.
 * Must be publicly accessible (no JWT required).
 * Mollie POSTs to this endpoint with the payment ID whenever the status changes.
 */
@RestController
@RequestMapping("/api/mollie")
public class MollieWebhookController {

    private static final Logger log = LoggerFactory.getLogger(MollieWebhookController.class);

    private final MollieService mollieService;
    private final InvoiceService invoiceService;
    private final MailService mailService;

    public MollieWebhookController(MollieService mollieService,
                                   InvoiceService invoiceService,
                                   MailService mailService) {
        this.mollieService = mollieService;
        this.invoiceService = invoiceService;
        this.mailService = mailService;
    }

    /**
     * Mollie calls this with ?id={molliePaymentId} after every status change.
     * We fetch the real status from Mollie, find the matching invoice, and update it.
     */
    @PostMapping("/webhook")
    public ResponseEntity<Void> handleWebhook(@RequestParam String id) {
        log.info("Mollie webhook received for payment id={}", id);

        try {
            String status = mollieService.getPaymentStatus(id);
            log.info("Mollie status for {}: {}", id, status);

            if ("paid".equalsIgnoreCase(status)) {
                Optional<Invoice> invoiceOpt = invoiceService.findByMolliePaymentId(id);
                if (invoiceOpt.isEmpty()) {
                    log.warn("No invoice found for molliePaymentId={}", id);
                    return ResponseEntity.ok().build();
                }
                Invoice invoice = invoiceOpt.get();
                if (invoice.getStatus() != Invoice.InvoiceStatus.PAID) {
                    invoice.markPaid();
                    invoiceService.saveReminderMeta(invoice); // reuses existing save method
                    log.info("Invoice {} marked PAID via Mollie webhook", invoice.getId());
                    sendPaymentConfirmation(invoice);
                }
            }
        } catch (Exception e) {
            // Always return 200 to Mollie — retries otherwise
            log.error("Error processing Mollie webhook for id={}: {}", id, e.getMessage());
        }

        return ResponseEntity.ok().build();
    }

    private void sendPaymentConfirmation(Invoice invoice) {
        try {
            String name = invoice.getStudent().getUsername();
            String to = invoice.getStudent().getEmail();
            String subject = "Betaling ontvangen – Villa Vredestein";
            String body = """
                    Beste %s,

                    We hebben je betaling van €%s ontvangen. Bedankt!

                    Je factuur is nu gemarkeerd als betaald. Je kunt de factuur downloaden via je dashboard.

                    Met vriendelijke groet,
                    Villa Vredestein
                    """.formatted(name, invoice.getAmount().toPlainString());

            mailService.sendInvoiceReminderMail(to, subject, body);
        } catch (Exception e) {
            log.warn("Could not send payment confirmation for invoiceId={}: {}", invoice.getId(), e.getMessage());
        }
    }
}