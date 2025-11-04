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

@Service
public class MailService {

    private static final Logger log = LoggerFactory.getLogger(MailService.class);

    private final JavaMailSender mailSender;
    private final boolean mailEnabled;
    private final String from;
    private final String bccAdmin;

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

    public void sendMailWithRole(String role, String to, String subject, String body, @Nullable String bcc) {
        // ✅ Rolgebaseerde controle
        switch (role.toUpperCase()) {
            case "ADMIN" -> log.info("✅ ADMIN verstuurt mail aan {}", to);
            case "CLEANER" -> {
                if (!subject.toLowerCase().contains("incident") && !subject.toLowerCase().contains("schoonmaak")) {
                    throw new AccessDeniedException("CLEANER mag alleen mails sturen over schoonmaaktaken of incidenten");
                }
                log.info("🧹 CLEANER verstuurt schoonmaakmail aan {}", to);
            }
            case "STUDENT" -> throw new AccessDeniedException("STUDENT mag geen e-mails verzenden");
            default -> throw new AccessDeniedException("Onbekende rol: " + role);
        }

        if (!mailEnabled) {
            log.warn("📧 [MAIL UITGESCHAKELD] To: {} | Subject: {}\n{}", to, subject, body);
            return;
        }

        if (to == null || to.isBlank()) {
            log.error("❌ Ongeldig e-mailadres: '{}'", to);
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

            log.info("📨 E-mail succesvol verzonden door {} naar {} met onderwerp '{}'", role, to, subject);

        } catch (MailException e) {
            log.error("⚠️ Fout bij verzenden van e-mail ({}) door {}: {}", subject, role, e.getMessage());
        }
    }

    public void sendMailWithRole(String role, String to, String subject, String body) {
        sendMailWithRole(role, to, subject, body, null);
    }
}