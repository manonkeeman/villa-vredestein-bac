package com.villavredestein.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CreateStudentRequestDTO {

    @NotBlank(message = "Gebruikersnaam is verplicht")
    private String username;

    @NotBlank(message = "E-mailadres is verplicht")
    @Email(message = "Voer een geldig e-mailadres in")
    private String email;

    @NotBlank(message = "Wachtwoord is verplicht")
    @Size(min = 6, message = "Wachtwoord moet minimaal 6 tekens lang zijn")
    private String password;

    public CreateStudentRequestDTO() {
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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}