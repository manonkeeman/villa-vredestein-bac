package com.villavredestein.dto;

public record DocumentResponseDTO(
        Long id,
        String title,
        String description,
        String roleAccess,
        String uploadedBy
) {
}