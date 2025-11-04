package com.villavredestein.service;

import com.villavredestein.dto.InvoiceRequestDTO;
import com.villavredestein.dto.InvoiceResponseDTO;
import com.villavredestein.model.Invoice;
import com.villavredestein.model.User;
import com.villavredestein.repository.InvoiceRepository;
import com.villavredestein.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class InvoiceService {

    private static final Logger log = LoggerFactory.getLogger(InvoiceService.class);

    private final InvoiceRepository invoiceRepository;
    private final UserRepository userRepository;

    public InvoiceService(InvoiceRepository invoiceRepository, UserRepository userRepository) {
        this.invoiceRepository = invoiceRepository;
        this.userRepository = userRepository;
    }

    public List<InvoiceResponseDTO> getAllInvoices() {
        return invoiceRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public InvoiceResponseDTO createInvoice(InvoiceRequestDTO dto) {
        User student = userRepository.findByEmail(dto.getStudentEmail())
                .orElseThrow(() -> new RuntimeException("Student niet gevonden: " + dto.getStudentEmail()));

        Invoice invoice = new Invoice();
        invoice.setTitle(dto.getTitle());
        invoice.setDescription(dto.getDescription());
        invoice.setAmount(dto.getAmount());
        invoice.setIssueDate(dto.getIssueDate());
        invoice.setDueDate(dto.getDueDate());
        invoice.setStudent(student);
        invoice.setStatus("OPEN");
        invoice.setReminderSent(false);

        Invoice saved = invoiceRepository.save(invoice);
        log.info("📄 Nieuwe factuur aangemaakt voor {}: €{}", student.getUsername(), dto.getAmount());
        return toDTO(saved);
    }

    public InvoiceResponseDTO updateStatus(Long id, String newStatus) {
        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Factuur niet gevonden: " + id));

        invoice.setStatus(newStatus.toUpperCase());
        Invoice updated = invoiceRepository.save(invoice);

        log.info("🔄 Factuur {} status gewijzigd naar {}", id, updated.getStatus());
        return toDTO(updated);
    }

    public void deleteInvoice(Long id) {
        invoiceRepository.findById(id).ifPresent(inv -> {
            invoiceRepository.delete(inv);
            log.info("🗑️ Factuur {} verwijderd", id);
        });
    }

    public List<Invoice> getAllOpenInvoices() {
        return invoiceRepository.findByStatusIgnoreCase("OPEN");
    }

    public List<Invoice> getOverdueInvoices() {
        return invoiceRepository.findByStatusIgnoreCaseAndDueDateBefore("OPEN", LocalDate.now());
    }

    public List<Invoice> getUpcomingInvoices() {
        LocalDate today = LocalDate.now();
        LocalDate inFourDays = today.plusDays(4);
        return invoiceRepository.findByStatusIgnoreCaseAndDueDateBetween("OPEN", today, inFourDays);
    }

    private InvoiceResponseDTO toDTO(Invoice invoice) {
        String studentName = invoice.getStudent() != null ? invoice.getStudent().getUsername() : null;
        String studentEmail = invoice.getStudent() != null ? invoice.getStudent().getEmail() : null;

        return new InvoiceResponseDTO(
                invoice.getId(),
                invoice.getTitle(),
                invoice.getAmount(),
                invoice.getDueDate(),
                invoice.getStatus(),
                invoice.isReminderSent(),
                studentName,
                studentEmail
        );
    }
}