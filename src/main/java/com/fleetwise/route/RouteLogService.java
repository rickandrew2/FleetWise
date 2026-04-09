package com.fleetwise.route;

import com.fleetwise.alert.AlertService;
import com.fleetwise.fuellog.FuelLogRepository;
import com.fleetwise.route.dto.RouteLogResponse;
import com.fleetwise.route.dto.RouteLogStatsResponse;
import com.fleetwise.route.dto.RouteLogUpsertRequest;
import com.fleetwise.user.User;
import com.fleetwise.user.UserRepository;
import com.fleetwise.user.UserRole;
import com.fleetwise.vehicle.Vehicle;
import com.fleetwise.vehicle.VehicleRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RouteLogService {

    private static final BigDecimal MPG_TO_KM_PER_LITER = new BigDecimal("0.425143707");

    private final RouteLogRepository routeLogRepository;
    private final VehicleRepository vehicleRepository;
    private final UserRepository userRepository;
    private final FuelLogRepository fuelLogRepository;
    private final RouteDistanceCalculator routeDistanceCalculator;
    private final AlertService alertService;

    @Transactional(readOnly = true)
    public List<RouteLogResponse> getRouteLogs(String currentEmail,
            UUID vehicleId,
            UUID driverId,
            LocalDate startDate,
            LocalDate endDate) {
        User currentUser = getCurrentUser(currentEmail);
        UUID effectiveDriverId = resolveDriverFilter(currentUser, driverId);

        return routeLogRepository.findFiltered(vehicleId, effectiveDriverId, startDate, endDate)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public RouteLogResponse getRouteLogById(String currentEmail, UUID id) {
        User currentUser = getCurrentUser(currentEmail);

        RouteLog routeLog;
        if (currentUser.getRole() == UserRole.DRIVER) {
            routeLog = routeLogRepository.findByIdAndDriverId(id, currentUser.getId())
                    .orElseThrow(() -> new EntityNotFoundException("Route log not found"));
        } else {
            routeLog = routeLogRepository.findById(id)
                    .orElseThrow(() -> new EntityNotFoundException("Route log not found"));
        }

        return toResponse(routeLog);
    }

    @Transactional
    public RouteLogResponse createRouteLog(String currentEmail, RouteLogUpsertRequest request) {
        User currentUser = getCurrentUser(currentEmail);

        Vehicle vehicle = vehicleRepository.findById(request.vehicleId())
                .orElseThrow(() -> new EntityNotFoundException("Vehicle not found"));

        UUID effectiveDriverId = resolveDriverForCreate(currentUser, request.driverId());
        if (!userRepository.existsById(effectiveDriverId)) {
            throw new EntityNotFoundException("Driver not found");
        }

        RouteDistanceCalculator.DistanceResult distanceResult = routeDistanceCalculator.calculate(
                request.originLat(),
                request.originLng(),
                request.destinationLat(),
                request.destinationLng());

        BigDecimal distanceKm = toDecimal(distanceResult.distanceKm(), 2);
        BigDecimal actualFuelUsedLiters = resolveActualFuelUsed(request, effectiveDriverId);
        BigDecimal expectedFuelLiters = calculateExpectedFuel(distanceKm, vehicle.getCombinedMpg());
        BigDecimal efficiencyScore = calculateEfficiencyScore(actualFuelUsedLiters, expectedFuelLiters);

        RouteLog routeLog = new RouteLog();
        routeLog.setVehicleId(request.vehicleId());
        routeLog.setDriverId(effectiveDriverId);
        routeLog.setTripDate(request.tripDate());
        routeLog.setOriginLabel(trimToNull(request.originLabel()));
        routeLog.setOriginLat(toDecimal(request.originLat(), 7));
        routeLog.setOriginLng(toDecimal(request.originLng(), 7));
        routeLog.setDestinationLabel(trimToNull(request.destinationLabel()));
        routeLog.setDestinationLat(toDecimal(request.destinationLat(), 7));
        routeLog.setDestinationLng(toDecimal(request.destinationLng(), 7));
        routeLog.setDistanceKm(distanceKm);
        routeLog.setEstimatedDurationMin(distanceResult.estimatedDurationMin());
        routeLog.setActualFuelUsedLiters(actualFuelUsedLiters);
        routeLog.setExpectedFuelLiters(expectedFuelLiters);
        routeLog.setEfficiencyScore(efficiencyScore);

        RouteLog savedRouteLog = routeLogRepository.save(routeLog);
        alertService.checkRouteLog(savedRouteLog);

        return toResponse(savedRouteLog);
    }

    @Transactional
    public void deleteRouteLog(UUID id) {
        if (!routeLogRepository.existsById(id)) {
            throw new EntityNotFoundException("Route log not found");
        }
        routeLogRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public RouteLogStatsResponse getRouteLogStats(String currentEmail,
            UUID vehicleId,
            UUID driverId,
            LocalDate startDate,
            LocalDate endDate) {
        User currentUser = getCurrentUser(currentEmail);
        UUID effectiveDriverId = resolveDriverFilter(currentUser, driverId);

        List<RouteLog> logs = routeLogRepository.findFiltered(vehicleId, effectiveDriverId, startDate, endDate);

        long totalTrips = logs.size();
        BigDecimal totalDistance = logs.stream()
                .map(RouteLog::getDistanceKm)
                .filter(value -> value != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);

        List<BigDecimal> efficiencies = logs.stream()
                .map(RouteLog::getEfficiencyScore)
                .filter(value -> value != null)
                .toList();

        Double averageEfficiency = null;
        if (!efficiencies.isEmpty()) {
            BigDecimal sumEfficiency = efficiencies.stream()
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            averageEfficiency = sumEfficiency
                    .divide(BigDecimal.valueOf(efficiencies.size()), 2, RoundingMode.HALF_UP)
                    .doubleValue();
        }

        return new RouteLogStatsResponse(totalTrips, totalDistance.doubleValue(), averageEfficiency);
    }

    private User getCurrentUser(String email) {
        return userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new EntityNotFoundException("Current user not found"));
    }

    private UUID resolveDriverFilter(User currentUser, UUID requestedDriverId) {
        if (currentUser.getRole() == UserRole.DRIVER) {
            if (requestedDriverId != null && !requestedDriverId.equals(currentUser.getId())) {
                throw new IllegalArgumentException("Drivers can only access their own route logs");
            }
            return currentUser.getId();
        }
        return requestedDriverId;
    }

    private UUID resolveDriverForCreate(User currentUser, UUID requestedDriverId) {
        if (currentUser.getRole() == UserRole.DRIVER) {
            if (requestedDriverId != null && !requestedDriverId.equals(currentUser.getId())) {
                throw new IllegalArgumentException("Drivers can only create their own route logs");
            }
            return currentUser.getId();
        }

        if (requestedDriverId == null) {
            return currentUser.getId();
        }
        return requestedDriverId;
    }

    private BigDecimal resolveActualFuelUsed(RouteLogUpsertRequest request, UUID driverId) {
        if (request.actualFuelUsedLiters() != null) {
            return toDecimal(request.actualFuelUsedLiters(), 2);
        }

        BigDecimal summedLiters = fuelLogRepository.sumLitersFilledForTrip(
                request.tripDate(),
                request.vehicleId(),
                driverId);

        if (summedLiters == null || summedLiters.compareTo(BigDecimal.ZERO) == 0) {
            return null;
        }

        return summedLiters.setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateExpectedFuel(BigDecimal distanceKm, BigDecimal combinedMpg) {
        if (distanceKm == null || combinedMpg == null || combinedMpg.compareTo(BigDecimal.ZERO) <= 0) {
            return null;
        }

        BigDecimal kmPerLiter = combinedMpg.multiply(MPG_TO_KM_PER_LITER);
        if (kmPerLiter.compareTo(BigDecimal.ZERO) <= 0) {
            return null;
        }

        return distanceKm.divide(kmPerLiter, 2, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateEfficiencyScore(BigDecimal actualFuelUsed, BigDecimal expectedFuel) {
        if (actualFuelUsed == null || expectedFuel == null || expectedFuel.compareTo(BigDecimal.ZERO) <= 0) {
            return null;
        }

        return actualFuelUsed.divide(expectedFuel, 2, RoundingMode.HALF_UP);
    }

    private RouteLogResponse toResponse(RouteLog routeLog) {
        return new RouteLogResponse(
                routeLog.getId(),
                routeLog.getVehicleId(),
                routeLog.getDriverId(),
                routeLog.getTripDate(),
                routeLog.getOriginLabel(),
                toDouble(routeLog.getOriginLat()),
                toDouble(routeLog.getOriginLng()),
                routeLog.getDestinationLabel(),
                toDouble(routeLog.getDestinationLat()),
                toDouble(routeLog.getDestinationLng()),
                toDouble(routeLog.getDistanceKm()),
                routeLog.getEstimatedDurationMin(),
                toDouble(routeLog.getActualFuelUsedLiters()),
                toDouble(routeLog.getExpectedFuelLiters()),
                toDouble(routeLog.getEfficiencyScore()),
                routeLog.getCreatedAt());
    }

    private BigDecimal toDecimal(Double value, int scale) {
        if (value == null) {
            return null;
        }
        return BigDecimal.valueOf(value).setScale(scale, RoundingMode.HALF_UP);
    }

    private Double toDouble(BigDecimal value) {
        return value == null ? null : value.doubleValue();
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
