package com.villavredestein.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.access.AccessDeniedException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class MailServiceTest {

    private static final String FROM_EMAIL    = "no-reply@villavredestein.com";
    private static final String ADMIN_BCC     = "audit@villavredestein.com";
    private static final String STUDENT_TO    = "student@villavredestein.com";

    private JavaMailSender mailSender;

    @BeforeEach
    void setUp() {
        mailSender = mock(JavaMailSender.class);
    }


    @Test
    void defaultConstructor_createsDisabledService() {
        MailService service = new MailService();

        assertDoesNotThrow(() -> service.sendMailWithRole("ADMIN", STUDENT_TO, "Test", "Body"));
        verify(mailSender, never()).send(any(SimpleMailMessage.class));
    }


    @Test
    void admin_canSend_mailIsSent_withAdminBcc() {
        MailService service = new MailService(mailSender, true, FROM_EMAIL, ADMIN_BCC);

        service.sendMailWithRole("ADMIN", STUDENT_TO, "Test", "Body");

        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(captor.capture());
        SimpleMailMessage msg = captor.getValue();
        assertThat(msg.getFrom()).isEqualTo(FROM_EMAIL);
        assertThat(msg.getTo()).containsExactly(STUDENT_TO);
        assertThat(msg.getSubject()).isEqualTo("Test");
        assertThat(msg.getText()).isEqualTo("Body");
    }

    @Test
    void sendMailWithRole_withExplicitBcc_usesThatBcc() {
        MailService service = new MailService(mailSender, true, FROM_EMAIL, "");

        service.sendMailWithRole("ADMIN", STUDENT_TO, "Test", "Body", "bcc@villavredestein.com");

        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(captor.capture());
        assertThat(captor.getValue().getBcc()).containsExactly("bcc@villavredestein.com");
    }

    @Test
    void sendMailWithRole_withRolePrefix_stripsPrefix() {
        MailService service = new MailService(mailSender, true, FROM_EMAIL, "");

        service.sendMailWithRole("ROLE_ADMIN", STUDENT_TO, "Test", "Body");

        verify(mailSender).send(any(SimpleMailMessage.class));
    }


    @Test
    void sendMailWithRole_withNullRole_throwsAccessDenied() {
        MailService service = new MailService(mailSender, true, FROM_EMAIL, ADMIN_BCC);

        assertThrows(AccessDeniedException.class,
                () -> service.sendMailWithRole(null, STUDENT_TO, "Test", "Body"));
        verify(mailSender, never()).send(any(SimpleMailMessage.class));
    }

    @Test
    void sendMailWithRole_withUnknownRole_throwsAccessDenied() {
        MailService service = new MailService(mailSender, true, FROM_EMAIL, ADMIN_BCC);

        assertThrows(AccessDeniedException.class,
                () -> service.sendMailWithRole("MANAGER", STUDENT_TO, "Test", "Body"));
        verify(mailSender, never()).send(any(SimpleMailMessage.class));
    }

    @Test
    void sendMailWithRole_withNullTo_throwsIllegalArgumentException() {
        MailService service = new MailService(mailSender, true, FROM_EMAIL, ADMIN_BCC);

        assertThrows(IllegalArgumentException.class,
                () -> service.sendMailWithRole("ADMIN", null, "Test", "Body"));
    }

    @Test
    void sendMailWithRole_withInvalidEmail_throwsIllegalArgumentException() {
        MailService service = new MailService(mailSender, true, FROM_EMAIL, ADMIN_BCC);

        assertThrows(IllegalArgumentException.class,
                () -> service.sendMailWithRole("ADMIN", "geen-email", "Test", "Body"));
    }

    @Test
    void sendMailWithRole_withNullSubject_throwsIllegalArgumentException() {
        MailService service = new MailService(mailSender, true, FROM_EMAIL, ADMIN_BCC);

        assertThrows(IllegalArgumentException.class,
                () -> service.sendMailWithRole("ADMIN", STUDENT_TO, null, "Body"));
    }

    @Test
    void sendMailWithRole_withNullBody_throwsIllegalArgumentException() {
        MailService service = new MailService(mailSender, true, FROM_EMAIL, ADMIN_BCC);

        assertThrows(IllegalArgumentException.class,
                () -> service.sendMailWithRole("ADMIN", STUDENT_TO, "Test", null));
    }


    @Test
    void cleaner_canSend_onlyForAllowedSubjects() {
        MailService service = new MailService(mailSender, true, FROM_EMAIL, ADMIN_BCC);

        assertDoesNotThrow(() -> service.sendMailWithRole("CLEANER", STUDENT_TO, "Incident: lekkage", "Body"));
        verify(mailSender, times(1)).send(any(SimpleMailMessage.class));

        assertThrows(AccessDeniedException.class,
                () -> service.sendMailWithRole("CLEANER", STUDENT_TO, "Huurbetaling", "Body"));
    }

    @Test
    void cleaner_withCleaningSubject_canSend() {
        MailService service = new MailService(mailSender, true, FROM_EMAIL, ADMIN_BCC);

        assertDoesNotThrow(() -> service.sendMailWithRole("CLEANER", STUDENT_TO, "cleaning taak vandaag", "Body"));
        verify(mailSender).send(any(SimpleMailMessage.class));
    }

    @Test
    void student_cannotSend_throwsAccessDenied() {
        MailService service = new MailService(mailSender, true, FROM_EMAIL, ADMIN_BCC);

        assertThrows(AccessDeniedException.class,
                () -> service.sendMailWithRole("STUDENT", STUDENT_TO, "Test", "Body"));
        verify(mailSender, never()).send(any(SimpleMailMessage.class));
    }


    @Test
    void mailDisabled_doesNotSend() {
        MailService service = new MailService(mailSender, false, FROM_EMAIL, ADMIN_BCC);

        service.sendMailWithRole("ADMIN", STUDENT_TO, "Test", "Body");

        verify(mailSender, never()).send(any(SimpleMailMessage.class));
    }

    @Test
    void noMailSenderBean_doesNotThrow() {
        MailService service = new MailService(null, true, FROM_EMAIL, ADMIN_BCC);

        assertDoesNotThrow(() -> service.sendMailWithRole("ADMIN", STUDENT_TO, "Test", "Body"));
    }

    @Test
    void sendMailWithRole_mailExceptionIsCaught() {
        MailService service = new MailService(mailSender, true, FROM_EMAIL, ADMIN_BCC);
        doThrow(new MailSendException("simulated failure")).when(mailSender).send(any(SimpleMailMessage.class));

        assertDoesNotThrow(() -> service.sendMailWithRole("ADMIN", STUDENT_TO, "Test", "Body"));
    }


    @Test
    void sendCleanerIncidentMail_happyPath_sends() {
        MailService service = new MailService(mailSender, true, FROM_EMAIL, ADMIN_BCC);

        service.sendCleanerIncidentMail(STUDENT_TO, "Waterlekkage gemeld");

        verify(mailSender).send(any(SimpleMailMessage.class));
    }

    @Test
    void sendCleanerIncidentMail_withNullBody_throwsIllegalArgumentException() {
        MailService service = new MailService(mailSender, true, FROM_EMAIL, ADMIN_BCC);

        assertThrows(IllegalArgumentException.class,
                () -> service.sendCleanerIncidentMail(STUDENT_TO, null));
    }


    @Test
    void sendCleanerCleaningTaskMail_happyPath_sends() {
        MailService service = new MailService(mailSender, true, FROM_EMAIL, ADMIN_BCC);

        service.sendCleanerCleaningTaskMail(STUDENT_TO, "Kamer 3 schoonmaken");

        verify(mailSender).send(any(SimpleMailMessage.class));
    }

    @Test
    void sendCleanerCleaningTaskMail_withNullBody_throwsIllegalArgumentException() {
        MailService service = new MailService(mailSender, true, FROM_EMAIL, ADMIN_BCC);

        assertThrows(IllegalArgumentException.class,
                () -> service.sendCleanerCleaningTaskMail(STUDENT_TO, null));
    }


    @Test
    void sendInvoiceReminderMail_happyPath_sends() {
        MailService service = new MailService(mailSender, true, FROM_EMAIL, ADMIN_BCC);

        service.sendInvoiceReminderMail(STUDENT_TO, "Herinnering factuur", "Uw factuur is bijna verlopen.");

        verify(mailSender).send(any(SimpleMailMessage.class));
    }

    @Test
    void sendInvoiceReminderMail_withNullSubject_throwsIllegalArgumentException() {
        MailService service = new MailService(mailSender, true, FROM_EMAIL, ADMIN_BCC);

        assertThrows(IllegalArgumentException.class,
                () -> service.sendInvoiceReminderMail(STUDENT_TO, null, "Body"));
    }

    @Test
    void sendInvoiceReminderMail_withNullBody_throwsIllegalArgumentException() {
        MailService service = new MailService(mailSender, true, FROM_EMAIL, ADMIN_BCC);

        assertThrows(IllegalArgumentException.class,
                () -> service.sendInvoiceReminderMail(STUDENT_TO, "Herinnering", null));
    }
}