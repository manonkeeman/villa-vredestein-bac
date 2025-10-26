package com.villavredestein.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class, IllegalArgumentException.class})
    public ResponseEntity<ApiError> badRequest(Exception ex, HttpServletRequest req){
        var err = new ApiError(Instant.now(), 400, "Bad Request", ex.getMessage(), req.getRequestURI());
        return ResponseEntity.badRequest().body(err);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> serverError(Exception ex, HttpServletRequest req){
        var err = new ApiError(Instant.now(), 500, "Server Error", ex.getMessage(), req.getRequestURI());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(err);
    }
}