package com.villavredestein.controller;

import com.villavredestein.dto.LoginRequestDTO;
import com.villavredestein.dto.LoginResponseDTO;
import com.villavredestein.dto.UserResponseDTO;
import com.villavredestein.repository.RoomRepository;
import com.villavredestein.security.JwtService;
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
@CrossOrigin
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserService userService;
    private final RoomRepository roomRepository;

    public AuthController(
            AuthenticationManager authenticationManager,
            JwtService jwtService,
            UserService userService,
            RoomRepository roomRepository
    ) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.userService = userService;
        this.roomRepository = roomRepository;
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

        boolean isStudentLogin = "STUDENT".equals(requestedMode)
                || (requestedMode == null && roles.contains("ROLE_STUDENT"));

        if (isStudentLogin) {
            String chosenRoom = normalizeRoom(request.room());
            if (chosenRoom == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Kies eerst je kamer.");
            }

            String assignedRoom = roomRepository.findByOccupant_Id(me.id())
                    .map(r -> r.getName())
                    .orElse(null);

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
                token
        ));
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