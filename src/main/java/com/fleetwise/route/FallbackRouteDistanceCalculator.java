package com.fleetwise.route;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Component
@Primary
@RequiredArgsConstructor
public class FallbackRouteDistanceCalculator implements RouteDistanceCalculator {

    private final GraphHopperApiClient graphHopperApiClient;
    private final HaversineRouteDistanceCalculator haversineRouteDistanceCalculator;

    @Override
    public DistanceResult calculate(double originLat, double originLng, double destinationLat, double destinationLng) {
        return graphHopperApiClient.calculate(originLat, originLng, destinationLat, destinationLng)
                .orElseGet(() -> haversineRouteDistanceCalculator.calculate(
                        originLat,
                        originLng,
                        destinationLat,
                        destinationLng));
    }
}