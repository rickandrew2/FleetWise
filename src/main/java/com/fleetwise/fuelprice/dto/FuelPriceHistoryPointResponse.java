package com.fleetwise.fuelprice.dto;

import com.fleetwise.fuelprice.FuelPriceType;

import java.time.LocalDate;

public record FuelPriceHistoryPointResponse(
        FuelPriceType fuelType,
        LocalDate effectiveDate,
        Double averagePricePerLiter) {
}
