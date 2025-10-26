package com.villavredestein.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Email(message = "E-mailadres is ongeldig")
    @NotBlank(message = "E-mailadres is verplicht")
    @Column(nullable = false, unique = true)
    private String email;

    @NotBlank(message = "Gebruikersnaam mag niet leeg zijn")
    @Size(min = 3, max = 50, message = "Gebruikersnaam moet tussen 3 en 50 tekens bevatten")
    @Column(nullable = false)
    private String username;

    @NotBlank(message = "Wachtwoord mag niet leeg zijn")
    @Size(min = 6, message = "Wachtwoord moet minstens 6 tekens bevatten")
    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    public User() {}

    public User(String email, String username, String password, Role role) {
        this.email = email;
        this.username = username;
        this.password = password;
        this.role = role;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }

    public enum Role {
        ADMIN,
        STUDENT,
        CLEANER
    }
}