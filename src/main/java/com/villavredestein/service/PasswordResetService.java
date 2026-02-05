package com.villavredestein.service;

import com.villavredestein.model.PasswordResetToken;
import com.villavredestein.model.User;
import com.villavredestein.repository.PasswordResetTokenRepository;
import com.villavredestein.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Locale;
import java.util.UUID;

@Service
@Transactional
public class PasswordResetService {

    private static final long EXPIRATION_MINUTES = 30;

    private final PasswordResetTokenRepository tokenRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public PasswordResetService(
            PasswordResetTokenRepository tokenRepository,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.tokenRepository = tokenRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public String createResetToken(String email) {
        String normalizedEmail = email == null ? null : email.trim().toLowerCase(Locale.ROOT);
        if (normalizedEmail == null || normalizedEmail.isBlank()) {
            throw new IllegalArgumentException("Email is required");
        }

        User user = userRepository.findByEmail(normalizedEmail)
                .orElseThrow(() -> new IllegalArgumentException("Unknown email"));

        String token = generateUniqueToken();
        Instant expiresAt = Instant.now().plus(EXPIRATION_MINUTES, ChronoUnit.MINUTES);

        tokenRepository.save(new PasswordResetToken(token, user, expiresAt));
        return token;
    }

    public void resetPassword(String token, String newPassword) {
        PasswordResetToken resetToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Invalid password reset token"));

        if (resetToken.isExpired()) throw new IllegalStateException("Password reset token expired");
        if (resetToken.isUsed()) throw new IllegalStateException("Password reset token already used");
        if (newPassword == null || newPassword.isBlank()) throw new IllegalArgumentException("New password is required");

        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        resetToken.markUsed();
        tokenRepository.save(resetToken);
    }

    private String generateUniqueToken() {
        String raw = UUID.randomUUID().toString().replace("-", "");
        String token = raw.length() > 64 ? raw.substring(0, 64) : raw;

        while (tokenRepository.existsByToken(token)) {
            raw = UUID.randomUUID().toString().replace("-", "");
            token = raw.length() > 64 ? raw.substring(0, 64) : raw;
        }
        return token;
    }
}