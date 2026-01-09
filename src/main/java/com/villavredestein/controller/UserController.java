package com.villavredestein.controller;

import com.villavredestein.dto.UserResponseDTO;
import com.villavredestein.dto.UserUpdateDTO;
import com.villavredestein.service.UserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "/api/users", produces = MediaType.APPLICATION_JSON_VALUE)
@CrossOrigin
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // =====================================================================
    // REQUEST DTOs
    // =====================================================================

    public static class CreateStudentRequest {

        @NotBlank(message = "username is verplicht")
        @Size(min = 2, max = 50, message = "username moet tussen 2 en 50 tekens zijn")
        private String username;

        @Email(message = "email moet geldig zijn")
        @NotBlank(message = "email is verplicht")
        @Size(max = 255, message = "email mag maximaal 255 tekens zijn")
        private String email;

        @NotBlank(message = "password is verplicht")
        @Size(min = 6, max = 72, message = "password moet tussen 6 en 72 tekens zijn")
        private String password;

        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }

        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }

    public static class ChangeRoleRequest {

        @NotBlank(message = "newRole is verplicht")
        @Pattern(regexp = "^(ADMIN|STUDENT|CLEANER)$", message = "newRole moet ADMIN, STUDENT of CLEANER zijn")
        private String newRole;

        public String getNewRole() { return newRole; }
        public void setNewRole(String newRole) { this.newRole = newRole; }
    }

    // =====================================================================
    // CREATE
    // =====================================================================

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping(value = "/students", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UserResponseDTO> createStudent(@Valid @RequestBody CreateStudentRequest request) {
        UserResponseDTO created = userService.createStudent(
                request.getUsername(),
                request.getEmail(),
                request.getPassword()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    // =====================================================================
    // READ
    // =====================================================================

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<List<UserResponseDTO>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/email/{email}")
    public ResponseEntity<UserResponseDTO> getByEmail(@PathVariable String email) {
        return userService.getUserByEmail(email)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PreAuthorize("hasAnyRole('ADMIN','STUDENT','CLEANER')")
    @GetMapping("/me")
    public ResponseEntity<UserResponseDTO> me() {
        return ResponseEntity.ok(userService.getMe());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDTO> getById(@PathVariable Long id) {
        return userService.getUserById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // =====================================================================
    // UPDATE
    // =====================================================================

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping(value = "/{id}/role", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UserResponseDTO> changeRole(@PathVariable Long id, @Valid @RequestBody ChangeRoleRequest request) {
        return ResponseEntity.ok(userService.changeRole(id, request.getNewRole()));
    }

    @PreAuthorize("hasAnyRole('ADMIN','STUDENT','CLEANER')")
    @PutMapping(value = "/me/profile", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UserResponseDTO> updateMyProfile(@Valid @RequestBody UserUpdateDTO dto) {
        // userService.getMe() haalt huidige gebruiker op; daarna updaten we op basis van het eigen id
        Long myId = userService.getMe().id();
        return ResponseEntity.ok(userService.updateProfile(myId, dto));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping(value = "/{id}/profile", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UserResponseDTO> updateAnyProfile(@PathVariable Long id, @Valid @RequestBody UserUpdateDTO dto) {
        return ResponseEntity.ok(userService.updateProfile(id, dto));
    }

    // =====================================================================
    // DELETE
    // =====================================================================

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}