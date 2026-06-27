package com.villavredestein.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record GoogleLoginRequestDTO(
        @NotBlank(message = "accessToken is required")
        String accessToken,

        @Pattern(
                regexp = "STUDENT|CLEANER|ADMIN",
                flags = Pattern.Flag.CASE_INSENSITIVE,
                message = "loginMode must be STUDENT, CLEANER or ADMIN"
        )
        String loginMode,

        @Size(max = 50)
        String room
) {}
