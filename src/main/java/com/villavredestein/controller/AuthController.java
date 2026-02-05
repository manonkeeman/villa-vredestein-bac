package com.villavredestein.controller;

import com.villavredestein.dto.LoginRequestDTO;
import com.villavredestein.dto.LoginResponseDTO;
import com.villavredestein.dto.UserResponseDTO;
import com.villavredestein.security.JwtService;
import com.villavredestein.service.UserService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Locale;

@Validated
@RestController
@RequestMapping(value = "/api/auth", produces = MediaType.APPLICATION_JSON_VALUE)
@CrossOrigin
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserService userService;

    public AuthController(AuthenticationManager authenticationManager, JwtService jwtService, UserService userService) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.userService = userService;
    }

    @PostMapping(value = "/login", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<LoginResponseDTO> login(@Valid @RequestBody LoginRequestDTO request) {
        String email = normalizeEmail(request.email());

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, request.password())
        );

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();

        // In your UserDetailsService you set username = user.getEmail(), so principal username is the email.
        String principalEmail = normalizeEmail(userDetails.getUsername());

        String authority = userDetails.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .findFirst()
                .orElse("ROLE_STUDENT");

        // Keep the ROLE_ prefix, because your security checks use hasRole/hasAnyRole which map to ROLE_*.
        String token = jwtService.generateToken(principalEmail, authority);

        log.info("Login successful for {} with role {}", principalEmail, authority);

        // Show a friendly username in the response when available
        String displayUsername = userService.getUserByEmail(principalEmail)
                .map(UserResponseDTO::username)
                .orElse(principalEmail);

        return ResponseEntity.ok(new LoginResponseDTO(
                displayUsername,
                principalEmail,
                authority,
                token
        ));
    }

    private String normalizeEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email is required");
        }
        return email.trim().toLowerCase(Locale.ROOT);
    }
}