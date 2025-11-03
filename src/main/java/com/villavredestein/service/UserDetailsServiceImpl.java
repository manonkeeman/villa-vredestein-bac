package com.villavredestein.service;

import com.villavredestein.model.User;
import com.villavredestein.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private static final Logger log = LoggerFactory.getLogger(UserDetailsServiceImpl.class);
    private final UserRepository userRepository;

    public UserDetailsServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.warn("❌ Login mislukt: gebruiker '{}' niet gevonden", email);
                    return new UsernameNotFoundException("Gebruiker niet gevonden: " + email);
                });

        log.info("✅ Gebruiker '{}' geladen met rol '{}'", user.getEmail(), user.getRole());

        // Zorgt ervoor dat Spring Security consistent "ROLE_" prefix gebruikt
        String sanitizedRole = user.getRole().startsWith("ROLE_")
                ? user.getRole().substring(5)
                : user.getRole();

        return org.springframework.security.core.userdetails.User
                .withUsername(user.getEmail())
                .password(user.getPassword())
                .roles(sanitizedRole)
                .build();
    }
}