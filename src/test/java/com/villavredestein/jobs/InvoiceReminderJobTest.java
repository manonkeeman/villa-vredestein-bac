package com.villavredestein.jobs;

import com.villavredestein.model.Invoice;
import com.villavredestein.model.User;
import com.villavredestein.repository.*;
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
@TestPropertySource(properties = "spring.jpa.open-in-view=true")
class InvoiceReminderJobTest {

    @Autowired private InvoiceRepository invoiceRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private DocumentRepository documentRepository;
    @Autowired private CleaningTaskRepository cleaningTaskRepository;
    @Autowired private PaymentRepository paymentRepository;
    @Autowired private RoomRepository roomRepository;
    @Autowired private InvoiceReminderJob invoiceReminderJob;

    @MockBean private MailService mailService;

    @BeforeEach
    @Transactional
    void setUp() {
        roomRepository.deleteAll();
        paymentRepository.deleteAll();
        cleaningTaskRepository.deleteAll();
        documentRepository.deleteAll();
        invoiceRepository.deleteAll();
        userRepository.deleteAll();

        User student = new User();
        student.setUsername("StudentUser");
        student.setEmail("student@villavredestein.nl");
        student.setRole("STUDENT");
        userRepository.saveAndFlush(student);

        Invoice invoice = new Invoice();
        invoice.setStudent(student);
        invoice.setAmount(500.0);
        invoice.setStatus("OPEN");
        invoice.setDueDate(LocalDate.now().plusDays(3)); // binnen 4 dagen → reminder
        invoice.setReminderSent(false);
        invoiceRepository.saveAndFlush(invoice);
    }

    @Test
    @Transactional
    void shouldSendReminderForUpcomingInvoices() {
        invoiceReminderJob.sendReminders();

        verify(mailService, times(1))
                .sendMailWithRole(anyString(), anyString(), anyString(), anyString());
    }
}