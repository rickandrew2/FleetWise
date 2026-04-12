package com.fleetwise.fuelprice.dto;

import com.fleetwise.fuelprice.FuelPriceType;

import java.time.LocalDate;

public record FuelPriceCurrentResponse(
        FuelPriceType fuelType,
        Double pricePerLiter,
        LocalDate effectiveDate,
        String source,
        boolean stale) {
}
