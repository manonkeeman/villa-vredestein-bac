package com.villavredestein.service;

import com.villavredestein.dto.ResetPasswordRequestDTO;
import com.villavredestein.model.PasswordResetToken;
import com.villavredestein.model.User;
import com.villavredestein.dto.PasswordResetTokenRepository;
import com.villavredestein.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HexFormat;
import java.util.Locale;
import java.util.Optional;

@Service
@Transactional
public class PasswordResetService {

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final long resetExpiryMinutes;

    public PasswordResetService(
            UserRepository userRepository,
            PasswordResetTokenRepository tokenRepository,
            PasswordEncoder passwordEncoder,
            @Value("${app.password-reset.expiry-minutes:30}") long resetExpiryMinutes
    ) {
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.resetExpiryMinutes = resetExpiryMinutes;
    }

    // -----------------------------
    // Preferred API (veilig)
    // -----------------------------

    public Optional<String> createResetTokenIfUserExists(String emailRaw) {
        String email = normalizeEmail(emailRaw);
        if (email.isBlank()) return Optional.empty();

        Optional<User> userOpt = userRepository.findByEmailIgnoreCase(email);
        if (userOpt.isEmpty()) return Optional.empty();

        User user = userOpt.get();

        String token = generateToken64Hex();
        Instant expiresAt = Instant.now().plus(resetExpiryMinutes, ChronoUnit.MINUTES);

        tokenRepository.save(new PasswordResetToken(token, user, expiresAt));
        return Optional.of(token);
    }

    public void resetPassword(ResetPasswordRequestDTO dto) {
        if (dto == null) throw new IllegalArgumentException("Request is required");

        String token = dto.token();
        String newPassword = dto.newPassword();

        if (token == null || token.isBlank()) throw new IllegalArgumentException("Token is required");
        if (newPassword == null || newPassword.isBlank()) throw new IllegalArgumentException("New password is required");
        if (newPassword.length() < 8) throw new IllegalArgumentException("New password must be at least 8 characters");

        PasswordResetToken prt = tokenRepository.findById(token.trim())
                .orElseThrow(() -> new IllegalArgumentException("Invalid token"));

        if (prt.isUsed()) throw new IllegalArgumentException("Token already used");
        if (prt.isExpired()) throw new IllegalArgumentException("Token expired");

        User user = prt.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));

        prt.markUsed();
    }

    // -----------------------------
    // Backwards compatible API
    // -----------------------------

    public String createResetToken(String emailRaw) {
        return createResetTokenIfUserExists(emailRaw).orElse(null);
    }

    public void resetPassword(String token, String newPassword) {
        resetPassword(new ResetPasswordRequestDTO(token, newPassword));
    }

    // -----------------------------
    // Helpers
    // -----------------------------

    private String normalizeEmail(String email) {
        if (email == null) return "";
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private static String generateToken64Hex() {
        byte[] bytes = new byte[32]; // 32 bytes = 64 hex chars
        new SecureRandom().nextBytes(bytes);
        return HexFormat.of().formatHex(bytes);
    }
}