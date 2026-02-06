package com.villavredestein.service;

import com.villavredestein.model.Room;
import com.villavredestein.repository.RoomRepository;
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
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
@Transactional
public class UserService implements org.springframework.security.core.userdetails.UserDetailsService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    private static final Set<User.Role> ALLOWED_ROLES =
            Set.of(User.Role.ADMIN, User.Role.STUDENT, User.Role.CLEANER);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoomRepository roomRepository;
    private final Path uploadDir;

    public UserService(
            UserRepository userRepository,
            @Lazy PasswordEncoder passwordEncoder,
            RoomRepository roomRepository,
            @Value("${app.upload-dir:uploads}") String uploadDir
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.roomRepository = roomRepository;
        this.uploadDir = Paths.get(uploadDir).toAbsolutePath().normalize();
    }

    // =====================================================================
    // # SPRING SECURITY
    // =====================================================================

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        if (email == null || email.isBlank()) {
            throw new UsernameNotFoundException("Email is required");
        }
        String normalizedEmail = normalizeEmail(email);

        User user = userRepository.findByEmailIgnoreCase(normalizedEmail)
                .orElseThrow(() -> {
                    log.warn("Login failed: user not found ({})", maskEmail(normalizedEmail));
                    return new UsernameNotFoundException("User not found");
                });

        User.Role role = user.getRole() == null ? User.Role.STUDENT : user.getRole();

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
        return createUser(username, email, rawPassword, User.Role.STUDENT);
    }

    public UserResponseDTO createAdmin(String username, String email, String rawPassword) {
        return createUser(username, email, rawPassword, User.Role.ADMIN);
    }

    public UserResponseDTO createCleaner(String username, String email, String rawPassword) {
        return createUser(username, email, rawPassword, User.Role.CLEANER);
    }

    public UserResponseDTO seedUserIfMissing(String username, String email, String rawPassword, User.Role role) {
        String normalizedEmail = normalizeEmail(email);

        return userRepository.findByEmailIgnoreCase(normalizedEmail)
                .map(this::toDTO)
                .orElseGet(() -> {
                    log.info("Seeding user {} with role {}", maskEmail(normalizedEmail), role);
                    return createUser(username, normalizedEmail, rawPassword, role);
                });
    }

    private UserResponseDTO createUser(String username, String email, String rawPassword, User.Role role) {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Username is required");
        }
        if (rawPassword == null || rawPassword.isBlank()) {
            throw new IllegalArgumentException("Password is required");
        }
        if (rawPassword.length() < 8) {
            throw new IllegalArgumentException("Password must be at least 8 characters");
        }
        if (role == null) {
            throw new IllegalArgumentException("Role is required");
        }
        if (!ALLOWED_ROLES.contains(role)) {
            throw new IllegalArgumentException("Role must be ADMIN, STUDENT or CLEANER");
        }

        String normalizedEmail = normalizeEmail(email);
        if (userRepository.existsByEmailIgnoreCase(normalizedEmail)) {
            throw new IllegalArgumentException("Email already exists");
        }

        User user = new User(
                username.trim(),
                normalizedEmail,
                passwordEncoder.encode(rawPassword),
                role
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

    @Transactional(readOnly = true)
    public Long getMyId() {
        return currentUser().getId();
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

        if (dto.getFullName() != null) {
            target.setFullName(dto.getFullName());
        }

        if (dto.getPhoneNumber() != null) {
            target.setPhoneNumber(dto.getPhoneNumber());
        }

        if (dto.getEmergencyPhoneNumber() != null) {
            target.setEmergencyPhoneNumber(dto.getEmergencyPhoneNumber());
        }

        if (dto.getStudyOrWork() != null) {
            target.setStudyOrWork(dto.getStudyOrWork());
        }

        if (dto.getSocialPreference() != null) {
            target.setSocialPreference(dto.getSocialPreference());
        }

        if (dto.getMealPreference() != null) {
            target.setMealPreference(dto.getMealPreference());
        }

        if (dto.getAvailabilityStatus() != null) {
            target.setAvailabilityStatus(dto.getAvailabilityStatus());
        }

        if (dto.getStatusToggle() != null) {
            target.setStatusToggle(dto.getStatusToggle());
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

    public void changeMyPassword(String oldPassword, String newPassword) {
        if (oldPassword == null || oldPassword.isBlank()) {
            throw new IllegalArgumentException("Old password is required");
        }
        if (newPassword == null || newPassword.isBlank()) {
            throw new IllegalArgumentException("New password is required");
        }
        if (newPassword.length() < 8) {
            throw new IllegalArgumentException("New password must be at least 8 characters");
        }

        User me = currentUser();
        if (!passwordEncoder.matches(oldPassword, me.getPassword())) {
            throw new AccessDeniedException("Old password is incorrect");
        }

        me.setPassword(passwordEncoder.encode(newPassword));
    }

    public UserResponseDTO uploadMyProfilePhoto(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File is required");
        }

        String contentType = file.getContentType();
        if (contentType == null || !(contentType.equalsIgnoreCase("image/jpeg")
                || contentType.equalsIgnoreCase("image/png")
                || contentType.equalsIgnoreCase("image/webp"))) {
            throw new IllegalArgumentException("Only JPEG, PNG or WEBP images are allowed");
        }

        User me = currentUser();

        try {
            Files.createDirectories(uploadDir);

            String extension = switch (contentType.toLowerCase()) {
                case "image/png" -> ".png";
                case "image/webp" -> ".webp";
                default -> ".jpg";
            };

            String filename = "profile_" + me.getId() + "_" + UUID.randomUUID() + extension;
            Path targetPath = uploadDir.resolve(filename).normalize();

            if (!targetPath.startsWith(uploadDir)) {
                throw new IllegalArgumentException("Invalid file path");
            }

            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

            deleteFileQuietly(me.getProfileImagePath());

            me.setProfileImagePath(filename);
            return toDTO(me);

        } catch (Exception e) {
            throw new RuntimeException("Failed to store profile photo", e);
        }
    }

    public UserResponseDTO deleteMyProfilePhoto() {
        User me = currentUser();
        deleteFileQuietly(me.getProfileImagePath());
        me.setProfileImagePath(null);
        return toDTO(me);
    }

    private void deleteFileQuietly(String relativePath) {
        if (relativePath == null || relativePath.isBlank()) return;
        try {
            Path p = uploadDir.resolve(relativePath).normalize();
            if (p.startsWith(uploadDir)) {
                Files.deleteIfExists(p);
            }
        } catch (Exception ignored) {
            // best effort
        }
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
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
            throw new AccessDeniedException("Not authenticated");
        }
        String name = auth.getName();
        if (name == null || name.isBlank()) {
            throw new AccessDeniedException("Not authenticated");
        }
        return name;
    }

    private boolean isAdmin() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
            return false;
        }
        return auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch("ROLE_ADMIN"::equals);
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

        String roomName = roomRepository.findByOccupant_Id(user.getId())
                .map(Room::getName)
                .orElse(null);

        return new UserResponseDTO(
                user.getId(),
                user.getUsername(),
                user.getFullName(),
                user.getEmail(),
                user.getRole() != null ? user.getRole().name() : null,
                roomName,
                user.getPhoneNumber(),
                user.getEmergencyPhoneNumber(),
                user.getStudyOrWork(),
                user.getSocialPreference() != null ? user.getSocialPreference().name() : null,
                user.getMealPreference() != null ? user.getMealPreference().name() : null,
                user.getAvailabilityStatus() != null ? user.getAvailabilityStatus().name() : null,
                user.isStatusToggle(),
                user.getProfileImagePath()
        );
    }
}