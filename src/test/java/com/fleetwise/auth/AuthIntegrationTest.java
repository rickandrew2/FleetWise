package com.fleetwise.auth;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AuthIntegrationTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        @Test
        void registerLoginAndAccessProtectedEndpoint() throws Exception {
                String email = "driver-" + UUID.randomUUID() + "@fleetwise.test";

                String registerPayload = """
                                {
                                  "name": "Test Driver",
                                                                                        "email": "%s",
                                  "password": "StrongPass123"
                                }
                                                                                """.formatted(email);

                mockMvc.perform(post("/api/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(registerPayload))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.email").value(email));

                String loginPayload = """
                                {
                                                                                        "email": "%s",
                                  "password": "StrongPass123"
                                }
                                                                                """.formatted(email);

                String loginResponse = mockMvc.perform(post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(loginPayload))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.token").isNotEmpty())
                                .andReturn()
                                .getResponse()
                                .getContentAsString();

                JsonNode jsonNode = objectMapper.readTree(loginResponse);
                String token = jsonNode.get("token").asText();
                assertThat(token).isNotBlank();

                mockMvc.perform(get("/api/protected/me"))
                                .andExpect(status().isUnauthorized());

                mockMvc.perform(get("/api/protected/me")
                                .header("Authorization", "Bearer " + token))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.status").value("authenticated"))
                                .andExpect(jsonPath("$.email").value(email));
        }
}