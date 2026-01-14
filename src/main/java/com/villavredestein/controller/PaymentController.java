package com.villavredestein.controller;

import com.villavredestein.dto.PaymentRequestDTO;
import com.villavredestein.model.Payment;
import com.villavredestein.service.PaymentService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

/**
 * REST-controller voor betalingen binnen Villa Vredestein
 */
@Validated
@RestController
@RequestMapping("/api/payments")
@CrossOrigin
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    // ==========================================================
    // STUDENT: alleen eigen payments
    // ==========================================================

    @PreAuthorize("hasRole('STUDENT')")
    @GetMapping("/me")
    public ResponseEntity<List<Payment>> getMyPayments(Principal principal) {
        String email = currentUserEmail(principal);
        return ResponseEntity.ok(paymentService.getPaymentsForStudent(email));
    }

    @PreAuthorize("hasRole('STUDENT')")
    @GetMapping("/me/open")
    public ResponseEntity<List<Payment>> getMyOpenPayments(Principal principal) {
        String email = currentUserEmail(principal);
        return ResponseEntity.ok(paymentService.getOpenPaymentsForStudent(email));
    }

    // ==========================================================
    // ADMIN: lezen
    // ==========================================================

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<List<Payment>> getAllPayments() {
        return ResponseEntity.ok(paymentService.getAllPayments());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/student/{email}")
    public ResponseEntity<List<Payment>> getPaymentsForStudent(@PathVariable @NotBlank @Email String email) {
        return ResponseEntity.ok(paymentService.getPaymentsForStudent(email));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<Payment> getPaymentById(@PathVariable @Positive Long id) {
        return ResponseEntity.ok(paymentService.getPaymentById(id));
    }

    // ==========================================================
    // ADMIN: schrijven
    // ==========================================================

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<Payment> createPayment(@Valid @RequestBody PaymentRequestDTO dto) {
        Payment created = paymentService.createPayment(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePayment(@PathVariable @Positive Long id) {
        paymentService.deletePayment(id);
        return ResponseEntity.noContent().build();
    }

    // ==========================================================
    // Helper
    // ==========================================================

    private String currentUserEmail(Principal principal) {
        if (principal == null || principal.getName() == null || principal.getName().isBlank()) {
            throw new AuthenticationException("Geen ingelogde gebruiker gevonden") {};
        }
        return principal.getName().trim().toLowerCase();
    }
}