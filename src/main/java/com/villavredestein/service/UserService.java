package com.villavredestein.service;

import com.villavredestein.dto.UserResponseDTO;
import com.villavredestein.dto.UserUpdateDTO;
import com.villavredestein.model.User;
import com.villavredestein.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;

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

    public UserResponseDTO createStudent(String username, String email, String rawPassword) {
        String normalizedEmail = normalizeEmail(email);
        if (userRepository.existsByEmailIgnoreCase(normalizedEmail)) {
            throw new IllegalArgumentException("Email bestaat al");
        }

        User user = new User(
                username,
                normalizedEmail,
                passwordEncoder.encode(rawPassword),
                "STUDENT"
        );

        return toDTO(userRepository.save(user));
    }

    // =====================================================================
    // READ
    // =====================================================================

    public List<UserResponseDTO> getAllUsers() {
        return userRepository.findAllByOrderByIdAsc().stream()
                .map(this::toDTO)
                .toList();
    }

    public Optional<UserResponseDTO> getUserById(Long id) {
        return userRepository.findById(id).map(this::toDTO);
    }

    public Optional<UserResponseDTO> getUserByEmail(String email) {
        return userRepository.findByEmailIgnoreCase(normalizeEmail(email)).map(this::toDTO);
    }

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
                .orElseThrow(() -> new EntityNotFoundException("User niet gevonden: " + id));

        user.setRole(normalized);
        return toDTO(user);
    }

    public UserResponseDTO updateProfile(Long id, UserUpdateDTO dto) {
        User target = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User niet gevonden: " + id));

        assertOwnerOrAdmin(target.getId());

        if (dto.getUsername() != null) {
            target.setUsername(dto.getUsername());
        }

        if (dto.getEmail() != null && !dto.getEmail().equalsIgnoreCase(target.getEmail())) {
            String normalizedEmail = normalizeEmail(dto.getEmail());
            if (userRepository.existsByEmailIgnoreCase(normalizedEmail)) {
                throw new IllegalArgumentException("Email bestaat al");
            }
            target.setEmail(normalizedEmail);
        }

        return toDTO(target);
    }

    // =====================================================================
    // DELETE
    // =====================================================================

    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User niet gevonden: " + id));
        userRepository.delete(user);
    }

    // =====================================================================
    // SECURITY HELPERS
    // =====================================================================

    private String normalizeEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email is verplicht");
        }
        return email.trim().toLowerCase();
    }

    private User currentUser() {
        String email = normalizeEmail(currentEmail());
        return userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new EntityNotFoundException("Current user not found"));
    }

    private String currentEmail() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getName() == null || auth.getName().isBlank()) {
            throw new AccessDeniedException("Not authenticated");
        }
        return auth.getName(); // email
    }

    private boolean isAdmin() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
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