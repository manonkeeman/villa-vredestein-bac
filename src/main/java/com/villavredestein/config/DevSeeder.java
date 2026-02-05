package com.villavredestein.config;

import com.villavredestein.model.User;
import com.villavredestein.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
@Profile("dev")
public class DevSeeder implements ApplicationRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${SEED_ENABLED:false}")
    private boolean seedEnabled;

    @Value("${SEED_ADMIN_EMAIL:admin@villavredestein.com}")
    private String adminEmail;

    @Value("${SEED_CLEANER_EMAIL:cleaner@villavredestein.com}")
    private String cleanerEmail;

    @Value("${SEED_STUDENT_EMAILS:student1@villavredestein.com}")
    private String studentEmailsCsv;

    @Value("${SEED_ADMIN_PASSWORD:}")
    private String adminPassword;

    @Value("${SEED_CLEANER_PASSWORD:}")
    private String cleanerPassword;

    @Value("${SEED_STUDENT_PASSWORD:}")
    private String studentPassword;

    public DevSeeder(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (!seedEnabled) {
            return;
        }

        if (adminPassword == null || adminPassword.isBlank()
                || cleanerPassword == null || cleanerPassword.isBlank()
                || studentPassword == null || studentPassword.isBlank()) {
            throw new IllegalStateException("Seeding enabled (SEED_ENABLED=true) but one or more SEED_*_PASSWORD values are missing.");
        }

        upsertUser(adminEmail, "Admin", User.Role.ADMIN, adminPassword);
        upsertUser(cleanerEmail, "Cleaner", User.Role.CLEANER, cleanerPassword);

        List<String> students = Arrays.stream(studentEmailsCsv.split(","))
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .toList();

        int i = 1;
        for (String email : students) {
            upsertUser(email, "Student " + i, User.Role.STUDENT, studentPassword);
            i++;
        }
    }

    private void upsertUser(String email, String username, User.Role role, String plainPassword) {
        if (email == null || email.isBlank()) {
            return;
        }
        if (plainPassword == null || plainPassword.isBlank()) {
            return;
        }

        String normalizedEmail = email.trim().toLowerCase();
        String normalizedUsername = (username == null || username.isBlank())
                ? normalizedEmail.split("@", 2)[0]
                : username.trim();

        String encodedPassword = passwordEncoder.encode(plainPassword);

        User user = userRepository.findByEmailIgnoreCase(normalizedEmail)
                .map(existing -> {
                    existing.setUsername(normalizedUsername);
                    existing.setEmail(normalizedEmail);
                    existing.setRole(role);
                    existing.setPassword(encodedPassword);
                    return existing;
                })
                .orElseGet(() -> new User(normalizedUsername, normalizedEmail, encodedPassword, role));

        userRepository.save(user);
    }
}