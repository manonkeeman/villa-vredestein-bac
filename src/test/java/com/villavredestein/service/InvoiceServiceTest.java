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
import java.math.BigDecimal;

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
                new BigDecimal("500.00"),
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
        assertThat(result.getAmount()).isEqualByComparingTo(new BigDecimal("500.00"));
        assertThat(result.getStudentEmail()).isEqualTo("student@example.com");
        verify(userRepository).findByEmail("student@example.com");
        verify(invoiceRepository).save(any(Invoice.class));
    }

    @Test
    void createInvoice_studentNotFound_throwsIllegalArgumentException() {
        InvoiceRequestDTO dto = new InvoiceRequestDTO(
                "Huur juli",
                "Huur kamer 2 juli",
                new BigDecimal("500.00"),
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
        inv1.setAmount(new BigDecimal("100.00"));
        inv1.setDueDate(LocalDate.of(2025, 1, 31));
        inv1.setStatus("OPEN");
        inv1.setStudent(student1);

        Invoice inv2 = new Invoice();
        inv2.setId(2L);
        inv2.setTitle("Factuur 2");
        inv2.setAmount(new BigDecimal("200.00"));
        inv2.setDueDate(LocalDate.of(2025, 2, 28));
        inv2.setStatus("PAID");
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
        inv.setAmount(new BigDecimal("150.00"));
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

        InvoiceResponseDTO result = invoiceService.updateStatus(1L, "paid");

        assertThat(result.getStatus()).isEqualTo("PAID");
        verify(invoiceRepository).findById(1L);
        verify(invoiceRepository).save(inv);
    }

    @Test
    void updateStatus_notExisting_throwsIllegalArgumentException() {
        when(invoiceRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> invoiceService.updateStatus(99L, "paid"));

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
        inv1.setStatus("OPEN");
        inv1.setDueDate(LocalDate.now().plusDays(10));

        Invoice inv2 = new Invoice();
        inv2.setStatus("OPEN");
        inv2.setDueDate(LocalDate.now().plusDays(20));

        Invoice inv3 = new Invoice();
        inv3.setStatus("PAID");
        inv3.setDueDate(LocalDate.now().plusDays(5));

        when(invoiceRepository.findAll()).thenReturn(List.of(inv1, inv2, inv3));

        List<Invoice> result = invoiceService.getAllOpenInvoices();

        assertThat(result).hasSize(2);
        verify(invoiceRepository).findAll();
    }

    @Test
    void getUpcomingInvoices_returnsListFromRepository() {
        // Upcoming = OPEN en dueDate binnenkort (service gebruikt LocalDate.now())
        Invoice inv1 = new Invoice();
        inv1.setStatus("OPEN");
        inv1.setDueDate(LocalDate.now().plusDays(3));

        Invoice inv2 = new Invoice();
        inv2.setStatus("OPEN");
        inv2.setDueDate(LocalDate.now().plusDays(30));

        Invoice inv3 = new Invoice();
        inv3.setStatus("PAID");
        inv3.setDueDate(LocalDate.now().plusDays(2));

        when(invoiceRepository.findAll()).thenReturn(List.of(inv1, inv2, inv3));

        List<Invoice> result = invoiceService.getUpcomingInvoices();

        assertThat(result).hasSize(1);
        verify(invoiceRepository).findAll();
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