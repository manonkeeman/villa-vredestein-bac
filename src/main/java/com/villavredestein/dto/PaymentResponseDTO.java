package com.villavredestein.dto;

import com.villavredestein.model.Payment;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PaymentResponseDTO(
        Long id,
        BigDecimal amount,
        LocalDateTime createdAt,
        LocalDateTime paidAt,
        Payment.PaymentStatus status,
        String description,
        String studentEmail,
        String studentUsername
) {
}