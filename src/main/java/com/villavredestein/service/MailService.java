package com.villavredestein.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.Nullable;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import java.util.Locale;

/**
 * {@code MailService} verzorgt de functionaliteit voor het versturen van e-mails
 * binnen de Villa Vredestein-applicatie.
 *
 * Deze service wordt gebruikt voor notificaties, herinneringen en interne
 * communicatie. De toegangsrechten voor het verzenden van e-mails zijn
 * afhankelijk van de gebruikersrol (ADMIN, CLEANER of STUDENT).
 */
@Service
public class MailService {

    private static final Logger log = LoggerFactory.getLogger(MailService.class);

    private final JavaMailSender mailSender;
    private final boolean mailEnabled;
    private final String from;
    private final String bccAdmin;

    /**
     * Categorisatie van uitgaande e-mail.
     */
    public enum MailCategory {
        CLEANING_TASK,
        INCIDENT,
        INVOICE_REMINDER,
        GENERIC
    }

    /**
     * Constructor voor {@link MailService}.
     *
     * @param mailSender  mailcomponent voor het versturen van berichten
     * @param mailEnabled bepaalt of e-mails daadwerkelijk worden verzonden
     * @param from        afzenderadres dat in de e-mail wordt gebruikt
     * @param bccAdmin    optioneel bcc-adres voor administratieve kopieën
     */
    public MailService(
            JavaMailSender mailSender,
            @Value("${app.mail.enabled:true}") boolean mailEnabled,
            @Value("${app.mail.from:no-reply@villavredestein.local}") String from,
            @Value("${app.mail.bcc.admin:}") String bccAdmin
    ) {
        this.mailSender = mailSender;
        this.mailEnabled = mailEnabled;
        this.from = from;
        this.bccAdmin = bccAdmin;
    }

    /**
     * Verstuurt een e-mail namens een gebruiker met een bepaalde rol.
     */
    public void sendMailWithRole(String role, String to, String subject, String body, @Nullable String bcc) {
        String safeTo = maskEmail(to);
        String normalizedRole = normalizeRole(role);

        if (subject == null || subject.isBlank()) {
            throw new IllegalArgumentException("Subject is verplicht");
        }
        if (body == null || body.isBlank()) {
            throw new IllegalArgumentException("Body is verplicht");
        }

        MailCategory category = MailCategory.GENERIC;
        String s = subject.trim().toLowerCase(Locale.ROOT);

        // Basic categorization (also useful for logging / filtering later)
        if (s.contains("factuur") || s.contains("huur") || s.contains("invoice") || s.contains("herinner")) {
            category = MailCategory.INVOICE_REMINDER;
        }

        if ("CLEANER".equals(normalizedRole)) {
            if (s.contains("incident")) {
                category = MailCategory.INCIDENT;
            } else if (s.contains("schoonmaak") || s.contains("cleaning")) {
                category = MailCategory.CLEANING_TASK;
            }
        }

        sendInternal(normalizedRole, category, to, subject, body, bcc, safeTo);
    }

    /**
     * Overload van {@link #sendMailWithRole(String, String, String, String, String)}
     * zonder BCC.
     *
     * @param role    gebruikersrol van de afzender
     * @param to      e-mailadres van de ontvanger
     * @param subject onderwerp van de e-mail
     * @param body    inhoud van het bericht
     */
    public void sendMailWithRole(String role, String to, String subject, String body) {
        sendMailWithRole(role, to, subject, body, null);
    }

    /**
     * CLEANER: verstuurt een incidentmail.
     */
    public void sendCleanerIncidentMail(String to, String body) {
        if (body == null || body.isBlank()) {
            throw new IllegalArgumentException("Body is verplicht");
        }
        String safeTo = maskEmail(to);
        sendInternal("CLEANER", MailCategory.INCIDENT, to, "Incidentmelding", body, null, safeTo);
    }

    /**
     * CLEANER: verstuurt een mail over een schoonmaaktaak.
     */
    public void sendCleanerCleaningTaskMail(String to, String body) {
        if (body == null || body.isBlank()) {
            throw new IllegalArgumentException("Body is verplicht");
        }
        String safeTo = maskEmail(to);
        sendInternal("CLEANER", MailCategory.CLEANING_TASK, to, "Schoonmaaktaak", body, null, safeTo);
    }

    /**
     * SYSTEEM: verstuurt een factuurherinnering (huur) naar een student.
     *
     * <p>Wordt gebruikt door scheduled jobs. Dit valt onder ADMIN-achtig systeemgedrag
     * en gebruikt daarom de interne afzender (from) en optioneel BCC naar admin.</p>
     */
    public void sendInvoiceReminderMail(String to, String subject, String body) {
        String safeTo = maskEmail(to);

        if (subject == null || subject.isBlank()) {
            throw new IllegalArgumentException("Subject is verplicht");
        }
        if (body == null || body.isBlank()) {
            throw new IllegalArgumentException("Body is verplicht");
        }

        // Jobs draaien zonder 'user role', maar functioneel is dit systeem/ADMIN-mail.
        sendInternal("ADMIN", MailCategory.INVOICE_REMINDER, to, subject, body, null, safeTo);
    }

    private void sendInternal(String normalizedRole,
                              MailCategory category,
                              String to,
                              String subject,
                              String body,
                              @Nullable String bcc,
                              String safeTo) {

        switch (normalizedRole) {
            case "ADMIN" -> log.info("ADMIN verstuurt mail (cat={}, to={})", category, safeTo);
            case "CLEANER" -> {
                if (category != MailCategory.INCIDENT && category != MailCategory.CLEANING_TASK) {
                    throw new AccessDeniedException("CLEANER mag alleen mails sturen over schoonmaaktaken of incidenten");
                }
                log.info("🧹 CLEANER verstuurt mail (cat={}, to={})", category, safeTo);
            }
            case "STUDENT" -> throw new AccessDeniedException("STUDENT mag geen e-mails verzenden");
            default -> throw new AccessDeniedException("Onbekende rol");
        }

        if (!mailEnabled) {
            log.warn("📧 [MAIL UITGESCHAKELD] cat={} | to={} | subject={} | body={}...", category, safeTo, subject, body.substring(0, Math.min(body.length(), 200)));
            return;
        }

        if (to == null || to.isBlank()) {
            log.error("Ongeldig e-mailadres: {}", safeTo);
            return;
        }

        try {
            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setFrom(from);
            msg.setTo(to);

            if (bcc != null && !bcc.isBlank()) {
                msg.setBcc(bcc);
            } else if (bccAdmin != null && !bccAdmin.isBlank()) {
                msg.setBcc(bccAdmin);
            }

            msg.setSubject(subject);
            msg.setText(body);
            mailSender.send(msg);

            log.info("E-mail succesvol verzonden (role={}, cat={}, to={}, subject={})", normalizedRole, category, safeTo, subject);

        } catch (MailException e) {
            log.error("Fout bij verzenden van e-mail (role={}, cat={}, to={}, subject={}): {}", normalizedRole, category, safeTo, subject, e.getMessage());
        }
    }

    private String normalizeRole(String role) {
        if (role == null) {
            throw new AccessDeniedException("Rol ontbreekt");
        }
        return role.trim().toUpperCase(Locale.ROOT);
    }

    private String maskEmail(String email) {
        if (email == null || email.isBlank()) return "(no-email)";
        int at = email.indexOf('@');
        if (at <= 1) return "***";
        return email.charAt(0) + "***" + email.substring(at);
    }
}