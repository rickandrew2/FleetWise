package com.fleetwise.dashboard.dto;

public record DashboardForecastResponse(
        Double projectedCost,
        String confidenceLevel,
        Integer basedOnMonths,
        Double priceUsed,
        Double avgLitersPerMonth) {
}
