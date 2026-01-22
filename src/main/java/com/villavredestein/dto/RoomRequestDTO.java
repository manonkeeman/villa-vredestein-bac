package com.villavredestein.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RoomRequestDTO(

        @NotBlank(message = "Room name is required")
        @Size(max = 50, message = "Room name may contain at most 50 characters")
        String name

) {
}