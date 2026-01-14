package com.villavredestein.service;

import com.villavredestein.dto.PaymentRequestDTO;
import com.villavredestein.model.Payment;
import com.villavredestein.model.Payment.PaymentStatus;
import com.villavredestein.model.User;
import com.villavredestein.repository.PaymentRepository;
import com.villavredestein.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final UserRepository userRepository;

    public PaymentService(PaymentRepository paymentRepository, UserRepository userRepository) {
        this.paymentRepository = paymentRepository;
        this.userRepository = userRepository;
    }

    // ==========================================================
    // READ
    // ==========================================================

    @Transactional(readOnly = true)
    public List<Payment> getAllPayments() {
        return paymentRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Payment> getPaymentsForStudent(String studentEmail) {
        if (studentEmail == null || studentEmail.isBlank()) {
            throw new IllegalArgumentException("studentEmail is verplicht");
        }
        return paymentRepository.findByStudent_Email(studentEmail.trim());
    }

    @Transactional(readOnly = true)
    public List<Payment> getOpenPaymentsForStudent(String studentEmail) {
        if (studentEmail == null || studentEmail.isBlank()) {
            throw new IllegalArgumentException("studentEmail is verplicht");
        }
        return paymentRepository.findByStudent_EmailAndStatus(studentEmail.trim(), PaymentStatus.OPEN);
    }

    @Transactional(readOnly = true)
    public Payment getPaymentById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("id is verplicht");
        }
        return paymentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Payment niet gevonden: " + id));
    }

    // ==========================================================
    // CREATE / UPDATE
    // ==========================================================

    public Payment createPayment(PaymentRequestDTO dto) {
        if (dto == null) {
            throw new IllegalArgumentException("PaymentRequestDTO is verplicht");
        }

        BigDecimal amount = dto.getAmount();
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("amount moet groter zijn dan 0");
        }

        boolean identifierProvided = hasStudentIdentifier(dto);
        User student = resolveStudentFromDto(dto)
                .orElseThrow(() -> {
                    if (identifierProvided) {
                        return new EntityNotFoundException("Student niet gevonden op basis van opgegeven identificatie");
                    }
                    return new IllegalArgumentException(
                            "student is verplicht (geef studentId/studentEmail/studentUsername mee in PaymentRequestDTO)"
                    );
                });

        Payment payment = new Payment();
        payment.setAmount(amount);
        payment.setDescription(dto.getDescription());
        payment.setStudent(student);
        payment.setCreatedAt(LocalDateTime.now());
        payment.setStatus(dto.getStatus() != null ? dto.getStatus() : PaymentStatus.OPEN);

        if (payment.getStatus() == PaymentStatus.PAID) {
            payment.setPaidAt(LocalDateTime.now());
        }

        return paymentRepository.save(payment);
    }

    /**
     * Status updaten (bijv. OPEN -> PAID).
     */
    public Payment updateStatus(Long paymentId, PaymentStatus newStatus) {
        if (paymentId == null) {
            throw new IllegalArgumentException("paymentId is verplicht");
        }
        if (newStatus == null) {
            throw new IllegalArgumentException("newStatus is verplicht");
        }

        Payment payment = getPaymentById(paymentId);
        payment.setStatus(newStatus);

        if (newStatus == PaymentStatus.PAID && payment.getPaidAt() == null) {
            payment.setPaidAt(LocalDateTime.now());
        }
        if (newStatus != PaymentStatus.PAID) {
            payment.setPaidAt(null);
        }

        return paymentRepository.save(payment);
    }

    /**
     * Payment verwijderen (admin).
     */
    public void deletePayment(Long id) {
        Payment payment = getPaymentById(id);
        paymentRepository.delete(payment);
    }

    // ==========================================================
    // Helpers: detect student identifier
    // ==========================================================

    private boolean hasStudentIdentifier(PaymentRequestDTO dto) {
        Long id = readLong(dto, "getStudentId")
                .or(() -> readLong(dto, "getUserId"))
                .orElse(null);
        if (id != null) {
            return true;
        }

        String email = readString(dto, "getStudentEmail")
                .or(() -> readString(dto, "getEmail"))
                .map(String::trim)
                .orElse("");
        if (!email.isBlank()) {
            return true;
        }

        String username = readString(dto, "getStudentUsername")
                .or(() -> readString(dto, "getUsername"))
                .map(String::trim)
                .orElse("");
        return !username.isBlank();
    }

    // ==========================================================
    // Helpers: resolve student via reflection (compile-safe)
    // ==========================================================

    private Optional<User> resolveStudentFromDto(PaymentRequestDTO dto) {
        Long id = readLong(dto, "getStudentId")
                .or(() -> readLong(dto, "getUserId"))
                .orElse(null);

        if (id != null) {
            return userRepository.findById(id);
        }

        String email = readString(dto, "getStudentEmail")
                .or(() -> readString(dto, "getEmail"))
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .orElse(null);

        if (email != null) {
            return findUserByStringRepoMethod("findByEmail", email)
                    .or(() -> findUserByStringRepoMethod("findByEmailIgnoreCase", email));
        }

        String username = readString(dto, "getStudentUsername")
                .or(() -> readString(dto, "getUsername"))
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .orElse(null);

        if (username != null) {
            return findUserByStringRepoMethod("findByUsername", username)
                    .or(() -> findUserByStringRepoMethod("findByUsernameIgnoreCase", username));
        }

        return Optional.empty();
    }

    private Optional<Long> readLong(Object target, String methodName) {
        try {
            Method m = target.getClass().getMethod(methodName);
            Object value = m.invoke(target);
            if (value instanceof Long l) {
                return Optional.of(l);
            }
            return Optional.empty();
        } catch (Exception ignored) {
            return Optional.empty();
        }
    }

    private Optional<String> readString(Object target, String methodName) {
        try {
            Method m = target.getClass().getMethod(methodName);
            Object value = m.invoke(target);
            if (value instanceof String s) {
                return Optional.of(s);
            }
            return Optional.empty();
        } catch (Exception ignored) {
            return Optional.empty();
        }
    }

    @SuppressWarnings("unchecked")
    private Optional<User> findUserByStringRepoMethod(String methodName, String value) {
        try {
            Method m = userRepository.getClass().getMethod(methodName, String.class);
            Object result = m.invoke(userRepository, value);

            if (result instanceof Optional<?> opt) {
                return (Optional<User>) opt;
            }
            if (result instanceof User u) {
                return Optional.of(u);
            }

            return Optional.empty();
        } catch (Exception ignored) {
            return Optional.empty();
        }
    }
}