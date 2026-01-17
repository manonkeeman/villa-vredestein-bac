package com.villavredestein.service;

import com.villavredestein.model.User;
import com.villavredestein.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
public class UserDetailsService implements org.springframework.security.core.userdetails.UserDetailsService {

    private static final Logger log = LoggerFactory.getLogger(UserDetailsService.class);
    private final UserRepository userRepository;

    public UserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Laadt een gebruiker op basis van het ingevoerde e-mailadres.
     */
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        if (email == null || email.isBlank()) {
            throw new UsernameNotFoundException("E-mailadres is verplicht");
        }

        String normalizedEmail = email.trim().toLowerCase(Locale.ROOT);

        User user = userRepository.findByEmailIgnoreCase(normalizedEmail)
                .orElseThrow(() -> {
                    log.warn("Login mislukt: gebruiker '{}' niet gevonden", maskEmail(normalizedEmail));
                    return new UsernameNotFoundException("Gebruiker niet gevonden: " + normalizedEmail);
                });

        User.Role role = user.getRole();
        if (role == null) {
            log.warn("Gebruiker '{}' heeft geen rol; default naar STUDENT", maskEmail(user.getEmail()));
            role = User.Role.STUDENT;
        }

        String normalizedRole = role.name(); // ADMIN / STUDENT / CLEANER
        log.info("Gebruiker '{}' geladen met rol '{}'", maskEmail(user.getEmail()), normalizedRole);

        return org.springframework.security.core.userdetails.User
                .withUsername(user.getEmail())
                .password(user.getPassword())
                .roles(normalizedRole)
                .build();
    }

    private String maskEmail(String email) {
        if (email == null) {
            return "<null>";
        }
        String trimmed = email.trim();
        int at = trimmed.indexOf('@');
        if (at <= 1) {
            return "***";
        }
        return trimmed.charAt(0) + "***" + trimmed.substring(at);
    }
}