package com.villavredestein.controller;

import com.villavredestein.model.Payment;
import com.villavredestein.model.User;
import com.villavredestein.service.PaymentService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/payments")
@CrossOrigin(origins = "*")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

     @PreAuthorize("hasRole('STUDENT')")
    @GetMapping("/student/{email}")
    public ResponseEntity<List<Payment>> getPaymentsForStudent(@PathVariable String email) {
        List<Payment> payments = paymentService.getPaymentsByStudentEmail(email);
        return ResponseEntity.ok(payments);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<List<Payment>> getAllPayments() {
        return ResponseEntity.ok(paymentService.getAllPayments());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<Payment> getPaymentById(@PathVariable Long id) {
        Optional<Payment> payment = paymentService.getPaymentById(id);
        return payment.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<Payment> createPayment(@RequestBody Payment payment) {
        Payment saved = paymentService.savePayment(payment);
        return ResponseEntity.ok(saved);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<Payment> updatePayment(@PathVariable Long id, @RequestBody Payment updatedPayment) {
        Optional<Payment> existing = paymentService.getPaymentById(id);
        if (existing.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Payment saved = paymentService.updatePayment(id, updatedPayment);
        return ResponseEntity.ok(saved);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePayment(@PathVariable Long id) {
        paymentService.deletePayment(id);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/open")
    public ResponseEntity<List<Payment>> getOpenPayments() {
        return ResponseEntity.ok(paymentService.getOpenPayments());
    }
}