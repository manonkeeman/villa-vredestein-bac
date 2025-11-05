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
class OverdueInvoiceJobTest {

    @Autowired private InvoiceRepository invoiceRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private DocumentRepository documentRepository;
    @Autowired private CleaningTaskRepository cleaningTaskRepository;
    @Autowired private PaymentRepository paymentRepository;
    @Autowired private RoomRepository roomRepository;
    @Autowired private OverdueInvoiceJob overdueInvoiceJob;

    @MockBean private MailService mailService;

    @BeforeEach
    @Transactional
    void setUp() {
        roomRepository.deleteAll();
        paymentRepository.deleteAll();
        cleaningTaskRepository.deleteAll();
        documentRepository.deleteAll();   // eerst documenten verwijderen (verwijzen naar users)
        invoiceRepository.deleteAll();
        userRepository.deleteAll();

        User student = new User();
        student.setUsername("StudentUser");
        student.setEmail("student@villavredestein.nl");
        student.setRole("STUDENT");
        userRepository.saveAndFlush(student);

        Invoice overdueInvoice = new Invoice();
        overdueInvoice.setStudent(student);
        overdueInvoice.setAmount(500.0);
        overdueInvoice.setStatus("OPEN");
        overdueInvoice.setDueDate(LocalDate.now().minusDays(3)); // te laat → overdue
        overdueInvoice.setReminderSent(false);
        invoiceRepository.saveAndFlush(overdueInvoice);
    }

    @Test
    @Transactional
    void shouldSendOverdueReminderForLateInvoices() {
        overdueInvoiceJob.sendOverdueReminders();

        verify(mailService, times(1))
                .sendMailWithRole(anyString(), anyString(), anyString(), anyString());
    }
}