package com.villavredestein.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.access.AccessDeniedException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class MailServiceTest {

    private JavaMailSender mailSender;

    @BeforeEach
    void setUp() {
        mailSender = mock(JavaMailSender.class);
    }

    @Test
    void admin_canSend_mailIsSent_withAdminBcc() {
        MailService service = new MailService(
                mailSender,
                true,
                "no-reply@villavredestein.nl",
                "audit@villavredestein.nl"
        );

        service.sendMailWithRole("ADMIN", "student@gmail.com", "Test", "Body");

        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(captor.capture());

        SimpleMailMessage msg = captor.getValue();
        assertThat(msg.getFrom()).isEqualTo("no-reply@villavredestein.nl");
        assertThat(msg.getTo()).containsExactly("student@gmail.com");
        assertThat(msg.getSubject()).isEqualTo("Test");
        assertThat(msg.getText()).isEqualTo("Body");
        assertThat(msg.getBcc()).containsExactly("audit@villavredestein.nl");
    }

    @Test
    void cleaner_canSend_onlyForAllowedSubjects() {
        MailService service = new MailService(
                mailSender,
                true,
                "no-reply@villavredestein.nl",
                "audit@villavredestein.nl"
        );

        assertDoesNotThrow(() -> service.sendMailWithRole("CLEANER", "admin@gmail.com", "Incident: lekkage", "Body"));
        verify(mailSender, times(1)).send(any(SimpleMailMessage.class));

        assertThrows(AccessDeniedException.class,
                () -> service.sendMailWithRole("CLEANER", "admin@gmail.com", "Huurbetaling", "Body"));
    }

    @Test
    void student_cannotSend_throwsAccessDenied() {
        MailService service = new MailService(
                mailSender,
                true,
                "no-reply@villavredestein.nl",
                "audit@villavredestein.nl"
        );

        assertThrows(AccessDeniedException.class,
                () -> service.sendMailWithRole("STUDENT", "admin@gmail.com", "Test", "Body"));

        verify(mailSender, never()).send(any(SimpleMailMessage.class));
    }

    @Test
    void mailDisabled_doesNotSend() {
        MailService service = new MailService(
                mailSender,
                false,
                "no-reply@villavredestein.nl",
                "audit@villavredestein.nl"
        );

        service.sendMailWithRole("ADMIN", "student@gmail.com", "Test", "Body");

        verify(mailSender, never()).send(any(SimpleMailMessage.class));
    }

    @Test
    void noMailSenderBean_available_doesNotThrow() {
        MailService service = new MailService(
                null,
                true,
                "no-reply@villavredestein.nl",
                "audit@villavredestein.nl"
        );

        assertDoesNotThrow(() -> service.sendMailWithRole("ADMIN", "student@gmail.com", "Test", "Body"));
    }
}