package com.fleetwise.fuelprice.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.List;

public record FuelPriceManualUpdateRequest(
        @NotNull LocalDate effectiveDate,
        @NotBlank @Size(max = 100) String source,
        @NotEmpty List<@Valid FuelPriceManualEntryRequest> entries) {
}
