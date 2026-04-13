package com.fleetwise.alert;

import com.fleetwise.alert.dto.AlertResponse;
import com.fleetwise.alert.dto.AlertUnreadCountResponse;
import com.fleetwise.fuellog.FuelLog;
import com.fleetwise.notification.EmailNotificationService;
import com.fleetwise.route.RouteLog;
import com.fleetwise.user.User;
import com.fleetwise.user.UserRepository;
import com.fleetwise.user.UserRole;
import com.fleetwise.vehicle.Vehicle;
import com.fleetwise.vehicle.VehicleRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AlertService {

    private final AlertRepository alertRepository;
    private final VehicleRepository vehicleRepository;
    private final UserRepository userRepository;
    private final EmailNotificationService emailNotificationService;

    @Value("${alert.high-cost-threshold:5000}")
    private BigDecimal highCostThreshold;

    @Value("${alert.overconsumption-threshold:1.25}")
    private BigDecimal overconsumptionThreshold;

    @Value("${alert.unusual-fillup-multiplier:1.10}")
    private BigDecimal unusualFillupMultiplier;

    @Transactional(readOnly = true)
    public List<AlertResponse> getAlerts(String currentEmail,
            AlertType alertType,
            UUID vehicleId,
            UUID driverId,
            Boolean isRead) {
        User currentUser = getCurrentUser(currentEmail);
        UUID effectiveDriverId = resolveDriverFilter(currentUser, driverId);

        return alertRepository.findFiltered(alertType, vehicleId, effectiveDriverId, isRead)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public AlertResponse markAlertAsRead(String currentEmail, UUID alertId) {
        User currentUser = getCurrentUser(currentEmail);

        Alert alert;
        if (currentUser.getRole() == UserRole.DRIVER) {
            alert = alertRepository.findByIdAndDriverId(alertId, currentUser.getId())
                    .orElseThrow(() -> new EntityNotFoundException("Alert not found"));
        } else {
            alert = alertRepository.findById(alertId)
                    .orElseThrow(() -> new EntityNotFoundException("Alert not found"));
        }

        alert.setIsRead(true);
        return toResponse(alertRepository.save(alert));
    }

    @Transactional(readOnly = true)
    public AlertUnreadCountResponse getUnreadCount(String currentEmail, UUID driverId) {
        User currentUser = getCurrentUser(currentEmail);
        UUID effectiveDriverId = resolveDriverFilter(currentUser, driverId);

        long unreadCount = alertRepository.countUnread(effectiveDriverId);
        return new AlertUnreadCountResponse(unreadCount);
    }

    @Transactional
    public void checkFuelLog(FuelLog fuelLog) {
        if (fuelLog.getTotalCost() != null && fuelLog.getTotalCost().compareTo(highCostThreshold) > 0) {
            createAlert(
                    AlertType.HIGH_COST,
                    fuelLog.getVehicleId(),
                    fuelLog.getDriverId(),
                    "Fuel fill-up cost exceeded threshold",
                    highCostThreshold,
                    fuelLog.getTotalCost());
        }

        if (fuelLog.getLitersFilled() != null) {
            vehicleRepository.findById(fuelLog.getVehicleId())
                    .map(Vehicle::getTankCapacityLiters)
                    .filter(capacity -> capacity != null && capacity.compareTo(BigDecimal.ZERO) > 0)
                    .ifPresent(capacity -> {
                        BigDecimal unusualThreshold = capacity.multiply(unusualFillupMultiplier)
                                .setScale(2, RoundingMode.HALF_UP);
                        if (fuelLog.getLitersFilled().compareTo(unusualThreshold) > 0) {
                            createAlert(
                                    AlertType.UNUSUAL_FILLUP,
                                    fuelLog.getVehicleId(),
                                    fuelLog.getDriverId(),
                                    "Fuel liters exceeded expected tank capacity threshold",
                                    unusualThreshold,
                                    fuelLog.getLitersFilled());
                        }
                    });
        }
    }

    @Transactional
    public void checkRouteLog(RouteLog routeLog) {
        if (routeLog.getEfficiencyScore() == null) {
            return;
        }

        if (routeLog.getEfficiencyScore().compareTo(overconsumptionThreshold) > 0) {
            createAlert(
                    AlertType.OVERCONSUMPTION,
                    routeLog.getVehicleId(),
                    routeLog.getDriverId(),
                    "Route fuel efficiency exceeded overconsumption threshold",
                    overconsumptionThreshold,
                    routeLog.getEfficiencyScore());
        }
    }

    private void createAlert(
            AlertType alertType,
            UUID vehicleId,
            UUID driverId,
            String message,
            BigDecimal thresholdValue,
            BigDecimal actualValue) {
        Alert alert = new Alert();
        alert.setVehicleId(vehicleId);
        alert.setDriverId(driverId);
        alert.setAlertType(alertType);
        alert.setMessage(message);
        alert.setThresholdValue(thresholdValue == null ? null : thresholdValue.setScale(2, RoundingMode.HALF_UP));
        alert.setActualValue(actualValue == null ? null : actualValue.setScale(2, RoundingMode.HALF_UP));
        alert.setIsRead(false);
        Alert savedAlert = alertRepository.save(alert);
        notifyRecipients(savedAlert);
    }

    private void notifyRecipients(Alert alert) {
        if (alert.getDriverId() != null) {
            userRepository.findById(alert.getDriverId())
                    .ifPresent(user -> emailNotificationService.sendAlertNotification(user, alert));
            return;
        }

        List<UserRole> fleetRoles = List.of(UserRole.ADMIN, UserRole.FLEET_MANAGER);
        userRepository.findAllByRoleInAndEmailNotificationsEnabledTrue(fleetRoles)
                .forEach(user -> emailNotificationService.sendAlertNotification(user, alert));
    }

    private User getCurrentUser(String email) {
        return userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new EntityNotFoundException("Current user not found"));
    }

    private UUID resolveDriverFilter(User currentUser, UUID requestedDriverId) {
        if (currentUser.getRole() == UserRole.DRIVER) {
            if (requestedDriverId != null && !requestedDriverId.equals(currentUser.getId())) {
                throw new IllegalArgumentException("Drivers can only access their own alerts");
            }
            return currentUser.getId();
        }
        return requestedDriverId;
    }

    private AlertResponse toResponse(Alert alert) {
        return new AlertResponse(
                alert.getId(),
                alert.getVehicleId(),
                alert.getDriverId(),
                alert.getAlertType(),
                alert.getMessage(),
                toDouble(alert.getThresholdValue()),
                toDouble(alert.getActualValue()),
                Boolean.TRUE.equals(alert.getIsRead()),
                alert.getTriggeredAt());
    }

    private Double toDouble(BigDecimal value) {
        return value == null ? null : value.doubleValue();
    }
}
