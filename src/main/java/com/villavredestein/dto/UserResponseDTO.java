package com.villavredestein.dto;

public record UserResponseDTO(
        Long id,
        String username,
        String email,
        String role
) {}