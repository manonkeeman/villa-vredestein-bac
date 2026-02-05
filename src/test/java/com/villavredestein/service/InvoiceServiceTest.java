package com.villavredestein.service;

import com.villavredestein.dto.InvoiceRequestDTO;
import com.villavredestein.dto.InvoiceResponseDTO;
import com.villavredestein.model.Invoice;
import com.villavredestein.model.User;
import com.villavredestein.repository.InvoiceRepository;
import com.villavredestein.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InvoiceServiceTest {

    @Mock InvoiceRepository invoiceRepository;
    @Mock UserRepository userRepository;

    @InjectMocks InvoiceService invoiceService;

    @Test
    void createInvoice_withExistingStudent_savesAndReturnsDto() {
        InvoiceRequestDTO dto = new InvoiceRequestDTO();
        dto.setTitle("Huur juli");
        dto.setDescription("Huur kamer 2 juli");
        dto.setAmount(new BigDecimal("500.00"));
        dto.setIssueDate(LocalDate.of(2025, 7, 1));
        dto.setDueDate(LocalDate.of(2025, 7, 31));
        dto.setStudentEmail("student@villavredestein.com");

        User student = new User("student", "student@villavredestein.com", "$2a$10$7QJZyq9ZQKQ7kG8xFv4Z7e9cYlQwZpZk1p9l4n0v2qXK0Qyq1FZ3K", User.Role.STUDENT);

        when(userRepository.findByEmailIgnoreCase("student@villavredestein.com"))
                .thenReturn(Optional.of(student));

        Invoice saved = new Invoice(
                dto.getTitle(),
                dto.getDescription(),
                dto.getAmount(),
                dto.getIssueDate(),
                dto.getDueDate(),
                7,
                2025,
                Invoice.InvoiceStatus.OPEN,
                student
        );
        ReflectionTestUtils.setField(saved, "id", 1L);

        when(invoiceRepository.save(any(Invoice.class))).thenReturn(saved);

        InvoiceResponseDTO result = invoiceService.createInvoice(dto);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getTitle()).isEqualTo("Huur juli");
        assertThat(result.getAmount()).isEqualByComparingTo(new BigDecimal("500.00"));
        assertThat(result.getStudentEmail()).isEqualTo("student@villavredestein.com");

        verify(userRepository).findByEmailIgnoreCase("student@villavredestein.com");
        verify(invoiceRepository).save(any(Invoice.class));
    }

    @Test
    void createInvoice_studentNotFound_throwsEntityNotFoundException() {
        InvoiceRequestDTO dto = new InvoiceRequestDTO();
        dto.setTitle("Huur juli");
        dto.setAmount(new BigDecimal("500.00"));
        dto.setIssueDate(LocalDate.of(2025, 7, 1));
        dto.setDueDate(LocalDate.of(2025, 7, 31));
        dto.setStudentEmail("student@villavredestein.com");

        when(userRepository.findByEmailIgnoreCase("student@villavredestein.com"))
                .thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> invoiceService.createInvoice(dto));

        verify(userRepository).findByEmailIgnoreCase("student@villavredestein.com");
        verify(invoiceRepository, never()).save(any());
    }

    @Test
    void getAllInvoices_returnsMappedDtoList() {
        User student1 = new User("student", "student@villavredestein.com", "$2a$10$7QJZyq9ZQKQ7kG8xFv4Z7e9cYlQwZpZk1p9l4n0v2qXK0Qyq1FZ3K", User.Role.STUDENT);
        User student2 = new User("student2", "student2@villavredestein.com", "$2a$10$7QJZyq9ZQKQ7kG8xFv4Z7e9cYlQwZpZk1p9l4n0v2qXK0Qyq1FZ3K", User.Role.STUDENT);

        Invoice inv1 = new Invoice(
                "Factuur 1", null, new BigDecimal("100.00"),
                LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 31),
                1, 2025, Invoice.InvoiceStatus.OPEN, student1
        );
        ReflectionTestUtils.setField(inv1, "id", 1L);

        Invoice inv2 = new Invoice(
                "Factuur 2", null, new BigDecimal("200.00"),
                LocalDate.of(2025, 2, 1), LocalDate.of(2025, 2, 28),
                2, 2025, Invoice.InvoiceStatus.PAID, student2
        );
        ReflectionTestUtils.setField(inv2, "id", 2L);

        when(invoiceRepository.findAllByOrderByIdDesc()).thenReturn(List.of(inv2, inv1));

        List<InvoiceResponseDTO> result = invoiceService.getAllInvoices();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getId()).isEqualTo(2L);
        assertThat(result.get(0).getStudentEmail()).isEqualTo("student2@villavredestein.com");
        assertThat(result.get(1).getId()).isEqualTo(1L);
        assertThat(result.get(1).getStudentEmail()).isEqualTo("student@villavredestein.com");

        verify(invoiceRepository).findAllByOrderByIdDesc();
    }

    @Test
    void getInvoiceById_existing_returnsDto() {
        User student = new User("student", "student@villavredestein.com", "$2a$10$7QJZyq9ZQKQ7kG8xFv4Z7e9cYlQwZpZk1p9l4n0v2qXK0Qyq1FZ3K", User.Role.STUDENT);

        Invoice inv = new Invoice(
                "Factuur 1", null, new BigDecimal("150.00"),
                LocalDate.of(2025, 3, 1), LocalDate.of(2025, 3, 31),
                3, 2025, Invoice.InvoiceStatus.OPEN, student
        );
        ReflectionTestUtils.setField(inv, "id", 1L);

        when(invoiceRepository.findById(1L)).thenReturn(Optional.of(inv));

        InvoiceResponseDTO result = invoiceService.getInvoiceById(1L);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getTitle()).isEqualTo("Factuur 1");
        assertThat(result.getStudentEmail()).isEqualTo("student@villavredestein.com");

        verify(invoiceRepository).findById(1L);
    }

    @Test
    void getInvoiceById_notExisting_throwsEntityNotFoundException() {
        when(invoiceRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> invoiceService.getInvoiceById(99L));

        verify(invoiceRepository).findById(99L);
    }

    @Test
    void updateStatus_existing_updatesAndReturnsDto() {
        User student = new User("student", "student@villavredestein.com", "$2a$10$7QJZyq9ZQKQ7kG8xFv4Z7e9cYlQwZpZk1p9l4n0v2qXK0Qyq1FZ3K", User.Role.STUDENT);

        Invoice inv = new Invoice(
                "Factuur 1", null, new BigDecimal("150.00"),
                LocalDate.of(2025, 3, 1), LocalDate.of(2025, 3, 31),
                3, 2025, Invoice.InvoiceStatus.OPEN, student
        );
        ReflectionTestUtils.setField(inv, "id", 1L);

        when(invoiceRepository.findById(1L)).thenReturn(Optional.of(inv));

        InvoiceResponseDTO result = invoiceService.updateStatus(1L, "paid");

        assertThat(result.getStatus()).isEqualTo("PAID");

        verify(invoiceRepository).findById(1L);
        verify(invoiceRepository, never()).save(any());
    }

    @Test
    void updateStatus_notExisting_throwsEntityNotFoundException() {
        when(invoiceRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> invoiceService.updateStatus(99L, "paid"));

        verify(invoiceRepository).findById(99L);
        verify(invoiceRepository, never()).save(any());
    }

    @Test
    void deleteInvoice_existing_deletes() {
        User student = new User("student", "student@villavredestein.com", "$2a$10$7QJZyq9ZQKQ7kG8xFv4Z7e9cYlQwZpZk1p9l4n0v2qXK0Qyq1FZ3K", User.Role.STUDENT);
        Invoice inv = new Invoice(
                "Factuur 1", null, new BigDecimal("150.00"),
                LocalDate.of(2025, 3, 1), LocalDate.of(2025, 3, 31),
                3, 2025, Invoice.InvoiceStatus.OPEN, student
        );
        ReflectionTestUtils.setField(inv, "id", 1L);

        when(invoiceRepository.findById(1L)).thenReturn(Optional.of(inv));

        assertDoesNotThrow(() -> invoiceService.deleteInvoice(1L));

        verify(invoiceRepository).findById(1L);
        verify(invoiceRepository).delete(inv);
    }

    @Test
    void deleteInvoice_notExisting_throwsEntityNotFoundException() {
        when(invoiceRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> invoiceService.deleteInvoice(99L));

        verify(invoiceRepository).findById(99L);
        verify(invoiceRepository, never()).deleteById(any());
    }

    @Test
    void getAllOpenInvoices_returnsOnlyOpenInvoices() {
        User student = new User("student", "student@villavredestein.com", "$2a$10$7QJZyq9ZQKQ7kG8xFv4Z7e9cYlQwZpZk1p9l4n0v2qXK0Qyq1FZ3K", User.Role.STUDENT);

        Invoice open1 = new Invoice("Inv1", null, new BigDecimal("10.00"),
                LocalDate.now(), LocalDate.now().plusDays(10),
                LocalDate.now().getMonthValue(), LocalDate.now().getYear(),
                Invoice.InvoiceStatus.OPEN, student);

        Invoice open2 = new Invoice("Inv2", null, new BigDecimal("20.00"),
                LocalDate.now(), LocalDate.now().plusDays(20),
                LocalDate.now().getMonthValue(), LocalDate.now().getYear(),
                Invoice.InvoiceStatus.OPEN, student);

        Invoice paid = new Invoice("Inv3", null, new BigDecimal("30.00"),
                LocalDate.now(), LocalDate.now().plusDays(5),
                LocalDate.now().getMonthValue(), LocalDate.now().getYear(),
                Invoice.InvoiceStatus.PAID, student);

        when(invoiceRepository.findByStatusOrderByIdDesc(Invoice.InvoiceStatus.OPEN)).thenReturn(List.of(open2, open1));

        List<Invoice> result = invoiceService.getAllOpenInvoices();

        assertThat(result).allMatch(i -> i.getStatus() == Invoice.InvoiceStatus.OPEN);

        verify(invoiceRepository).findByStatusOrderByIdDesc(Invoice.InvoiceStatus.OPEN);
    }

    @Test
    void getUpcomingInvoices_returnsUpcomingOpenInvoices() {
        User student = new User("student", "student@villavredestein.com", "$2a$10$7QJZyq9ZQKQ7kG8xFv4Z7e9cYlQwZpZk1p9l4n0v2qXK0Qyq1FZ3K", User.Role.STUDENT);

        Invoice upcoming = new Invoice("Soon", null, new BigDecimal("10.00"),
                LocalDate.now(), LocalDate.now().plusDays(3),
                LocalDate.now().getMonthValue(), LocalDate.now().getYear(),
                Invoice.InvoiceStatus.OPEN, student);

        Invoice farAway = new Invoice("Later", null, new BigDecimal("20.00"),
                LocalDate.now(), LocalDate.now().plusDays(30),
                LocalDate.now().getMonthValue(), LocalDate.now().getYear(),
                Invoice.InvoiceStatus.OPEN, student);

        Invoice paid = new Invoice("Paid", null, new BigDecimal("30.00"),
                LocalDate.now(), LocalDate.now().plusDays(2),
                LocalDate.now().getMonthValue(), LocalDate.now().getYear(),
                Invoice.InvoiceStatus.PAID, student);

        when(invoiceRepository.findByStatusAndDueDateBetweenOrderByDueDateAsc(
                eq(Invoice.InvoiceStatus.OPEN),
                any(LocalDate.class),
                any(LocalDate.class)
        )).thenReturn(List.of(upcoming));

        List<Invoice> result = invoiceService.getUpcomingInvoices();

        assertThat(result).containsExactly(upcoming);

        verify(invoiceRepository).findByStatusAndDueDateBetweenOrderByDueDateAsc(
                eq(Invoice.InvoiceStatus.OPEN),
                any(LocalDate.class),
                any(LocalDate.class)
        );
    }
}