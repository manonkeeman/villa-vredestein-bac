package com.villavredestein.integration;

import com.villavredestein.model.User;
import com.villavredestein.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class AuthIntegrationTest extends BaseIntegrationTest {

    @Autowired UserRepository userRepository;

    @BeforeEach
    void setup() {
        userRepository.deleteAll();

        User admin = new User(
                "admin",
                "admin@villavredestein.nl",
                passwordEncoder.encode("Admin123!"),
                User.Role.ADMIN
        );

        userRepository.save(admin);
    }

    @Test
    void login_returnsJwtToken() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                          {"email":"admin@villavredestein.nl","password":"Admin123!"}
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty());
    }
}