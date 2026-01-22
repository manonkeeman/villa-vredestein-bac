package com.villavredestein.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class DocumentRequestDTO {

    @NotBlank(message = "roleAccess is required")
    @Pattern(
            regexp = "^(ALL|ADMIN|STUDENT|CLEANER)$",
            message = "roleAccess must be ALL, ADMIN, STUDENT or CLEANER"
    )
    private String roleAccess;

    public String getRoleAccess() {
        return roleAccess;
    }

    public void setRoleAccess(String roleAccess) {
        this.roleAccess = roleAccess;
    }

    public String getNormalizedRoleAccess() {
        if (roleAccess == null || roleAccess.isBlank()) {
            return "ROLE_ALL";
        }

        String normalized = roleAccess.trim().toUpperCase();
        if ("ALL".equals(normalized)) {
            return "ROLE_ALL";
        }
        return normalized;
    }
}