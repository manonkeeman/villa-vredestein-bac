package com.villavredestein.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record AssignmentDTO(
        @NotNull Long taskId,
        @NotNull Long userId,
        @NotNull @FutureOrPresent LocalDate dueDate
) {}