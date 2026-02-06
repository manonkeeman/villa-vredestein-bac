package com.villavredestein.dto;

public record UserResponseDTO(
        Long id,
        String username,
        String fullName,
        String email,
        String role,
        String roomName,
        String phoneNumber,
        String emergencyPhoneNumber,
        String studyOrWork,
        String socialPreference,
        String mealPreference,
        String availabilityStatus,
        boolean statusToggle,
        String profileImagePath
) {}