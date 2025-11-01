package com.villavredestein.dto;

public class LoginResponseDTO {

    private String username;
    private String email;
    private String role;
    private String token;

    public LoginResponseDTO() {}

    public LoginResponseDTO(String username, String email, String role, String token) {
        this.username = username;
        this.email = email;
        this.role = role;
        this.token = token;
    }

    public String getUsername() { return username; }
    public String getEmail() { return email; }
    public String getRole() { return role; }
    public String getToken() { return token; }
}