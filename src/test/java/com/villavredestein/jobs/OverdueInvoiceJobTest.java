package com.villavredestein.jobs;

import com.villavredestein.model.Invoice;
import com.villavredestein.model.User;
import com.villavredestein.repository.*;
import com.villavredestein.service.MailService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

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

    @Autowired private CapturingMailService capturingMailService;

    @TestConfiguration
    static class MailTestConfig {
        @Bean
        @Primary
        CapturingMailService capturingMailService() {
            return new CapturingMailService();
        }

        @Bean
        @Primary
        MailService mailService(CapturingMailService capturingMailService) {
            return capturingMailService;
        }
    }

    static class CapturingMailService extends MailService {
        private int sentCount = 0;

        public int getSentCount() {
            return sentCount;
        }

        @Override
        public void sendMailWithRole(String role, String to, String subject, String body) {
            sentCount++;
        }
    }

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
        overdueInvoice.setAmount(new BigDecimal("500.00"));
        overdueInvoice.setStatus("OPEN");
        overdueInvoice.setDueDate(LocalDate.now().minusDays(3)); // te laat → overdue
        overdueInvoice.setReminderSent(false);
        invoiceRepository.saveAndFlush(overdueInvoice);
    }

    @Test
    @Transactional
    void shouldSendOverdueReminderForLateInvoices() {
        overdueInvoiceJob.sendOverdueReminders();

        org.junit.jupiter.api.Assertions.assertEquals(1, capturingMailService.getSentCount());
    }
}