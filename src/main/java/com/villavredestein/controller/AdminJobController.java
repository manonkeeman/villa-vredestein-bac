package com.villavredestein.controller;

import com.villavredestein.jobs.InvoiceReminderJob;
import com.villavredestein.jobs.MissedCleaningTaskJob;
import com.villavredestein.jobs.MonthlyRentReminderJob;
import com.villavredestein.jobs.OverdueInvoiceJob;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping(value = "/api/admin/jobs", produces = MediaType.APPLICATION_JSON_VALUE)
@PreAuthorize("hasRole('ADMIN')")
public class AdminJobController {

    private final InvoiceReminderJob invoiceReminderJob;
    private final OverdueInvoiceJob overdueInvoiceJob;
    private final MissedCleaningTaskJob missedCleaningTaskJob;
    private final MonthlyRentReminderJob monthlyRentReminderJob;

    public AdminJobController(InvoiceReminderJob invoiceReminderJob, OverdueInvoiceJob overdueInvoiceJob,
                              MissedCleaningTaskJob missedCleaningTaskJob, MonthlyRentReminderJob monthlyRentReminderJob) {
        this.invoiceReminderJob = invoiceReminderJob;
        this.overdueInvoiceJob = overdueInvoiceJob;
        this.missedCleaningTaskJob = missedCleaningTaskJob;
        this.monthlyRentReminderJob = monthlyRentReminderJob;
    }

    @PostMapping("/reminders/trigger")
    public ResponseEntity<Map<String, String>> triggerReminders() {
        invoiceReminderJob.sendReminders();
        return ResponseEntity.ok(Map.of("message", "Invoice reminder job triggered"));
    }

    @PostMapping("/overdue/trigger")
    public ResponseEntity<Map<String, String>> triggerOverdue() {
        overdueInvoiceJob.sendOverdueReminders();
        return ResponseEntity.ok(Map.of("message", "Overdue invoice job triggered"));
    }

    @PostMapping("/cleaning/missed/trigger")
    public ResponseEntity<Map<String, String>> triggerMissedCleaning() {
        missedCleaningTaskJob.sendMissedTaskNotifications();
        return ResponseEntity.ok(Map.of("message", "Missed cleaning task job triggered"));
    }

    @PostMapping("/rent-reminder/trigger")
    public ResponseEntity<Map<String, String>> triggerRentReminder() {
        monthlyRentReminderJob.sendRentReminders();
        return ResponseEntity.ok(Map.of("message", "Rent reminder sent to all students"));
    }
}