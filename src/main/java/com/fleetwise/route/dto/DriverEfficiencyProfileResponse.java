package com.fleetwise.route.dto;

import java.util.UUID;

public record DriverEfficiencyProfileResponse(
        UUID driverId,
        Double avgScore30Days,
        Double stdDev,
        Integer totalTrips,
        String trend) {
}
