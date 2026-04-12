package com.fleetwise.fuelprice;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class FuelPriceControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FuelPriceHistoryRepository fuelPriceHistoryRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        fuelPriceHistoryRepository.deleteAll();
        userRepository.deleteAll();

        seedUser("admin@fleetwise.test", UserRole.ADMIN);
        seedUser("manager@fleetwise.test", UserRole.FLEET_MANAGER);
        seedUser("driver@fleetwise.test", UserRole.DRIVER);
    }

    @Test
    void shouldEnforceRoleAccessAndExposeCurrentPrices() throws Exception {
        mockMvc.perform(get("/api/fuel-prices/current"))
                .andExpect(status().isUnauthorized());

        String managerToken = loginAndGetToken("manager@fleetwise.test", "StrongPass123");
        String manualPayload = """
                {
                  "effectiveDate": "2026-04-08",
                  "source": "DOE Weekly Advisory",
                  "entries": [
                    {"fuelType": "DIESEL", "pricePerLiter": 70.00, "brand": "Shell"},
                    {"fuelType": "DIESEL", "pricePerLiter": 71.00, "brand": "Petron"},
                    {"fuelType": "GASOLINE_91", "pricePerLiter": 73.00, "brand": "Shell"},
                    {"fuelType": "GASOLINE_95", "pricePerLiter": 76.00, "brand": "Caltex"}
                  ]
                }
                """;

        mockMvc.perform(post("/api/fuel-prices/manual-update")
                        .header("Authorization", "Bearer " + managerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(manualPayload))
                .andExpect(status().isForbidden());

        String adminToken = loginAndGetToken("admin@fleetwise.test", "StrongPass123");
        mockMvc.perform(post("/api/fuel-prices/manual-update")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(manualPayload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.updatedRecords").value(4))
                .andExpect(jsonPath("$.fallbackUsed").value(false));

        String driverToken = loginAndGetToken("driver@fleetwise.test", "StrongPass123");
        mockMvc.perform(get("/api/fuel-prices/current/DIESEL")
                        .header("Authorization", "Bearer " + driverToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fuelType").value("DIESEL"))
                .andExpect(jsonPath("$.pricePerLiter").value(70.5))
                .andExpect(jsonPath("$.source").value("DOE Weekly Advisory"));

        mockMvc.perform(get("/api/fuel-prices/history")
                        .header("Authorization", "Bearer " + driverToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].fuelType").exists())
                .andExpect(jsonPath("$[0].averagePricePerLiter").isNumber());
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
