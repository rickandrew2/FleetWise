package com.fleetwise.vehicle.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record EpaLookupRequest(
                @Min(1980) @Max(2100) int year,
                @NotBlank @Size(max = 50) String make,
                @NotBlank @Size(max = 50) String model) {
}