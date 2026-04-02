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

    // =====================================================================
    // # Constructors
    // =====================================================================

    @Test
    void defaultConstructor_createsDisabledService() {
        // Arrange
        MailService service = new MailService();

        // Act & Assert
        assertDoesNotThrow(() -> service.sendMailWithRole("ADMIN", STUDENT_TO, "Test", "Body"));
        verify(mailSender, never()).send(any(SimpleMailMessage.class));
    }

    // =====================================================================
    // # sendMailWithRole – happy paths
    // =====================================================================

    @Test
    void admin_canSend_mailIsSent_withAdminBcc() {
        // Arrange
        MailService service = new MailService(mailSender, true, FROM_EMAIL, ADMIN_BCC);

        // Act
        service.sendMailWithRole("ADMIN", STUDENT_TO, "Test", "Body");

        // Assert
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
        // Arrange
        MailService service = new MailService(mailSender, true, FROM_EMAIL, "");

        // Act
        service.sendMailWithRole("ADMIN", STUDENT_TO, "Test", "Body", "bcc@villavredestein.com");

        // Assert
        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(captor.capture());
        assertThat(captor.getValue().getBcc()).containsExactly("bcc@villavredestein.com");
    }

    @Test
    void sendMailWithRole_withRolePrefix_stripsPrefix() {
        // Arrange
        MailService service = new MailService(mailSender, true, FROM_EMAIL, "");

        // Act
        service.sendMailWithRole("ROLE_ADMIN", STUDENT_TO, "Test", "Body");

        // Assert
        verify(mailSender).send(any(SimpleMailMessage.class));
    }

    // =====================================================================
    // # sendMailWithRole – validatie fouten
    // =====================================================================

    @Test
    void sendMailWithRole_withNullRole_throwsAccessDenied() {
        // Arrange
        MailService service = new MailService(mailSender, true, FROM_EMAIL, ADMIN_BCC);

        // Act & Assert
        assertThrows(AccessDeniedException.class,
                () -> service.sendMailWithRole(null, STUDENT_TO, "Test", "Body"));
        verify(mailSender, never()).send(any(SimpleMailMessage.class));
    }

    @Test
    void sendMailWithRole_withUnknownRole_throwsAccessDenied() {
        // Arrange
        MailService service = new MailService(mailSender, true, FROM_EMAIL, ADMIN_BCC);

        // Act & Assert
        assertThrows(AccessDeniedException.class,
                () -> service.sendMailWithRole("MANAGER", STUDENT_TO, "Test", "Body"));
        verify(mailSender, never()).send(any(SimpleMailMessage.class));
    }

    @Test
    void sendMailWithRole_withNullTo_throwsIllegalArgumentException() {
        // Arrange
        MailService service = new MailService(mailSender, true, FROM_EMAIL, ADMIN_BCC);

        // Act & Assert
        assertThrows(IllegalArgumentException.class,
                () -> service.sendMailWithRole("ADMIN", null, "Test", "Body"));
    }

    @Test
    void sendMailWithRole_withInvalidEmail_throwsIllegalArgumentException() {
        // Arrange
        MailService service = new MailService(mailSender, true, FROM_EMAIL, ADMIN_BCC);

        // Act & Assert
        assertThrows(IllegalArgumentException.class,
                () -> service.sendMailWithRole("ADMIN", "geen-email", "Test", "Body"));
    }

    @Test
    void sendMailWithRole_withNullSubject_throwsIllegalArgumentException() {
        // Arrange
        MailService service = new MailService(mailSender, true, FROM_EMAIL, ADMIN_BCC);

        // Act & Assert
        assertThrows(IllegalArgumentException.class,
                () -> service.sendMailWithRole("ADMIN", STUDENT_TO, null, "Body"));
    }

    @Test
    void sendMailWithRole_withNullBody_throwsIllegalArgumentException() {
        // Arrange
        MailService service = new MailService(mailSender, true, FROM_EMAIL, ADMIN_BCC);

        // Act & Assert
        assertThrows(IllegalArgumentException.class,
                () -> service.sendMailWithRole("ADMIN", STUDENT_TO, "Test", null));
    }

    // =====================================================================
    // # sendMailWithRole – rollen
    // =====================================================================

    @Test
    void cleaner_canSend_onlyForAllowedSubjects() {
        // Arrange
        MailService service = new MailService(mailSender, true, FROM_EMAIL, ADMIN_BCC);

        // Act & Assert – incident is toegestaan
        assertDoesNotThrow(() -> service.sendMailWithRole("CLEANER", STUDENT_TO, "Incident: lekkage", "Body"));
        verify(mailSender, times(1)).send(any(SimpleMailMessage.class));

        // Assert – huur is niet toegestaan voor cleaner
        assertThrows(AccessDeniedException.class,
                () -> service.sendMailWithRole("CLEANER", STUDENT_TO, "Huurbetaling", "Body"));
    }

    @Test
    void cleaner_withCleaningSubject_canSend() {
        // Arrange
        MailService service = new MailService(mailSender, true, FROM_EMAIL, ADMIN_BCC);

        // Act & Assert
        assertDoesNotThrow(() -> service.sendMailWithRole("CLEANER", STUDENT_TO, "cleaning taak vandaag", "Body"));
        verify(mailSender).send(any(SimpleMailMessage.class));
    }

    @Test
    void student_cannotSend_throwsAccessDenied() {
        // Arrange
        MailService service = new MailService(mailSender, true, FROM_EMAIL, ADMIN_BCC);

        // Act & Assert
        assertThrows(AccessDeniedException.class,
                () -> service.sendMailWithRole("STUDENT", STUDENT_TO, "Test", "Body"));
        verify(mailSender, never()).send(any(SimpleMailMessage.class));
    }

    // =====================================================================
    // # Mail disabled / no sender
    // =====================================================================

    @Test
    void mailDisabled_doesNotSend() {
        // Arrange
        MailService service = new MailService(mailSender, false, FROM_EMAIL, ADMIN_BCC);

        // Act
        service.sendMailWithRole("ADMIN", STUDENT_TO, "Test", "Body");

        // Assert
        verify(mailSender, never()).send(any(SimpleMailMessage.class));
    }

    @Test
    void noMailSenderBean_doesNotThrow() {
        // Arrange
        MailService service = new MailService(null, true, FROM_EMAIL, ADMIN_BCC);

        // Act & Assert
        assertDoesNotThrow(() -> service.sendMailWithRole("ADMIN", STUDENT_TO, "Test", "Body"));
    }

    @Test
    void sendMailWithRole_mailExceptionIsCaught() {
        // Arrange
        MailService service = new MailService(mailSender, true, FROM_EMAIL, ADMIN_BCC);
        doThrow(new MailSendException("simulated failure")).when(mailSender).send(any(SimpleMailMessage.class));

        // Act & Assert
        assertDoesNotThrow(() -> service.sendMailWithRole("ADMIN", STUDENT_TO, "Test", "Body"));
    }

    // =====================================================================
    // # sendCleanerIncidentMail
    // =====================================================================

    @Test
    void sendCleanerIncidentMail_happyPath_sends() {
        // Arrange
        MailService service = new MailService(mailSender, true, FROM_EMAIL, ADMIN_BCC);

        // Act
        service.sendCleanerIncidentMail(STUDENT_TO, "Waterlekkage gemeld");

        // Assert
        verify(mailSender).send(any(SimpleMailMessage.class));
    }

    @Test
    void sendCleanerIncidentMail_withNullBody_throwsIllegalArgumentException() {
        // Arrange
        MailService service = new MailService(mailSender, true, FROM_EMAIL, ADMIN_BCC);

        // Act & Assert
        assertThrows(IllegalArgumentException.class,
                () -> service.sendCleanerIncidentMail(STUDENT_TO, null));
    }

    // =====================================================================
    // # sendCleanerCleaningTaskMail
    // =====================================================================

    @Test
    void sendCleanerCleaningTaskMail_happyPath_sends() {
        // Arrange
        MailService service = new MailService(mailSender, true, FROM_EMAIL, ADMIN_BCC);

        // Act
        service.sendCleanerCleaningTaskMail(STUDENT_TO, "Kamer 3 schoonmaken");

        // Assert
        verify(mailSender).send(any(SimpleMailMessage.class));
    }

    @Test
    void sendCleanerCleaningTaskMail_withNullBody_throwsIllegalArgumentException() {
        // Arrange
        MailService service = new MailService(mailSender, true, FROM_EMAIL, ADMIN_BCC);

        // Act & Assert
        assertThrows(IllegalArgumentException.class,
                () -> service.sendCleanerCleaningTaskMail(STUDENT_TO, null));
    }

    // =====================================================================
    // # sendInvoiceReminderMail
    // =====================================================================

    @Test
    void sendInvoiceReminderMail_happyPath_sends() {
        // Arrange
        MailService service = new MailService(mailSender, true, FROM_EMAIL, ADMIN_BCC);

        // Act
        service.sendInvoiceReminderMail(STUDENT_TO, "Herinnering factuur", "Uw factuur is bijna verlopen.");

        // Assert
        verify(mailSender).send(any(SimpleMailMessage.class));
    }

    @Test
    void sendInvoiceReminderMail_withNullSubject_throwsIllegalArgumentException() {
        // Arrange
        MailService service = new MailService(mailSender, true, FROM_EMAIL, ADMIN_BCC);

        // Act & Assert
        assertThrows(IllegalArgumentException.class,
                () -> service.sendInvoiceReminderMail(STUDENT_TO, null, "Body"));
    }

    @Test
    void sendInvoiceReminderMail_withNullBody_throwsIllegalArgumentException() {
        // Arrange
        MailService service = new MailService(mailSender, true, FROM_EMAIL, ADMIN_BCC);

        // Act & Assert
        assertThrows(IllegalArgumentException.class,
                () -> service.sendInvoiceReminderMail(STUDENT_TO, "Herinnering", null));
    }
}