package com.villavredestein.dto;

import com.villavredestein.model.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, String> {
    List<PasswordResetToken> findAllByExpiresAtBefore(Instant time);
}