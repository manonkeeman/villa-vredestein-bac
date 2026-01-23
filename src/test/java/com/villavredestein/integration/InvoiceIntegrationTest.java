package com.villavredestein.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.villavredestein.model.User;
import com.villavredestein.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class InvoiceIntegrationTest extends BaseIntegrationTest {

    @Autowired UserRepository userRepository;
    @Autowired ObjectMapper objectMapper;

    @BeforeEach
    void setup() {
        userRepository.deleteAll();

        // Admin user for login
        User admin = new User(
                "admin",
                "admin@villavredestein.nl",
                passwordEncoder.encode("Admin123!"),
                User.Role.ADMIN
        );
        userRepository.save(admin);
    }

    private String loginAndGetToken() throws Exception {
        String body = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"admin@villavredestein.nl","password":"Admin123!"}
                                """))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode json = objectMapper.readTree(body);
        JsonNode token = json.get("token");
        assertThat(token).isNotNull();
        assertThat(token.asText()).isNotBlank();
        return token.asText();
    }

    @Test
    void getInvoices_withValidJwt_returns200() throws Exception {
        String token = loginAndGetToken();

        mockMvc.perform(get("/api/invoices")
                        .accept(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    void getInvoices_withoutJwt_returns401or403() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/invoices")
                        .accept(MediaType.APPLICATION_JSON))
                .andReturn();

        int statusCode = result.getResponse().getStatus();

        // Afhankelijk van jouw Spring Security config kan dit 401 (unauthorized) of 403 (forbidden) zijn.
        assertThat(statusCode).isIn(401, 403);
    }
}