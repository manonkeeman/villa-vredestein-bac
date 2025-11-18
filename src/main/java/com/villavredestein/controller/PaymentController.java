package com.villavredestein.controller;

import com.villavredestein.model.Payment;
import com.villavredestein.service.PaymentService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * {@code PaymentController} beheert alle API-endpoints met betrekking tot
 * huurbetalingen binnen de Villa Vredestein webapplicatie.
 *
 * <p>De controller biedt functionaliteit voor het ophalen, aanmaken, bijwerken
 * en verwijderen van betalingen. Daarnaast kunnen openstaande betalingen worden opgevraagd.
 * Toegang tot de endpoints is afhankelijk van de gebruikersrol (ADMIN of STUDENT).</p>
 *
 * <p>De controller maakt gebruik van {@link PaymentService} voor de verwerking van
 * de businesslogica en database-interacties.</p>
 *
 */
@RestController
@RequestMapping("/api/payments")
@CrossOrigin
public class PaymentController {

    private final PaymentService paymentService;

    /**
     * Constructor voor {@link PaymentController}.
     *
     * @param paymentService service die betalingsbeheer verzorgt
     */
    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    /**
     * Haalt alle betalingen op die gekoppeld zijn aan een specifieke student.
     *
     * <p>Beschikbaar voor gebruikers met de rollen ADMIN en STUDENT.</p>
     *
     * @param email e-mailadres van de student
     * @return lijst van {@link Payment} objecten
     */
    @PreAuthorize("hasAnyRole('STUDENT','ADMIN')")
    @GetMapping("/student/{email}")
    public ResponseEntity<List<Payment>> getPaymentsForStudent(@PathVariable String email) {
        return ResponseEntity.ok(paymentService.getPaymentsByStudentEmail(email));
    }


    /**
     * Haalt een overzicht op van alle betalingen in het systeem.
     *
     * <p>Alleen toegankelijk voor gebruikers met de rol ADMIN.</p>
     *
     * @return lijst van {@link Payment} objecten
     */
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<List<Payment>> getAllPayments() {
        return ResponseEntity.ok(paymentService.getAllPayments());
    }

    /**
     * Haalt een enkele betaling op basis van het ID.
     *
     * <p>Alleen toegankelijk voor gebruikers met de rol ADMIN.</p>
     *
     * @param id het unieke ID van de betaling
     * @return {@link Payment} object bij succes of 404 Not Found als niet gevonden
     */
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<Payment> getPaymentById(@PathVariable Long id) {
        Optional<Payment> payment = paymentService.getPaymentById(id);
        return payment.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Maakt een nieuwe betaling aan.
     *
     * <p>Alleen toegankelijk voor gebruikers met de rol ADMIN.</p>
     *
     * @param payment de nieuwe betaling die moet worden opgeslagen
     * @return het aangemaakte {@link Payment} object
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<Payment> createPayment(@RequestBody Payment payment) {
        Payment created = paymentService.savePayment(payment);
        return ResponseEntity.ok(created);
    }

    /**
     * Wijzigt een bestaande betaling.
     *
     * <p>Alleen toegankelijk voor gebruikers met de rol ADMIN.</p>
     *
     * @param id het unieke ID van de betaling
     * @param updatedPayment bijgewerkte betalingsinformatie
     * @return het bijgewerkte {@link Payment} object of 404 Not Found als de betaling niet bestaat
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<Payment> updatePayment(@PathVariable Long id, @RequestBody Payment updatedPayment) {
        try {
            Payment updated = paymentService.updatePayment(id, updatedPayment);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Verwijdert een betaling op basis van het ID.
     *
     * <p>Alleen toegankelijk voor gebruikers met de rol ADMIN.</p>
     *
     * @param id het unieke ID van de betaling
     * @return HTTP 204 No Content bij succes
     */
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePayment(@PathVariable Long id) {
        paymentService.deletePayment(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Haalt alle openstaande (onbetaalde) betalingen op.
     *
     * <p>Alleen toegankelijk voor gebruikers met de rol ADMIN.</p>
     *
     * @return lijst van {@link Payment} objecten met status OPEN
     */
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/open")
    public ResponseEntity<List<Payment>> getOpenPayments() {
        return ResponseEntity.ok(paymentService.getOpenPayments());
    }
}