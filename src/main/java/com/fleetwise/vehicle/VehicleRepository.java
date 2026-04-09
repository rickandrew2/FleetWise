package com.fleetwise.vehicle;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface VehicleRepository extends JpaRepository<Vehicle, UUID> {

    boolean existsByPlateNumberIgnoreCase(String plateNumber);

    Optional<Vehicle> findByPlateNumberIgnoreCase(String plateNumber);
}