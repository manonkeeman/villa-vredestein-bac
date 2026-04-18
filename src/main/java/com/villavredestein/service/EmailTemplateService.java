package com.villavredestein.service;

import com.villavredestein.model.EmailTemplate;
import com.villavredestein.model.EmailTemplate.TemplateType;
import com.villavredestein.repository.EmailTemplateRepository;
import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Transactional
public class EmailTemplateService {

    private static final Logger log = LoggerFactory.getLogger(EmailTemplateService.class);

    private final EmailTemplateRepository repo;

    public EmailTemplateService(EmailTemplateRepository repo) {
        this.repo = repo;
    }

    // =====================================================================
    // # Seed defaults on startup
    // =====================================================================

    @PostConstruct
    public void seedDefaults() {
        seed(TemplateType.PAYMENT_NEW,
                "Factuur {{maand}} – Villa Vredestein",
                """
                Beste {{naam}},

                Je factuur voor {{maand}} staat klaar. Het te betalen bedrag is {{bedrag}}.

                Betaal veilig via iDEAL:
                {{betaalLink}}

                De vervaldatum is {{vervaldatum}}. Betaal op tijd om extra kosten te voorkomen.

                Met vriendelijke groet,
                Villa Vredestein
                """);

        seed(TemplateType.PAYMENT_REMINDER_1,
                "Herinnering: factuur {{maand}} nog niet betaald",
                """
                Beste {{naam}},

                We hebben nog geen betaling ontvangen voor {{maand}} ({{bedrag}}).

                Je kunt alsnog betalen via iDEAL:
                {{betaalLink}}

                Vervaldatum: {{vervaldatum}}.

                Met vriendelijke groet,
                Villa Vredestein
                """);

        seed(TemplateType.PAYMENT_REMINDER_2,
                "Tweede herinnering: factuur {{maand}} – actie vereist",
                """
                Beste {{naam}},

                Dit is de tweede herinnering voor je openstaande factuur van {{maand}} ({{bedrag}}).

                Betaal zo spoedig mogelijk via iDEAL:
                {{betaalLink}}

                Neem contact op met de beheerder als je vragen hebt.

                Met vriendelijke groet,
                Villa Vredestein
                """);
    }

    private void seed(TemplateType type, String subject, String body) {
        if (!repo.existsByType(type)) {
            repo.save(new EmailTemplate(type, subject, body));
            log.info("Seeded default email template for type={}", type);
        }
    }

    // =====================================================================
    // # Read
    // =====================================================================

    public List<EmailTemplate> getAll() {
        return repo.findAll();
    }

    public EmailTemplate getByType(TemplateType type) {
        return repo.findByType(type)
                .orElseThrow(() -> new IllegalStateException("Email template niet gevonden voor type: " + type));
    }

    // =====================================================================
    // # Update
    // =====================================================================

    public EmailTemplate update(TemplateType type, String subject, String body) {
        EmailTemplate template = getByType(type);
        template.setSubject(subject);
        template.setBody(body);
        log.info("Email template updated for type={}", type);
        return repo.save(template);
    }
}