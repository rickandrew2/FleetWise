package com.fleetwise.vehicle;

import com.fleetwise.fuelprice.FuelPriceType;
import com.fleetwise.fuellog.FuelLog;
import com.fleetwise.fuellog.FuelLogRepository;
import com.fleetwise.route.RouteLog;
import com.fleetwise.route.RouteLogRepository;
import com.fleetwise.vehicle.dto.EpaLookupRequest;
import com.fleetwise.vehicle.dto.EpaVehicleOption;
import com.fleetwise.vehicle.dto.FuelEconomyVehicleData;
import com.fleetwise.vehicle.dto.VehicleResponse;
import com.fleetwise.vehicle.dto.VehicleStatsResponse;
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
    private final FuelLogRepository fuelLogRepository;
    private final RouteLogRepository routeLogRepository;

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

    @Transactional(readOnly = true)
    public VehicleStatsResponse getVehicleStats(UUID id) {
        if (!vehicleRepository.existsById(id)) {
            throw new EntityNotFoundException("Vehicle not found");
        }

        List<FuelLog> fuelLogs = fuelLogRepository.findFiltered(id, null, null, null);
        List<RouteLog> routeLogs = routeLogRepository.findFiltered(id, null, null, null);

        BigDecimal totalFuelLiters = fuelLogs.stream()
                .map(FuelLog::getLitersFilled)
                .filter(value -> value != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);

        BigDecimal totalFuelCost = fuelLogs.stream()
                .map(FuelLog::getTotalCost)
                .filter(value -> value != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);

        BigDecimal totalDistanceKm = routeLogs.stream()
                .map(RouteLog::getDistanceKm)
                .filter(value -> value != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);

        List<BigDecimal> efficiencyScores = routeLogs.stream()
                .map(RouteLog::getEfficiencyScore)
                .filter(value -> value != null)
                .toList();

        Double averageEfficiencyScore = null;
        if (!efficiencyScores.isEmpty()) {
            BigDecimal sum = efficiencyScores.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
            averageEfficiencyScore = sum.divide(BigDecimal.valueOf(efficiencyScores.size()), 2, RoundingMode.HALF_UP)
                    .doubleValue();
        }

        return new VehicleStatsResponse(
                id,
                fuelLogs.size(),
                totalFuelLiters.doubleValue(),
                totalFuelCost.doubleValue(),
                routeLogs.size(),
                totalDistanceKm.doubleValue(),
                averageEfficiencyScore);
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
        vehicle.setFuelType(normalizeFuelTypeOrNull(request.fuelType()));
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
            vehicle.setFuelType(FuelPriceType.fromVehicleFuelType(data.fuelType())
                    .map(FuelPriceType::name)
                    .orElse(null));
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

    private String normalizeFuelTypeOrNull(String fuelType) {
        String trimmed = trimToNull(fuelType);
        if (trimmed == null) {
            return null;
        }

        return FuelPriceType.fromVehicleFuelType(trimmed)
                .map(FuelPriceType::name)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Fuel type must be one of: DIESEL, GASOLINE_91, GASOLINE_95, DIESEL_PLUS"));
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