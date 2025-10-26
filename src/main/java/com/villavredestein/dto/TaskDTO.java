package com.villavredestein.dto;

import jakarta.validation.constraints.NotBlank;

public record TaskDTO(
        @NotBlank String name,
        @NotBlank String description
) {}