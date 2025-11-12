package com.villavredestein.service;

import com.villavredestein.dto.UserResponseDTO;
import com.villavredestein.dto.UserUpdateDTO;
import com.villavredestein.model.User;
import com.villavredestein.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * {@code UserService} beheert de businesslogica rondom gebruikers binnen
 * de Villa Vredestein webapplicatie.
 *
 * <p>De service is verantwoordelijk voor het ophalen, bijwerken en verwijderen
 * van gebruikers, evenals het wijzigen van rollen en profielgegevens.
 * De klasse vormt de brug tussen de {@link com.villavredestein.controller.UserController}
 * en de {@link UserRepository} en zorgt voor dataoverdracht via DTO’s.</p>
 *
 * <p>Alle methoden zijn transactiegericht ({@link Transactional}) om de
 * consistentie van gegevens in de database te waarborgen.</p>
 *
 * <p>Deze service is essentieel voor beheerfunctionaliteit, zoals het
 * wijzigen van gebruikersrollen of het updaten van studentprofielen.</p>
 *
 */
@Service
@Transactional
public class UserService {

    private final UserRepository userRepository;

    /**
     * Constructor voor {@link UserService}.
     *
     * @param userRepository repository voor gebruikersbeheer
     */
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Haalt alle gebruikers op uit de database en zet deze om naar {@link UserResponseDTO}-objecten.
     *
     * @return lijst van {@link UserResponseDTO}-objecten
     */
    public List<UserResponseDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Haalt een gebruiker op aan de hand van het unieke ID.
     *
     * @param id het unieke ID van de gebruiker
     * @return optioneel {@link User}-object
     */
    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    /**
     * Zoekt een gebruiker op basis van e-mailadres.
     *
     * @param email het e-mailadres van de gebruiker
     * @return optioneel {@link User}-object
     */
    public Optional<User> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    /**
     * Verwijdert een gebruiker uit de database.
     *
     * @param id het unieke ID van de gebruiker
     */
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    /**
     * Wijzigt de rol van een bestaande gebruiker.
     *
     * <p>De nieuwe rol wordt automatisch omgezet naar hoofdletters,
     * bijvoorbeeld ‘ADMIN’, ‘STUDENT’ of ‘CLEANER’.</p>
     *
     * @param id het unieke ID van de gebruiker
     * @param newRole de nieuwe rol
     * @return bijgewerkte {@link UserResponseDTO}
     * @throws RuntimeException als de gebruiker niet wordt gevonden
     */
    public UserResponseDTO changeRole(Long id, String newRole) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setRole(newRole.toUpperCase());
        return toDTO(userRepository.save(user));
    }

    /**
     * Werkt de profielgegevens van een gebruiker bij, zoals naam en e-mailadres.
     *
     * @param id het unieke ID van de gebruiker
     * @param dto {@link UserUpdateDTO} met nieuwe profielgegevens
     * @return bijgewerkte {@link UserResponseDTO}
     * @throws RuntimeException als de gebruiker niet wordt gevonden
     */
    public UserResponseDTO updateProfile(Long id, UserUpdateDTO dto) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());
        return toDTO(userRepository.save(user));
    }

    /**
     * Zet een {@link User}-entiteit om naar een {@link UserResponseDTO}.
     *
     * @param user het te converteren {@link User}-object
     * @return {@link UserResponseDTO} met vereenvoudigde gebruikersinformatie
     */
    private UserResponseDTO toDTO(User user) {
        return new UserResponseDTO(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getRole()
        );
    }
}