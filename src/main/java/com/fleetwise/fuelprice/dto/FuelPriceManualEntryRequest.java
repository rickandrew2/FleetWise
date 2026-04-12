package com.fleetwise.fuelprice.dto;

import com.fleetwise.fuelprice.FuelPriceType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record FuelPriceManualEntryRequest(
        @NotNull FuelPriceType fuelType,
        @NotNull @DecimalMin("0.01") BigDecimal pricePerLiter,
        @Size(max = 50) String brand) {
}
