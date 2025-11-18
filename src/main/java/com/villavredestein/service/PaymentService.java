package com.villavredestein.service;

import com.villavredestein.model.Payment;
import com.villavredestein.model.User;
import com.villavredestein.repository.PaymentRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * {@code PaymentService} beheert de businesslogica rondom huurbetalingen
 * binnen de Villa Vredestein webapplicatie.
 *
 * <p>De service is verantwoordelijk voor het ophalen, aanmaken, bijwerken en verwijderen
 * van betalingen. Daarnaast verstuurt de service automatische bevestigingsmails bij
 * succesvolle betalingen.</p>
 *
 * <p>De klasse werkt samen met {@link PaymentRepository} voor database-interacties
 * en met {@link MailService} voor notificaties richting studenten.</p>
 *
 * <p>Elke betaling is gekoppeld aan een {@link User}-entiteit (student) en bevat
 * informatie over bedrag, status en datum.</p>
 *
 */
@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final MailService mailService;

    /**
     * Constructor voor {@link PaymentService}.
     *
     * @param paymentRepository repository voor opslag en beheer van {@link Payment}-entiteiten
     * @param mailService service voor het verzenden van e-mailnotificaties
     */
    public PaymentService(PaymentRepository paymentRepository, MailService mailService) {
        this.paymentRepository = paymentRepository;
        this.mailService = mailService;
    }

    /**
     * Haalt alle betalingen op die gekoppeld zijn aan een specifieke student.
     *
     * @param student de {@link User} waarvan de betalingen moeten worden opgehaald
     * @return lijst van {@link Payment}-objecten
     */
    public List<Payment> getPaymentsForStudent(User student) {
        return paymentRepository.findByStudent(student);
    }

    /**
     * Haalt alle betalingen op uit de database.
     *
     * @return lijst van alle {@link Payment}-objecten
     */
    public List<Payment> getAllPayments() {
        return paymentRepository.findAll();
    }

    /**
     * Haalt één betaling op basis van het unieke ID.
     *
     * @param id het unieke ID van de betaling
     * @return optioneel {@link Payment}-object
     */
    public Optional<Payment> getPaymentById(Long id) {
        return paymentRepository.findById(id);
    }

    /**
     * Haalt alle betalingen op met status ‘OPEN’.
     *
     * @return lijst van openstaande {@link Payment}-objecten
     */
    public List<Payment> getOpenPayments() {
        return paymentRepository.findByStatus("OPEN");
    }

    /**
     * Haalt alle betalingen op op basis van het e-mailadres van de student.
     *
     * @param email e-mailadres van de student
     * @return lijst van {@link Payment}-objecten
     */
    public List<Payment> getPaymentsByStudentEmail(String email) {
        return paymentRepository.findByStudentEmail(email);
    }

    /**
     * Slaat een nieuwe betaling op in de database.
     *
     * <p>Na het opslaan wordt gecontroleerd of de betaling de status ‘PAID’ heeft.
     * In dat geval wordt automatisch een bevestigingsmail verzonden naar de student.</p>
     *
     * @param payment de betaling die moet worden opgeslagen
     * @return het opgeslagen {@link Payment}-object
     */
    public Payment savePayment(Payment payment) {
        Payment saved = paymentRepository.save(payment);
        sendPaymentConfirmationIfPaid(saved);
        return saved;
    }

    /**
     * Wijzigt een bestaande betaling op basis van het ID.
     *
     * <p>De methode werkt de betalingsgegevens bij en verstuurt een automatische
     * bevestigingsmail als de betaling de status ‘PAID’ krijgt.</p>
     *
     * @param id het unieke ID van de te wijzigen betaling
     * @param updatedPayment de nieuwe betalingsgegevens
     * @return het bijgewerkte {@link Payment}-object
     * @throws RuntimeException als de betaling niet wordt gevonden
     */
    public Payment updatePayment(Long id, Payment updatedPayment) {
        return paymentRepository.findById(id)
                .map(existing -> {
                    existing.setAmount(updatedPayment.getAmount());
                    existing.setDate(updatedPayment.getDate());
                    existing.setStatus(updatedPayment.getStatus());
                    existing.setDescription(updatedPayment.getDescription());

                    Payment saved = paymentRepository.save(existing);
                    sendPaymentConfirmationIfPaid(saved);
                    return saved;
                })
                .orElseThrow(() -> new RuntimeException("Payment not found with id: " + id));
    }

    /**
     * Verwijdert een betaling op basis van ID.
     *
     * @param id het unieke ID van de te verwijderen betaling
     */
    public void deletePayment(Long id) {
        paymentRepository.deleteById(id);
    }

    /**
     * Controleert of een betaling de status ‘PAID’ heeft en stuurt in dat geval
     * een bevestigingsmail naar de gekoppelde student.
     *
     * @param payment de betaling waarvoor een bevestiging moet worden verstuurd
     */
    private void sendPaymentConfirmationIfPaid(Payment payment) {
        if ("PAID".equalsIgnoreCase(payment.getStatus()) && payment.getStudent() != null) {
            User student = payment.getStudent();
            String subject = "Bevestiging huurbetaling ontvangen";
            String body = String.format("""
                    Beste %s,

                    Wij hebben je betaling van €%.2f ontvangen.

                    Bedankt dat je op tijd hebt betaald!

                    Met vriendelijke groet,
                    Villa Vredestein Beheer
                    """,
                    safe(student.getUsername()),
                    payment.getAmount());

            mailService.sendMailWithRole("ADMIN", student.getEmail(), subject, body);
        }
    }

    /**
     * Zorgt ervoor dat null- of lege waarden worden vervangen door een standaardwaarde.
     *
     * @param s invoerstring
     * @return veilige string zonder null-waarde
     */
    private String safe(String s) {
        return (s == null || s.isBlank()) ? "student" : s;
    }
}