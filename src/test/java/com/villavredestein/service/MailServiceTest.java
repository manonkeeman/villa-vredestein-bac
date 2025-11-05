package com.villavredestein.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
class MailServiceTest {

    @MockBean
    private JavaMailSender mailSender;

    @Autowired
    private MailService mailService;

    @Test
    void testAdminCanSendMail() {
        // Arrange
        String role = "ADMIN";
        String to = "student@villa.nl";
        String subject = "Test onderwerp";
        String body = "Hallo student, dit is een test!";

        mailService.sendMailWithRole(role, to, subject, body);

        verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
    }

    @Test
    void testStudentCannotSendMail() {
        String role = "STUDENT";
        String to = "admin@villa.nl";
        String subject = "Ik probeer te mailen";
        String body = "Dit zou niet mogen.";

        assertThrows(
                org.springframework.security.access.AccessDeniedException.class,
                () -> mailService.sendMailWithRole(role, to, subject, body)
        );
    }
}