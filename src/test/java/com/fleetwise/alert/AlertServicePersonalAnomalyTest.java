package com.fleetwise.alert;

import com.fleetwise.notification.EmailNotificationService;
import com.fleetwise.route.RouteLog;
import com.fleetwise.route.RouteLogRepository;
import com.fleetwise.user.User;
import com.fleetwise.user.UserRepository;
import com.fleetwise.user.UserRole;
import com.fleetwise.vehicle.VehicleRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AlertServicePersonalAnomalyTest {

    @Mock
    private AlertRepository alertRepository;

    @Mock
    private VehicleRepository vehicleRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private EmailNotificationService emailNotificationService;

    @Mock
    private RouteLogRepository routeLogRepository;

    @InjectMocks
    private AlertService alertService;

    @Test
    void shouldCreatePersonalAnomalyAlertWhenCurrentScoreIsFarAboveBaseline() {
        UUID driverId = UUID.randomUUID();
        UUID vehicleId = UUID.randomUUID();

        RouteLog currentRoute = routeLog(vehicleId, driverId, "1.42", LocalDate.now(), Instant.now());
        currentRoute.setId(UUID.randomUUID());

        List<RouteLog> baselineRoutes = List.of(
                routeLog(vehicleId, driverId, "1.00", LocalDate.now().minusDays(10), Instant.now().minusSeconds(2000)),
                routeLog(vehicleId, driverId, "1.05", LocalDate.now().minusDays(9), Instant.now().minusSeconds(1900)),
                routeLog(vehicleId, driverId, "1.08", LocalDate.now().minusDays(8), Instant.now().minusSeconds(1800))
        );

        User driver = new User();
        driver.setId(driverId);
        driver.setName("Juan Dela Cruz");
        driver.setEmail("driver@fleetwise.test");
        driver.setRole(UserRole.DRIVER);

        when(routeLogRepository.findEfficiencyLogsForDriverSince(eq(driverId), any(), eq(currentRoute.getId())))
                .thenReturn(baselineRoutes);
        when(userRepository.findById(driverId)).thenReturn(Optional.of(driver));
        when(alertRepository.save(any(Alert.class))).thenAnswer(invocation -> invocation.getArgument(0));

        alertService.checkPersonalizedAnomaly(currentRoute);

        ArgumentCaptor<Alert> captor = ArgumentCaptor.forClass(Alert.class);
        verify(alertRepository).save(captor.capture());

        Alert saved = captor.getValue();
        assertThat(saved.getAlertType()).isEqualTo(AlertType.PERSONAL_ANOMALY);
        assertThat(saved.getDriverId()).isEqualTo(driverId);
        assertThat(saved.getVehicleId()).isEqualTo(vehicleId);
        assertThat(saved.getMessage()).contains("Juan Dela Cruz");
        assertThat(saved.getActualValue()).isEqualByComparingTo("1.42");
    }

    private RouteLog routeLog(UUID vehicleId, UUID driverId, String score, LocalDate tripDate, Instant createdAt) {
        RouteLog routeLog = new RouteLog();
        routeLog.setId(UUID.randomUUID());
        routeLog.setVehicleId(vehicleId);
        routeLog.setDriverId(driverId);
        routeLog.setTripDate(tripDate);
        routeLog.setEfficiencyScore(new BigDecimal(score));
        routeLog.setCreatedAt(createdAt);
        return routeLog;
    }
}
