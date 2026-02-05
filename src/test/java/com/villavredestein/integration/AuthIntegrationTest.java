package com.villavredestein.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.villavredestein.model.User;
import com.villavredestein.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@EnabledIfSystemProperty(named = "runIT", matches = "true")
class AuthIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private String adminPassword;

    @BeforeEach
    void setup() {
        userRepository.deleteAll();

        adminPassword = UUID.randomUUID().toString();

        User admin = new User(
                ADMIN_USERNAME,
                ADMIN_EMAIL,
                passwordEncoder.encode(adminPassword),
                User.Role.ADMIN
        );

        userRepository.save(admin);
    }

    @Test
    void login_missingEmailAndPassword_returns400() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void login_invalidEmailFormat_returns400() throws Exception {
        var payload = objectMapper.createObjectNode();
        payload.put("email", "not-an-email");
        payload.put("password", "x"); // geen echte password, alleen voor validatiepad

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void login_validCredentials_returnsJwtToken() throws Exception {
        var payload = objectMapper.createObjectNode();
        payload.put("email", ADMIN_EMAIL);
        payload.put("password", adminPassword);

        String body = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThat(body).contains("token");
    }
}