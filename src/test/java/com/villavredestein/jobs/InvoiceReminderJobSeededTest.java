package com.villavredestein.jobs;

import com.villavredestein.model.Invoice;
import com.villavredestein.model.User;
import com.villavredestein.repository.InvoiceRepository;
import com.villavredestein.repository.UserRepository;
import com.villavredestein.service.MailService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SpringBootTest
@TestPropertySource(properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.sql.init.mode=never"
})
@Transactional
class InvoiceReminderJobSeededTest {

    @Autowired private InvoiceRepository invoiceRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private InvoiceReminderJob invoiceReminderJob;

    @MockBean private MailService mailService;

    @BeforeEach
    void setUp() {
        User student = new User();
        student.setUsername("StudentUser");
        student.setEmail("student@villa.nl");
        student.setRole("STUDENT");
        userRepository.save(student);

        Invoice dueSoon = new Invoice();
        dueSoon.setStudent(student);
        dueSoon.setAmount(500.0);
        dueSoon.setStatus("OPEN");
        dueSoon.setDueDate(LocalDate.now().plusDays(3));
        dueSoon.setReminderSent(false);
        invoiceRepository.save(dueSoon);

        Invoice overdue = new Invoice();
        overdue.setStudent(student);
        overdue.setAmount(400.0);
        overdue.setStatus("OVERDUE");
        overdue.setDueDate(LocalDate.now().minusDays(2));
        overdue.setReminderSent(false);
        invoiceRepository.save(overdue);
    }

    @Test
    void shouldSendRemindersOnlyForInvoicesDueSoon() {

        invoiceReminderJob.sendReminders();

        verify(mailService, times(1))
                .sendMailWithRole(anyString(), anyString(), anyString(), anyString());
    }
}