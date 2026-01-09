package com.villavredestein.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CreateStudentRequestDTO {

    @NotBlank(message = "username is verplicht")
    private String username;

    @Email(message = "email moet geldig zijn")
    @NotBlank(message = "email is verplicht")
    private String email;

    @NotBlank(message = "password is verplicht")
    @Size(min = 6, message = "password moet minimaal 6 tekens zijn")
    private String password;

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