package com.villavredestein.service;

import com.villavredestein.dto.InvoiceRequestDTO;
import com.villavredestein.dto.InvoiceResponseDTO;
import com.villavredestein.model.Invoice;
import com.villavredestein.model.Invoice.InvoiceStatus;
import com.villavredestein.model.User;
import com.villavredestein.repository.InvoiceRepository;
import com.villavredestein.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Service
@Transactional
public class InvoiceService {

    private static final Logger log = LoggerFactory.getLogger(InvoiceService.class);

    private final InvoiceRepository invoiceRepository;
    private final UserRepository userRepository;
    private final InvoicePdfService invoicePdfService;

    public InvoiceService(InvoiceRepository invoiceRepository,
                          UserRepository userRepository,
                          InvoicePdfService invoicePdfService) {
        this.invoiceRepository = invoiceRepository;
        this.userRepository = userRepository;
        this.invoicePdfService = invoicePdfService;
    }


    public InvoiceResponseDTO createInvoice(InvoiceRequestDTO dto) {

        User student = userRepository.findByEmailIgnoreCase(dto.getStudentEmail())
                .orElseThrow(() ->
                        new EntityNotFoundException("Student niet gevonden: " + dto.getStudentEmail()));

        LocalDate issueDate = (dto.getIssueDate() != null) ? dto.getIssueDate() : LocalDate.now();

        Invoice invoice = new Invoice(
                dto.getTitle(),
                dto.getDescription(),
                dto.getAmount(),
                issueDate,
                dto.getDueDate(),
                issueDate.getMonthValue(),
                issueDate.getYear(),
                InvoiceStatus.OPEN,
                student
        );

        if (invoiceRepository.existsByStudentAndInvoiceMonthAndInvoiceYear(student, invoice.getInvoiceMonth(), invoice.getInvoiceYear())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Er bestaat al een factuur voor deze student in " + invoice.getInvoiceMonth() + "-" + invoice.getInvoiceYear()
            );
        }

        Invoice saved = invoiceRepository.save(invoice);

        log.info("📄 Factuur aangemaakt (invoiceId={}, student={}, amount={})", saved.getId(), safe(student.getEmail()), saved.getAmount());

        return toDTO(saved);
    }


    public List<InvoiceResponseDTO> getAllInvoices() {
        return invoiceRepository.findAllByOrderByIdDesc()
                .stream()
                .map(this::toDTO)
                .toList();
    }

    public InvoiceResponseDTO getInvoiceById(Long id) {
        return toDTO(findInvoiceOrThrow(id));
    }

    public InvoiceResponseDTO getInvoiceByIdForCaller(Long id, String callerEmail, boolean isAdmin) {
        Invoice invoice = findInvoiceOrThrow(id);

        if (!isAdmin && !invoice.getStudent().getEmail().equalsIgnoreCase(callerEmail)) {
            throw new AccessDeniedException("You can only view your own invoices");
        }

        return toDTO(invoice);
    }

    public List<InvoiceResponseDTO> getInvoicesForStudent(String studentEmail) {
        if (studentEmail == null || studentEmail.isBlank()) {
            throw new IllegalArgumentException("studentEmail is verplicht");
        }
        return invoiceRepository.findByStudent_EmailIgnoreCaseOrderByIdDesc(studentEmail.trim())
                .stream()
                .map(this::toDTO)
                .toList();
    }


    public InvoiceResponseDTO updateStatus(Long id, InvoiceStatus newStatus) {

        Invoice invoice = findInvoiceOrThrow(id);

        invoice.setStatus(newStatus);

        log.info("📌 Factuurstatus gewijzigd (invoiceId={}, status={})", id, newStatus);

        return toDTO(invoice);
    }

    public InvoiceResponseDTO updateStatus(Long id, String newStatus) {
        if (newStatus == null || newStatus.isBlank()) {
            throw new IllegalArgumentException("Status is verplicht");
        }
        try {
            return updateStatus(id, InvoiceStatus.valueOf(newStatus.trim().toUpperCase(Locale.ROOT)));
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Ongeldige status: " + newStatus + ". Toegestaan: OPEN, PAID, OVERDUE, CANCELLED");
        }
    }


    public void deleteInvoice(Long id) {
        Invoice invoice = findInvoiceOrThrow(id);
        invoiceRepository.delete(invoice);
        log.warn("🗑️ Factuur verwijderd (invoiceId={})", id);
    }


    public List<Invoice> getAllOpenInvoices() {
        return invoiceRepository.findByStatusOrderByIdDesc(InvoiceStatus.OPEN);
    }

    public List<Invoice> getUpcomingInvoices() {
        LocalDate today = LocalDate.now();
        LocalDate inFourDays = today.plusDays(4);
        return invoiceRepository.findByStatusAndDueDateBetweenOrderByDueDateAsc(InvoiceStatus.OPEN, today, inFourDays);
    }

    public byte[] generatePdf(Long id, String callerEmail, boolean isAdmin) {
        Invoice invoice = findInvoiceOrThrow(id);
        if (!isAdmin && !invoice.getStudent().getEmail().equalsIgnoreCase(callerEmail)) {
            throw new AccessDeniedException("You can only download your own invoices");
        }
        return invoicePdfService.generate(invoice);
    }

    public void saveReminderMeta(Invoice invoice) {
        if (invoice == null || invoice.getId() == null) {
            throw new IllegalArgumentException("Invoice ontbreekt of heeft geen id");
        }
        invoiceRepository.save(invoice);
    }


    private Invoice findInvoiceOrThrow(Long id) {
        return invoiceRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Factuur niet gevonden: " + id));
    }


    public void attachMolliePayment(Invoice invoice, String mollieId, String checkoutUrl) {
        invoice.setMolliePaymentId(mollieId);
        invoice.setCheckoutUrl(checkoutUrl);
        invoiceRepository.save(invoice);
    }

    public Optional<Invoice> findByMolliePaymentId(String mollieId) {
        return invoiceRepository.findByMolliePaymentId(mollieId);
    }

    public Invoice getRawById(Long id) {
        return findInvoiceOrThrow(id);
    }

    public List<Invoice> getUnpaidForMonth(int month, int year) {
        return invoiceRepository.findByInvoiceMonthAndInvoiceYearAndStatusNotIn(
                month, year,
                List.of(Invoice.InvoiceStatus.PAID, Invoice.InvoiceStatus.CANCELLED)
        );
    }

    private InvoiceResponseDTO toDTO(Invoice invoice) {
        return new InvoiceResponseDTO(
                invoice.getId(),
                invoice.getTitle(),
                invoice.getDescription(),
                invoice.getAmount(),
                invoice.getIssueDate(),
                invoice.getDueDate(),
                invoice.getInvoiceMonth(),
                invoice.getInvoiceYear(),
                invoice.getStatus().name(),
                invoice.getReminderCount(),
                invoice.getLastReminderSentAt(),
                invoice.getCheckoutUrl(),
                invoice.getPaidAt(),
                invoice.getStudent().getUsername(),
                invoice.getStudent().getEmail()
        );
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }
}