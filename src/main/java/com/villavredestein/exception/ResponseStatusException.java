package com.villavredestein.exception;

public class ResponseStatusException extends RuntimeException {
    public ResponseStatusException(String message) {
        super(message);
    }
}