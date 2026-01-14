package com.villavredestein.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.NotNull;

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

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Gebruikersnaam is verplicht")
    @Size(max = 50, message = "Gebruikersnaam mag maximaal 50 tekens bevatten")
    @Column(nullable = false, length = 50)
    private String username;

    @NotBlank(message = "E-mail is verplicht")
    @Email(message = "Ongeldig e-mailadres")
    @Size(max = 100, message = "E-mail mag maximaal 100 tekens bevatten")
    @Column(nullable = false, length = 100)
    private String email;

    @NotBlank(message = "Wachtwoord is verplicht")
    @Size(min = 8, max = 255, message = "Wachtwoord moet minimaal 8 tekens bevatten")
    @Column(nullable = false)
    private String password;

    @NotNull(message = "Rol is verplicht")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role;

    protected User() {
    }

    public User(String username, String email, String password, Role role) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.role = role;
        normalize();
    }

    @PrePersist
    @PreUpdate
    private void normalize() {
        if (username != null) {
            username = username.trim();
        }
        if (email != null) {
            email = email.trim().toLowerCase();
        }
    }

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

    public void setUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Gebruikersnaam mag niet leeg zijn");
        }
        this.username = username;
        normalize();
    }

    public void setEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("E-mail mag niet leeg zijn");
        }
        this.email = email;
        normalize();
    }

    public void setPassword(String password) {
        if (password == null || password.length() < 8) {
            throw new IllegalArgumentException("Wachtwoord moet minimaal 8 tekens bevatten");
        }
        this.password = password;
    }

    public void setRole(Role role) {
        if (role == null) {
            throw new IllegalArgumentException("Rol is verplicht");
        }
        this.role = role;
    }

    public boolean hasRole(Role expectedRole) {
        return this.role == expectedRole;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User)) return false;
        User other = (User) o;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return 31;
    }
}