package com.villavredestein.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Entity
@Table(
        name = "users",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_user_email", columnNames = "email"),
                @UniqueConstraint(name = "uk_user_username", columnNames = "username")
        }
)
public class User {

    public enum Role {
        ADMIN,
        STUDENT,
        CLEANER
    }

    // =====================================================================
    // # FIELDS
    // =====================================================================

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Username is required")
    @Size(max = 50, message = "Username may not exceed 50 characters")
    @Column(nullable = false, length = 50)
    private String username;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    @Size(max = 100, message = "Email may not exceed 100 characters")
    @Column(nullable = false, length = 100)
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 255, message = "Password must be at least 8 characters")
    @Column(nullable = false)
    private String password;

    @NotNull(message = "Role is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role;

    // =====================================================================
    // # CONSTRUCTORS
    // =====================================================================

    protected User() {
    }

    public User(String username, String email, String password, Role role) {
        this.username = require(username, "username");
        this.email = require(email, "email");
        this.password = require(password, "password");
        this.role = require(role, "role");
        normalize();
    }

    // =====================================================================
    // # LIFECYCLE
    // =====================================================================

    @PrePersist
    @PreUpdate
    private void normalize() {
        username = username.trim();
        email = email.trim().toLowerCase();
    }

    // =====================================================================
    // # GETTERS
    // =====================================================================

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public Role getRole() {
        return role;
    }

    // =====================================================================
    // # SETTERS
    // =====================================================================

    public void setUsername(String username) {
        this.username = require(username, "username");
        normalize();
    }

    public void setEmail(String email) {
        this.email = require(email, "email");
        normalize();
    }

    public void setPassword(String password) {
        this.password = require(password, "password");
    }

    public void setRole(Role role) {
        this.role = require(role, "role");
    }

    // =====================================================================
    // # HELPERS
    // =====================================================================

    private <T> T require(T value, String field) {
        if (value == null) {
            throw new IllegalArgumentException(field + " is required");
        }
        if (value instanceof String s && s.trim().isEmpty()) {
            throw new IllegalArgumentException(field + " may not be blank");
        }
        return value;
    }

    public boolean hasRole(Role expectedRole) {
        return this.role == expectedRole;
    }

    // =====================================================================
    // # OBJECT CONTRACT
    // =====================================================================

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User other)) return false;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return 31;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", role=" + role +
                '}';
    }
}