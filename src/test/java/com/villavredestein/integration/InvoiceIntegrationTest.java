package com.villavredestein.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.villavredestein.model.User;
import com.villavredestein.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@EnabledIfSystemProperty(named = "runIT", matches = "true")
class InvoiceIntegrationTest extends BaseIntegrationTest {

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

    private String loginAndGetToken() throws Exception {
        var payload = objectMapper.createObjectNode();
        payload.put("email", ADMIN_EMAIL);
        payload.put("password", adminPassword);

        String body = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode json = objectMapper.readTree(body);
        JsonNode token = json.get("token");

        assertThat(token).as("Response moet een 'token' field bevatten").isNotNull();
        assertThat(token.asText()).as("Token mag niet leeg zijn").isNotBlank();

        return token.asText();
    }

    @Test
    void getInvoices_withValidJwt_returns200() throws Exception {
        String token = loginAndGetToken();

        mockMvc.perform(get("/api/invoices")
                        .accept(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
    }

    @Test
    void getInvoices_withoutJwt_returns401or403() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/invoices")
                        .accept(MediaType.APPLICATION_JSON))
                .andReturn();

        assertThat(result.getResponse().getStatus()).isIn(401, 403);
    }
}