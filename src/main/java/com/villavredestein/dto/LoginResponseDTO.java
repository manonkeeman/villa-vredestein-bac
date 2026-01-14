package com.villavredestein.dto;

public class LoginResponseDTO {

    private final String username;
    private final String email;
    private final String role;
    private final String token;

    protected LoginResponseDTO() {
        this.username = null;
        this.email = null;
        this.role = null;
        this.token = null;
    }

    public LoginResponseDTO(String username, String email, String role, String token) {
        this.username = username;
        this.email = email;
        this.role = role;
        this.token = token;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public String getRole() {
        return role;
    }

    public String getToken() {
        return token;
    }
}