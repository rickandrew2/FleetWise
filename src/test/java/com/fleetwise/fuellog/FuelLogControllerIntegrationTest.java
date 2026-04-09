package com.fleetwise.fuellog;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class FuelLogControllerIntegrationTest {

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

    private User adminUser;
    private User managerUser;
    private User driverUser;
    private User secondDriverUser;
    private Vehicle vehicle;

    @BeforeEach
    void setUp() {
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
    void shouldEnforceFuelLogAuthorizationAndCrud() throws Exception {
        mockMvc.perform(get("/api/fuel-logs"))
                .andExpect(status().isUnauthorized());

        String driverToken = loginAndGetToken(driverUser.getEmail(), "StrongPass123");

        String forbiddenDriverPayload = """
                {
                  "vehicleId": "%s",
                  "driverId": "%s",
                  "logDate": "2026-04-09",
                  "odometerReadingKm": 1234.5,
                  "litersFilled": 20.0,
                  "pricePerLiter": 65.5,
                  "stationName": "Station A",
                  "stationLat": 13.7565,
                  "stationLng": 121.0583,
                  "notes": "test"
                }
                """.formatted(vehicle.getId(), secondDriverUser.getId());

        mockMvc.perform(post("/api/fuel-logs")
                .header("Authorization", "Bearer " + driverToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(forbiddenDriverPayload))
                .andExpect(status().isBadRequest());

        String driverPayload = """
                {
                  "vehicleId": "%s",
                  "logDate": "2026-04-09",
                  "odometerReadingKm": 1500.0,
                  "litersFilled": 30.0,
                  "pricePerLiter": 65.5,
                  "stationName": "Station Driver",
                  "stationLat": 13.7000,
                  "stationLng": 121.0500,
                  "notes": "driver log"
                }
                """.formatted(vehicle.getId());

        String driverCreateResponse = mockMvc.perform(post("/api/fuel-logs")
                .header("Authorization", "Bearer " + driverToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(driverPayload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.driverId").value(driverUser.getId().toString()))
                .andExpect(jsonPath("$.totalCost").value(1965.0))
                .andReturn()
                .getResponse()
                .getContentAsString();

        String driverFuelLogId = objectMapper.readTree(driverCreateResponse).get("id").asText();

        String managerToken = loginAndGetToken(managerUser.getEmail(), "StrongPass123");
        String managerPayload = """
                {
                  "vehicleId": "%s",
                  "driverId": "%s",
                  "logDate": "2026-04-10",
                  "odometerReadingKm": 1600.0,
                  "litersFilled": 10.0,
                  "pricePerLiter": 70.0,
                  "stationName": "Station Manager",
                  "stationLat": 13.6800,
                  "stationLng": 121.0400,
                  "notes": "manager log"
                }
                """.formatted(vehicle.getId(), secondDriverUser.getId());

        String managerCreateResponse = mockMvc.perform(post("/api/fuel-logs")
                .header("Authorization", "Bearer " + managerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(managerPayload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.driverId").value(secondDriverUser.getId().toString()))
                .andExpect(jsonPath("$.totalCost").value(700.0))
                .andReturn()
                .getResponse()
                .getContentAsString();

        String managerFuelLogId = objectMapper.readTree(managerCreateResponse).get("id").asText();

        mockMvc.perform(get("/api/fuel-logs")
                .header("Authorization", "Bearer " + driverToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(driverFuelLogId));

        mockMvc.perform(get("/api/fuel-logs/stats")
                .header("Authorization", "Bearer " + driverToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalLogs").value(1))
                .andExpect(jsonPath("$.totalCost").value(1965.0));

        mockMvc.perform(get("/api/fuel-logs/" + managerFuelLogId)
                .header("Authorization", "Bearer " + driverToken))
                .andExpect(status().isNotFound());

        mockMvc.perform(delete("/api/fuel-logs/" + managerFuelLogId)
                .header("Authorization", "Bearer " + managerToken))
                .andExpect(status().isForbidden());

        String adminToken = loginAndGetToken(adminUser.getEmail(), "StrongPass123");
        mockMvc.perform(delete("/api/fuel-logs/" + managerFuelLogId)
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNoContent());
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
        Vehicle vehicle = new Vehicle();
        vehicle.setPlateNumber("FUEL-001");
        vehicle.setMake("Toyota");
        vehicle.setModel("Hilux");
        vehicle.setYear(2022);
        vehicle.setFuelType("Diesel");
        vehicle.setTankCapacityLiters(new BigDecimal("70.00"));
        return vehicleRepository.save(vehicle);
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
