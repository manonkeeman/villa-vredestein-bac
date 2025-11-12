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

/**
 * Service die verantwoordelijk is voor het aanmaken, beheren en ophalen van facturen.
 */
@Service
public class InvoiceService {

    private static final Logger log = LoggerFactory.getLogger(InvoiceService.class);

    private final InvoiceRepository invoiceRepository;
    private final UserRepository userRepository;

    public InvoiceService(InvoiceRepository invoiceRepository, UserRepository userRepository) {
        this.invoiceRepository = invoiceRepository;
        this.userRepository = userRepository;
    }

    /** Maak een nieuwe factuur aan voor een student. */
    public InvoiceResponseDTO createInvoice(InvoiceRequestDTO dto) {
        User student = userRepository.findByEmail(dto.getStudentEmail())
                .orElseThrow(() -> new IllegalArgumentException("Student niet gevonden: " + dto.getStudentEmail()));

        Invoice invoice = new Invoice();
        invoice.setTitle(dto.getTitle());
        invoice.setDescription(dto.getDescription());
        invoice.setAmount(dto.getAmount());
        invoice.setIssueDate(dto.getIssueDate() != null ? dto.getIssueDate() : LocalDate.now());
        invoice.setDueDate(dto.getDueDate());
        invoice.setStatus("OPEN");
        invoice.setStudent(student);

        Invoice saved = invoiceRepository.save(invoice);
        log.info("📄 Nieuwe factuur aangemaakt voor {}: €{}", student.getEmail(), saved.getAmount());
        return toDTO(saved);
    }

    /** Haal alle facturen op. */
    public List<InvoiceResponseDTO> getAllInvoices() {
        return invoiceRepository.findAll()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /** Update de status van een factuur. */
    public InvoiceResponseDTO updateStatus(Long id, String newStatus) {
        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Factuur niet gevonden: " + id));

        invoice.setStatus(newStatus.toUpperCase());
        Invoice updated = invoiceRepository.save(invoice);
        return toDTO(updated);
    }

    /** Verwijder een factuur. */
    public void deleteInvoice(Long id) {
        if (!invoiceRepository.existsById(id)) {
            throw new IllegalArgumentException("Factuur niet gevonden: " + id);
        }
        invoiceRepository.deleteById(id);
    }

    /** Haal alle openstaande facturen op. */
    public List<Invoice> getAllOpenInvoices() {
        return invoiceRepository.findByStatusIgnoreCase("OPEN");
    }

    /** Haal facturen op die binnen 4 dagen vervallen. */
    public List<Invoice> getUpcomingInvoices() {
        LocalDate today = LocalDate.now();
        LocalDate inFourDays = today.plusDays(4);
        return invoiceRepository.findByStatusIgnoreCaseAndDueDateBetween("OPEN", today, inFourDays);
    }

    InvoiceResponseDTO toDTO(Invoice invoice) {
        if (invoice == null) {
            throw new IllegalArgumentException("Invoice mag niet null zijn");
        }
        if (invoice.getStudent() == null) {
            throw new IllegalArgumentException("Invoice heeft geen gekoppelde student");
        }
        return new InvoiceResponseDTO(
                invoice.getId(),
                invoice.getTitle(),
                invoice.getAmount(),
                invoice.getDueDate(),
                invoice.getStatus(),
                invoice.isReminderSent(),
                invoice.getStudent().getUsername(),
                invoice.getStudent().getEmail()
        );
    }
}