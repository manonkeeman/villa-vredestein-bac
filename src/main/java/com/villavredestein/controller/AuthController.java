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
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * {@code AuthController} verzorgt authenticatie en validatie van JWT-tokens
 * binnen de Villa Vredestein web-API.
 *
 * <p>Deze controller behandelt login-aanvragen, genereert tokens bij geldige
 * inlogpogingen en controleert bestaande tokens op geldigheid. De controller
 * werkt samen met {@link JwtService} en het Spring Security framework om
 * gebruikers veilig te authenticeren.</p>
 *
 * <p>Toegankelijk voor alle clients (CORS open) via het pad {@code /api/auth}.</p>
 */
@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final JwtService jwtService;

    /**
     * Constructor voor {@link AuthController}.
     *
     * @param authenticationManager verwerkt authenticatiepogingen
     * @param userDetailsService laadt gebruikersgegevens uit de database
     * @param jwtService verzorgt het genereren en valideren van JWT-tokens
     */
    public AuthController(AuthenticationManager authenticationManager,
                          UserDetailsService userDetailsService,
                          JwtService jwtService) {
        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
        this.jwtService = jwtService;
    }

    /**
     * Verwerkt een loginverzoek en genereert een JWT-token bij geldige credentials.
     *
     * <p>Bij een succesvolle login retourneert dit endpoint de gebruikersnaam, e-mailadres,
     * toegewezen rol en een nieuw gegenereerd JWT-token.</p>
     *
     * @param request {@link LoginRequestDTO} met e-mail en wachtwoord
     * @return {@link LoginResponseDTO} bij succes of een foutmelding bij mislukking
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

            String role = userDetails.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .findFirst()
                    .orElse("ROLE_STUDENT");

            String jwtToken = jwtService.generateToken(userDetails.getUsername(), role);

            log.info("Ingelogd: {} met rol {}", userDetails.getUsername(), role);

            return ResponseEntity.ok(new LoginResponseDTO(
                    userDetails.getUsername(),
                    request.getEmail(),
                    role,
                    jwtToken
            ));

        } catch (BadCredentialsException e) {
            log.warn("Onjuiste inloggegevens voor gebruiker: {}", request.getEmail());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Ongeldige gebruikersnaam of wachtwoord"));
        } catch (Exception e) {
            log.error("Fout tijdens login voor {}: {}", request.getEmail(), e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Er is een fout opgetreden tijdens het inloggen"));
        }
    }

    /**
     * Valideert een bestaand JWT-token en controleert of deze nog geldig is.
     *
     * <p>Geeft bij een geldige token de gebruikersnaam en rol terug. Indien de token ongeldig
     * of verlopen is, wordt een foutmelding met {@code 401 Unauthorized} geretourneerd.</p>
     *
     * @param token de JWT-token die gecontroleerd moet worden
     * @return {@code valid: true} bij succes of foutinformatie bij mislukking
     */
    @GetMapping("/validate")
    public ResponseEntity<?> validateToken(@RequestParam String token) {
        try {
            String username = jwtService.extractUsername(token);
            boolean isValid = jwtService.validateToken(token, username);

            if (isValid) {
                String role = jwtService.extractRole(token);
                log.info("Geldige token voor {} met rol {}", username, role);
                return ResponseEntity.ok(Map.of(
                        "valid", true,
                        "username", username,
                        "role", role
                ));
            } else {
                log.warn("Ongeldige of verlopen token voor {}", username);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("valid", false, "error", "Token is verlopen of ongeldig"));
            }

        } catch (Exception e) {
            log.error("Fout bij tokenvalidatie: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Ongeldige of verlopen token"));
        }
    }
}