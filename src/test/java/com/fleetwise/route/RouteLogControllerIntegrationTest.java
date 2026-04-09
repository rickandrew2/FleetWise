package com.fleetwise.route;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fleetwise.fuellog.FuelLog;
import com.fleetwise.fuellog.FuelLogRepository;
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
import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class RouteLogControllerIntegrationTest {

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
    private PasswordEncoder passwordEncoder;

    private User adminUser;
    private User managerUser;
    private User driverUser;
    private User secondDriverUser;
    private Vehicle vehicle;

    @BeforeEach
    void setUp() {
        routeLogRepository.deleteAll();
        fuelLogRepository.deleteAll();
        vehicleRepository.deleteAll();
        userRepository.deleteAll();

        adminUser = seedUser("admin@fleetwise.test", UserRole.ADMIN);
        managerUser = seedUser("manager@fleetwise.test", UserRole.FLEET_MANAGER);
        driverUser = seedUser("driver@fleetwise.test", UserRole.DRIVER);
        secondDriverUser = seedUser("driver2@fleetwise.test", UserRole.DRIVER);
        vehicle = seedVehicle();
        seedFuelLog(vehicle, driverUser, LocalDate.of(2026, 4, 11), new BigDecimal("18.00"));
    }

    @Test
    void shouldEnforceRouteLogAuthorizationAndCrud() throws Exception {
        mockMvc.perform(get("/api/routes"))
                .andExpect(status().isUnauthorized());

        String driverToken = loginAndGetToken(driverUser.getEmail(), "StrongPass123");

        String forbiddenDriverPayload = """
                {
                  "vehicleId": "%s",
                  "driverId": "%s",
                  "tripDate": "2026-04-11",
                  "originLabel": "Batangas City",
                  "originLat": 13.7565,
                  "originLng": 121.0583,
                  "destinationLabel": "Lipa City",
                  "destinationLat": 13.9411,
                  "destinationLng": 121.1636
                }
                """.formatted(vehicle.getId(), secondDriverUser.getId());

        mockMvc.perform(post("/api/routes")
                .header("Authorization", "Bearer " + driverToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(forbiddenDriverPayload))
                .andExpect(status().isBadRequest());

        String driverPayload = """
                {
                  "vehicleId": "%s",
                  "tripDate": "2026-04-11",
                  "originLabel": "Batangas City",
                  "originLat": 13.7565,
                  "originLng": 121.0583,
                  "destinationLabel": "Lipa City",
                  "destinationLat": 13.9411,
                  "destinationLng": 121.1636
                }
                """.formatted(vehicle.getId());

        String driverCreateResponse = mockMvc.perform(post("/api/routes")
                .header("Authorization", "Bearer " + driverToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(driverPayload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.driverId").value(driverUser.getId().toString()))
                .andExpect(jsonPath("$.distanceKm").isNumber())
                .andExpect(jsonPath("$.actualFuelUsedLiters").value(18.0))
                .andReturn()
                .getResponse()
                .getContentAsString();

        String driverRouteLogId = objectMapper.readTree(driverCreateResponse).get("id").asText();

        String managerToken = loginAndGetToken(managerUser.getEmail(), "StrongPass123");

        String managerPayload = """
                {
                  "vehicleId": "%s",
                  "driverId": "%s",
                  "tripDate": "2026-04-12",
                  "originLabel": "Tanauan",
                  "originLat": 14.0837,
                  "originLng": 121.1490,
                  "destinationLabel": "Calamba",
                  "destinationLat": 14.2117,
                  "destinationLng": 121.1653,
                  "actualFuelUsedLiters": 12.5
                }
                """.formatted(vehicle.getId(), secondDriverUser.getId());

        String managerCreateResponse = mockMvc.perform(post("/api/routes")
                .header("Authorization", "Bearer " + managerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(managerPayload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.driverId").value(secondDriverUser.getId().toString()))
                .andExpect(jsonPath("$.actualFuelUsedLiters").value(12.5))
                .andReturn()
                .getResponse()
                .getContentAsString();

        String managerRouteLogId = objectMapper.readTree(managerCreateResponse).get("id").asText();

        mockMvc.perform(get("/api/routes")
                .header("Authorization", "Bearer " + driverToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(driverRouteLogId));

        mockMvc.perform(get("/api/routes/stats")
                .header("Authorization", "Bearer " + driverToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalTrips").value(1))
                .andExpect(jsonPath("$.totalDistanceKm").isNumber());

        mockMvc.perform(get("/api/routes/" + managerRouteLogId)
                .header("Authorization", "Bearer " + driverToken))
                .andExpect(status().isNotFound());

        mockMvc.perform(delete("/api/routes/" + managerRouteLogId)
                .header("Authorization", "Bearer " + managerToken))
                .andExpect(status().isForbidden());

        String adminToken = loginAndGetToken(adminUser.getEmail(), "StrongPass123");
        mockMvc.perform(delete("/api/routes/" + managerRouteLogId)
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
        Vehicle seededVehicle = new Vehicle();
        seededVehicle.setPlateNumber("ROUTE-001");
        seededVehicle.setMake("Toyota");
        seededVehicle.setModel("Hilux");
        seededVehicle.setYear(2022);
        seededVehicle.setFuelType("Diesel");
        seededVehicle.setTankCapacityLiters(new BigDecimal("70.00"));
        seededVehicle.setCombinedMpg(new BigDecimal("25.00"));
        return vehicleRepository.save(seededVehicle);
    }

    private void seedFuelLog(Vehicle seededVehicle, User driver, LocalDate logDate, BigDecimal litersFilled) {
        FuelLog fuelLog = new FuelLog();
        fuelLog.setVehicleId(seededVehicle.getId());
        fuelLog.setDriverId(driver.getId());
        fuelLog.setLogDate(logDate);
        fuelLog.setLitersFilled(litersFilled);
        fuelLog.setPricePerLiter(new BigDecimal("65.00"));
        fuelLog.setTotalCost(litersFilled.multiply(new BigDecimal("65.00")));
        fuelLogRepository.save(fuelLog);
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
