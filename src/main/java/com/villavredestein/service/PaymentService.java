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

    public PaymentService(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    public List<Payment> getPaymentsForStudent(User student) {
        return paymentRepository.findByStudent(student);
    }

    public List<Payment> getAllPayments() {
        return paymentRepository.findAll();
    }

    public Optional<Payment> getPaymentById(Long id) {
        return paymentRepository.findById(id);
    }

    public Payment savePayment(Payment payment) {
        return paymentRepository.save(payment);
    }

    public void deletePayment(Long id) {
        paymentRepository.deleteById(id);
    }

    public List<Payment> getOpenPayments() {
        return paymentRepository.findByStatus("OPEN");
    }

    public List<Payment> getPaymentsByStudentEmail(String email) {
        return paymentRepository.findByStudentEmail(email);
    }

    public Payment updatePayment(Long id, Payment updated) {
        return paymentRepository.findById(id)
                .map(existing -> {
                    existing.setAmount(updated.getAmount());
                    existing.setDate(updated.getDate());
                    existing.setStatus(updated.getStatus());
                    existing.setDescription(updated.getDescription());
                    return paymentRepository.save(existing);
                })
                .orElseThrow(() -> new RuntimeException("Payment not found"));
    }
}