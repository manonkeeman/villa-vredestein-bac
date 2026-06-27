package com.villavredestein.controller;

import com.villavredestein.dto.GoogleLoginRequestDTO;
import com.villavredestein.dto.LoginRequestDTO;
import com.villavredestein.dto.LoginResponseDTO;
import com.villavredestein.dto.UserResponseDTO;
import com.villavredestein.security.JwtService;
import com.villavredestein.service.GoogleTokenVerifierService;
import com.villavredestein.service.UserService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

@Validated
@RestController
@RequestMapping(value = "/api/auth", produces = MediaType.APPLICATION_JSON_VALUE)
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserService userService;
    private final GoogleTokenVerifierService googleTokenVerifierService;

    public AuthController(
            AuthenticationManager authenticationManager,
            JwtService jwtService,
            UserService userService,
            GoogleTokenVerifierService googleTokenVerifierService
    ) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.userService = userService;
        this.googleTokenVerifierService = googleTokenVerifierService;
    }

    @PostMapping(value = "/login", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<LoginResponseDTO> login(@Valid @RequestBody LoginRequestDTO request) {
        String email = normalizeEmail(request.email());

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, request.password())
        );

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String principalEmail = normalizeEmail(userDetails.getUsername());

        Set<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .map(String::toUpperCase)
                .collect(Collectors.toSet());

        String requestedMode = normalizeMode(request.loginMode()); // null if not provided

        if (requestedMode != null) {
            String requiredRole = "ROLE_" + requestedMode;
            if (!roles.contains(requiredRole)) {
                throw new ResponseStatusException(
                        HttpStatus.FORBIDDEN,
                        "Account heeft geen toegang voor: " + requestedMode
                );
            }
        }

        UserResponseDTO me = userService.getUserByEmail(principalEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));

        boolean isStudentLogin = "STUDENT".equals(requestedMode);

        if (isStudentLogin) {
            String chosenRoom = normalizeRoom(request.room());
            if (chosenRoom == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Kies eerst je kamer.");
            }

            String assignedRoom = me.roomName();

            if (assignedRoom == null) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "Voor dit account is nog geen kamer gekoppeld. Neem contact op met beheer."
                );
            }

            if (!assignedRoom.equalsIgnoreCase(chosenRoom)) {
                throw new ResponseStatusException(
                        HttpStatus.FORBIDDEN,
                        "Verkeerde kamer gekozen. Dit account hoort bij: " + assignedRoom
                );
            }
        }

        String primaryRole = roles.contains("ROLE_ADMIN") ? "ROLE_ADMIN"
                : roles.contains("ROLE_CLEANER") ? "ROLE_CLEANER"
                : "ROLE_STUDENT";

        String token = jwtService.generateToken(principalEmail, primaryRole);

        log.info("Login successful for {} with role {}", principalEmail, primaryRole);

        String displayUsername = (me.username() != null && !me.username().isBlank())
                ? me.username()
                : principalEmail;

        return ResponseEntity.ok(new LoginResponseDTO(
                displayUsername,
                principalEmail,
                primaryRole,
                token,
                me
        ));
    }

    @PostMapping(value = "/google-login", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<LoginResponseDTO> googleLogin(@Valid @RequestBody GoogleLoginRequestDTO request) {
        String email = normalizeEmail(googleTokenVerifierService.verifyAndGetEmail(request.accessToken()));

        UserResponseDTO me = userService.getUserByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "Geen account gevonden voor dit Google-account. Neem contact op met de beheerder."));

        String roleStr = me.role() != null ? me.role().toUpperCase(Locale.ROOT) : "";
        String requestedMode = normalizeMode(request.loginMode());

        if (requestedMode != null && !roleStr.equals(requestedMode)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Account heeft geen toegang voor: " + requestedMode);
        }

        boolean isStudentLogin = "STUDENT".equals(roleStr) || "STUDENT".equals(requestedMode);
        if (isStudentLogin) {
            String chosenRoom = normalizeRoom(request.room());
            if (chosenRoom == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Kies eerst je kamer.");
            }
            String assignedRoom = me.roomName();
            if (assignedRoom == null) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "Voor dit account is nog geen kamer gekoppeld. Neem contact op met beheer.");
            }
            if (!assignedRoom.equalsIgnoreCase(chosenRoom)) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                        "Verkeerde kamer gekozen. Dit account hoort bij: " + assignedRoom);
            }
        }

        String primaryRole = "ROLE_" + roleStr;
        String token = jwtService.generateToken(email, primaryRole);

        log.info("Google login successful for {} with role {}", email, primaryRole);

        String displayUsername = (me.username() != null && !me.username().isBlank())
                ? me.username()
                : email;

        return ResponseEntity.ok(new LoginResponseDTO(displayUsername, email, primaryRole, token, me));
    }

    private String normalizeEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email is required");
        }
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private String normalizeMode(String loginMode) {
        if (loginMode == null || loginMode.isBlank()) return null;
        return loginMode.trim().toUpperCase(Locale.ROOT);
    }

    private String normalizeRoom(String room) {
        if (room == null || room.isBlank()) return null;
        return room.trim();
    }
}