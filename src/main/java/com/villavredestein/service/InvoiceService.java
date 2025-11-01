package com.villavredestein.service;

import com.villavredestein.model.Invoice;
import com.villavredestein.repository.InvoiceRepository;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final JavaMailSender mailSender;

    public InvoiceService(InvoiceRepository invoiceRepository, JavaMailSender mailSender) {
        this.invoiceRepository = invoiceRepository;
        this.mailSender = mailSender;
    }

    public List<Invoice> getAllInvoices() {
        return invoiceRepository.findAll();
    }

    public List<Invoice> getInvoicesByStudentEmail(String email) {
        return invoiceRepository.findByStudentEmail(email);
    }

    public Invoice updateStatus(Long id, String newStatus) {
        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Invoice not found"));
        invoice.setStatus(newStatus);
        return invoiceRepository.save(invoice);
    }

    public Invoice saveInvoice(Invoice invoice) {
        return invoiceRepository.save(invoice);
    }

    public void deleteInvoice(Long id) {
        invoiceRepository.deleteById(id);
    }

    @Scheduled(cron = "0 0 0 1 * ?")
    public void sendPaymentReminders() {
        List<Invoice> all = invoiceRepository.findAll();
        LocalDate today = LocalDate.now();

        for (Invoice invoice : all) {
            if (!invoice.getReminderSent()
                    && invoice.getDueDate().isBefore(today)
                    && invoice.getStudent() != null) {

                String to = invoice.getStudent().getEmail();
                String subject = "Herinnering: openstaande betaling " + invoice.getTitle();
                String body = String.format(
                        "Beste %s,\n\nEr staat nog een betaling open voor %s.\n" +
                                "De vervaldatum was %s. Graag zo spoedig mogelijk overmaken.\n\n" +
                                "Met vriendelijke groet,\nBeheer Villa Vredestein",
                        invoice.getStudent().getUsername(),
                        invoice.getTitle(),
                        invoice.getDueDate()
                );

                try {
                    SimpleMailMessage message = new SimpleMailMessage();
                    message.setTo(to);
                    message.setSubject(subject);
                    message.setText(body);
                    mailSender.send(message);

                    invoice.setReminderSent(true);
                    invoiceRepository.save(invoice);
                    System.out.println("✅ Reminder sent to: " + to);

                } catch (Exception e) {
                    System.out.println("⚠️ Kon e-mail niet verzenden naar " + to + ": " + e.getMessage());
                }
            }
        }
    }
}