package com.villavredestein.controller;

import com.villavredestein.dto.*;
import com.villavredestein.model.User;
import com.villavredestein.service.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
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
    public ResponseEntity<UserResponseDTO> changeRole(@PathVariable Long id, @RequestParam String newRole) {
        return ResponseEntity.ok(userService.changeRole(id, newRole));
    }

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
            @RequestParam String status
    ) {
        return ResponseEntity.ok(invoiceService.updateStatus(id, status));
    }

    @DeleteMapping("/invoices/{id}")
    public ResponseEntity<Void> deleteInvoice(@PathVariable Long id) {
        invoiceService.deleteInvoice(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/schedules")
    public ResponseEntity<List<CleaningResponseDTO>> getAllSchedules() {
        return ResponseEntity.ok(cleaningService.getAllSchedules());
    }

    @GetMapping("/schedules/week/{week}")
    public ResponseEntity<CleaningResponseDTO> getScheduleByWeek(@PathVariable int week) {
        return ResponseEntity.ok(cleaningService.getByWeek(week));
    }

    @PostMapping("/schedules")
    public ResponseEntity<CleaningResponseDTO> createSchedule(@RequestParam int week) {
        return ResponseEntity.ok(cleaningService.createSchedule(week));
    }

    @PostMapping("/schedules/{scheduleId}/tasks")
    public ResponseEntity<CleaningRequestDTO> addTask(
            @PathVariable Long scheduleId,
            @RequestBody CleaningRequestDTO dto
    ) {
        return ResponseEntity.ok(cleaningService.addTask(scheduleId, dto));
    }

    @PutMapping("/tasks/{taskId}/note")
    public ResponseEntity<CleaningRequestDTO> updateTaskNote(
            @PathVariable Long taskId,
            @RequestParam String note
    ) {
        return ResponseEntity.ok(cleaningService.updateTaskNote(taskId, note));
    }

    @DeleteMapping("/tasks/{taskId}")
    public ResponseEntity<Void> deleteTask(@PathVariable Long taskId) {
        cleaningService.deleteTask(taskId);
        return ResponseEntity.noContent().build();
    }
}