package com.villavredestein.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.access.AccessDeniedException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MailServiceTest {

    @Mock
    JavaMailSender mailSender;

    MailService mailServiceEnabled;
    MailService mailServiceDisabled;

    @BeforeEach
    void setUp() {
        mailServiceEnabled = new MailService(mailSender, true,
                "no-reply@villavredestein.local", "admin@villa.nl");
        mailServiceDisabled = new MailService(mailSender, false,
                "no-reply@villavredestein.local", "admin@villa.nl");
    }

    @Test
    void admin_withMailEnabled_sendsMailWithAdminBcc() {
        mailServiceEnabled.sendMailWithRole(
                "ADMIN",
                "student@villa.nl",
                "Test onderwerp",
                "Hallo student"
        );

        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(captor.capture());

        SimpleMailMessage sent = captor.getValue();
        assertThat(sent.getFrom()).isEqualTo("no-reply@villavredestein.local");
        assertThat(sent.getTo()).containsExactly("student@villa.nl");
        assertThat(sent.getBcc()).containsExactly("admin@villa.nl");
        assertThat(sent.getSubject()).isEqualTo("Test onderwerp");
        assertThat(sent.getText()).isEqualTo("Hallo student");
    }

    @Test
    void cleaner_withValidSubject_sendsMailWithCustomBcc() {
        mailServiceEnabled.sendMailWithRole(
                "CLEANER",
                "student@villa.nl",
                "Incident schoonmaak keuken",   // bevat 'incident' → toegestaan
                "Er is iets gebeurd",
                "cleaner-bcc@villa.nl"
        );

        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(captor.capture());

        SimpleMailMessage sent = captor.getValue();
        assertThat(sent.getBcc()).containsExactly("cleaner-bcc@villa.nl");
    }

    @Test
    void cleaner_withInvalidSubject_throwsAccessDenied() {
        // Geen 'incident' of 'schoonmaak' in subject
        assertThrows(AccessDeniedException.class, () ->
                mailServiceEnabled.sendMailWithRole(
                        "CLEANER",
                        "student@villa.nl",
                        "Vraag over huur",
                        "Mag ik later betalen?",
                        null
                )
        );

        verifyNoInteractions(mailSender);
    }

    @Test
    void student_cannotSendMail_throwsAccessDenied() {
        assertThrows(AccessDeniedException.class, () ->
                mailServiceEnabled.sendMailWithRole(
                        "STUDENT",
                        "admin@villa.nl",
                        "Test",
                        "Mag ik mailen?"
                )
        );

        verifyNoInteractions(mailSender);
    }

    @Test
    void unknownRole_throwsAccessDenied() {
        assertThrows(AccessDeniedException.class, () ->
                mailServiceEnabled.sendMailWithRole(
                        "GAST",
                        "iemand@villa.nl",
                        "Test",
                        "Body"
                )
        );

        verifyNoInteractions(mailSender);
    }

    @Test
    void mailDisabled_logsAndDoesNotSend() {
        mailServiceDisabled.sendMailWithRole(
                "ADMIN",
                "student@villa.nl",
                "Onderwerp",
                "Body"
        );

        verifyNoInteractions(mailSender);
    }

    @Test
    void invalidRecipient_logsAndDoesNotSend() {
        mailServiceEnabled.sendMailWithRole(
                "ADMIN",
                "   ",
                "Onderwerp",
                "Body"
        );

        verifyNoInteractions(mailSender);
    }

    @Test
    void mailSenderThrowsException_isCaughtAndDoesNotPropagate() {
        doThrow(new MailSendException("boom"))
                .when(mailSender).send(any(SimpleMailMessage.class));

        mailServiceEnabled.sendMailWithRole(
                "ADMIN",
                "student@villa.nl",
                "Onderwerp",
                "Body"
        );

        verify(mailSender).send(any(SimpleMailMessage.class));
    }
}