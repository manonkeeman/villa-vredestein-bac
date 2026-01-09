package com.villavredestein.config;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * 404 – Wordt gebruikt wanneer een entiteit niet gevonden wordt in de database.
     */
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(EntityNotFoundException ex) {
        log.warn("Niet gevonden: {}", ex.getMessage());
        return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    /**
     * 403 – Wordt gebruikt wanneer een gebruiker onvoldoende rechten heeft.
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> handleForbidden(AccessDeniedException ex) {
        log.warn("Toegang geweigerd: {}", ex.getMessage());
        return buildResponse(HttpStatus.FORBIDDEN, "Toegang geweigerd tot deze resource");
    }

    /**
     * 400 – Validatiefouten vanuit @Valid op request bodies.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
        List<Map<String, String>> fieldErrors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(this::toFieldError)
                .collect(Collectors.toList());

        Map<String, Object> body = baseBody(HttpStatus.BAD_REQUEST, "Validatie mislukt");
        body.put("fieldErrors", fieldErrors);

        log.warn("Validatie mislukt: {}", fieldErrors);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    /**
     * 400 – Validatiefouten vanuit @Validated op path/query params (ConstraintViolationException).
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Map<String, Object>> handleConstraintViolation(ConstraintViolationException ex) {
        Map<String, Object> body = baseBody(HttpStatus.BAD_REQUEST, "Validatie mislukt");
        body.put("violations", ex.getConstraintViolations().stream()
                .map(v -> Map.of(
                        "path", String.valueOf(v.getPropertyPath()),
                        "message", v.getMessage()
                ))
                .collect(Collectors.toList()));

        log.warn("ConstraintViolation: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    /**
     * 400 – Onleesbare JSON / verkeerd format (bijv. enum/number parse errors).
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> handleNotReadable(HttpMessageNotReadableException ex) {
        log.warn("Onleesbare request body: {}", ex.getMessage());
        return buildResponse(HttpStatus.BAD_REQUEST, "Request body is ongeldig of niet leesbaar");
    }

    /**
     * 400 – Wordt gebruikt bij ongeldige invoer of verkeerde parameters.
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleBadRequest(IllegalArgumentException ex) {
        log.warn("Ongeldige invoer: {}", ex.getMessage());
        return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    /**
     * 500 – Catch-all voor overige onverwachte fouten.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneric(Exception ex) {
        log.error("Onverwachte fout opgetreden", ex);
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Er is een interne fout opgetreden");
    }

    // =====================================================================
    // Helpers
    // =====================================================================

    private Map<String, Object> baseBody(HttpStatus status, String message) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase());
        body.put("message", message);
        return body;
    }

    private ResponseEntity<Map<String, Object>> buildResponse(HttpStatus status, String message) {
        return ResponseEntity.status(status).body(baseBody(status, message));
    }

    private Map<String, String> toFieldError(FieldError fe) {
        return Map.of(
                "field", fe.getField(),
                "message", fe.getDefaultMessage() == null ? "Ongeldige waarde" : fe.getDefaultMessage()
        );
    }
}