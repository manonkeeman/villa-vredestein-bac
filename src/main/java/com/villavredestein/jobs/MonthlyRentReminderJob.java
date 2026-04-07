package com.villavredestein.jobs;

import com.villavredestein.model.User;
import com.villavredestein.repository.UserRepository;
import com.villavredestein.service.MailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

@Component
@ConditionalOnProperty(value = "spring.task.scheduling.enabled", havingValue = "true", matchIfMissing = true)
public class MonthlyRentReminderJob {

    private static final Logger log = LoggerFactory.getLogger(MonthlyRentReminderJob.class);
    private static final Locale NL = Locale.forLanguageTag("nl-NL");
    private static final DateTimeFormatter MONTH_NL = DateTimeFormatter.ofPattern("MMMM yyyy", NL);

    private final UserRepository userRepository;
    private final MailService mailService;

    public MonthlyRentReminderJob(UserRepository userRepository, MailService mailService) {
        this.userRepository = userRepository;
        this.mailService = mailService;
    }

    // Runs every 28th of the month at 9:00 AM Amsterdam time
    @Scheduled(cron = "0 0 9 28 * *", zone = "Europe/Amsterdam")
    public void sendRentReminders() {
        LocalDate today = LocalDate.now();
        LocalDate dueDate = today.withDayOfMonth(1).plusMonths(1);
        String monthLabel = today.format(MONTH_NL);
        String dueDateFormatted = dueDate.format(DateTimeFormatter.ofPattern("1 MMMM yyyy", NL));

        log.info("MonthlyRentReminderJob started (month={})", monthLabel);

        List<User> students = userRepository.findByRole(User.Role.STUDENT);
        log.info("Sending rent reminders to {} students", students.size());

        for (User student : students) {
            sendToStudent(student, monthLabel, dueDateFormatted);
        }

        log.info("MonthlyRentReminderJob finished ({} students)", students.size());
    }

    private void sendToStudent(User student, String monthLabel, String dueDateFormatted) {
        String to = student.getEmail();
        if (to == null || to.isBlank()) {
            log.warn("Skip: no email for student {}", student.getId());
            return;
        }

        String name = student.getUsername() != null ? student.getUsername() : "Student";
        String subject = "Herinnering huurbetaling " + monthLabel + " – Villa Vredestein";
        String body = """
                Beste %s,

                Dit is een vriendelijke herinnering dat de huur voor %s vóór %s betaald dient te zijn.

                Betaal op tijd om extra kosten te voorkomen. Bij vragen kun je contact opnemen met de beheerder.

                Met vriendelijke groet,
                Villa Vredestein
                """.formatted(name, monthLabel, dueDateFormatted);

        try {
            mailService.sendInvoiceReminderMail(to, subject, body);
            log.info("Rent reminder sent to {}", maskEmail(to));
        } catch (Exception e) {
            log.error("Failed to send rent reminder to {}: {}", maskEmail(to), e.getMessage());
        }
    }

    private String maskEmail(String email) {
        if (email == null || email.isBlank()) return "(no-email)";
        int at = email.indexOf('@');
        if (at <= 1) return "***";
        return email.charAt(0) + "***" + email.substring(at);
    }
}