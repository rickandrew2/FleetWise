package com.fleetwise.dashboard.dto;

import java.util.UUID;

public record TopDriverResponse(
                UUID driverId,
                String driverName,
                Double averageEfficiencyScore,
                long routeCount) {
}
