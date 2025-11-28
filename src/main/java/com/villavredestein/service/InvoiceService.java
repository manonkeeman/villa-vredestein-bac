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
 * {@code InvoiceService} beheert alle functionaliteit rondom facturen binnen de web-API.
 *
 * <p>De service verzorgt CRUD-operaties, businesslogica voor facturen,
 * statusupdates, ophalen op criteria, en validatie van studentgegevens.
 * Alle data wordt vertaald naar DTO's zodat de controllerlaag schoon blijft.</p>
 */
@Service
public class InvoiceService {

    private static final Logger log = LoggerFactory.getLogger(InvoiceService.class);

    private final InvoiceRepository invoiceRepository;
    private final UserRepository userRepository;

    /**
     * Constructor voor {@link InvoiceService}.
     *
     * @param invoiceRepository repository voor facturen
     * @param userRepository repository voor gebruikers/studenten
     */
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
     *
     * @param dto gegevens van de nieuwe factuur
     * @return de aangemaakte factuur in {@link InvoiceResponseDTO} formaat
     * @throws IllegalArgumentException als de student niet bestaat
     */
    public InvoiceResponseDTO createInvoice(InvoiceRequestDTO dto) {

        User student = userRepository.findByEmail(dto.getStudentEmail())
                .orElseThrow(() ->
                        new IllegalArgumentException("Student niet gevonden: " + dto.getStudentEmail()));

        Invoice invoice = new Invoice();
        invoice.setTitle(dto.getTitle());
        invoice.setDescription(dto.getDescription());
        invoice.setAmount(dto.getAmount());
        invoice.setIssueDate(dto.getIssueDate() != null ? dto.getIssueDate() : LocalDate.now());
        invoice.setDueDate(dto.getDueDate());
        invoice.setStatus("OPEN");
        invoice.setStudent(student);

        Invoice saved = invoiceRepository.save(invoice);

        log.info("📄 Factuur aangemaakt voor {}: €{}", student.getEmail(), saved.getAmount());

        return toDTO(saved);
    }

    // =====================================================================
    // READ — GET ALL
    // =====================================================================

    /**
     * Haalt alle facturen op.
     *
     * @return lijst van {@link InvoiceResponseDTO}
     */
    public List<InvoiceResponseDTO> getAllInvoices() {
        return invoiceRepository.findAll()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    // =====================================================================
    // READ — GET BY ID (NIEUW)
    // =====================================================================

    /**
     * Haalt één factuur op aan de hand van ID.
     *
     * @param id ID van de factuur
     * @return {@link InvoiceResponseDTO} van de gevonden factuur
     * @throws IllegalArgumentException als de factuur niet bestaat
     */
    public InvoiceResponseDTO getInvoiceById(Long id) {
        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException("Factuur niet gevonden: " + id));

        return toDTO(invoice);
    }

    // =====================================================================
    // UPDATE
    // =====================================================================

    /**
     * Wijzigt de status van een bestaande factuur.
     *
     * @param id        ID van de factuur
     * @param newStatus nieuwe status, zoals "OPEN" of "BETAALD"
     * @return de bijgewerkte factuur in DTO-formaat
     * @throws IllegalArgumentException als de factuur niet bestaat
     */
    public InvoiceResponseDTO updateStatus(Long id, String newStatus) {

        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException("Factuur niet gevonden: " + id));

        invoice.setStatus(newStatus.toUpperCase());

        Invoice updated = invoiceRepository.save(invoice);

        log.info("📌 Factuurstatus gewijzigd: {} → {}", id, newStatus);

        return toDTO(updated);
    }

    // =====================================================================
    // DELETE
    // =====================================================================

    /**
     * Verwijdert een factuur op basis van ID.
     *
     * @param id ID van de factuur
     * @throws IllegalArgumentException als de factuur niet bestaat
     */
    public void deleteInvoice(Long id) {

        if (!invoiceRepository.existsById(id)) {
            throw new IllegalArgumentException("Factuur niet gevonden: " + id);
        }

        invoiceRepository.deleteById(id);

        log.warn("🗑️ Factuur verwijderd: {}", id);
    }

    // =====================================================================
    // SPECIALE BUSINESSLOGICA
    // =====================================================================

    /**
     * Haalt alle openstaande facturen op.
     *
     * @return lijst van open facturen
     */
    public List<Invoice> getAllOpenInvoices() {
        return invoiceRepository.findByStatusIgnoreCase("OPEN");
    }

    /**
     * Haalt openstaande facturen op die binnen vier dagen vervallen.
     *
     * @return lijst van bijna-vervallende facturen
     */
    public List<Invoice> getUpcomingInvoices() {
        LocalDate today = LocalDate.now();
        LocalDate inFourDays = today.plusDays(4);
        return invoiceRepository.findByStatusIgnoreCaseAndDueDateBetween("OPEN", today, inFourDays);
    }

    // =====================================================================
    // DTO MAPPING
    // =====================================================================

    /**
     * Zet een {@link Invoice} om naar een {@link InvoiceResponseDTO}.
     *
     * @param invoice de factuur-entiteit
     * @return de factuur in DTO-formaat
     * @throws IllegalArgumentException als de factuur of student ontbreekt
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
                invoice.getAmount(),
                invoice.getDueDate(),
                invoice.getStatus(),
                invoice.isReminderSent(),
                invoice.getStudent().getUsername(),
                invoice.getStudent().getEmail()
        );
    }
}