package com.fleetwise.fuellog.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.UUID;

public record FuelLogUpsertRequest(
        @NotNull UUID vehicleId,
        UUID driverId,
        @NotNull LocalDate logDate,
        @DecimalMin("0.0") Double odometerReadingKm,
        @NotNull @DecimalMin("0.01") Double litersFilled,
        @NotNull @DecimalMin("0.01") Double pricePerLiter,
        @Size(max = 100) String stationName,
        @DecimalMin("-90.0") @DecimalMax("90.0") Double stationLat,
        @DecimalMin("-180.0") @DecimalMax("180.0") Double stationLng,
        @Size(max = 2000) String notes) {
}
