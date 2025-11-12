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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InvoiceServiceTest {

    @Mock
    private InvoiceRepository invoiceRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private InvoiceService invoiceService;

    @Test
    void createInvoice_ShouldSaveWithOpenStatus() {
        when(userRepository.findByEmail("student@villa.nl"))
                .thenReturn(Optional.of(new User("student", "student@villa.nl", "ROLE_STUDENT", "pwd")));
        when(invoiceRepository.save(any(Invoice.class))).thenAnswer(invocation -> invocation.getArgument(0));

        InvoiceRequestDTO dto = new InvoiceRequestDTO(
                "Titel", "Desc", 1200.0, LocalDate.now(), LocalDate.now().plusDays(7), "student@villa.nl");

        InvoiceResponseDTO result = invoiceService.createInvoice(dto);

        assertEquals("OPEN", result.getStatus());
        verify(invoiceRepository).save(any(Invoice.class));
    }

    @Test
    void createInvoice_ShouldThrow_WhenUserNotFound() {
        InvoiceRequestDTO dto = new InvoiceRequestDTO(
                "Titel", "Desc", 500.0, LocalDate.now(), LocalDate.now().plusDays(7), "nietgevonden@villa.nl");
        when(userRepository.findByEmail("nietgevonden@villa.nl")).thenReturn(Optional.empty());
        org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class, () -> invoiceService.createInvoice(dto));
    }

    @Test
    void createInvoice_ShouldSetCurrentDate_WhenIssueDateIsNull() {
        User user = new User("student", "student@villa.nl", "ROLE_STUDENT", "pwd");
        when(userRepository.findByEmail("student@villa.nl")).thenReturn(Optional.of(user));
        when(invoiceRepository.save(any(Invoice.class))).thenAnswer(invocation -> invocation.getArgument(0));
        InvoiceRequestDTO dto = new InvoiceRequestDTO(
                "Titel", "Desc", 300.0, null, LocalDate.now().plusDays(5), "student@villa.nl");
        InvoiceResponseDTO result = invoiceService.createInvoice(dto);
        assertEquals("OPEN", result.getStatus());
        verify(invoiceRepository).save(any(Invoice.class));
    }

    @Test
    void toDTO_ShouldThrow_WhenInvoiceIsNull() {
        assertThrows(IllegalArgumentException.class, () -> invoiceService.toDTO(null));
    }

    @Test
    void toDTO_ShouldThrow_WhenStudentIsNull() {
        Invoice invoice = new Invoice();
        invoice.setId(1L);
        invoice.setTitle("Test");
        invoice.setAmount(500.0);
        invoice.setStatus("OPEN");
        assertThrows(IllegalArgumentException.class, () -> invoiceService.toDTO(invoice));
    }

    @Test
    void updateStatus_ShouldChangeStatusToPaid() {
        User student = new User("student", "student@villa.nl", "ROLE_STUDENT", "pwd");
        Invoice invoice = new Invoice();
        invoice.setId(1L);
        invoice.setStatus("OPEN");
        invoice.setStudent(student);
        when(invoiceRepository.findById(1L)).thenReturn(Optional.of(invoice));
        when(invoiceRepository.save(any(Invoice.class))).thenAnswer(invocation -> invocation.getArgument(0));

        invoiceService.updateStatus(1L, "BETAALD");

        assertEquals("BETAALD", invoice.getStatus());
        verify(invoiceRepository).save(invoice);
    }

    @Test
    void deleteInvoice_ShouldCallRepositoryDelete() {
        Long id = 5L;
        when(invoiceRepository.existsById(id)).thenReturn(true);
        invoiceService.deleteInvoice(id);
        verify(invoiceRepository).deleteById(id);
    }

    @Test
    void getAllInvoices_ShouldReturnList() {
        User student = new User("student", "student@villa.nl", "ROLE_STUDENT", "pwd");
        Invoice invoice1 = new Invoice();
        invoice1.setTitle("Januari");
        invoice1.setStudent(student);
        Invoice invoice2 = new Invoice();
        invoice2.setTitle("Februari");
        invoice2.setStudent(student);
        when(invoiceRepository.findAll()).thenReturn(java.util.List.of(invoice1, invoice2));
        var invoices = invoiceService.getAllInvoices();
        assertEquals(2, invoices.size());
        assertEquals("Januari", invoices.get(0).getTitle());
    }
}