package com.fleetwise.vehicle;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "vehicles")
public class Vehicle {

    @Id
    private UUID id;

    @Column(name = "plate_number", nullable = false, unique = true, length = 20)
    private String plateNumber;

    @Column(length = 50)
    private String make;

    @Column(length = 50)
    private String model;

    @Column(name = "vehicle_year")
    private Integer year;

    @Column(name = "fuel_type", length = 30)
    private String fuelType;

    @Column(name = "tank_capacity_liters")
    private BigDecimal tankCapacityLiters;

    @Column(name = "epa_vehicle_id")
    private Integer epaVehicleId;

    @Column(name = "combined_mpg")
    private BigDecimal combinedMpg;

    @Column(name = "city_mpg")
    private BigDecimal cityMpg;

    @Column(name = "highway_mpg")
    private BigDecimal highwayMpg;

    @Column(name = "assigned_driver_id")
    private UUID assignedDriverId;

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