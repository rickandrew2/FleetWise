package com.fleetwise.report;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ReportSnapshot(
        ReportType reportType,
        Instant generatedAt,
        long totalVehicles,
        BigDecimal totalFuelCost,
        Double averageEfficiencyScore,
        long activeAlertsCount,
        List<TopInefficientRoute> topInefficientRoutes) {

    public record TopInefficientRoute(UUID routeId, UUID vehicleId, UUID driverId, Double efficiencyScore) {
    }
}
