package com.villavredestein.service;

import com.villavredestein.dto.PaymentRequestDTO;
import com.villavredestein.dto.PaymentResponseDTO;
import com.villavredestein.model.Payment;
import com.villavredestein.model.User;
import com.villavredestein.repository.PaymentRepository;
import com.villavredestein.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final UserRepository userRepository;

    public PaymentService(PaymentRepository paymentRepository, UserRepository userRepository) {
        this.paymentRepository = paymentRepository;
        this.userRepository = userRepository;
    }

    // =====================================================================
    // # READ
    // =====================================================================
    @Transactional(readOnly = true)
    public List<PaymentResponseDTO> getAllPayments() {
        return paymentRepository.findAllByOrderByIdDesc()
                .stream()
                .map(this::toResponseDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PaymentResponseDTO> getPaymentsForStudent(String studentEmail) {
        String email = normalizeEmail(studentEmail);

        return paymentRepository.findByStudent_EmailIgnoreCaseOrderByIdDesc(email)
                .stream()
                .map(this::toResponseDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PaymentResponseDTO> getOpenPaymentsForStudent(String studentEmail) {
        String email = normalizeEmail(studentEmail);

        List<Payment> open = paymentRepository.findByStudent_EmailIgnoreCaseAndStatusOrderByIdDesc(
                email,
                Payment.PaymentStatus.OPEN
        );

        List<Payment> pending = paymentRepository.findByStudent_EmailIgnoreCaseAndStatusOrderByIdDesc(
                email,
                Payment.PaymentStatus.PENDING
        );

        return concat(open, pending)
                .stream()
                .map(this::toResponseDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public PaymentResponseDTO getPaymentById(Long id) {
        Payment payment = paymentRepository.findById(requireId(id))
                .orElseThrow(() -> new EntityNotFoundException("Payment not found: " + id));
        return toResponseDTO(payment);
    }

    // =====================================================================
    // # CREATE / UPDATE
    // =====================================================================
    public PaymentResponseDTO createPayment(PaymentRequestDTO dto) {
        if (dto == null) {
            throw new IllegalArgumentException("PaymentRequestDTO is required");
        }

        String email = normalizeEmail(dto.getStudentEmail());
        User student = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new EntityNotFoundException("Student not found: " + email));

        Payment payment = new Payment(
                dto.getAmount(),
                null,
                Payment.PaymentStatus.OPEN,
                dto.getDescription(),
                student
        );

        payment.setStatus(dto.getStatus());

        if (payment.getStatus() == Payment.PaymentStatus.PAID) {
            LocalDateTime paidAt = dto.getPaidAt() != null ? dto.getPaidAt() : LocalDateTime.now();
            payment.setPaidAt(paidAt);
        } else {
            payment.setPaidAt(dto.getPaidAt());
        }

        Payment saved = paymentRepository.save(payment);
        return toResponseDTO(saved);
    }

    public PaymentResponseDTO updateStatus(Long paymentId, String newStatus) {
        Payment payment = paymentRepository.findById(requireId(paymentId))
                .orElseThrow(() -> new EntityNotFoundException("Payment not found: " + paymentId));

        payment.setStatus(newStatus);

        if (payment.getStatus() == Payment.PaymentStatus.PAID && payment.getPaidAt() == null) {
            payment.setPaidAt(LocalDateTime.now());
        }

        if (payment.getStatus() != Payment.PaymentStatus.PAID) {
            payment.setPaidAt(null);
        }

        Payment saved = paymentRepository.save(payment);
        return toResponseDTO(saved);
    }

    public void deletePayment(Long id) {
        Payment payment = paymentRepository.findById(requireId(id))
                .orElseThrow(() -> new EntityNotFoundException("Payment not found: " + id));
        paymentRepository.delete(payment);
    }

    // =====================================================================
    // # Helpers
    // =====================================================================
    private Long requireId(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("id is required");
        }
        return id;
    }

    private String normalizeEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("studentEmail is required");
        }
        return email.trim().toLowerCase();
    }

    private List<Payment> concat(List<Payment> a, List<Payment> b) {
        if (a.isEmpty()) return b;
        if (b.isEmpty()) return a;
        return java.util.stream.Stream.concat(a.stream(), b.stream()).toList();
    }

    private PaymentResponseDTO toResponseDTO(Payment payment) {
        String studentName = payment.getStudent() != null ? payment.getStudent().getUsername() : null;
        String studentEmail = payment.getStudent() != null ? payment.getStudent().getEmail() : null;

        return new PaymentResponseDTO(
                payment.getId(),
                payment.getAmount(),
                payment.getCreatedAt(),
                payment.getPaidAt(),
                payment.getStatus().name(),
                payment.getDescription(),
                studentName,
                studentEmail
        );
    }
}