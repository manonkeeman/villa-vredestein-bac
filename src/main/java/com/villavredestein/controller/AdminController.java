package com.villavredestein.controller;

import com.villavredestein.dto.*;
import com.villavredestein.service.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "*")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final UserService userService;
    private final InvoiceService invoiceService;
    private final CleaningService cleaningService;

    public AdminController(UserService userService,
                           InvoiceService invoiceService,
                           CleaningService cleaningService) {
        this.userService = userService;
        this.invoiceService = invoiceService;
        this.cleaningService = cleaningService;
    }

    // === USERS ===
    @GetMapping("/users")
    public ResponseEntity<List<UserResponseDTO>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/users/{id}/role")
    public ResponseEntity<UserResponseDTO> changeRole(
            @PathVariable Long id,
            @RequestParam String newRole) {
        return ResponseEntity.ok(userService.changeRole(id, newRole));
    }

    // === INVOICES ===
    @GetMapping("/invoices")
    public ResponseEntity<List<InvoiceResponseDTO>> getAllInvoices() {
        return ResponseEntity.ok(invoiceService.getAllInvoices());
    }

    @PostMapping("/invoices")
    public ResponseEntity<InvoiceResponseDTO> createInvoice(@RequestBody InvoiceRequestDTO dto) {
        return ResponseEntity.ok(invoiceService.createInvoice(dto));
    }

    @PutMapping("/invoices/{id}/status")
    public ResponseEntity<InvoiceResponseDTO> updateInvoiceStatus(
            @PathVariable Long id,
            @RequestParam String status) {
        return ResponseEntity.ok(invoiceService.updateStatus(id, status));
    }

    @DeleteMapping("/invoices/{id}")
    public ResponseEntity<Void> deleteInvoice(@PathVariable Long id) {
        invoiceService.deleteInvoice(id);
        return ResponseEntity.noContent().build();
    }

    // === CLEANING ===
    @GetMapping("/cleaning/tasks")
    public ResponseEntity<List<CleaningRequestDTO>> getCleaningTasks(
            @RequestParam(required = false) Integer weekNumber) {
        if (weekNumber != null) {
            return ResponseEntity.ok(cleaningService.getTasksByWeek(weekNumber));
        }
        return ResponseEntity.ok(cleaningService.getAllTasks());
    }

    @PostMapping("/cleaning/tasks")
    public ResponseEntity<CleaningRequestDTO> createTask(@RequestBody CleaningRequestDTO dto) {
        return ResponseEntity.ok(cleaningService.addTask(dto));
    }

    @PutMapping("/cleaning/tasks/{taskId}")
    public ResponseEntity<CleaningRequestDTO> updateTask(
            @PathVariable Long taskId,
            @RequestBody CleaningRequestDTO dto) {
        return ResponseEntity.ok(cleaningService.updateTask(taskId, dto));
    }

    @PutMapping("/cleaning/tasks/{taskId}/toggle")
    public ResponseEntity<CleaningRequestDTO> toggleTask(@PathVariable Long taskId) {
        return ResponseEntity.ok(cleaningService.toggleTask(taskId));
    }

    @DeleteMapping("/cleaning/tasks/{taskId}")
    public ResponseEntity<Void> deleteTask(@PathVariable Long taskId) {
        cleaningService.deleteTask(taskId);
        return ResponseEntity.noContent().build();
    }
}