package com.villavredestein.service;

import com.villavredestein.dto.InvoiceRequestDTO;
import com.villavredestein.dto.InvoiceResponseDTO;
import com.villavredestein.model.Invoice;
import com.villavredestein.model.User;
import com.villavredestein.repository.InvoiceRepository;
import com.villavredestein.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class InvoiceService {

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
                .orElseThrow(() -> new RuntimeException("Student niet gevonden"));

        Invoice invoice = new Invoice();
        invoice.setTitle(dto.getTitle());
        invoice.setDescription(dto.getDescription());
        invoice.setAmount(dto.getAmount());
        invoice.setIssueDate(dto.getIssueDate());
        invoice.setDueDate(dto.getDueDate());
        invoice.setStudent(student);
        invoice.setStatus("OPEN");

        Invoice saved = invoiceRepository.save(invoice);
        return toDTO(saved);
    }

    /** 🔹 Extra method voor StudentController */
    public List<InvoiceResponseDTO> getInvoicesByStudentEmail(String email) {
        return invoiceRepository.findAll().stream()
                .filter(i -> i.getStudent() != null && email.equalsIgnoreCase(i.getStudent().getEmail()))
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /** 🔹 Extra methods voor AdminController */
    public InvoiceResponseDTO updateStatus(Long id, String newStatus) {
        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Factuur niet gevonden"));
        invoice.setStatus(newStatus);
        return toDTO(invoiceRepository.save(invoice));
    }

    public void deleteInvoice(Long id) {
        if (invoiceRepository.existsById(id)) {
            invoiceRepository.deleteById(id);
        }
    }

    private InvoiceResponseDTO toDTO(Invoice invoice) {
        return new InvoiceResponseDTO(
                invoice.getId(),
                invoice.getTitle(),
                invoice.getAmount(),
                invoice.getDueDate(),
                invoice.getStatus(),
                invoice.isReminderSent()
        );
    }
}