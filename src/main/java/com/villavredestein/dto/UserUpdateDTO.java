package com.villavredestein.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

/**
 * DTO voor het bijwerken van een gebruikersprofiel.
 *
 * <p>LET OP:
 * - Rol-wijzigingen verlopen via een apart ADMIN-endpoint
 * - Daarom bevat deze DTO GEEN 'role' veld
 * - Geschikt voor partial updates (velden mogen null zijn)</p>
 */
public class UserUpdateDTO {

    @Size(min = 2, max = 50, message = "Username moet tussen 2 en 50 tekens zijn")
    private String username;

    @Email(message = "Email moet een geldig emailadres zijn")
    @Size(max = 255, message = "Email mag maximaal 255 tekens zijn")
    private String email;

    public UserUpdateDTO() {
    }

    public UserUpdateDTO(String username, String email) {
        this.username = username;
        this.email = email;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}