package com.fleetwise.vehicle;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fleetwise.fuellog.FuelLogRepository;
import com.fleetwise.user.User;
import com.fleetwise.user.UserRepository;
import com.fleetwise.user.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class VehicleControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private VehicleRepository vehicleRepository;

        @Autowired
        private FuelLogRepository fuelLogRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
                fuelLogRepository.deleteAll();
        vehicleRepository.deleteAll();
        userRepository.deleteAll();
        seedUser("manager@fleetwise.test", UserRole.FLEET_MANAGER);
        seedUser("driver@fleetwise.test", UserRole.DRIVER);
        seedUser("admin@fleetwise.test", UserRole.ADMIN);
    }

    @Test
    void shouldEnforceVehicleEndpointAuthorizationAndCrud() throws Exception {
        mockMvc.perform(get("/api/vehicles"))
                .andExpect(status().isUnauthorized());

        String driverToken = loginAndGetToken("driver@fleetwise.test", "StrongPass123");
        mockMvc.perform(get("/api/vehicles")
                        .header("Authorization", "Bearer " + driverToken))
                .andExpect(status().isForbidden());

        String managerToken = loginAndGetToken("manager@fleetwise.test", "StrongPass123");
        String createPayload = """
                {
                  "plateNumber": "abc-123",
                  "make": "Toyota",
                  "model": "Hilux",
                  "year": 2021,
                  "fuelType": "Diesel",
                  "tankCapacityLiters": 70.0,
                  "epaVehicleId": null,
                  "assignedDriverId": null
                }
                """;

        String createResponse = mockMvc.perform(post("/api/vehicles")
                        .header("Authorization", "Bearer " + managerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createPayload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.plateNumber").value("ABC-123"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        String vehicleId = objectMapper.readTree(createResponse).get("id").asText();

        mockMvc.perform(get("/api/vehicles")
                        .header("Authorization", "Bearer " + managerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(vehicleId));

        mockMvc.perform(get("/api/vehicles/" + vehicleId)
                        .header("Authorization", "Bearer " + managerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.make").value("Toyota"));

        String updatePayload = """
                {
                  "plateNumber": "abc-123",
                  "make": "Toyota",
                  "model": "Hilux GR",
                  "year": 2022,
                  "fuelType": "Diesel",
                  "tankCapacityLiters": 75.0,
                  "epaVehicleId": null,
                  "assignedDriverId": null
                }
                """;

        mockMvc.perform(put("/api/vehicles/" + vehicleId)
                        .header("Authorization", "Bearer " + managerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updatePayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.model").value("Hilux GR"));

        mockMvc.perform(delete("/api/vehicles/" + vehicleId)
                        .header("Authorization", "Bearer " + managerToken))
                .andExpect(status().isForbidden());

        String adminToken = loginAndGetToken("admin@fleetwise.test", "StrongPass123");
        mockMvc.perform(delete("/api/vehicles/" + vehicleId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNoContent());
    }

    private void seedUser(String email, UserRole role) {
        User user = new User();
        user.setName(role.name());
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode("StrongPass123"));
        user.setRole(role);
        userRepository.save(user);
    }

    private String loginAndGetToken(String email, String password) throws Exception {
        String payload = """
                {
                  "email": "%s",
                  "password": "%s"
                }
                """.formatted(email, password);

        String response = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode jsonNode = objectMapper.readTree(response);
        return jsonNode.get("token").asText();
    }
}