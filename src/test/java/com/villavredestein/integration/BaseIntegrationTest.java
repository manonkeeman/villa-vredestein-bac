package com.villavredestein.integration;

import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc(addFilters = true)
@ActiveProfiles("test")
@EnabledIfSystemProperty(named = "runIT", matches = "true")
public abstract class BaseIntegrationTest {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected PasswordEncoder passwordEncoder;

    protected static final String ADMIN_EMAIL = "admin@villavredestein.com";
    protected static final String ADMIN_USERNAME = "admin";

    protected static String randomPassword() {
        return java.util.UUID.randomUUID().toString();
    }
}