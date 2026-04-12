package com.fleetwise.fuelprice.dto;

import java.time.LocalDate;

public record FuelPriceUpdateResultResponse(
        int updatedRecords,
        LocalDate effectiveDate,
        boolean fallbackUsed,
        String message) {
}
