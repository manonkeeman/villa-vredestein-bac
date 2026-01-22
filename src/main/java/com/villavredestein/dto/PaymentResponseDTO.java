package com.villavredestein.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PaymentResponseDTO(
        Long id,
        BigDecimal amount,
        LocalDateTime createdAt,
        LocalDateTime paidAt,
        String status,
        String description,
        String studentName,
        String studentEmail
) {
}