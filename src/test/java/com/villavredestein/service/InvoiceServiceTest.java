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
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InvoiceServiceTest {

    @Mock InvoiceRepository invoiceRepository;
    @Mock UserRepository userRepository;
    @InjectMocks InvoiceService invoiceService;

    // =====================================================================
    // # createInvoice
    // =====================================================================

    @Test
    void createInvoice_withExistingStudent_savesAndReturnsDto() {
        // Arrange
        InvoiceRequestDTO dto = new InvoiceRequestDTO();
        dto.setTitle("Huur juli");
        dto.setDescription("Huur kamer 2 juli");
        dto.setAmount(new BigDecimal("500.00"));
        dto.setIssueDate(LocalDate.of(2025, 7, 1));
        dto.setDueDate(LocalDate.of(2025, 7, 31));
        dto.setStudentEmail("student@villavredestein.com");

        User student = new User("student", "student@villavredestein.com", "hash", User.Role.STUDENT);
        when(userRepository.findByEmailIgnoreCase("student@villavredestein.com")).thenReturn(Optional.of(student));

        Invoice saved = new Invoice(dto.getTitle(), dto.getDescription(), dto.getAmount(),
                dto.getIssueDate(), dto.getDueDate(), 7, 2025, Invoice.InvoiceStatus.OPEN, student);
        ReflectionTestUtils.setField(saved, "id", 1L);
        when(invoiceRepository.save(any(Invoice.class))).thenReturn(saved);

        // Act
        InvoiceResponseDTO result = invoiceService.createInvoice(dto);

        // Assert
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getTitle()).isEqualTo("Huur juli");
        assertThat(result.getAmount()).isEqualByComparingTo(new BigDecimal("500.00"));
        assertThat(result.getStudentEmail()).isEqualTo("student@villavredestein.com");
        verify(invoiceRepository).save(any(Invoice.class));
    }

    @Test
    void createInvoice_studentNotFound_throwsEntityNotFoundException() {
        // Arrange
        InvoiceRequestDTO dto = new InvoiceRequestDTO();
        dto.setTitle("Huur juli");
        dto.setAmount(new BigDecimal("500.00"));
        dto.setIssueDate(LocalDate.of(2025, 7, 1));
        dto.setDueDate(LocalDate.of(2025, 7, 31));
        dto.setStudentEmail("onbekend@villavredestein.com");

        when(userRepository.findByEmailIgnoreCase("onbekend@villavredestein.com")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> invoiceService.createInvoice(dto));
        verify(invoiceRepository, never()).save(any());
    }

    @Test
    void createInvoice_duplicateInMonth_throwsConflict() {
        // Arrange
        InvoiceRequestDTO dto = new InvoiceRequestDTO();
        dto.setTitle("Huur juli");
        dto.setAmount(new BigDecimal("500.00"));
        dto.setIssueDate(LocalDate.of(2025, 7, 1));
        dto.setDueDate(LocalDate.of(2025, 7, 31));
        dto.setStudentEmail("student@villavredestein.com");

        User student = new User("student", "student@villavredestein.com", "hash", User.Role.STUDENT);
        when(userRepository.findByEmailIgnoreCase("student@villavredestein.com")).thenReturn(Optional.of(student));
        when(invoiceRepository.existsByStudentAndInvoiceMonthAndInvoiceYear(any(User.class), anyInt(), anyInt()))
                .thenReturn(true);

        // Act & Assert
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> invoiceService.createInvoice(dto));
        assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        verify(invoiceRepository, never()).save(any());
    }

    // =====================================================================
    // # getAllInvoices
    // =====================================================================

    @Test
    void getAllInvoices_returnsMappedDtoList() {
        // Arrange
        User s1 = new User("student", "student@villavredestein.com", "hash", User.Role.STUDENT);
        User s2 = new User("student2", "student2@villavredestein.com", "hash", User.Role.STUDENT);

        Invoice inv1 = new Invoice("Factuur 1", null, new BigDecimal("100.00"),
                LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 31), 1, 2025, Invoice.InvoiceStatus.OPEN, s1);
        ReflectionTestUtils.setField(inv1, "id", 1L);

        Invoice inv2 = new Invoice("Factuur 2", null, new BigDecimal("200.00"),
                LocalDate.of(2025, 2, 1), LocalDate.of(2025, 2, 28), 2, 2025, Invoice.InvoiceStatus.PAID, s2);
        ReflectionTestUtils.setField(inv2, "id", 2L);

        when(invoiceRepository.findAllByOrderByIdDesc()).thenReturn(List.of(inv2, inv1));

        // Act
        List<InvoiceResponseDTO> result = invoiceService.getAllInvoices();

        // Assert
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getId()).isEqualTo(2L);
        assertThat(result.get(1).getId()).isEqualTo(1L);
    }

    // =====================================================================
    // # getInvoiceById
    // =====================================================================

    @Test
    void getInvoiceById_existing_returnsDto() {
        // Arrange
        User student = new User("student", "student@villavredestein.com", "hash", User.Role.STUDENT);
        Invoice inv = new Invoice("Factuur 1", null, new BigDecimal("150.00"),
                LocalDate.of(2025, 3, 1), LocalDate.of(2025, 3, 31), 3, 2025, Invoice.InvoiceStatus.OPEN, student);
        ReflectionTestUtils.setField(inv, "id", 1L);
        when(invoiceRepository.findById(1L)).thenReturn(Optional.of(inv));

        // Act
        InvoiceResponseDTO result = invoiceService.getInvoiceById(1L);

        // Assert
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getStudentEmail()).isEqualTo("student@villavredestein.com");
    }

    @Test
    void getInvoiceById_notExisting_throwsEntityNotFoundException() {
        // Arrange
        when(invoiceRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> invoiceService.getInvoiceById(99L));
    }

    // =====================================================================
    // # getInvoiceByIdForCaller
    // =====================================================================

    @Test
    void getInvoiceByIdForCaller_admin_canAccessAnyInvoice() {
        // Arrange
        User student = new User("student", "student@villavredestein.com", "hash", User.Role.STUDENT);
        Invoice inv = new Invoice("Factuur 1", null, new BigDecimal("100.00"),
                LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 31), 1, 2025, Invoice.InvoiceStatus.OPEN, student);
        ReflectionTestUtils.setField(inv, "id", 1L);
        when(invoiceRepository.findById(1L)).thenReturn(Optional.of(inv));

        // Act
        InvoiceResponseDTO result = invoiceService.getInvoiceByIdForCaller(1L, "admin@villavredestein.com", true);

        // Assert
        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    void getInvoiceByIdForCaller_student_canAccessOwnInvoice() {
        // Arrange
        User student = new User("student", "student@villavredestein.com", "hash", User.Role.STUDENT);
        Invoice inv = new Invoice("Factuur 1", null, new BigDecimal("100.00"),
                LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 31), 1, 2025, Invoice.InvoiceStatus.OPEN, student);
        ReflectionTestUtils.setField(inv, "id", 1L);
        when(invoiceRepository.findById(1L)).thenReturn(Optional.of(inv));

        // Act
        InvoiceResponseDTO result = invoiceService.getInvoiceByIdForCaller(1L, "student@villavredestein.com", false);

        // Assert
        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    void getInvoiceByIdForCaller_student_cannotAccessOtherInvoice_throwsAccessDenied() {
        // Arrange
        User student = new User("student", "student@villavredestein.com", "hash", User.Role.STUDENT);
        Invoice inv = new Invoice("Factuur 1", null, new BigDecimal("100.00"),
                LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 31), 1, 2025, Invoice.InvoiceStatus.OPEN, student);
        ReflectionTestUtils.setField(inv, "id", 1L);
        when(invoiceRepository.findById(1L)).thenReturn(Optional.of(inv));

        // Act & Assert
        assertThrows(AccessDeniedException.class,
                () -> invoiceService.getInvoiceByIdForCaller(1L, "ander@villavredestein.com", false));
    }

    // =====================================================================
    // # getInvoicesForStudent
    // =====================================================================

    @Test
    void getInvoicesForStudent_withValidEmail_returnsDtoList() {
        // Arrange
        User student = new User("student", "student@villavredestein.com", "hash", User.Role.STUDENT);
        Invoice inv = new Invoice("Factuur 1", null, new BigDecimal("100.00"),
                LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 31), 1, 2025, Invoice.InvoiceStatus.OPEN, student);
        ReflectionTestUtils.setField(inv, "id", 1L);
        when(invoiceRepository.findByStudent_EmailIgnoreCaseOrderByIdDesc("student@villavredestein.com"))
                .thenReturn(List.of(inv));

        // Act
        List<InvoiceResponseDTO> result = invoiceService.getInvoicesForStudent("student@villavredestein.com");

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getStudentEmail()).isEqualTo("student@villavredestein.com");
    }

    @Test
    void getInvoicesForStudent_withNullEmail_throwsIllegalArgumentException() {
        // Arrange / Act / Assert
        assertThrows(IllegalArgumentException.class, () -> invoiceService.getInvoicesForStudent(null));
    }

    @Test
    void getInvoicesForStudent_withBlankEmail_throwsIllegalArgumentException() {
        // Arrange / Act / Assert
        assertThrows(IllegalArgumentException.class, () -> invoiceService.getInvoicesForStudent("   "));
    }

    // =====================================================================
    // # updateStatus
    // =====================================================================

    @Test
    void updateStatus_existing_updatesAndReturnsDto() {
        // Arrange
        User student = new User("student", "student@villavredestein.com", "hash", User.Role.STUDENT);
        Invoice inv = new Invoice("Factuur 1", null, new BigDecimal("150.00"),
                LocalDate.of(2025, 3, 1), LocalDate.of(2025, 3, 31), 3, 2025, Invoice.InvoiceStatus.OPEN, student);
        ReflectionTestUtils.setField(inv, "id", 1L);
        when(invoiceRepository.findById(1L)).thenReturn(Optional.of(inv));

        // Act
        InvoiceResponseDTO result = invoiceService.updateStatus(1L, "paid");

        // Assert
        assertThat(result.getStatus()).isEqualTo("PAID");
    }

    @Test
    void updateStatus_notExisting_throwsEntityNotFoundException() {
        // Arrange
        when(invoiceRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> invoiceService.updateStatus(99L, "paid"));
    }

    @Test
    void updateStatus_withNullStatus_throwsIllegalArgumentException() {
        // Arrange / Act / Assert
        assertThrows(IllegalArgumentException.class, () -> invoiceService.updateStatus(1L, (String) null));
    }

    @Test
    void updateStatus_withInvalidStatus_throwsIllegalArgumentException() {
        // Arrange
        User student = new User("student", "student@villavredestein.com", "hash", User.Role.STUDENT);
        Invoice inv = new Invoice("Factuur 1", null, new BigDecimal("150.00"),
                LocalDate.of(2025, 3, 1), LocalDate.of(2025, 3, 31), 3, 2025, Invoice.InvoiceStatus.OPEN, student);
        ReflectionTestUtils.setField(inv, "id", 1L);
        when(invoiceRepository.findById(1L)).thenReturn(Optional.of(inv));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> invoiceService.updateStatus(1L, "ONBEKEND"));
    }

    // =====================================================================
    // # deleteInvoice
    // =====================================================================

    @Test
    void deleteInvoice_existing_deletes() {
        // Arrange
        User student = new User("student", "student@villavredestein.com", "hash", User.Role.STUDENT);
        Invoice inv = new Invoice("Factuur 1", null, new BigDecimal("150.00"),
                LocalDate.of(2025, 3, 1), LocalDate.of(2025, 3, 31), 3, 2025, Invoice.InvoiceStatus.OPEN, student);
        ReflectionTestUtils.setField(inv, "id", 1L);
        when(invoiceRepository.findById(1L)).thenReturn(Optional.of(inv));

        // Act & Assert
        assertDoesNotThrow(() -> invoiceService.deleteInvoice(1L));
        verify(invoiceRepository).delete(inv);
    }

    @Test
    void deleteInvoice_notExisting_throwsEntityNotFoundException() {
        // Arrange
        when(invoiceRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> invoiceService.deleteInvoice(99L));
        verify(invoiceRepository, never()).deleteById(any());
    }

    // =====================================================================
    // # saveReminderMeta
    // =====================================================================

    @Test
    void saveReminderMeta_validInvoice_saves() {
        // Arrange
        User student = new User("student", "student@villavredestein.com", "hash", User.Role.STUDENT);
        Invoice inv = new Invoice("Factuur 1", null, new BigDecimal("100.00"),
                LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 31), 1, 2025, Invoice.InvoiceStatus.OPEN, student);
        ReflectionTestUtils.setField(inv, "id", 1L);
        when(invoiceRepository.save(inv)).thenReturn(inv);

        // Act
        invoiceService.saveReminderMeta(inv);

        // Assert
        verify(invoiceRepository).save(inv);
    }

    @Test
    void saveReminderMeta_nullInvoice_throwsIllegalArgumentException() {
        // Arrange / Act / Assert
        assertThrows(IllegalArgumentException.class, () -> invoiceService.saveReminderMeta(null));
    }

    @Test
    void saveReminderMeta_invoiceWithoutId_throwsIllegalArgumentException() {
        // Arrange
        User student = new User("student", "student@villavredestein.com", "hash", User.Role.STUDENT);
        Invoice inv = new Invoice("Factuur 1", null, new BigDecimal("100.00"),
                LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 31), 1, 2025, Invoice.InvoiceStatus.OPEN, student);
        // id is null: niet gepersisteerd

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> invoiceService.saveReminderMeta(inv));
    }

    // =====================================================================
    // # getAllOpenInvoices
    // =====================================================================

    @Test
    void getAllOpenInvoices_returnsOnlyOpenInvoices() {
        // Arrange
        User student = new User("student", "student@villavredestein.com", "hash", User.Role.STUDENT);
        Invoice open1 = new Invoice("Inv1", null, new BigDecimal("10.00"),
                LocalDate.now(), LocalDate.now().plusDays(10),
                LocalDate.now().getMonthValue(), LocalDate.now().getYear(), Invoice.InvoiceStatus.OPEN, student);
        Invoice open2 = new Invoice("Inv2", null, new BigDecimal("20.00"),
                LocalDate.now(), LocalDate.now().plusDays(20),
                LocalDate.now().getMonthValue(), LocalDate.now().getYear(), Invoice.InvoiceStatus.OPEN, student);
        when(invoiceRepository.findByStatusOrderByIdDesc(Invoice.InvoiceStatus.OPEN))
                .thenReturn(List.of(open2, open1));

        // Act
        List<Invoice> result = invoiceService.getAllOpenInvoices();

        // Assert
        assertThat(result).allMatch(i -> i.getStatus() == Invoice.InvoiceStatus.OPEN);
        verify(invoiceRepository).findByStatusOrderByIdDesc(Invoice.InvoiceStatus.OPEN);
    }

    // =====================================================================
    // # getUpcomingInvoices
    // =====================================================================

    @Test
    void getUpcomingInvoices_returnsUpcomingOpenInvoices() {
        // Arrange
        User student = new User("student", "student@villavredestein.com", "hash", User.Role.STUDENT);
        Invoice upcoming = new Invoice("Soon", null, new BigDecimal("10.00"),
                LocalDate.now(), LocalDate.now().plusDays(3),
                LocalDate.now().getMonthValue(), LocalDate.now().getYear(), Invoice.InvoiceStatus.OPEN, student);
        when(invoiceRepository.findByStatusAndDueDateBetweenOrderByDueDateAsc(
                eq(Invoice.InvoiceStatus.OPEN), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(List.of(upcoming));

        // Act
        List<Invoice> result = invoiceService.getUpcomingInvoices();

        // Assert
        assertThat(result).containsExactly(upcoming);
        verify(invoiceRepository).findByStatusAndDueDateBetweenOrderByDueDateAsc(
                eq(Invoice.InvoiceStatus.OPEN), any(LocalDate.class), any(LocalDate.class));
    }
}