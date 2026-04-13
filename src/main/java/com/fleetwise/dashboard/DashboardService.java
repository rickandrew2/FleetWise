package com.fleetwise.dashboard;

import com.fleetwise.alert.AlertRepository;
import com.fleetwise.dashboard.dto.CostTrendPointResponse;
import com.fleetwise.dashboard.dto.DashboardForecastResponse;
import com.fleetwise.dashboard.dto.DashboardSummaryResponse;
import com.fleetwise.dashboard.dto.TopDriverResponse;
import com.fleetwise.fuellog.FuelLog;
import com.fleetwise.fuellog.FuelLogRepository;
import com.fleetwise.route.RouteLog;
import com.fleetwise.route.RouteLogRepository;
import com.fleetwise.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardService {

        private static final DateTimeFormatter MONTH_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM");

        private final FuelLogRepository fuelLogRepository;
        private final RouteLogRepository routeLogRepository;
        private final AlertRepository alertRepository;
        private final UserRepository userRepository;
        private final ForecastService forecastService;

        @Transactional(readOnly = true)
        public DashboardSummaryResponse getSummary() {
                LocalDate now = LocalDate.now();
                YearMonth currentMonth = YearMonth.from(now);

                BigDecimal monthToDateCost = fuelLogRepository.findAll().stream()
                                .filter(log -> log.getLogDate() != null)
                                .filter(log -> YearMonth.from(log.getLogDate()).equals(currentMonth))
                                .map(FuelLog::getTotalCost)
                                .filter(value -> value != null)
                                .reduce(BigDecimal.ZERO, BigDecimal::add)
                                .setScale(2, RoundingMode.HALF_UP);

                List<BigDecimal> efficiencyScores = routeLogRepository.findAll().stream()
                                .map(RouteLog::getEfficiencyScore)
                                .filter(value -> value != null)
                                .toList();

                Double fleetEfficiencyScore = null;
                if (!efficiencyScores.isEmpty()) {
                        BigDecimal total = efficiencyScores.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
                        fleetEfficiencyScore = total
                                        .divide(BigDecimal.valueOf(efficiencyScores.size()), 2, RoundingMode.HALF_UP)
                                        .doubleValue();
                }

                long activeAlertsCount = alertRepository.countUnread(null);

                return new DashboardSummaryResponse(monthToDateCost.doubleValue(), fleetEfficiencyScore,
                                activeAlertsCount);
        }

        @Transactional(readOnly = true)
        public List<TopDriverResponse> getTopDrivers() {
                Map<UUID, List<RouteLog>> byDriver = routeLogRepository.findAll().stream()
                                .filter(routeLog -> routeLog.getDriverId() != null)
                                .filter(routeLog -> routeLog.getEfficiencyScore() != null)
                                .collect(Collectors.groupingBy(RouteLog::getDriverId));

                return byDriver.entrySet().stream()
                                .map(entry -> {
                                        UUID driverId = entry.getKey();
                                        List<RouteLog> driverRoutes = entry.getValue();
                                        BigDecimal avgEfficiency = driverRoutes.stream()
                                                        .map(RouteLog::getEfficiencyScore)
                                                        .reduce(BigDecimal.ZERO, BigDecimal::add)
                                                        .divide(BigDecimal.valueOf(driverRoutes.size()), 2,
                                                                        RoundingMode.HALF_UP);

                                        String driverName = userRepository.findById(driverId)
                                                        .map(user -> user.getName() != null ? user.getName()
                                                                        : user.getEmail())
                                                        .orElse("Unknown Driver");

                                        return new TopDriverResponse(driverId, driverName, avgEfficiency.doubleValue(),
                                                        driverRoutes.size());
                                })
                                .sorted(Comparator.comparing(TopDriverResponse::averageEfficiencyScore))
                                .limit(5)
                                .toList();
        }

        @Transactional(readOnly = true)
        public List<CostTrendPointResponse> getCostTrend() {
                LocalDate now = LocalDate.now();
                YearMonth endMonth = YearMonth.from(now);
                YearMonth startMonth = endMonth.minusMonths(5);

                Map<YearMonth, BigDecimal> monthlyTotals = fuelLogRepository.findAll().stream()
                                .filter(log -> log.getLogDate() != null)
                                .filter(log -> log.getTotalCost() != null)
                                .collect(Collectors.groupingBy(
                                                log -> YearMonth.from(log.getLogDate()),
                                                Collectors.reducing(BigDecimal.ZERO, FuelLog::getTotalCost,
                                                                BigDecimal::add)));

                List<CostTrendPointResponse> trend = new ArrayList<>();
                YearMonth cursor = startMonth;
                while (!cursor.isAfter(endMonth)) {
                        BigDecimal total = monthlyTotals.getOrDefault(cursor, BigDecimal.ZERO).setScale(2,
                                        RoundingMode.HALF_UP);
                        trend.add(new CostTrendPointResponse(cursor.format(MONTH_FORMAT), total.doubleValue()));
                        cursor = cursor.plusMonths(1);
                }

                return trend;
        }

        @Transactional(readOnly = true)
        public DashboardForecastResponse getForecast() {
                return forecastService.forecastNextMonthCost();
        }
}
