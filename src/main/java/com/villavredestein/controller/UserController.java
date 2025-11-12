package com.villavredestein.controller;

import com.villavredestein.dto.UserResponseDTO;
import com.villavredestein.dto.UserUpdateDTO;
import com.villavredestein.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * {@code UserController} beheert alle API-endpoints die betrekking hebben op gebruikersbeheer
 * binnen de Villa Vredestein webapplicatie.
 *
 * <p>De controller maakt het mogelijk om gebruikers op te halen, te verwijderen, hun rol te wijzigen
 * en profielinformatie bij te werken. Toegang tot deze functies is afhankelijk van de gebruikersrol:
 * ADMIN heeft volledige beheertoegang, terwijl STUDENT en CLEANER hun eigen profiel kunnen bijwerken.</p>
 *
 * <p>De controller werkt samen met {@link UserService} om de businesslogica uit te voeren en
 * gegevens te beheren in de database.</p>
 */
@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
public class UserController {

    private final UserService userService;

    /**
     * Constructor voor {@link UserController}.
     *
     * @param userService service die gebruikersbeheer verzorgt
     */
    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Haalt een lijst op van alle geregistreerde gebruikers.
     *
     * <p>Alleen toegankelijk voor gebruikers met de rol ADMIN.</p>
     *
     * @return lijst van {@link UserResponseDTO} objecten
     */
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<List<UserResponseDTO>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    /**
     * Zoekt een gebruiker op basis van e-mailadres.
     *
     * <p>Alleen toegankelijk voor gebruikers met de rol ADMIN.</p>
     *
     * @param email het e-mailadres van de gebruiker
     * @return {@link UserResponseDTO} met gebruikersinformatie of 404 Not Found als niet gevonden
     */
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/email/{email}")
    public ResponseEntity<UserResponseDTO> getByEmail(@PathVariable String email) {
        return userService.getUserByEmail(email)
                .map(user -> ResponseEntity.ok(
                        new UserResponseDTO(user.getId(), user.getUsername(), user.getEmail(), user.getRole())
                ))
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Verwijdert een gebruiker op basis van het ID.
     *
     * <p>Alleen toegankelijk voor gebruikers met de rol ADMIN.</p>
     *
     * @param id het unieke ID van de gebruiker
     * @return HTTP 204 No Content bij succesvolle verwijdering
     */
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Wijzigt de rol van een gebruiker.
     *
     * <p>Alleen toegankelijk voor gebruikers met de rol ADMIN.</p>
     *
     * @param id het unieke ID van de gebruiker
     * @param newRole de nieuwe rol (bijv. ADMIN, STUDENT of CLEANER)
     * @return bijgewerkte {@link UserResponseDTO} met nieuwe rol
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}/role")
    public ResponseEntity<UserResponseDTO> changeRole(
            @PathVariable Long id,
            @RequestParam String newRole) {
        return ResponseEntity.ok(userService.changeRole(id, newRole));
    }

    /**
     * Werkt het gebruikersprofiel bij op basis van opgegeven gegevens.
     *
     * <p>Beschikbaar voor gebruikers met de rollen ADMIN, STUDENT en CLEANER.
     * Studenten en schoonmakers kunnen hiermee hun eigen profielinformatie wijzigen.</p>
     *
     * @param id het unieke ID van de gebruiker
     * @param dto {@link UserUpdateDTO} met de nieuwe profielgegevens
     * @return bijgewerkte {@link UserResponseDTO}
     */
    @PutMapping("/{id}/profile")
    @PreAuthorize("hasAnyRole('ADMIN','STUDENT','CLEANER')")
    public ResponseEntity<UserResponseDTO> updateProfile(
            @PathVariable Long id,
            @RequestBody UserUpdateDTO dto) {
        return ResponseEntity.ok(userService.updateProfile(id, dto));
    }
}