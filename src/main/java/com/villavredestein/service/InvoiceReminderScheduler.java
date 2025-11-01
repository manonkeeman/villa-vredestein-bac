package com.villavredestein.service;

import com.villavredestein.model.Invoice;
import com.villavredestein.model.User;
import com.villavredestein.repository.InvoiceRepository;
import com.villavredestein.repository.UserRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;

@Component
public class InvoiceReminderScheduler {

    private final InvoiceRepository invoiceRepository;
    private final UserRepository userRepository;
    private final MailService mailService;

    public InvoiceReminderScheduler(InvoiceRepository invoiceRepository,
                                    UserRepository userRepository,
                                    MailService mailService) {
        this.invoiceRepository = invoiceRepository;
        this.userRepository = userRepository;
        this.mailService = mailService;
    }

    /**
     * 🕘 Wordt elke dag om 09:00 uitgevoerd
     * - Stuurt herinneringen voor facturen die over 7 dagen, vandaag of 7 dagen geleden vervallen.
     */
    @Scheduled(cron = "0 0 9 * * ?")
    public void sendReminders() {
        LocalDate today = LocalDate.now();

        LocalDate in7Days = today.plusDays(7);
        LocalDate sevenDaysAgo = today.minusDays(7);

        // Openstaande facturen die binnenkort, vandaag of recent zijn vervallen
        List<Invoice> dueSoon = invoiceRepository.findByStatusAndDueDateBetween("OPEN", in7Days, in7Days);
        List<Invoice> onDue = invoiceRepository.findByStatusAndDueDateBetween("OPEN", today, today);
        List<Invoice> overdue = invoiceRepository.findByStatusAndDueDateBetween("OPEN", sevenDaysAgo, sevenDaysAgo);

        dueSoon.forEach(this::sendDueSoon);
        onDue.forEach(this::sendOnDue);
        overdue.forEach(this::sendOverdue);
    }

    private void sendDueSoon(Invoice invoice) {
        User student = invoice.getStudent();
        if (student == null || student.getEmail() == null) return;

        String monthName = getMonthName(invoice.getMonth());
        String subject = "Herinnering: Huur " + monthName + " " + invoice.getYear() + " vervalt op " + invoice.getDueDate();
        String body = InvoiceReminderFormatter.bodyDueSoon(invoice, student.getUsername());

        mailService.send(student.getEmail(), subject, body);
        System.out.println("📧 [DUE SOON] Herinnering gestuurd naar " + student.getEmail());
    }

    private void sendOnDue(Invoice invoice) {
        User student = invoice.getStudent();
        if (student == null || student.getEmail() == null) return;

        String monthName = getMonthName(invoice.getMonth());
        String subject = "Vandaag vervalt je huur " + monthName + " " + invoice.getYear();
        String body = InvoiceReminderFormatter.bodyDueSoon(invoice, student.getUsername());

        mailService.send(student.getEmail(), subject, body);
        System.out.println("📧 [DUE TODAY] Herinnering gestuurd naar " + student.getEmail());
    }

    private void sendOverdue(Invoice invoice) {
        User student = invoice.getStudent();
        if (student == null || student.getEmail() == null) return;

        String monthName = getMonthName(invoice.getMonth());
        String subject = "Achterstand: Huur " + monthName + " " + invoice.getYear() + " is nog niet betaald";
        String body = InvoiceReminderFormatter.bodyOverdue(invoice, student.getUsername());

        mailService.send(student.getEmail(), subject, body);
        System.out.println("📧 [OVERDUE] Herinnering gestuurd naar " + student.getEmail());
    }

    /** Hulpmethode om maandnummer (1–12) om te zetten naar maandnaam */
    private String getMonthName(int month) {
        try {
            return Month.of(month).getDisplayName(TextStyle.FULL, new Locale("nl", "NL"));
        } catch (Exception e) {
            return "Onbekende maand";
        }
    }
}