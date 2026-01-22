package com.villavredestein.dto;

public record LoginResponseDTO(
        String username,
        String email,
        String role,
        String token
) {
}