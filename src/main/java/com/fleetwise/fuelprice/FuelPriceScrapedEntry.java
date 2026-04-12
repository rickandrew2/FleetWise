package com.fleetwise.fuelprice;

import java.math.BigDecimal;
import java.time.LocalDate;

public record FuelPriceScrapedEntry(
        FuelPriceType fuelType,
        BigDecimal pricePerLiter,
        String brand,
        LocalDate effectiveDate,
        String source) {
}
