package com.villavredestein.jobs;

import com.villavredestein.model.CleaningTask;
import com.villavredestein.model.User;
import com.villavredestein.repository.CleaningTaskRepository;
import com.villavredestein.service.MailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

@Component
@ConditionalOnProperty(value = "spring.task.scheduling.enabled", havingValue = "true", matchIfMissing = true)
public class MissedCleaningTaskJob {

    private static final Logger log = LoggerFactory.getLogger(MissedCleaningTaskJob.class);
    private static final Locale NL = Locale.forLanguageTag("nl-NL");
    private static final DateTimeFormatter DATE_NL = DateTimeFormatter.ofPattern("d MMMM yyyy", NL);

    private final CleaningTaskRepository taskRepository;
    private final MailService mailService;

    public MissedCleaningTaskJob(CleaningTaskRepository taskRepository, MailService mailService) {
        this.taskRepository = taskRepository;
        this.mailService = mailService;
    }

    @Scheduled(cron = "0 30 9 * * *", zone = "Europe/Amsterdam")
    public void sendMissedTaskNotifications() {
        LocalDate today = LocalDate.now();
        log.info("MissedCleaningTaskJob started (today={})", today);

        List<CleaningTask> overdueTasks = taskRepository.findOverdueTasks(today);
        log.info("Overdue cleaning tasks found: {}", overdueTasks.size());

        for (CleaningTask task : overdueTasks) {
            processTask(task);
        }

        log.info("MissedCleaningTaskJob finished");
    }

    private void processTask(CleaningTask task) {
        User assignedTo = task.getAssignedTo();
        if (assignedTo == null) return;

        String email = assignedTo.getEmail();
        if (email == null || email.isBlank()) {
            log.warn("Skip: no email for assignee of task {}", task.getId());
            return;
        }

        String deadline = task.getDeadline() != null
                ? task.getDeadline().format(DATE_NL)
                : "onbekend";

        String subject = "Schoonmaak taak niet voltooid: " + task.getName();
        String body = String.format("""
                Beste %s,

                Je hebt de volgende schoonmaak taak nog niet afgerond:

                Taak: %s
                Week: %d
                Deadline: %s

                Vergeet niet om dit zo snel mogelijk in orde te brengen.

                Met vriendelijke groet,
                Villa Vredestein
                """,
                safeName(assignedTo),
                task.getName(),
                task.getWeekNumber(),
                deadline);

        try {
            mailService.sendMailWithRole("ADMIN", email, subject, body);
            log.info("Missed task notification sent (taskId={}, to={})", task.getId(), maskEmail(email));
        } catch (Exception e) {
            log.error("Failed to send missed task notification (taskId={}, to={}): {}", task.getId(), maskEmail(email), e.getMessage());
        }
    }

    private String safeName(User user) {
        if (user == null) return "bewoner";
        String u = user.getUsername();
        return (u == null || u.isBlank()) ? "bewoner" : u.trim();
    }

    private String maskEmail(String email) {
        if (email == null || email.isBlank()) return "(no-email)";
        int at = email.indexOf('@');
        if (at <= 1) return "***";
        return email.charAt(0) + "***" + email.substring(at);
    }
}