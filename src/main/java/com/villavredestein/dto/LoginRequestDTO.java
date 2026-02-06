package com.villavredestein.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record LoginRequestDTO(
        @NotBlank(message = "Email is required")
        @Email(message = "Email must be valid")
        String email,

        @NotBlank(message = "Password is required")
        @Size(min = 8, message = "Password must be at least 8 characters long")
        String password,

        @Pattern(
                regexp = "STUDENT|CLEANER|ADMIN",
                message = "loginMode must be STUDENT, CLEANER or ADMIN"
        )
        String loginMode,

        @Size(max = 50, message = "Room name may contain at most 50 characters")
        String room
) {}