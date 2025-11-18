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

/**
 * {@code MailService} verzorgt de functionaliteit voor het versturen van e-mails
 * binnen de Villa Vredestein-applicatie.
 *
 * <p>Deze service wordt gebruikt voor notificaties, herinneringen en interne
 * communicatie. De toegangsrechten voor het verzenden van e-mails zijn
 * afhankelijk van de gebruikersrol (ADMIN, CLEANER of STUDENT).</p>
 *
 * <ul>
 *   <li><b>ADMIN</b> mag alle e-mails verzenden.</li>
 *   <li><b>CLEANER</b> mag uitsluitend e-mails versturen die betrekking hebben
 *       op schoonmaaktaken of incidenten.</li>
 *   <li><b>STUDENT</b> mag geen e-mails versturen.</li>
 * </ul>
 *
 * <p>De klasse maakt gebruik van {@link JavaMailSender} voor het verzenden van
 * e-mails en logt alle gebeurtenissen voor transparantie en foutdiagnose.</p>
 *
 * <p>Mailfunctionaliteit kan worden uitgeschakeld via de configuratieproperty
 * {@code app.mail.enabled} in <code>application.yml</code>, bijvoorbeeld voor
 * test- of ontwikkelomgevingen.</p>
 *
 */
@Service
public class MailService {

    private static final Logger log = LoggerFactory.getLogger(MailService.class);

    private final JavaMailSender mailSender;
    private final boolean mailEnabled;
    private final String from;
    private final String bccAdmin;

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
     *
     * <p>Controleert eerst de toegangsrechten op basis van de rol:</p>
     * <ul>
     *   <li>ADMIN – mag alle e-mails verzenden.</li>
     *   <li>CLEANER – mag alleen e-mails verzenden die betrekking hebben op
     *       schoonmaaktaken of incidenten.</li>
     *   <li>STUDENT – heeft geen verzendrechten.</li>
     * </ul>
     *
     * <p>Als mailfunctionaliteit is uitgeschakeld, wordt de mailinhoud gelogd
     * maar niet verzonden.</p>
     *
     * @param role    gebruikersrol van de afzender
     * @param to      e-mailadres van de ontvanger
     * @param subject onderwerpregel van de e-mail
     * @param body    inhoud van het bericht
     * @param bcc     optioneel bcc-adres (kan {@code null} zijn)
     * @throws AccessDeniedException als de rol geen toestemming heeft om te verzenden
     */
    public void sendMailWithRole(String role, String to, String subject, String body, @Nullable String bcc) {
        switch (role.toUpperCase()) {
            case "ADMIN" -> log.info("ADMIN verstuurt mail aan {}", to);
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
            log.error("Ongeldig e-mailadres: '{}'", to);
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

            log.info("E-mail succesvol verzonden door {} naar {} met onderwerp '{}'", role, to, subject);

        } catch (MailException e) {
            log.error("Fout bij verzenden van e-mail ({}) door {}: {}", subject, role, e.getMessage());
        }
    }

    /**
     * Overload van {@link #sendMailWithRole(String, String, String, String, String)}
     * zonder BCC-parameter.
     *
     * @param role    gebruikersrol van de afzender
     * @param to      e-mailadres van de ontvanger
     * @param subject onderwerp van de e-mail
     * @param body    inhoud van het bericht
     */
    public void sendMailWithRole(String role, String to, String subject, String body) {
        sendMailWithRole(role, to, subject, body, null);
    }
}