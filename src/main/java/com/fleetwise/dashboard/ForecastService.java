package com.fleetwise.dashboard;

import com.fleetwise.dashboard.dto.DashboardForecastResponse;
import com.fleetwise.fuelprice.FuelPriceHistory;
import com.fleetwise.fuelprice.FuelPriceHistoryRepository;
import com.fleetwise.fuelprice.FuelPriceType;
import com.fleetwise.fuellog.FuelLog;
import com.fleetwise.fuellog.FuelLogRepository;
import com.fleetwise.vehicle.Vehicle;
import com.fleetwise.vehicle.VehicleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ForecastService {

    private static final ZoneId MANILA_ZONE = ZoneId.of("Asia/Manila");

    private final FuelLogRepository fuelLogRepository;
    private final VehicleRepository vehicleRepository;
    private final FuelPriceHistoryRepository fuelPriceHistoryRepository;

    @Transactional(readOnly = true)
    public DashboardForecastResponse forecastNextMonthCost() {
        LocalDate now = LocalDate.now(MANILA_ZONE);
        LocalDate threeMonthsAgo = now.minusMonths(3);

        List<FuelLog> recentLogs = fuelLogRepository.findAll().stream()
                .filter(log -> log.getLogDate() != null)
                .filter(log -> !log.getLogDate().isBefore(threeMonthsAgo))
                .toList();

        Set<YearMonth> monthsWithData = recentLogs.stream()
                .map(FuelLog::getLogDate)
                .map(YearMonth::from)
                .collect(Collectors.toSet());

        int basedOnMonths = monthsWithData.size();
        if (recentLogs.isEmpty()) {
            return new DashboardForecastResponse(0.0, "LOW", 0, 0.0, 0.0);
        }

        Map<UUID, Vehicle> vehiclesById = vehicleRepository.findAllById(
                recentLogs.stream().map(FuelLog::getVehicleId).collect(Collectors.toSet())).stream()
                .collect(Collectors.toMap(Vehicle::getId, vehicle -> vehicle));

        BigDecimal dieselLiters = BigDecimal.ZERO;
        BigDecimal gasolineLiters = BigDecimal.ZERO;
        for (FuelLog log : recentLogs) {
            if (log.getLitersFilled() == null) {
                continue;
            }

            Vehicle vehicle = vehiclesById.get(log.getVehicleId());
            FuelPriceType mappedFuelType = vehicle == null
                    ? null
                    : FuelPriceType.fromVehicleFuelType(vehicle.getFuelType()).orElse(null);
            if (mappedFuelType == null) {
                continue;
            }

            if (mappedFuelType == FuelPriceType.DIESEL || mappedFuelType == FuelPriceType.DIESEL_PLUS) {
                dieselLiters = dieselLiters.add(log.getLitersFilled());
            } else {
                gasolineLiters = gasolineLiters.add(log.getLitersFilled());
            }
        }

        int divisor = Math.max(basedOnMonths, 1);
        BigDecimal avgDieselLitersPerMonth = dieselLiters.divide(BigDecimal.valueOf(divisor), 4, RoundingMode.HALF_UP);
        BigDecimal avgGasolineLitersPerMonth = gasolineLiters.divide(BigDecimal.valueOf(divisor), 4, RoundingMode.HALF_UP);
        BigDecimal avgLitersPerMonth = avgDieselLitersPerMonth.add(avgGasolineLitersPerMonth);

        LocalDate historicalAnchorDate = now.minusMonths(3);

        BigDecimal currentDieselPrice = getCurrentPrice(FuelPriceType.DIESEL).orElse(BigDecimal.ZERO);
        BigDecimal currentGasolinePrice = getCurrentGasolinePrice().orElse(BigDecimal.ZERO);

        BigDecimal dieselPriceThreeMonthsAgo = getHistoricalPrice(FuelPriceType.DIESEL, historicalAnchorDate)
                .orElse(currentDieselPrice);
        BigDecimal gasolinePriceThreeMonthsAgo = getHistoricalGasolinePrice(historicalAnchorDate)
                .orElse(currentGasolinePrice);

        BigDecimal dieselTrendFactor = calculateTrendFactor(currentDieselPrice, dieselPriceThreeMonthsAgo);
        BigDecimal gasolineTrendFactor = calculateTrendFactor(currentGasolinePrice, gasolinePriceThreeMonthsAgo);

        BigDecimal projectedDieselCost = avgDieselLitersPerMonth
                .multiply(currentDieselPrice)
                .multiply(BigDecimal.ONE.add(dieselTrendFactor));
        BigDecimal projectedGasolineCost = avgGasolineLitersPerMonth
                .multiply(currentGasolinePrice)
                .multiply(BigDecimal.ONE.add(gasolineTrendFactor));

        BigDecimal projectedCost = projectedDieselCost.add(projectedGasolineCost).setScale(2, RoundingMode.HALF_UP);

        BigDecimal weightedPriceUsed = BigDecimal.ZERO;
        if (avgLitersPerMonth.compareTo(BigDecimal.ZERO) > 0) {
            weightedPriceUsed = avgDieselLitersPerMonth.multiply(currentDieselPrice)
                    .add(avgGasolineLitersPerMonth.multiply(currentGasolinePrice))
                    .divide(avgLitersPerMonth, 2, RoundingMode.HALF_UP);
        }

        return new DashboardForecastResponse(
                projectedCost.doubleValue(),
                confidenceLevel(basedOnMonths),
                basedOnMonths,
                weightedPriceUsed.doubleValue(),
                avgLitersPerMonth.setScale(2, RoundingMode.HALF_UP).doubleValue());
    }

    private String confidenceLevel(int basedOnMonths) {
        if (basedOnMonths >= 3) {
            return "HIGH";
        }
        if (basedOnMonths == 2) {
            return "MEDIUM";
        }
        return "LOW";
    }

    private BigDecimal calculateTrendFactor(BigDecimal currentPrice, BigDecimal previousPrice) {
        if (currentPrice == null || previousPrice == null || previousPrice.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }

        return currentPrice.subtract(previousPrice)
                .divide(previousPrice, 6, RoundingMode.HALF_UP);
    }

    private java.util.Optional<BigDecimal> getCurrentPrice(FuelPriceType fuelPriceType) {
        return fuelPriceHistoryRepository.findLatestEffectiveDateByFuelType(fuelPriceType)
                .flatMap(latestDate -> averagePriceForDate(fuelPriceType, latestDate));
    }

    private java.util.Optional<BigDecimal> getHistoricalPrice(FuelPriceType fuelPriceType, LocalDate anchorDate) {
        return fuelPriceHistoryRepository.findLatestEffectiveDateByFuelTypeOnOrBefore(fuelPriceType, anchorDate)
                .flatMap(date -> averagePriceForDate(fuelPriceType, date));
    }

    private java.util.Optional<BigDecimal> getCurrentGasolinePrice() {
        java.util.Optional<BigDecimal> gas91 = getCurrentPrice(FuelPriceType.GASOLINE_91);
        if (gas91.isPresent()) {
            return gas91;
        }
        return getCurrentPrice(FuelPriceType.GASOLINE_95);
    }

    private java.util.Optional<BigDecimal> getHistoricalGasolinePrice(LocalDate anchorDate) {
        java.util.Optional<BigDecimal> gas91 = getHistoricalPrice(FuelPriceType.GASOLINE_91, anchorDate);
        if (gas91.isPresent()) {
            return gas91;
        }
        return getHistoricalPrice(FuelPriceType.GASOLINE_95, anchorDate);
    }

    private java.util.Optional<BigDecimal> averagePriceForDate(FuelPriceType fuelPriceType, LocalDate effectiveDate) {
        List<FuelPriceHistory> rows = fuelPriceHistoryRepository
                .findByFuelTypeAndEffectiveDateOrderByBrandAsc(fuelPriceType, effectiveDate);
        if (rows.isEmpty()) {
            return java.util.Optional.empty();
        }

        BigDecimal total = rows.stream()
                .map(FuelPriceHistory::getPricePerLiter)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return java.util.Optional.of(total.divide(BigDecimal.valueOf(rows.size()), 4, RoundingMode.HALF_UP));
    }
}
