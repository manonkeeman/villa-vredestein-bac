package com.villavredestein.service;

import com.villavredestein.dto.PaymentRequestDTO;
import com.villavredestein.dto.PaymentResponseDTO;
import com.villavredestein.model.Payment;
import com.villavredestein.model.User;
import com.villavredestein.repository.PaymentRepository;
import com.villavredestein.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock PaymentRepository paymentRepository;
    @Mock UserRepository userRepository;
    @InjectMocks PaymentService paymentService;


    @Test
    void getAllPayments_returnsMappedDtoList() {
        User student = makeStudent(1L, "student", "s@test.com");
        Payment p1 = makePayment(1L, new BigDecimal("100.00"), Payment.PaymentStatus.OPEN, student);
        Payment p2 = makePayment(2L, new BigDecimal("200.00"), Payment.PaymentStatus.PAID, student);
        when(paymentRepository.findAllByOrderByIdDesc()).thenReturn(List.of(p2, p1));

        List<PaymentResponseDTO> result = paymentService.getAllPayments();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).id()).isEqualTo(2L);
        assertThat(result.get(1).id()).isEqualTo(1L);
    }

    @Test
    void getAllPayments_empty_returnsEmptyList() {
        when(paymentRepository.findAllByOrderByIdDesc()).thenReturn(List.of());

        List<PaymentResponseDTO> result = paymentService.getAllPayments();

        assertThat(result).isEmpty();
    }


    @Test
    void getPaymentsForStudent_validEmail_returnsList() {
        User student = makeStudent(1L, "student", "s@test.com");
        Payment p = makePayment(1L, new BigDecimal("100.00"), Payment.PaymentStatus.OPEN, student);
        when(paymentRepository.findByStudent_EmailIgnoreCaseOrderByIdDesc("s@test.com")).thenReturn(List.of(p));

        List<PaymentResponseDTO> result = paymentService.getPaymentsForStudent("S@TEST.COM");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).studentEmail()).isEqualTo("s@test.com");
    }

    @Test
    void getPaymentsForStudent_blankEmail_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> paymentService.getPaymentsForStudent("   "));
        verify(paymentRepository, never()).findByStudent_EmailIgnoreCaseOrderByIdDesc(any());
    }

    @Test
    void getPaymentsForStudent_nullEmail_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> paymentService.getPaymentsForStudent(null));
        verify(paymentRepository, never()).findByStudent_EmailIgnoreCaseOrderByIdDesc(any());
    }


    @Test
    void getOpenPaymentsForStudent_returnsCombinedOpenAndPendingPayments() {
        User student = makeStudent(1L, "student", "s@test.com");
        Payment open = makePayment(1L, new BigDecimal("100.00"), Payment.PaymentStatus.OPEN, student);
        Payment pending = makePayment(2L, new BigDecimal("200.00"), Payment.PaymentStatus.PENDING, student);
        when(paymentRepository.findByStudent_EmailIgnoreCaseAndStatusOrderByIdDesc("s@test.com", Payment.PaymentStatus.OPEN))
                .thenReturn(List.of(open));
        when(paymentRepository.findByStudent_EmailIgnoreCaseAndStatusOrderByIdDesc("s@test.com", Payment.PaymentStatus.PENDING))
                .thenReturn(List.of(pending));

        List<PaymentResponseDTO> result = paymentService.getOpenPaymentsForStudent("s@test.com");

        assertThat(result).hasSize(2);
    }

    @Test
    void getOpenPaymentsForStudent_noOpenPayments_returnsOnlyPending() {
        User student = makeStudent(1L, "student", "s@test.com");
        Payment pending = makePayment(1L, new BigDecimal("100.00"), Payment.PaymentStatus.PENDING, student);
        when(paymentRepository.findByStudent_EmailIgnoreCaseAndStatusOrderByIdDesc("s@test.com", Payment.PaymentStatus.OPEN))
                .thenReturn(List.of());
        when(paymentRepository.findByStudent_EmailIgnoreCaseAndStatusOrderByIdDesc("s@test.com", Payment.PaymentStatus.PENDING))
                .thenReturn(List.of(pending));

        List<PaymentResponseDTO> result = paymentService.getOpenPaymentsForStudent("s@test.com");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).status()).isEqualTo("PENDING");
    }


    @Test
    void getPaymentById_found_returnsDto() {
        User student = makeStudent(1L, "student", "s@test.com");
        Payment p = makePayment(1L, new BigDecimal("100.00"), Payment.PaymentStatus.OPEN, student);
        when(paymentRepository.findById(1L)).thenReturn(Optional.of(p));

        PaymentResponseDTO result = paymentService.getPaymentById(1L);

        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.status()).isEqualTo("OPEN");
    }

    @Test
    void getPaymentById_notFound_throwsEntityNotFoundException() {
        when(paymentRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> paymentService.getPaymentById(99L));
    }

    @Test
    void getPaymentById_zeroId_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> paymentService.getPaymentById(0L));
        verify(paymentRepository, never()).findById(any());
    }

    @Test
    void getPaymentById_nullId_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> paymentService.getPaymentById(null));
        verify(paymentRepository, never()).findById(any());
    }


    @Test
    void createPayment_success_returnsDto() {
        User student = makeStudent(1L, "student", "s@test.com");
        when(userRepository.findByEmailIgnoreCase("s@test.com")).thenReturn(Optional.of(student));
        Payment saved = makePayment(1L, new BigDecimal("150.00"), Payment.PaymentStatus.OPEN, student);
        when(paymentRepository.save(any(Payment.class))).thenReturn(saved);

        PaymentRequestDTO dto = new PaymentRequestDTO();
        dto.setAmount(new BigDecimal("150.00"));
        dto.setStudentEmail("s@test.com");
        dto.setStatus("OPEN");

        PaymentResponseDTO result = paymentService.createPayment(dto);

        assertThat(result.amount()).isEqualByComparingTo(new BigDecimal("150.00"));
        assertThat(result.studentEmail()).isEqualTo("s@test.com");
        verify(paymentRepository).save(any(Payment.class));
    }

    @Test
    void createPayment_studentNotFound_throwsEntityNotFoundException() {
        when(userRepository.findByEmailIgnoreCase("notfound@test.com")).thenReturn(Optional.empty());

        PaymentRequestDTO dto = new PaymentRequestDTO();
        dto.setAmount(new BigDecimal("100.00"));
        dto.setStudentEmail("notfound@test.com");
        dto.setStatus("OPEN");

        assertThrows(EntityNotFoundException.class, () -> paymentService.createPayment(dto));
        verify(paymentRepository, never()).save(any());
    }

    @Test
    void createPayment_nullDto_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> paymentService.createPayment(null));
        verify(paymentRepository, never()).save(any());
    }


    @Test
    void updateStatus_toPaid_setsPaidAtAndReturnsDto() {
        User student = makeStudent(1L, "student", "s@test.com");
        Payment p = makePayment(1L, new BigDecimal("100.00"), Payment.PaymentStatus.OPEN, student);
        when(paymentRepository.findById(1L)).thenReturn(Optional.of(p));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(inv -> inv.getArgument(0));

        PaymentResponseDTO result = paymentService.updateStatus(1L, "PAID");

        assertThat(result.status()).isEqualTo("PAID");
    }

    @Test
    void updateStatus_fromPaidToOpen_clearsPaidAt() {
        User student = makeStudent(1L, "student", "s@test.com");
        Payment p = new Payment(new BigDecimal("100.00"), LocalDateTime.now(), Payment.PaymentStatus.PAID, "Test", student);
        ReflectionTestUtils.setField(p, "id", 1L);
        when(paymentRepository.findById(1L)).thenReturn(Optional.of(p));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(inv -> inv.getArgument(0));

        PaymentResponseDTO result = paymentService.updateStatus(1L, "OPEN");

        assertThat(result.status()).isEqualTo("OPEN");
        assertThat(result.paidAt()).isNull();
    }

    @Test
    void updateStatus_notFound_throwsEntityNotFoundException() {
        when(paymentRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> paymentService.updateStatus(99L, "PAID"));
    }


    @Test
    void deletePayment_success_deletesPayment() {
        User student = makeStudent(1L, "student", "s@test.com");
        Payment p = makePayment(1L, new BigDecimal("100.00"), Payment.PaymentStatus.OPEN, student);
        when(paymentRepository.findById(1L)).thenReturn(Optional.of(p));

        assertDoesNotThrow(() -> paymentService.deletePayment(1L));
        verify(paymentRepository).delete(p);
    }

    @Test
    void deletePayment_notFound_throwsEntityNotFoundException() {
        when(paymentRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> paymentService.deletePayment(99L));
        verify(paymentRepository, never()).delete(any());
    }

    @Test
    void deletePayment_nullId_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> paymentService.deletePayment(null));
        verify(paymentRepository, never()).findById(any());
    }


    private User makeStudent(long id, String username, String email) {
        User user = new User(username, email, "hash", User.Role.STUDENT);
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }

    private Payment makePayment(long id, BigDecimal amount, Payment.PaymentStatus status, User student) {
        Payment p = new Payment(amount, null, status, "Test payment", student);
        ReflectionTestUtils.setField(p, "id", id);
        return p;
    }
}