package com.villavredestein.service;

import com.villavredestein.model.Payment;
import com.villavredestein.model.User;
import com.villavredestein.repository.PaymentRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final MailService mailService;

    public PaymentService(PaymentRepository paymentRepository, MailService mailService) {
        this.paymentRepository = paymentRepository;
        this.mailService = mailService;
    }

    // === GETTERS ===

    public List<Payment> getPaymentsForStudent(User student) {
        return paymentRepository.findByStudent(student);
    }

    public List<Payment> getAllPayments() {
        return paymentRepository.findAll();
    }

    public Optional<Payment> getPaymentById(Long id) {
        return paymentRepository.findById(id);
    }

    public List<Payment> getOpenPayments() {
        return paymentRepository.findByStatus("OPEN");
    }

    public List<Payment> getPaymentsByStudentEmail(String email) {
        return paymentRepository.findByStudentEmail(email);
    }

    // === CREATE ===
    public Payment savePayment(Payment payment) {
        Payment saved = paymentRepository.save(payment);

        // Automatische bevestigingsmail bij betaling
        sendPaymentConfirmationIfPaid(saved);
        return saved;
    }

    // === UPDATE ===
    public Payment updatePayment(Long id, Payment updatedPayment) {
        return paymentRepository.findById(id)
                .map(existing -> {
                    existing.setAmount(updatedPayment.getAmount());
                    existing.setDate(updatedPayment.getDate());
                    existing.setStatus(updatedPayment.getStatus());
                    existing.setDescription(updatedPayment.getDescription());

                    Payment saved = paymentRepository.save(existing);
                    sendPaymentConfirmationIfPaid(saved);
                    return saved;
                })
                .orElseThrow(() -> new RuntimeException("Payment not found with id: " + id));
    }

    // === DELETE ===
    public void deletePayment(Long id) {
        paymentRepository.deleteById(id);
    }

    // === MAIL LOGIC ===
    private void sendPaymentConfirmationIfPaid(Payment payment) {
        if ("PAID".equalsIgnoreCase(payment.getStatus()) && payment.getStudent() != null) {
            User student = payment.getStudent();
            String subject = "✅ Bevestiging huurbetaling ontvangen";
            String body = String.format("""
                    Beste %s,

                    Wij hebben je betaling van €%.2f ontvangen.

                    Bedankt dat je op tijd hebt betaald!

                    Met vriendelijke groet,
                    Villa Vredestein Beheer
                    """,
                    safe(student.getUsername()),
                    payment.getAmount());

            mailService.sendMailWithRole("ADMIN", student.getEmail(), subject, body);
        }
    }

    private String safe(String s) {
        return (s == null || s.isBlank()) ? "student" : s;
    }
}