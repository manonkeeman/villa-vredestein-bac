package com.villavredestein;

import com.villavredestein.model.User;
import com.villavredestein.service.UserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class VredesteinApplication {

    public static void main(String[] args) {
        SpringApplication.run(VredesteinApplication.class, args);
    }

    @Bean
    CommandLineRunner seedUsers(
            UserService userService,
            @Value("${SEED_ADMIN_EMAIL:}") String adminEmail,
            @Value("${SEED_ADMIN_PASSWORD:}") String adminPassword,
            @Value("${SEED_CLEANER_EMAIL:}") String cleanerEmail,
            @Value("${SEED_CLEANER_PASSWORD:}") String cleanerPassword
    ) {
        return args -> {
            if (!adminEmail.isBlank() && !adminPassword.isBlank()) {
                userService.seedUserIfMissing(
                        "Villa Vredestein Admin",
                        adminEmail,
                        adminPassword,
                        User.Role.ADMIN
                );
            }

            if (!cleanerEmail.isBlank() && !cleanerPassword.isBlank()) {
                userService.seedUserIfMissing(
                        "Schoonmaak",
                        cleanerEmail,
                        cleanerPassword,
                        User.Role.CLEANER
                );
            }
        };
    }
}