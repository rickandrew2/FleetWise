package com.fleetwise.vehicle;

import com.fleetwise.vehicle.dto.EpaLookupRequest;
import com.fleetwise.vehicle.dto.EpaVehicleOption;
import com.fleetwise.vehicle.dto.FuelEconomyVehicleData;
import com.fleetwise.vehicle.dto.VehicleResponse;
import com.fleetwise.vehicle.dto.VehicleUpsertRequest;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class VehicleService {

    private final VehicleRepository vehicleRepository;
    private final FuelEconomyApiClient fuelEconomyApiClient;

    @Transactional(readOnly = true)
    public List<VehicleResponse> getAllVehicles() {
        return vehicleRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public VehicleResponse getVehicleById(UUID id) {
        Vehicle vehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Vehicle not found"));
        return toResponse(vehicle);
    }

    @Transactional
    public VehicleResponse createVehicle(VehicleUpsertRequest request) {
        String normalizedPlate = normalizePlate(request.plateNumber());
        if (vehicleRepository.existsByPlateNumberIgnoreCase(normalizedPlate)) {
            throw new IllegalArgumentException("Plate number already exists");
        }

        Vehicle vehicle = new Vehicle();
        applyRequest(vehicle, request, normalizedPlate);
        Vehicle savedVehicle = vehicleRepository.save(vehicle);
        return toResponse(savedVehicle);
    }

    @Transactional
    public VehicleResponse updateVehicle(UUID id, VehicleUpsertRequest request) {
        Vehicle vehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Vehicle not found"));

        String normalizedPlate = normalizePlate(request.plateNumber());
        vehicleRepository.findByPlateNumberIgnoreCase(normalizedPlate)
                .filter(existing -> !existing.getId().equals(id))
                .ifPresent(existing -> {
                    throw new IllegalArgumentException("Plate number already exists");
                });

        applyRequest(vehicle, request, normalizedPlate);
        Vehicle savedVehicle = vehicleRepository.save(vehicle);
        return toResponse(savedVehicle);
    }

    @Transactional
    public void deleteVehicle(UUID id) {
        if (!vehicleRepository.existsById(id)) {
            throw new EntityNotFoundException("Vehicle not found");
        }
        vehicleRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<EpaVehicleOption> lookupEpaOptions(EpaLookupRequest request) {
        return fuelEconomyApiClient.lookupVehicleOptions(
                request.year(),
                request.make().trim(),
                request.model().trim());
    }

    private void applyRequest(Vehicle vehicle, VehicleUpsertRequest request, String normalizedPlate) {
        vehicle.setPlateNumber(normalizedPlate);
        vehicle.setMake(request.make().trim());
        vehicle.setModel(request.model().trim());
        vehicle.setYear(request.year());
        vehicle.setFuelType(trimToNull(request.fuelType()));
        vehicle.setTankCapacityLiters(toDecimal(request.tankCapacityLiters()));
        vehicle.setEpaVehicleId(request.epaVehicleId());
        vehicle.setAssignedDriverId(request.assignedDriverId());

        if (request.epaVehicleId() != null) {
            fuelEconomyApiClient.fetchVehicleData(request.epaVehicleId())
                    .ifPresent(data -> applyFuelEconomyData(vehicle, data));
        }
    }

    private void applyFuelEconomyData(Vehicle vehicle, FuelEconomyVehicleData data) {
        vehicle.setCombinedMpg(toDecimal(data.combinedMpg()));
        vehicle.setCityMpg(toDecimal(data.cityMpg()));
        vehicle.setHighwayMpg(toDecimal(data.highwayMpg()));
        if (vehicle.getFuelType() == null || vehicle.getFuelType().isBlank()) {
            vehicle.setFuelType(data.fuelType());
        }
    }

    private VehicleResponse toResponse(Vehicle vehicle) {
        return new VehicleResponse(
                vehicle.getId(),
                vehicle.getPlateNumber(),
                vehicle.getMake(),
                vehicle.getModel(),
                vehicle.getYear(),
                vehicle.getFuelType(),
                toDouble(vehicle.getTankCapacityLiters()),
                vehicle.getEpaVehicleId(),
                toDouble(vehicle.getCombinedMpg()),
                toDouble(vehicle.getCityMpg()),
                toDouble(vehicle.getHighwayMpg()),
                vehicle.getAssignedDriverId(),
                vehicle.getCreatedAt());
    }

    private String normalizePlate(String plateNumber) {
        return plateNumber.trim().toUpperCase();
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private BigDecimal toDecimal(Double value) {
        if (value == null) {
            return null;
        }
        return BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_UP);
    }

    private Double toDouble(BigDecimal value) {
        return value == null ? null : value.doubleValue();
    }
}