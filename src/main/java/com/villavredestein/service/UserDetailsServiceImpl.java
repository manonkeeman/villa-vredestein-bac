package com.villavredestein.service;

import com.villavredestein.model.User;
import com.villavredestein.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * {@code UserDetailsServiceImpl} vormt de brug tussen de applicatie en
 * het authenticatiemechanisme van Spring Security.
 * <p>Wordt aangeroepen door de {@code AuthenticationManager} binnen de
 * {@link com.villavredestein.controller.AuthController} tijdens login.</p>
 */
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private static final Logger log = LoggerFactory.getLogger(UserDetailsServiceImpl.class);
    private final UserRepository userRepository;

    /**
     * Constructor voor {@link UserDetailsServiceImpl}.
     *
     * @param userRepository repository voor gebruikersbeheer
     */
    public UserDetailsServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Laadt een gebruiker op basis van het ingevoerde e-mailadres.
     *
     * <p>Als de gebruiker niet wordt gevonden, wordt een
     * {@link UsernameNotFoundException} gegooid, waardoor Spring Security
     * de authenticatie zal weigeren.</p>
     *
     * @param email e-mailadres van de gebruiker (fungeert als gebruikersnaam)
     * @return een {@link UserDetails}-object dat gebruikt wordt door Spring Security
     * @throws UsernameNotFoundException als geen gebruiker met dit e-mailadres bestaat
     */
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.warn("Login mislukt: gebruiker '{}' niet gevonden", email);
                    return new UsernameNotFoundException("Gebruiker niet gevonden: " + email);
                });

        log.info("Gebruiker '{}' geladen met rol '{}'", user.getEmail(), user.getRole());

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