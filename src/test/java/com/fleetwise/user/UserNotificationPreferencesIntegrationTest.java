package com.fleetwise.user;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fleetwise.alert.AlertRepository;
import com.fleetwise.fuellog.FuelLogRepository;
import com.fleetwise.report.ReportJobRepository;
import com.fleetwise.route.RouteLogRepository;
import com.fleetwise.vehicle.VehicleRepository;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class UserNotificationPreferencesIntegrationTest {

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
    private RouteLogRepository routeLogRepository;

    @Autowired
    private AlertRepository alertRepository;

    @Autowired
    private ReportJobRepository reportJobRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User driverUser;

    @BeforeEach
    void setUp() {
        reportJobRepository.deleteAll();
        alertRepository.deleteAll();
        routeLogRepository.deleteAll();
        fuelLogRepository.deleteAll();
        vehicleRepository.deleteAll();
        userRepository.deleteAll();

        driverUser = seedUser("driver@fleetwise.test", UserRole.DRIVER);
    }

    @Test
    void shouldRequireAuthenticationForPreferencesEndpoints() throws Exception {
        mockMvc.perform(get("/api/users/me/preferences"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(put("/api/users/me/preferences")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"emailNotificationsEnabled\":true}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldGetDefaultNotificationPreferencesAndUpdateThem() throws Exception {
        String token = loginAndGetToken(driverUser.getEmail(), "StrongPass123");

        mockMvc.perform(get("/api/users/me/preferences")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountEmail").value("driver@fleetwise.test"))
                .andExpect(jsonPath("$.notificationEmail").isEmpty())
                .andExpect(jsonPath("$.effectiveNotificationEmail").value("driver@fleetwise.test"))
                .andExpect(jsonPath("$.emailNotificationsEnabled").value(false));

        String payload = """
                {
                  "emailNotificationsEnabled": true,
                  "notificationEmail": "alerts@fleetwise.test"
                }
                """;

        mockMvc.perform(put("/api/users/me/preferences")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.notificationEmail").value("alerts@fleetwise.test"))
                .andExpect(jsonPath("$.effectiveNotificationEmail").value("alerts@fleetwise.test"))
                .andExpect(jsonPath("$.emailNotificationsEnabled").value(true));

        mockMvc.perform(get("/api/users/me/preferences")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.notificationEmail").value("alerts@fleetwise.test"))
                .andExpect(jsonPath("$.emailNotificationsEnabled").value(true));
    }

    @Test
    void shouldRejectInvalidNotificationEmail() throws Exception {
        String token = loginAndGetToken(driverUser.getEmail(), "StrongPass123");

        String payload = """
                {
                  "emailNotificationsEnabled": true,
                  "notificationEmail": "not-an-email"
                }
                """;

        mockMvc.perform(put("/api/users/me/preferences")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.notificationEmail").exists());
    }

    private User seedUser(String email, UserRole role) {
        User user = new User();
        user.setName(role.name());
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode("StrongPass123"));
        user.setRole(role);
        return userRepository.save(user);
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
