package com.fleetwise.dashboard;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fleetwise.alert.Alert;
import com.fleetwise.alert.AlertRepository;
import com.fleetwise.alert.AlertType;
import com.fleetwise.fuellog.FuelLog;
import com.fleetwise.fuellog.FuelLogRepository;
import com.fleetwise.report.ReportJobRepository;
import com.fleetwise.route.RouteLog;
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
import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class DashboardControllerIntegrationTest {

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

    private User managerUser;
    private User driverUser;

    @BeforeEach
    void setUp() {
        reportJobRepository.deleteAll();
        alertRepository.deleteAll();
        routeLogRepository.deleteAll();
        fuelLogRepository.deleteAll();
        vehicleRepository.deleteAll();
        userRepository.deleteAll();

        managerUser = seedUser("manager@fleetwise.test", UserRole.FLEET_MANAGER);
        driverUser = seedUser("driver@fleetwise.test", UserRole.DRIVER);

        Vehicle vehicle = seedVehicle();
        seedFuelLog(vehicle, driverUser, LocalDate.now().minusDays(2), new BigDecimal("1200.00"));
        seedFuelLog(vehicle, driverUser, LocalDate.now().minusDays(1), new BigDecimal("1800.00"));
        seedRouteLog(vehicle, driverUser, LocalDate.now().minusDays(1), new BigDecimal("1.10"));
        seedRouteLog(vehicle, driverUser, LocalDate.now(), new BigDecimal("1.25"));
        seedUnreadAlert(vehicle, driverUser);
    }

    @Test
    void shouldServeDashboardKpisWithRoleRestrictions() throws Exception {
        mockMvc.perform(get("/api/dashboard/summary"))
                .andExpect(status().isUnauthorized());

        String driverToken = loginAndGetToken(driverUser.getEmail(), "StrongPass123");
        mockMvc.perform(get("/api/dashboard/summary")
                .header("Authorization", "Bearer " + driverToken))
                .andExpect(status().isForbidden());

        String managerToken = loginAndGetToken(managerUser.getEmail(), "StrongPass123");

        mockMvc.perform(get("/api/dashboard/summary")
                .header("Authorization", "Bearer " + managerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.monthToDateFuelCost").value(3000.0))
                .andExpect(jsonPath("$.fleetEfficiencyScore").value(1.18))
                .andExpect(jsonPath("$.activeAlertsCount").value(1));

        mockMvc.perform(get("/api/dashboard/top-drivers")
                .header("Authorization", "Bearer " + managerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].driverId").value(driverUser.getId().toString()))
                .andExpect(jsonPath("$[0].averageEfficiencyScore").value(1.18));

        mockMvc.perform(get("/api/dashboard/cost-trend")
                .header("Authorization", "Bearer " + managerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(6));
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
        vehicle.setPlateNumber("KPI-001");
        vehicle.setMake("Toyota");
        vehicle.setModel("Hilux");
        vehicle.setYear(2022);
        vehicle.setFuelType("Diesel");
        vehicle.setTankCapacityLiters(new BigDecimal("70.00"));
        vehicle.setCombinedMpg(new BigDecimal("25.00"));
        return vehicleRepository.save(vehicle);
    }

    private void seedFuelLog(Vehicle vehicle, User driver, LocalDate logDate, BigDecimal totalCost) {
        FuelLog fuelLog = new FuelLog();
        fuelLog.setVehicleId(vehicle.getId());
        fuelLog.setDriverId(driver.getId());
        fuelLog.setLogDate(logDate);
        fuelLog.setLitersFilled(new BigDecimal("20.00"));
        fuelLog.setPricePerLiter(new BigDecimal("60.00"));
        fuelLog.setTotalCost(totalCost);
        fuelLogRepository.save(fuelLog);
    }

    private void seedRouteLog(Vehicle vehicle, User driver, LocalDate tripDate, BigDecimal efficiencyScore) {
        RouteLog routeLog = new RouteLog();
        routeLog.setVehicleId(vehicle.getId());
        routeLog.setDriverId(driver.getId());
        routeLog.setTripDate(tripDate);
        routeLog.setOriginLabel("A");
        routeLog.setOriginLat(new BigDecimal("13.7565000"));
        routeLog.setOriginLng(new BigDecimal("121.0583000"));
        routeLog.setDestinationLabel("B");
        routeLog.setDestinationLat(new BigDecimal("13.9411000"));
        routeLog.setDestinationLng(new BigDecimal("121.1636000"));
        routeLog.setDistanceKm(new BigDecimal("25.00"));
        routeLog.setEstimatedDurationMin(40);
        routeLog.setActualFuelUsedLiters(new BigDecimal("3.00"));
        routeLog.setExpectedFuelLiters(new BigDecimal("2.50"));
        routeLog.setEfficiencyScore(efficiencyScore);
        routeLogRepository.save(routeLog);
    }

    private void seedUnreadAlert(Vehicle vehicle, User driver) {
        Alert alert = new Alert();
        alert.setVehicleId(vehicle.getId());
        alert.setDriverId(driver.getId());
        alert.setAlertType(AlertType.HIGH_COST);
        alert.setMessage("High cost");
        alert.setThresholdValue(new BigDecimal("5000.00"));
        alert.setActualValue(new BigDecimal("7000.00"));
        alert.setIsRead(false);
        alertRepository.save(alert);
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
