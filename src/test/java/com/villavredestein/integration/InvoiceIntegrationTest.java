package com.villavredestein.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.villavredestein.model.Invoice;
import com.villavredestein.model.Room;
import com.villavredestein.model.User;
import com.villavredestein.repository.InvoiceRepository;
import com.villavredestein.repository.RoomRepository;
import com.villavredestein.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class InvoiceIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private InvoiceRepository invoiceRepository;

    @Autowired
    private RoomRepository roomRepository;

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

    private String loginAsStudentAndGetToken(String email, String password, String roomName) throws Exception {
        var payload = objectMapper.createObjectNode();
        payload.put("email", email);
        payload.put("password", password);
        payload.put("room", roomName);

        String body = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readTree(body).get("token").asText();
    }

    private User saveStudentWithRoom(String username, String email, String rawPassword, String roomName) {
        User student = new User(username, email, passwordEncoder.encode(rawPassword), User.Role.STUDENT);
        userRepository.save(student);
        Room room = new Room(roomName);
        room.assignOccupant(student);
        roomRepository.save(room);
        return student;
    }

    private Invoice saveInvoice(User student) {
        Invoice invoice = new Invoice(
                "Huur test",
                null,
                new BigDecimal("500.00"),
                LocalDate.now(),
                LocalDate.now().plusMonths(1),
                LocalDate.now().getMonthValue(),
                LocalDate.now().getYear(),
                Invoice.InvoiceStatus.OPEN,
                student
        );
        return invoiceRepository.save(invoice);
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

    @Test
    void createInvoice_asAdmin_validBody_returns201() throws Exception {
        String studentEmail = "student-" + UUID.randomUUID() + "@test.com";
        User student = new User("student", studentEmail, passwordEncoder.encode("pass1234"), User.Role.STUDENT);
        userRepository.save(student);

        String adminToken = loginAndGetToken();

        var payload = objectMapper.createObjectNode();
        payload.put("title", "Huur mei");
        payload.put("amount", "550.00");
        payload.put("issueDate", LocalDate.now().toString());
        payload.put("dueDate", LocalDate.now().plusMonths(1).toString());
        payload.put("studentEmail", studentEmail);

        mockMvc.perform(post("/api/invoices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Huur mei"))
                .andExpect(jsonPath("$.status").value("OPEN"));
    }

    @Test
    void createInvoice_asAdmin_missingRequiredFields_returns400() throws Exception {
        String adminToken = loginAndGetToken();

        var payload = objectMapper.createObjectNode();
        payload.put("title", "Huur mei");

        mockMvc.perform(post("/api/invoices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createInvoice_withoutAuth_returns401or403() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/invoices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andReturn();

        assertThat(result.getResponse().getStatus()).isIn(401, 403);
    }

    @Test
    void getInvoiceById_asStudent_ownInvoice_returns200() throws Exception {
        String password = UUID.randomUUID().toString();
        User student = saveStudentWithRoom("student", "own@test.com", password, "Kamer 1");
        Invoice invoice = saveInvoice(student);

        String studentToken = loginAsStudentAndGetToken("own@test.com", password, "Kamer 1");

        mockMvc.perform(get("/api/invoices/" + invoice.getId())
                        .accept(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + studentToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(invoice.getId()));
    }

    @Test
    void getInvoiceById_asStudent_otherStudentInvoice_returns403() throws Exception {
        String s1Password = UUID.randomUUID().toString();
        User student1 = saveStudentWithRoom("student1", "s1@test.com", s1Password, "Kamer 1");
        Invoice invoice = saveInvoice(student1);

        String s2Password = UUID.randomUUID().toString();
        saveStudentWithRoom("student2", "s2@test.com", s2Password, "Kamer 2");

        String s2Token = loginAsStudentAndGetToken("s2@test.com", s2Password, "Kamer 2");

        mockMvc.perform(get("/api/invoices/" + invoice.getId())
                        .accept(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + s2Token))
                .andExpect(status().isForbidden());
    }

    @Test
    void deleteInvoice_asStudent_returns403() throws Exception {
        String password = UUID.randomUUID().toString();
        User student = saveStudentWithRoom("student", "del@test.com", password, "Kamer 3");
        Invoice invoice = saveInvoice(student);

        String studentToken = loginAsStudentAndGetToken("del@test.com", password, "Kamer 3");

        mockMvc.perform(delete("/api/invoices/" + invoice.getId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + studentToken))
                .andExpect(status().isForbidden());
    }
}