package com.villavredestein.controller;

import com.villavredestein.dto.LoginRequestDTO;
import com.villavredestein.dto.LoginResponseDTO;
import com.villavredestein.security.JwtService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * {@code AuthController} verzorgt authenticatie, token-generatie en token-validatie
 * binnen de Villa Vredestein API.
 */
@RestController
@RequestMapping("/api/auth")
@CrossOrigin
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthController(AuthenticationManager authenticationManager, JwtService jwtService) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    // =====================================================================
    // LOGIN
    // =====================================================================

    /**
     * Verwerkt een loginpoging en geeft bij succes een JWT-token terug.
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequestDTO request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()
                    )
            );

            UserDetails userDetails = (UserDetails) authentication.getPrincipal();

            String role = userDetails.getAuthorities()
                    .stream()
                    .map(GrantedAuthority::getAuthority)
                    .findFirst()
                    .orElse("ROLE_STUDENT");

            String jwtToken = jwtService.generateToken(userDetails.getUsername(), role);

            log.info("Login succesvol voor {} met rol {}", userDetails.getUsername(), role);

            return ResponseEntity.ok(new LoginResponseDTO(
                    userDetails.getUsername(),
                    request.getEmail(),
                    role,
                    jwtToken
            ));

        } catch (BadCredentialsException e) {
            log.warn("Ongeldige login poging voor {}", request.getEmail());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Ongeldige gebruikersnaam of wachtwoord"));
        } catch (Exception e) {
            log.error("Interne fout bij login voor {}: {}", request.getEmail(), e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Er is een fout opgetreden tijdens het inloggen"));
        }
    }

    // =====================================================================
    // TOKEN VALIDATIE
    // =====================================================================

    @GetMapping("/validate")
    public ResponseEntity<?> validateToken(@RequestParam String token) {
        try {
            String username = jwtService.extractUsername(token);
            boolean valid = jwtService.validateToken(token, username);

            if (!valid) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("valid", false, "error", "Token is verlopen of ongeldig"));
            }

            String role = jwtService.extractRole(token);

            return ResponseEntity.ok(Map.of(
                    "valid", true,
                    "username", username,
                    "role", role
            ));

        } catch (Exception e) {
            log.warn("Token validatie mislukt: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Ongeldige of beschadigde token"));
        }
    }

    // =====================================================================
    // INTERNAL SERVER ERROR 500 (testing)
    // =====================================================================

    @GetMapping("/force-error")
    public ResponseEntity<?> forceError() {
        throw new RuntimeException("Geforceerde serverfout voor test");
    }
}