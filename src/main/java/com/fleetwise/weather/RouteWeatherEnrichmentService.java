package com.fleetwise.weather;

import com.fleetwise.route.RouteLog;
import com.fleetwise.route.RouteLogRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class RouteWeatherEnrichmentService {

    private final RouteLogRepository routeLogRepository;
    private final WeatherApiClient weatherApiClient;

    @Async
    @Transactional
    public void enrichRouteWeatherAsync(UUID routeLogId, double originLat, double originLng, LocalDate tripDate) {
        try {
            WeatherApiClient.WeatherSnapshot snapshot = weatherApiClient
                    .fetchTripWeather(originLat, originLng, tripDate)
                    .orElse(null);
            if (snapshot == null) {
                return;
            }

            RouteLog routeLog = routeLogRepository.findById(routeLogId)
                    .orElseThrow(() -> new EntityNotFoundException("Route log not found"));

            routeLog.setWeatherCondition(snapshot.weatherCondition());
            if (snapshot.temperatureCelsius() != null) {
                routeLog.setTemperatureCelsius(BigDecimal.valueOf(snapshot.temperatureCelsius())
                        .setScale(2, RoundingMode.HALF_UP));
            }

            routeLogRepository.save(routeLog);
        } catch (Exception ex) {
            log.debug("Skipping weather enrichment for routeLogId={}", routeLogId, ex);
        }
    }
}
