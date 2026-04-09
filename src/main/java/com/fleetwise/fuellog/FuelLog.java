package com.fleetwise.fuellog;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "fuel_logs")
public class FuelLog {

    @Id
    private UUID id;

    @Column(name = "vehicle_id", nullable = false)
    private UUID vehicleId;

    @Column(name = "driver_id", nullable = false)
    private UUID driverId;

    @Column(name = "log_date", nullable = false)
    private LocalDate logDate;

    @Column(name = "odometer_reading_km", precision = 10, scale = 2)
    private BigDecimal odometerReadingKm;

    @Column(name = "liters_filled", nullable = false, precision = 8, scale = 2)
    private BigDecimal litersFilled;

    @Column(name = "price_per_liter", nullable = false, precision = 10, scale = 2)
    private BigDecimal pricePerLiter;

    @Column(name = "total_cost", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalCost;

    @Column(name = "station_name", length = 100)
    private String stationName;

    @Column(name = "station_lat", precision = 10, scale = 7)
    private BigDecimal stationLat;

    @Column(name = "station_lng", precision = 10, scale = 7)
    private BigDecimal stationLng;

    @Column(name = "notes")
    private String notes;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    public void prePersist() {
        if (id == null) {
            id = UUID.randomUUID();
        }
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }
}
