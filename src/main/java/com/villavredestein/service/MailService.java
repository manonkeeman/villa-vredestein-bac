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
     */
    public void sendInvoiceReminderMail(String to, String subject, String body) {
        String safeTo = maskEmail(to);

        if (subject == null || subject.isBlank()) {
            throw new IllegalArgumentException("Subject is verplicht");
        }
        if (body == null || body.isBlank()) {
            throw new IllegalArgumentException("Body is verplicht");
        }

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