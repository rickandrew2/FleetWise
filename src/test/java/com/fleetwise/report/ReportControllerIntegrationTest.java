package com.fleetwise.report;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fleetwise.alert.AlertRepository;
import com.fleetwise.fuellog.FuelLogRepository;
import com.fleetwise.route.RouteLogRepository;
import com.fleetwise.user.User;
import com.fleetwise.user.UserRepository;
import com.fleetwise.user.UserRole;
import com.fleetwise.vehicle.VehicleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ReportControllerIntegrationTest {

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
        }

        @Test
        void shouldRestrictAndGenerateReportJobs() throws Exception {
                mockMvc.perform(get("/api/reports"))
                                .andExpect(status().isUnauthorized());

                String driverToken = loginAndGetToken(driverUser.getEmail(), "StrongPass123");
                mockMvc.perform(get("/api/reports")
                                .header("Authorization", "Bearer " + driverToken))
                                .andExpect(status().isForbidden());

                String managerToken = loginAndGetToken(managerUser.getEmail(), "StrongPass123");

                String generatePayload = """
                                {
                                  "reportType": "WEEKLY"
                                }
                                """;

                String generateResponse = mockMvc.perform(post("/api/reports/generate")
                                .header("Authorization", "Bearer " + managerToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(generatePayload))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.reportType").value("WEEKLY"))
                                .andExpect(jsonPath("$.status").value("COMPLETED"))
                                .andExpect(jsonPath("$.filePath").isNotEmpty())
                                .andReturn()
                                .getResponse()
                                .getContentAsString();

                String reportId = objectMapper.readTree(generateResponse).get("id").asText();

                mockMvc.perform(get("/api/reports")
                                .header("Authorization", "Bearer " + managerToken))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$[0].id").value(reportId));

                MvcResult downloadResult = mockMvc.perform(get("/api/reports/" + reportId + "/download")
                                .header("Authorization", "Bearer " + managerToken))
                                .andExpect(status().isOk())
                                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.header()
                                                .string(HttpHeaders.CONTENT_DISPOSITION,
                                                                containsString("attachment; filename=")))
                                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.content()
                                                .contentType(MediaType.APPLICATION_OCTET_STREAM))
                                .andReturn();

                assertThat(downloadResult.getResponse().getContentAsByteArray()).isNotEmpty();
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
