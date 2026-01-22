package com.villavredestein.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public class UserRequestDTO {

    @Size(min = 2, max = 50, message = "Gebruikersnaam moet tussen 2 en 50 tekens zijn")
    private String username;

    @Email(message = "E-mailadres moet een geldig e-mailadres zijn")
    @Size(max = 255, message = "E-mailadres mag maximaal 255 tekens zijn")
    private String email;

    public UserRequestDTO() {
    }

    public UserRequestDTO(String username, String email) {
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