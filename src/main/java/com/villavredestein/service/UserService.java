package com.villavredestein.service;

import com.villavredestein.dto.UserRequestDTO;
import com.villavredestein.dto.UserResponseDTO;
import com.villavredestein.model.Room;
import com.villavredestein.model.User;
import com.villavredestein.repository.RoomRepository;
import com.villavredestein.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
@Transactional
public class UserService implements UserDetailsService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    private static final Set<User.Role> ALLOWED_ROLES =
            Set.of(User.Role.ADMIN, User.Role.STUDENT, User.Role.CLEANER);

    private static final Set<String> ALLOWED_IMAGE_TYPES =
            Set.of("image/jpeg", "image/png", "image/webp");

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

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
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

    public UserResponseDTO createStudent(String username, String email, String rawPassword) {
        return createUser(username, email, rawPassword, User.Role.STUDENT);
    }

    public UserResponseDTO createUserWithRole(String username, String email, String rawPassword, String role) {
        return createUser(username, email, rawPassword, parseRole(role));
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
        validateNewUserInput(username, rawPassword, role);

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

    public UserResponseDTO changeRole(Long id, String newRole) {
        User.Role role = parseRole(newRole);
        validateAllowedRole(role, "newRole must be ADMIN, STUDENT or CLEANER");

        User user = findUserByIdOrThrow(id);
        user.setRole(role);
        return toDTO(user);
    }

    public UserResponseDTO updateProfile(Long id, UserRequestDTO dto) {
        if (dto == null) {
            throw new IllegalArgumentException("UserRequestDTO is required");
        }

        User target = findUserByIdOrThrow(id);
        assertOwnerOrAdmin(target.getId());

        if (hasText(dto.getUsername())) {
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

        if (hasText(dto.getEmail()) && !dto.getEmail().equalsIgnoreCase(target.getEmail())) {
            String normalizedEmail = normalizeEmail(dto.getEmail());
            if (userRepository.existsByEmailIgnoreCase(normalizedEmail)) {
                throw new IllegalArgumentException("Email already exists");
            }
            target.setEmail(normalizedEmail);
        }

        return toDTO(target);
    }

    public void changeMyPassword(String oldPassword, String newPassword) {
        if (!hasText(oldPassword)) {
            throw new IllegalArgumentException("Old password is required");
        }
        if (!hasText(newPassword)) {
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

        String contentType = normalizeContentType(file.getContentType());
        if (!ALLOWED_IMAGE_TYPES.contains(contentType)) {
            throw new IllegalArgumentException("Only JPEG, PNG or WEBP images are allowed");
        }

        User me = currentUser();

        try {
            Files.createDirectories(uploadDir);

            String extension = determineImageExtension(contentType);
            String filename = "profile_" + me.getId() + "_" + UUID.randomUUID() + extension;
            Path targetPath = uploadDir.resolve(filename).normalize();

            if (!targetPath.startsWith(uploadDir)) {
                throw new IllegalArgumentException("Invalid file path");
            }

            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, targetPath, StandardCopyOption.REPLACE_EXISTING);
            }

            deleteFileQuietly(me.getProfileImagePath());
            me.setProfileImagePath(filename);
            return toDTO(me);
        } catch (IOException exception) {
            throw new RuntimeException("Failed to store profile photo", exception);
        }
    }

    public UserResponseDTO deleteMyProfilePhoto() {
        User me = currentUser();
        deleteFileQuietly(me.getProfileImagePath());
        me.setProfileImagePath(null);
        return toDTO(me);
    }

    public void deleteUser(Long id) {
        User user = findUserByIdOrThrow(id);
        userRepository.delete(user);
    }

    private void validateNewUserInput(String username, String rawPassword, User.Role role) {
        if (!hasText(username)) {
            throw new IllegalArgumentException("Username is required");
        }
        if (!hasText(rawPassword)) {
            throw new IllegalArgumentException("Password is required");
        }
        if (rawPassword.length() < 8) {
            throw new IllegalArgumentException("Password must be at least 8 characters");
        }
        validateAllowedRole(role, "Role must be ADMIN, STUDENT or CLEANER");
    }

    private void validateAllowedRole(User.Role role, String message) {
        if (role == null) {
            throw new IllegalArgumentException("Role is required");
        }
        if (!ALLOWED_ROLES.contains(role)) {
            throw new IllegalArgumentException(message);
        }
    }

    private User findUserByIdOrThrow(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + id));
    }

    private void deleteFileQuietly(String relativePath) {
        if (!hasText(relativePath)) {
            return;
        }

        try {
            Path path = uploadDir.resolve(relativePath).normalize();
            if (path.startsWith(uploadDir)) {
                Files.deleteIfExists(path);
            }
        } catch (Exception ignored) {
            // best effort cleanup
        }
    }

    private String normalizeEmail(String email) {
        if (!hasText(email)) {
            throw new IllegalArgumentException("Email is required");
        }
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private String normalizeContentType(String contentType) {
        return contentType == null ? "" : contentType.trim().toLowerCase(Locale.ROOT);
    }

    private String determineImageExtension(String contentType) {
        return switch (contentType) {
            case "image/png" -> ".png";
            case "image/webp" -> ".webp";
            default -> ".jpg";
        };
    }

    private User currentUser() {
        String email = normalizeEmail(currentEmail());
        return userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new EntityNotFoundException("Current user not found"));
    }

    private String currentEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {
            throw new AccessDeniedException("Not authenticated");
        }

        String name = authentication.getName();
        if (!hasText(name)) {
            throw new AccessDeniedException("Not authenticated");
        }

        return name;
    }

    private boolean isAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {
            return false;
        }

        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch("ROLE_ADMIN"::equals);
    }

    private void assertOwnerOrAdmin(Long targetUserId) {
        if (isAdmin()) {
            return;
        }

        User me = currentUser();
        if (!me.getId().equals(targetUserId)) {
            throw new AccessDeniedException("You may only update your own profile");
        }
    }

    private User.Role parseRole(String role) {
        if (!hasText(role)) {
            throw new IllegalArgumentException("Role is required");
        }

        String normalizedRole = role.trim().toUpperCase(Locale.ROOT);
        if (normalizedRole.startsWith("ROLE_")) {
            normalizedRole = normalizedRole.substring("ROLE_".length());
        }

        try {
            return User.Role.valueOf(normalizedRole);
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException(
                    "Invalid role: " + role + ". Use ADMIN, STUDENT or CLEANER."
            );
        }
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private String maskEmail(String email) {
        if (!hasText(email)) {
            return "(no-email)";
        }

        String trimmed = email.trim();
        int atIndex = trimmed.indexOf('@');
        if (atIndex <= 1) {
            return "***";
        }

        return trimmed.charAt(0) + "***" + trimmed.substring(atIndex);
    }

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