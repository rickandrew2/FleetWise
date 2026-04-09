package com.fleetwise.alert.dto;

import com.fleetwise.alert.AlertType;

import java.time.Instant;
import java.util.UUID;

public record AlertResponse(
        UUID id,
        UUID vehicleId,
        UUID driverId,
        AlertType alertType,
        String message,
        Double thresholdValue,
        Double actualValue,
        boolean isRead,
        Instant triggeredAt) {
}
