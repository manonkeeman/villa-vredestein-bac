package com.villavredestein.service;

import com.villavredestein.dto.UserRequestDTO;
import com.villavredestein.dto.UserResponseDTO;
import com.villavredestein.model.User;
import com.villavredestein.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

@Service
@Transactional
public class UserService implements org.springframework.security.core.userdetails.UserDetailsService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    private static final Set<User.Role> ALLOWED_ROLES =
            Set.of(User.Role.ADMIN, User.Role.STUDENT, User.Role.CLEANER);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, @Lazy PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // =====================================================================
    // # SPRING SECURITY
    // =====================================================================

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        if (email == null || email.isBlank()) {
            throw new UsernameNotFoundException("Email is required");
        }

        String normalizedEmail = email.trim().toLowerCase(Locale.ROOT);

        User user = userRepository.findByEmailIgnoreCase(normalizedEmail)
                .orElseThrow(() -> {
                    log.warn("Login failed: user not found ({})", maskEmail(normalizedEmail));
                    return new UsernameNotFoundException("User not found");
                });

        User.Role role = user.getRole() != null ? user.getRole() : User.Role.STUDENT;

        return org.springframework.security.core.userdetails.User
                .withUsername(user.getEmail())
                .password(user.getPassword())
                .roles(role.name())
                .build();
    }

    // =====================================================================
    // # CREATE
    // =====================================================================

    public UserResponseDTO createStudent(String username, String email, String rawPassword) {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Username is required");
        }
        if (rawPassword == null || rawPassword.isBlank()) {
            throw new IllegalArgumentException("Password is required");
        }

        String normalizedEmail = normalizeEmail(email);
        if (userRepository.existsByEmailIgnoreCase(normalizedEmail)) {
            throw new IllegalArgumentException("Email already exists");
        }

        User user = new User(
                username.trim(),
                normalizedEmail,
                passwordEncoder.encode(rawPassword),
                User.Role.STUDENT
        );

        return toDTO(userRepository.save(user));
    }

    // =====================================================================
    // # READ
    // =====================================================================

    @Transactional(readOnly = true)
    public List<UserResponseDTO> getAllUsers() {
        return userRepository.findAllByOrderByIdAsc()
                .stream()
                .map(this::toDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public Optional<UserResponseDTO> getUserById(Long id) {
        return userRepository.findById(id).map(this::toDTO);
    }

    @Transactional(readOnly = true)
    public Optional<UserResponseDTO> getUserByEmail(String email) {
        return userRepository.findByEmailIgnoreCase(normalizeEmail(email)).map(this::toDTO);
    }

    @Transactional(readOnly = true)
    public UserResponseDTO getMe() {
        return toDTO(currentUser());
    }

    // =====================================================================
    // # UPDATE
    // =====================================================================

    public UserResponseDTO changeRole(Long id, String newRole) {
        User.Role role = parseRole(newRole);
        if (!ALLOWED_ROLES.contains(role)) {
            throw new IllegalArgumentException("newRole must be ADMIN, STUDENT or CLEANER");
        }

        User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + id));

        user.setRole(role);
        return toDTO(user);
    }

    public UserResponseDTO updateProfile(Long id, UserRequestDTO dto) {
        User target = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + id));

        assertOwnerOrAdmin(target.getId());

        if (dto.getUsername() != null && !dto.getUsername().isBlank()) {
            target.setUsername(dto.getUsername().trim());
        }

        if (dto.getEmail() != null && !dto.getEmail().isBlank()
                && !dto.getEmail().equalsIgnoreCase(target.getEmail())) {

            String normalizedEmail = normalizeEmail(dto.getEmail());
            if (userRepository.existsByEmailIgnoreCase(normalizedEmail)) {
                throw new IllegalArgumentException("Email already exists");
            }
            target.setEmail(normalizedEmail);
        }

        return toDTO(target);
    }

    // =====================================================================
    // # DELETE
    // =====================================================================

    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + id));
        userRepository.delete(user);
    }

    // =====================================================================
    // # SECURITY HELPERS
    // =====================================================================

    private String normalizeEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email is required");
        }
        return email.trim().toLowerCase(Locale.ROOT);
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
                .map(GrantedAuthority::getAuthority)
                .anyMatch(a -> a.equals("ROLE_ADMIN"));
    }

    private void assertOwnerOrAdmin(Long targetUserId) {
        if (isAdmin()) return;

        User me = currentUser();
        if (!me.getId().equals(targetUserId)) {
            throw new AccessDeniedException("You may only update your own profile");
        }
    }

    private User.Role parseRole(String role) {
        if (role == null || role.isBlank()) {
            throw new IllegalArgumentException("Role is required");
        }
        String r = role.trim().toUpperCase(Locale.ROOT);
        if (r.startsWith("ROLE_")) {
            r = r.substring("ROLE_".length());
        }
        try {
            return User.Role.valueOf(r);
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Invalid role: " + role + ". Use ADMIN, STUDENT or CLEANER.");
        }
    }

    private String maskEmail(String email) {
        if (email == null || email.isBlank()) return "(no-email)";
        String trimmed = email.trim();
        int at = trimmed.indexOf('@');
        if (at <= 1) return "***";
        return trimmed.charAt(0) + "***" + trimmed.substring(at);
    }

    // =====================================================================
    // # MAPPER
    // =====================================================================

    private UserResponseDTO toDTO(User user) {
        return new UserResponseDTO(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getRole() != null ? user.getRole().name() : null
        );
    }
}