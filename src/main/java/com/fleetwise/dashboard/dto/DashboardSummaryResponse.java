package com.fleetwise.dashboard.dto;

public record DashboardSummaryResponse(
                double monthToDateFuelCost,
                Double fleetEfficiencyScore,
                long activeAlertsCount) {
}
