package com.fleetwise.fuellog.dto;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record FuelLogResponse(
        UUID id,
        UUID vehicleId,
        UUID driverId,
        LocalDate logDate,
        Double odometerReadingKm,
        Double litersFilled,
        Double pricePerLiter,
        Double totalCost,
        String stationName,
        Double stationLat,
        Double stationLng,
        String notes,
        Instant createdAt) {
}
