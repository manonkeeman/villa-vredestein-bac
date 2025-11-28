package com.villavredestein.service;

import com.villavredestein.dto.InvoiceRequestDTO;
import com.villavredestein.dto.InvoiceResponseDTO;
import com.villavredestein.model.Invoice;
import com.villavredestein.model.User;
import com.villavredestein.repository.InvoiceRepository;
import com.villavredestein.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InvoiceServiceTest {

    @Mock
    InvoiceRepository invoiceRepository;

    @Mock
    UserRepository userRepository;

    @InjectMocks
    InvoiceService invoiceService;

    @Test
    void createInvoice_withExistingStudent_savesAndReturnsDto() {
        InvoiceRequestDTO dto = new InvoiceRequestDTO(
                "Huur juli",
                "Huur kamer 2 juli",
                500.0,
                null,
                LocalDate.of(2025, 7, 31),
                "student@example.com"
        );

        User student = new User();
        student.setEmail("student@example.com");
        student.setUsername("student1");

        when(userRepository.findByEmail("student@example.com"))
                .thenReturn(Optional.of(student));

        Invoice saved = new Invoice();
        saved.setId(1L);
        saved.setTitle(dto.getTitle());
        saved.setAmount(dto.getAmount());
        saved.setDueDate(dto.getDueDate());
        saved.setStatus("OPEN");
        saved.setStudent(student);

        when(invoiceRepository.save(any(Invoice.class)))
                .thenReturn(saved);

        InvoiceResponseDTO result = invoiceService.createInvoice(dto);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getTitle()).isEqualTo("Huur juli");
        assertThat(result.getAmount()).isEqualTo(500.0);
        assertThat(result.getStudentEmail()).isEqualTo("student@example.com");
        verify(userRepository).findByEmail("student@example.com");
        verify(invoiceRepository).save(any(Invoice.class));
    }

    @Test
    void createInvoice_studentNotFound_throwsIllegalArgumentException() {
        InvoiceRequestDTO dto = new InvoiceRequestDTO(
                "Huur juli",
                "Huur kamer 2 juli",
                500.0,
                null,
                LocalDate.of(2025, 7, 31),
                "missing@example.com"
        );

        when(userRepository.findByEmail("missing@example.com"))
                .thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> invoiceService.createInvoice(dto));

        verify(userRepository).findByEmail("missing@example.com");
        verify(invoiceRepository, never()).save(any(Invoice.class));
    }

    @Test
    void getAllInvoices_returnsMappedDtoList() {
        User student1 = new User();
        student1.setUsername("student1");
        student1.setEmail("s1@example.com");

        User student2 = new User();
        student2.setUsername("student2");
        student2.setEmail("s2@example.com");

        Invoice inv1 = new Invoice();
        inv1.setId(1L);
        inv1.setTitle("Factuur 1");
        inv1.setAmount(100.0);
        inv1.setDueDate(LocalDate.of(2025, 1, 31));
        inv1.setStatus("OPEN");
        inv1.setStudent(student1);

        Invoice inv2 = new Invoice();
        inv2.setId(2L);
        inv2.setTitle("Factuur 2");
        inv2.setAmount(200.0);
        inv2.setDueDate(LocalDate.of(2025, 2, 28));
        inv2.setStatus("BETAALD");
        inv2.setStudent(student2);

        when(invoiceRepository.findAll()).thenReturn(List.of(inv1, inv2));

        List<InvoiceResponseDTO> result = invoiceService.getAllInvoices();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getId()).isEqualTo(1L);
        assertThat(result.get(0).getStudentEmail()).isEqualTo("s1@example.com");
        assertThat(result.get(1).getId()).isEqualTo(2L);
        assertThat(result.get(1).getStudentEmail()).isEqualTo("s2@example.com");
        verify(invoiceRepository).findAll();
    }

    @Test
    void getInvoiceById_existing_returnsDto() {
        User student = new User();
        student.setUsername("student1");
        student.setEmail("s1@example.com");

        Invoice inv = new Invoice();
        inv.setId(1L);
        inv.setTitle("Factuur 1");
        inv.setAmount(150.0);
        inv.setDueDate(LocalDate.of(2025, 3, 31));
        inv.setStatus("OPEN");
        inv.setStudent(student);

        when(invoiceRepository.findById(1L)).thenReturn(Optional.of(inv));

        InvoiceResponseDTO result = invoiceService.getInvoiceById(1L);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getTitle()).isEqualTo("Factuur 1");
        assertThat(result.getStudentEmail()).isEqualTo("s1@example.com");
        verify(invoiceRepository).findById(1L);
    }

    @Test
    void getInvoiceById_notExisting_throwsIllegalArgumentException() {
        when(invoiceRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> invoiceService.getInvoiceById(99L));

        verify(invoiceRepository).findById(99L);
    }

    @Test
    void updateStatus_existing_updatesAndReturnsDto() {
        User student = new User();
        student.setUsername("student1");
        student.setEmail("s1@example.com");

        Invoice inv = new Invoice();
        inv.setId(1L);
        inv.setStatus("OPEN");
        inv.setStudent(student);

        when(invoiceRepository.findById(1L)).thenReturn(Optional.of(inv));
        when(invoiceRepository.save(inv)).thenReturn(inv);

        InvoiceResponseDTO result = invoiceService.updateStatus(1L, "betaald");

        assertThat(result.getStatus()).isEqualTo("BETAALD");
        verify(invoiceRepository).findById(1L);
        verify(invoiceRepository).save(inv);
    }

    @Test
    void updateStatus_notExisting_throwsIllegalArgumentException() {
        when(invoiceRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> invoiceService.updateStatus(99L, "betaald"));

        verify(invoiceRepository).findById(99L);
        verify(invoiceRepository, never()).save(any(Invoice.class));
    }

    @Test
    void deleteInvoice_existing_deletes() {
        when(invoiceRepository.existsById(1L)).thenReturn(true);

        invoiceService.deleteInvoice(1L);

        verify(invoiceRepository).existsById(1L);
        verify(invoiceRepository).deleteById(1L);
    }

    @Test
    void deleteInvoice_notExisting_throwsIllegalArgumentException() {
        when(invoiceRepository.existsById(99L)).thenReturn(false);

        assertThrows(IllegalArgumentException.class,
                () -> invoiceService.deleteInvoice(99L));

        verify(invoiceRepository).existsById(99L);
        verify(invoiceRepository, never()).deleteById(anyLong());
    }

    @Test
    void getAllOpenInvoices_returnsListFromRepository() {
        Invoice inv1 = new Invoice();
        Invoice inv2 = new Invoice();
        when(invoiceRepository.findByStatusIgnoreCase("OPEN"))
                .thenReturn(List.of(inv1, inv2));

        List<Invoice> result = invoiceService.getAllOpenInvoices();

        assertThat(result).hasSize(2);
        verify(invoiceRepository).findByStatusIgnoreCase("OPEN");
    }

    @Test
    void getUpcomingInvoices_returnsListFromRepository() {
        Invoice inv1 = new Invoice();
        when(invoiceRepository.findByStatusIgnoreCaseAndDueDateBetween(
                eq("OPEN"), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(List.of(inv1));

        List<Invoice> result = invoiceService.getUpcomingInvoices();

        assertThat(result).hasSize(1);
        verify(invoiceRepository)
                .findByStatusIgnoreCaseAndDueDateBetween(
                        eq("OPEN"), any(LocalDate.class), any(LocalDate.class));
    }

    @Test
    void toDTO_nullInvoice_throwsIllegalArgumentException() {
        // Let op: hiervoor moet toDTO in InvoiceService NIET meer private zijn
        assertThrows(IllegalArgumentException.class,
                () -> invoiceService.toDTO(null));
    }

    @Test
    void toDTO_invoiceWithoutStudent_throwsIllegalArgumentException() {
        Invoice invoice = new Invoice();
        assertThrows(IllegalArgumentException.class,
                () -> invoiceService.toDTO(invoice));
    }
}