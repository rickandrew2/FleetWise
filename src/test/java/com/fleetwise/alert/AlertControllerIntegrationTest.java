package com.fleetwise.alert;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fleetwise.fuellog.FuelLogRepository;
import com.fleetwise.route.RouteLogRepository;
import com.fleetwise.user.User;
import com.fleetwise.user.UserRepository;
import com.fleetwise.user.UserRole;
import com.fleetwise.vehicle.Vehicle;
import com.fleetwise.vehicle.VehicleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AlertControllerIntegrationTest {

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
        private PasswordEncoder passwordEncoder;

        private User adminUser;
        private User managerUser;
        private User driverUser;
        private User secondDriverUser;
        private Vehicle vehicle;

        @BeforeEach
        void setUp() {
                alertRepository.deleteAll();
                routeLogRepository.deleteAll();
                fuelLogRepository.deleteAll();
                vehicleRepository.deleteAll();
                userRepository.deleteAll();

                adminUser = seedUser("admin@fleetwise.test", UserRole.ADMIN);
                managerUser = seedUser("manager@fleetwise.test", UserRole.FLEET_MANAGER);
                driverUser = seedUser("driver@fleetwise.test", UserRole.DRIVER);
                secondDriverUser = seedUser("driver2@fleetwise.test", UserRole.DRIVER);
                vehicle = seedVehicle();
        }

        @Test
        void shouldGenerateAndManageAlertsWithRoleRules() throws Exception {
                mockMvc.perform(get("/api/alerts"))
                                .andExpect(status().isUnauthorized());

                String driverToken = loginAndGetToken(driverUser.getEmail(), "StrongPass123");

                String highCostFillupPayload = """
                                {
                                  "vehicleId": "%s",
                                                                                        "logDate": "2026-04-15",
                                  "odometerReadingKm": 5000.0,
                                  "litersFilled": 100.0,
                                  "pricePerLiter": 100.0,
                                  "stationName": "Very Expensive Station"
                                }
                                """.formatted(vehicle.getId());

                mockMvc.perform(post("/api/fuel-logs")
                                .header("Authorization", "Bearer " + driverToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(highCostFillupPayload))
                                .andExpect(status().isCreated());

                mockMvc.perform(get("/api/alerts")
                                .header("Authorization", "Bearer " + driverToken)
                                .param("isRead", "false"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$[0].alertType").exists())
                                .andExpect(jsonPath("$[0].driverId").value(driverUser.getId().toString()));

                mockMvc.perform(get("/api/alerts/unread-count")
                                .header("Authorization", "Bearer " + driverToken))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.unreadCount").value(2));

                mockMvc.perform(get("/api/alerts")
                                .header("Authorization", "Bearer " + driverToken)
                                .param("driverId", secondDriverUser.getId().toString()))
                                .andExpect(status().isBadRequest());

                String managerToken = loginAndGetToken(managerUser.getEmail(), "StrongPass123");

                String inefficientRoutePayload = """
                                {
                                  "vehicleId": "%s",
                                  "driverId": "%s",
                                  "tripDate": "2026-04-16",
                                  "originLabel": "A",
                                  "originLat": 13.7565,
                                  "originLng": 121.0583,
                                  "destinationLabel": "B",
                                  "destinationLat": 13.9411,
                                  "destinationLng": 121.1636,
                                  "actualFuelUsedLiters": 20.0
                                }
                                """.formatted(vehicle.getId(), secondDriverUser.getId());

                mockMvc.perform(post("/api/routes")
                                .header("Authorization", "Bearer " + managerToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(inefficientRoutePayload))
                                .andExpect(status().isCreated());

                String overconsumptionAlertsResponse = mockMvc.perform(get("/api/alerts")
                                .header("Authorization", "Bearer " + managerToken)
                                .param("alertType", AlertType.OVERCONSUMPTION.name())
                                .param("driverId", secondDriverUser.getId().toString())
                                .param("isRead", "false"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$[0].alertType").value("OVERCONSUMPTION"))
                                .andReturn()
                                .getResponse()
                                .getContentAsString();

                JsonNode jsonNode = objectMapper.readTree(overconsumptionAlertsResponse);
                String overconsumptionAlertId = jsonNode.get(0).get("id").asText();

                mockMvc.perform(put("/api/alerts/" + overconsumptionAlertId + "/read")
                                .header("Authorization", "Bearer " + managerToken))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.isRead").value(true));

                String adminToken = loginAndGetToken(adminUser.getEmail(), "StrongPass123");

                mockMvc.perform(get("/api/alerts/unread-count")
                                .header("Authorization", "Bearer " + adminToken))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.unreadCount").value(2));
        }

        private User seedUser(String email, UserRole role) {
                User user = new User();
                user.setName(role.name());
                user.setEmail(email);
                user.setPasswordHash(passwordEncoder.encode("StrongPass123"));
                user.setRole(role);
                return userRepository.save(user);
        }

        private Vehicle seedVehicle() {
                Vehicle seededVehicle = new Vehicle();
                seededVehicle.setPlateNumber("ALERT-001");
                seededVehicle.setMake("Toyota");
                seededVehicle.setModel("Hilux");
                seededVehicle.setYear(2022);
                seededVehicle.setFuelType("DIESEL");
                seededVehicle.setTankCapacityLiters(new BigDecimal("70.00"));
                seededVehicle.setCombinedMpg(new BigDecimal("25.00"));
                return vehicleRepository.save(seededVehicle);
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
