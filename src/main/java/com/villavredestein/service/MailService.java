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

    @Nullable
    private final JavaMailSender mailSender;
    private final boolean mailEnabled;
    private final String from;
    private final String bccAdmin;

    // =====================================================================
    // # Categories
    // =====================================================================
    public enum MailCategory {
        CLEANING_TASK,
        INCIDENT,
        INVOICE_REMINDER,
        GENERIC
    }

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

    // =====================================================================
    // # Test constructor
    // =====================================================================
    protected MailService() {
        this.mailSender = null;
        this.mailEnabled = false;
        this.from = "no-reply@villavredestein.local";
        this.bccAdmin = "";
    }

    // =====================================================================
    // # Public API
    // =====================================================================
    public void sendMailWithRole(String role, String to, String subject, String body, @Nullable String bcc) {
        String normalizedRole = normalizeRole(role);
        requireValidRecipient(to);

        if (subject == null || subject.isBlank()) {
            throw new IllegalArgumentException("Subject is required");
        }
        if (body == null || body.isBlank()) {
            throw new IllegalArgumentException("Body is required");
        }

        MailCategory category = categorize(normalizedRole, subject);
        String safeTo = maskEmail(to);

        sendInternal(normalizedRole, category, to, subject, body, bcc, safeTo);
    }

    public void sendMailWithRole(String role, String to, String subject, String body) {
        sendMailWithRole(role, to, subject, body, null);
    }

    public void sendCleanerIncidentMail(String to, String body) {
        requireValidRecipient(to);
        if (body == null || body.isBlank()) {
            throw new IllegalArgumentException("Body is required");
        }
        sendInternal("CLEANER", MailCategory.INCIDENT, to, "Incident report", body, null, maskEmail(to));
    }

    public void sendCleanerCleaningTaskMail(String to, String body) {
        requireValidRecipient(to);
        if (body == null || body.isBlank()) {
            throw new IllegalArgumentException("Body is required");
        }
        sendInternal("CLEANER", MailCategory.CLEANING_TASK, to, "Cleaning task", body, null, maskEmail(to));
    }

    public void sendInvoiceReminderMail(String to, String subject, String body) {
        requireValidRecipient(to);

        if (subject == null || subject.isBlank()) {
            throw new IllegalArgumentException("Subject is required");
        }
        if (body == null || body.isBlank()) {
            throw new IllegalArgumentException("Body is required");
        }

        sendInternal("ADMIN", MailCategory.INVOICE_REMINDER, to, subject, body, null, maskEmail(to));
    }

    // =====================================================================
    // # Internal send
    // =====================================================================
    private void sendInternal(
            String normalizedRole,
            MailCategory category,
            String to,
            String subject,
            String body,
            @Nullable String bcc,
            String safeTo
    ) {
        switch (normalizedRole) {
            case "ADMIN" -> log.info("ADMIN sending mail (cat={}, to={})", category, safeTo);
            case "CLEANER" -> {
                if (category != MailCategory.INCIDENT && category != MailCategory.CLEANING_TASK) {
                    throw new AccessDeniedException("CLEANER may only send mail about incidents or cleaning tasks");
                }
                log.info("CLEANER sending mail (cat={}, to={})", category, safeTo);
            }
            case "STUDENT" -> throw new AccessDeniedException("STUDENT is not allowed to send emails");
            default -> throw new AccessDeniedException("Unknown role");
        }

        if (!mailEnabled) {
            log.warn("[MAIL DISABLED] cat={} | to={} | subject={} | body={}...", category, safeTo, subject, preview(body));
            return;
        }

        if (mailSender == null) {
            log.warn("[MAIL ENABLED] but no mailSender configured (cat={}, to={})", category, safeTo);
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

            log.info("Mail sent successfully (role={}, cat={}, to={}, subject={})", normalizedRole, category, safeTo, subject);

        } catch (MailException e) {
            log.error("Mail send failed (role={}, cat={}, to={}, subject={}): {}", normalizedRole, category, safeTo, subject, e.getMessage());
        }
    }

    // =====================================================================
    // # Categorization
    // =====================================================================
    private MailCategory categorize(String normalizedRole, String subject) {
        String s = subject.trim().toLowerCase(Locale.ROOT);

        MailCategory category = MailCategory.GENERIC;

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

        return category;
    }

    // =====================================================================
    // # Validation helpers
    // =====================================================================
    private String normalizeRole(String role) {
        if (role == null) {
            throw new AccessDeniedException("Role is missing");
        }
        String normalized = role.trim().toUpperCase(Locale.ROOT);
        if (normalized.startsWith("ROLE_")) {
            normalized = normalized.substring("ROLE_".length());
        }
        return normalized;
    }

    private void requireValidRecipient(String to) {
        if (to == null || to.isBlank()) {
            throw new IllegalArgumentException("Recipient email (to) is required");
        }
        String trimmed = to.trim();
        int at = trimmed.indexOf('@');
        int dot = trimmed.lastIndexOf('.');
        if (at < 1 || dot < at + 2 || dot == trimmed.length() - 1) {
            throw new IllegalArgumentException("Invalid email address: " + maskEmail(trimmed));
        }
    }

    private String maskEmail(String email) {
        if (email == null || email.isBlank()) return "(no-email)";
        int at = email.indexOf('@');
        if (at <= 1) return "***";
        return email.charAt(0) + "***" + email.substring(at);
    }

    private String preview(String text) {
        if (text == null) return "";
        return text.substring(0, Math.min(text.length(), 200));
    }
}