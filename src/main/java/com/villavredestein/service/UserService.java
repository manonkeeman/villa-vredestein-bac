package com.villavredestein.service;

import com.villavredestein.dto.UserResponseDTO;
import com.villavredestein.dto.UserUpdateDTO;
import com.villavredestein.model.User;
import com.villavredestein.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * {@code UserService} bevat businesslogica rondom gebruikers.
 *
 * <p>Belangrijk security-principe:
 * STUDENT/CLEANER mogen alleen zichzelf wijzigen (ownership).
 * ADMIN mag iedereen wijzigen.</p>
 */
@Service
@Transactional
public class UserService {

    private static final Set<String> ALLOWED_ROLES = Set.of("ADMIN", "STUDENT", "CLEANER");

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // =====================================================================
    // CREATE
    // =====================================================================

    /**
     * Maakt een nieuwe student aan met rol STUDENT en een gehashte password.
     */
    public UserResponseDTO createStudent(String username, String email, String rawPassword) {
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email bestaat al");
        }

        User user = new User(
                username,
                email,
                passwordEncoder.encode(rawPassword),
                "STUDENT"
        );

        return toDTO(userRepository.save(user));
    }

    // =====================================================================
    // READ
    // =====================================================================

    public List<UserResponseDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public Optional<UserResponseDTO> getUserById(Long id) {
        return userRepository.findById(id).map(this::toDTO);
    }

    public Optional<UserResponseDTO> getUserByEmail(String email) {
        return userRepository.findByEmail(email).map(this::toDTO);
    }

    /**
     * Geeft het profiel van de ingelogde gebruiker terug.
     */
    public UserResponseDTO getMe() {
        return toDTO(currentUser());
    }

    // =====================================================================
    // UPDATE
    // =====================================================================

    public UserResponseDTO changeRole(Long id, String newRole) {
        String normalized = normalizeRole(newRole);
        if (!ALLOWED_ROLES.contains(normalized)) {
            throw new IllegalArgumentException("newRole moet ADMIN, STUDENT of CLEANER zijn");
        }

        User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        user.setRole(normalized);
        return toDTO(userRepository.save(user));
    }

    /**
     * STUDENT/CLEANER mogen alleen hun eigen profiel aanpassen.
     * ADMIN mag iedereen aanpassen.
     */
    public UserResponseDTO updateProfile(Long id, UserUpdateDTO dto) {
        User target = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        assertOwnerOrAdmin(target.getId());

        if (dto.getUsername() != null) {
            target.setUsername(dto.getUsername());
        }

        if (dto.getEmail() != null && !dto.getEmail().equalsIgnoreCase(target.getEmail())) {
            if (userRepository.existsByEmail(dto.getEmail())) {
                throw new IllegalArgumentException("Email bestaat al");
            }
            target.setEmail(dto.getEmail());
        }

        return toDTO(userRepository.save(target));
    }

    // =====================================================================
    // DELETE
    // =====================================================================

    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new EntityNotFoundException("User not found");
        }
        userRepository.deleteById(id);
    }

    // =====================================================================
    // SECURITY HELPERS
    // =====================================================================

    private User currentUser() {
        String email = currentEmail();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("Current user not found"));
    }

    private String currentEmail() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getName() == null) {
            throw new AccessDeniedException("Not authenticated");
        }
        return auth.getName(); // email
    }

    private boolean isAdmin() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) return false;

        return auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }

    private void assertOwnerOrAdmin(Long targetUserId) {
        if (isAdmin()) return;

        User me = currentUser();
        if (!me.getId().equals(targetUserId)) {
            throw new AccessDeniedException("Je mag alleen je eigen profiel wijzigen");
        }
    }

    private String normalizeRole(String role) {
        if (role == null) {
            return "";
        }
        String r = role.trim().toUpperCase();
        if (r.startsWith("ROLE_")) {
            r = r.substring(5);
        }
        return r;
    }

    // =====================================================================
    // MAPPER
    // =====================================================================

    private UserResponseDTO toDTO(User user) {
        return new UserResponseDTO(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getRole()
        );
    }
}