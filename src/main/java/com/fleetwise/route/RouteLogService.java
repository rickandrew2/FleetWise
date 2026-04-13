package com.fleetwise.route;

import com.fleetwise.alert.AlertService;
import com.fleetwise.fuelprice.FuelPriceService;
import com.fleetwise.fuelprice.FuelPriceType;
import com.fleetwise.fuelprice.dto.FuelPriceCurrentResponse;
import com.fleetwise.fuellog.FuelLogRepository;
import com.fleetwise.route.dto.DriverEfficiencyProfileResponse;
import com.fleetwise.route.dto.RouteEstimateRequest;
import com.fleetwise.route.dto.RouteEstimateResponse;
import com.fleetwise.route.dto.RouteLogResponse;
import com.fleetwise.route.dto.RouteLogStatsResponse;
import com.fleetwise.route.dto.RouteLogUpsertRequest;
import com.fleetwise.user.User;
import com.fleetwise.user.UserRepository;
import com.fleetwise.user.UserRole;
import com.fleetwise.vehicle.Vehicle;
import com.fleetwise.vehicle.VehicleRepository;
import com.fleetwise.weather.RouteWeatherEnrichmentService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RouteLogService {

    private static final BigDecimal MPG_TO_KM_PER_LITER = new BigDecimal("0.425143707");
    private static final BigDecimal TREND_DELTA_THRESHOLD = new BigDecimal("0.05");
    private static final ZoneId MANILA_ZONE = ZoneId.of("Asia/Manila");

    private final RouteLogRepository routeLogRepository;
    private final VehicleRepository vehicleRepository;
    private final UserRepository userRepository;
    private final FuelLogRepository fuelLogRepository;
    private final RouteDistanceCalculator routeDistanceCalculator;
    private final AlertService alertService;
    private final FuelPriceService fuelPriceService;
    private final RouteWeatherEnrichmentService routeWeatherEnrichmentService;

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

    @Transactional(readOnly = true)
    public List<RouteLogResponse> getTopInefficientRoutes(String currentEmail, UUID driverId, Integer limit) {
        User currentUser = getCurrentUser(currentEmail);
        UUID effectiveDriverId = resolveDriverFilter(currentUser, driverId);
        int safeLimit = sanitizeLimit(limit);

        return routeLogRepository.findTopInefficient(effectiveDriverId, PageRequest.of(0, safeLimit))
                .stream()
                .map(this::toResponse)
                .toList();
    }

            @Transactional(readOnly = true)
            public RouteEstimateResponse estimateRoute(String currentEmail, RouteEstimateRequest request) {
            getCurrentUser(currentEmail);

            Vehicle vehicle = vehicleRepository.findById(request.vehicleId())
                .orElseThrow(() -> new EntityNotFoundException("Vehicle not found"));

            RouteDistanceCalculator.DistanceResult distanceResult = routeDistanceCalculator.calculate(
                request.originLat(),
                request.originLng(),
                request.destinationLat(),
                request.destinationLng());

            BigDecimal distanceKm = toDecimal(distanceResult.distanceKm(), 2);
            BigDecimal expectedFuelLiters = calculateExpectedFuel(distanceKm, vehicle.getCombinedMpg());

            FuelPriceType fuelType = FuelPriceType.fromVehicleFuelType(vehicle.getFuelType())
                .orElseThrow(() -> new IllegalStateException("Vehicle fuel type is not mapped to known fuel prices"));
            FuelPriceCurrentResponse currentPrice = fuelPriceService.getCurrentPrice(fuelType);
            BigDecimal currentPricePerLiter = BigDecimal.valueOf(currentPrice.pricePerLiter()).setScale(2, RoundingMode.HALF_UP);

            BigDecimal estimatedCost = null;
            if (expectedFuelLiters != null) {
                estimatedCost = expectedFuelLiters.multiply(currentPricePerLiter).setScale(2, RoundingMode.HALF_UP);
            }

            return new RouteEstimateResponse(
                toDouble(distanceKm),
                distanceResult.estimatedDurationMin(),
                toDouble(expectedFuelLiters),
                toDouble(estimatedCost),
                currentPrice.pricePerLiter(),
                vehicleName(vehicle),
                fuelType.displayName());
            }

            @Transactional(readOnly = true)
            public DriverEfficiencyProfileResponse getDriverEfficiencyProfile(String currentEmail, UUID driverId) {
            User currentUser = getCurrentUser(currentEmail);
            UUID effectiveDriverId = resolveDriverFilter(currentUser, driverId);
            if (effectiveDriverId == null) {
                throw new IllegalArgumentException("driverId is required");
            }

            LocalDate startDate = LocalDate.now(MANILA_ZONE).minusDays(30);
            List<RouteLog> logs = routeLogRepository.findEfficiencyLogsForDriverSince(effectiveDriverId, startDate, null);

            List<BigDecimal> scores = logs.stream()
                .map(RouteLog::getEfficiencyScore)
                .filter(score -> score != null)
                .toList();

            BigDecimal avgScore = calculateAverage(scores);
            BigDecimal stdDev = calculateStdDev(scores, avgScore);

            return new DriverEfficiencyProfileResponse(
                effectiveDriverId,
                toDouble(avgScore),
                toDouble(stdDev),
                scores.size(),
                calculateTrend(logs));
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
        routeWeatherEnrichmentService.enrichRouteWeatherAsync(
            savedRouteLog.getId(),
            request.originLat(),
            request.originLng(),
            request.tripDate());

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
                routeLog.getWeatherCondition(),
                toDouble(routeLog.getTemperatureCelsius()),
                routeLog.getCreatedAt());
    }

    private BigDecimal calculateAverage(List<BigDecimal> scores) {
        if (scores.isEmpty()) {
            return null;
        }

        BigDecimal total = scores.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        return total.divide(BigDecimal.valueOf(scores.size()), 2, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateStdDev(List<BigDecimal> scores, BigDecimal average) {
        if (scores.isEmpty() || average == null) {
            return null;
        }

        BigDecimal variance = BigDecimal.ZERO;
        for (BigDecimal score : scores) {
            BigDecimal diff = score.subtract(average);
            variance = variance.add(diff.multiply(diff));
        }

        variance = variance.divide(BigDecimal.valueOf(scores.size()), 6, RoundingMode.HALF_UP);
        return BigDecimal.valueOf(Math.sqrt(variance.doubleValue())).setScale(2, RoundingMode.HALF_UP);
    }

    private String calculateTrend(List<RouteLog> logs) {
        List<BigDecimal> orderedScores = logs.stream()
                .sorted((left, right) -> {
                    int dateCompare = left.getTripDate().compareTo(right.getTripDate());
                    if (dateCompare != 0) {
                        return dateCompare;
                    }
                    return left.getCreatedAt().compareTo(right.getCreatedAt());
                })
                .map(RouteLog::getEfficiencyScore)
                .filter(score -> score != null)
                .toList();

        if (orderedScores.size() < 3) {
            return "STABLE";
        }

        int splitIndex = orderedScores.size() / 2;
        if (splitIndex == 0 || splitIndex == orderedScores.size()) {
            return "STABLE";
        }

        BigDecimal earlierAverage = calculateAverage(orderedScores.subList(0, splitIndex));
        BigDecimal recentAverage = calculateAverage(orderedScores.subList(splitIndex, orderedScores.size()));
        if (earlierAverage == null || recentAverage == null) {
            return "STABLE";
        }

        BigDecimal delta = recentAverage.subtract(earlierAverage);
        if (delta.compareTo(TREND_DELTA_THRESHOLD.negate()) <= 0) {
            return "IMPROVING";
        }
        if (delta.compareTo(TREND_DELTA_THRESHOLD) >= 0) {
            return "DECLINING";
        }
        return "STABLE";
    }

    private String vehicleName(Vehicle vehicle) {
        return vehicle.getPlateNumber() + " • " + vehicle.getMake() + " " + vehicle.getModel();
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

    private int sanitizeLimit(Integer limit) {
        if (limit == null) {
            return 10;
        }
        return Math.min(Math.max(limit, 1), 50);
    }
}
