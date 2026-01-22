package com.villavredestein.controller;

import com.villavredestein.dto.PaymentRequestDTO;
import com.villavredestein.dto.PaymentResponseDTO;
import com.villavredestein.service.PaymentService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// =====================================================================
// # PaymentController
// =====================================================================
@Validated
@RestController
@RequestMapping(value = "/api/payments", produces = MediaType.APPLICATION_JSON_VALUE)
@CrossOrigin
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    // =====================================================================
    // # READ - current user
    // =====================================================================
    @PreAuthorize("hasAnyRole('STUDENT','ADMIN')")
    @GetMapping("/me")
    public ResponseEntity<List<PaymentResponseDTO>> getMyPayments(Authentication authentication) {
        String email = currentUserEmail(authentication);
        return ResponseEntity.ok(paymentService.getPaymentsForStudent(email));
    }

    @PreAuthorize("hasAnyRole('STUDENT','ADMIN')")
    @GetMapping("/me/open")
    public ResponseEntity<List<PaymentResponseDTO>> getMyOpenPayments(Authentication authentication) {
        String email = currentUserEmail(authentication);
        return ResponseEntity.ok(paymentService.getOpenPaymentsForStudent(email));
    }

    // =====================================================================
    // # READ - admin
    // =====================================================================
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<List<PaymentResponseDTO>> getAllPayments() {
        return ResponseEntity.ok(paymentService.getAllPayments());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/student/{email}")
    public ResponseEntity<List<PaymentResponseDTO>> getPaymentsForStudent(@PathVariable @NotBlank @Email String email) {
        return ResponseEntity.ok(paymentService.getPaymentsForStudent(email.trim().toLowerCase()));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<PaymentResponseDTO> getPaymentById(@PathVariable @Positive Long id) {
        return ResponseEntity.ok(paymentService.getPaymentById(id));
    }

    // =====================================================================
    // # CREATE / DELETE - admin
    // =====================================================================
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PaymentResponseDTO> createPayment(@Valid @RequestBody PaymentRequestDTO dto) {
        PaymentResponseDTO created = paymentService.createPayment(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePayment(@PathVariable @Positive Long id) {
        paymentService.deletePayment(id);
        return ResponseEntity.noContent().build();
    }

    // =====================================================================
    // # Helpers
    // =====================================================================
    private String currentUserEmail(Authentication authentication) {
        if (authentication == null || authentication.getName() == null || authentication.getName().isBlank()) {
            throw new AuthenticationCredentialsNotFoundException("No authenticated user found");
        }
        return authentication.getName().trim().toLowerCase();
    }
}