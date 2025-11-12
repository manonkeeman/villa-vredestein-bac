package com.villavredestein.config;

import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Globale exception handler voor alle REST-endpoints van Villa Vredestein.
 * Zorgt voor consistente foutafhandeling en gestructureerde JSON-responses.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * 404 – Wordt gebruikt wanneer een entiteit niet gevonden wordt in de database.
     */
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(EntityNotFoundException ex) {
        log.warn("❌ Niet gevonden: {}", ex.getMessage());
        return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    /**
     * 403 – Wordt gebruikt wanneer een gebruiker onvoldoende rechten heeft.
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> handleForbidden(AccessDeniedException ex) {
        log.warn("🚫 Toegang geweigerd: {}", ex.getMessage());
        return buildResponse(HttpStatus.FORBIDDEN, "Toegang geweigerd tot deze resource");
    }

    /**
     * 400 – Wordt gebruikt bij ongeldige invoer of verkeerde parameters.
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleBadRequest(IllegalArgumentException ex) {
        log.warn("⚠️ Ongeldige invoer: {}", ex.getMessage());
        return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    /**
     * 500 – Catch-all voor overige onverwachte fouten.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneric(Exception ex) {
        log.error("💥 Onverwachte fout opgetreden", ex);
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Er is een interne fout opgetreden");
    }

    /**
     * Bouwt een consistente foutresponse.
     */
    private ResponseEntity<Map<String, Object>> buildResponse(HttpStatus status, String message) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase());
        body.put("message", message);

        return ResponseEntity
                .status(status)
                .body(body);
    }
}