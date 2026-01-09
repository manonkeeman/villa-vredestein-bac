package com.villavredestein.service;

import com.villavredestein.dto.InvoiceRequestDTO;
import com.villavredestein.dto.InvoiceResponseDTO;
import com.villavredestein.model.Invoice;
import com.villavredestein.model.Invoice.InvoiceStatus;
import com.villavredestein.model.User;
import com.villavredestein.repository.InvoiceRepository;
import com.villavredestein.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * {@code InvoiceService} beheert alle functionaliteit rondom facturen binnen de web-API.
 *
 * <p>De service verzorgt CRUD-operaties, businesslogica voor facturen,
 * statusupdates, ophalen op criteria, en validatie van studentgegevens.
 * Alle data wordt vertaald naar DTO's zodat de controllerlaag schoon blijft.</p>
 */
@Service
@Transactional
public class InvoiceService {

    private static final Logger log = LoggerFactory.getLogger(InvoiceService.class);

    private final InvoiceRepository invoiceRepository;
    private final UserRepository userRepository;

    public InvoiceService(InvoiceRepository invoiceRepository,
                          UserRepository userRepository) {
        this.invoiceRepository = invoiceRepository;
        this.userRepository = userRepository;
    }

    // =====================================================================
    // CREATE
    // =====================================================================

    /**
     * Maakt een nieuwe factuur aan voor een student.
     */
    public InvoiceResponseDTO createInvoice(InvoiceRequestDTO dto) {

        User student = userRepository.findByEmail(dto.getStudentEmail())
                .orElseThrow(() ->
                        new IllegalArgumentException("Student niet gevonden: " + dto.getStudentEmail()));

        // issueDate default naar vandaag (als client het niet meegeeft)
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

        // Uniekheid: 1 factuur per student per maand/jaar (optioneel maar professioneel)
        if (invoiceRepository.existsByStudentAndInvoiceMonthAndInvoiceYear(student, invoice.getInvoiceMonth(), invoice.getInvoiceYear())) {
            throw new IllegalArgumentException("Er bestaat al een factuur voor deze student in " + invoice.getInvoiceMonth() + "-" + invoice.getInvoiceYear());
        }

        Invoice saved = invoiceRepository.save(invoice);

        log.info("📄 Factuur aangemaakt (invoiceId={}, student={}, amount={})", saved.getId(), safe(student.getEmail()), saved.getAmount());

        return toDTO(saved);
    }

    // =====================================================================
    // READ
    // =====================================================================

    /**
     * Haalt alle facturen op (gesorteerd nieuwste eerst).
     */
    public List<InvoiceResponseDTO> getAllInvoices() {
        return invoiceRepository.findAllByOrderByIdDesc()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Haalt één factuur op aan de hand van ID.
     */
    public InvoiceResponseDTO getInvoiceById(Long id) {
        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Factuur niet gevonden: " + id));
        return toDTO(invoice);
    }

    /**
     * Haalt alle facturen op voor een student (op basis van e-mail).
     *
     * <p>Handig voor STUDENT endpoints. Ownership checks kun je hier toevoegen
     * op basis van ingelogde gebruiker indien gewenst.</p>
     */
    public List<InvoiceResponseDTO> getInvoicesForStudent(String studentEmail) {
        return invoiceRepository.findByStudent_EmailIgnoreCaseOrderByIdDesc(studentEmail)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    // =====================================================================
    // UPDATE
    // =====================================================================

    /**
     * Wijzigt de status van een bestaande factuur (enum-veilig).
     */
    public InvoiceResponseDTO updateStatus(Long id, InvoiceStatus newStatus) {

        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Factuur niet gevonden: " + id));

        invoice.setStatus(newStatus);
        Invoice updated = invoiceRepository.save(invoice);

        log.info("📌 Factuurstatus gewijzigd (invoiceId={}, status={})", id, newStatus);

        return toDTO(updated);
    }

    /**
     * Backwards compatible overload (voor controllers die nog String gebruiken).
     * Probeer zo snel mogelijk over te stappen op de enum variant.
     */
    public InvoiceResponseDTO updateStatus(Long id, String newStatus) {
        if (newStatus == null || newStatus.isBlank()) {
            throw new IllegalArgumentException("Status is verplicht");
        }
        try {
            return updateStatus(id, InvoiceStatus.valueOf(newStatus.trim().toUpperCase()));
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Ongeldige status: " + newStatus + ". Toegestaan: OPEN, PAID, OVERDUE, CANCELLED");
        }
    }

    // =====================================================================
    // DELETE
    // =====================================================================

    /**
     * Verwijdert een factuur op basis van ID.
     */
    public void deleteInvoice(Long id) {
        if (!invoiceRepository.existsById(id)) {
            throw new IllegalArgumentException("Factuur niet gevonden: " + id);
        }
        invoiceRepository.deleteById(id);
        log.warn("🗑️ Factuur verwijderd (invoiceId={})", id);
    }

    // =====================================================================
    // BUSINESSLOGICA VOOR JOBS
    // =====================================================================

    /**
     * Haalt alle openstaande facturen op (entity-level, voor jobs).
     */
    public List<Invoice> getAllOpenInvoices() {
        return invoiceRepository.findByStatusOrderByIdDesc(InvoiceStatus.OPEN);
    }

    /**
     * Haalt openstaande facturen op die binnen N dagen vervallen.
     *
     * <p>Deze methode is bewust entity-level omdat jobs intern werken.
     * De job bepaalt het N-dagen-venster via configuratie.</p>
     */
    public List<Invoice> getUpcomingInvoices() {
        LocalDate today = LocalDate.now();
        LocalDate inFourDays = today.plusDays(4);
        return invoiceRepository.findByStatusAndDueDateBetweenOrderByDueDateAsc(InvoiceStatus.OPEN, today, inFourDays);
    }

    /**
     * Slaat reminder metadata op (lastReminderSentAt/reminderCount/status).
     */
    public void saveReminderMeta(Invoice invoice) {
        if (invoice == null || invoice.getId() == null) {
            throw new IllegalArgumentException("Invoice ontbreekt of heeft geen id");
        }
        invoiceRepository.save(invoice);
    }

    // =====================================================================
    // DTO MAPPING
    // =====================================================================

    /**
     * Zet een {@link Invoice} om naar een {@link InvoiceResponseDTO}.
     */
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
                invoice.getDescription(),
                invoice.getAmount(),
                invoice.getIssueDate(),
                invoice.getDueDate(),
                invoice.getStatus(),
                invoice.getReminderCount(),
                invoice.getLastReminderSentAt(),
                invoice.getStudent().getUsername(),
                invoice.getStudent().getEmail()
        );
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }
}