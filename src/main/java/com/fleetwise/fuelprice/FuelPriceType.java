package com.fleetwise.fuelprice;

import java.util.Locale;
import java.util.Optional;

public enum FuelPriceType {
    DIESEL("Diesel"),
    GASOLINE_91("Gasoline 91"),
    GASOLINE_95("Gasoline 95"),
    DIESEL_PLUS("Diesel Plus");

    private final String displayName;

    FuelPriceType(String displayName) {
        this.displayName = displayName;
    }

    public String displayName() {
        return displayName;
    }

    public static Optional<FuelPriceType> fromVehicleFuelType(String rawValue) {
        if (rawValue == null || rawValue.isBlank()) {
            return Optional.empty();
        }

        String normalized = rawValue.trim()
                .toUpperCase(Locale.ROOT)
                .replace('-', '_')
                .replace(' ', '_');

        return switch (normalized) {
            case "DIESEL", "DSL" -> Optional.of(DIESEL);
            case "DIESEL_PLUS", "PREMIUM_DIESEL", "DIESEL_PREMIUM" -> Optional.of(DIESEL_PLUS);
            case "GASOLINE_95", "PREMIUM", "PREMIUM_GASOLINE", "UNLEADED_95", "XCS", "BLAZE" ->
                Optional.of(GASOLINE_95);
            case "GASOLINE_91", "GASOLINE", "REGULAR", "REGULAR_GASOLINE", "UNLEADED", "UNLEADED_91", "XTRA_ADVANCE" ->
                Optional.of(GASOLINE_91);
            default -> Optional.empty();
        };
    }
}
