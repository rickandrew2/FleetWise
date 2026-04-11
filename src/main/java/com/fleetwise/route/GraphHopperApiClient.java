package com.fleetwise.route;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class GraphHopperApiClient {

    private final RestTemplateBuilder restTemplateBuilder;

    @Value("${graphhopper.api.base-url:https://graphhopper.com/api/1/route}")
    private String baseUrl;

    @Value("${graphhopper.api.key:}")
    private String apiKey;

    @Value("${graphhopper.api.vehicle:car}")
    private String vehicle;

    public Optional<RouteDistanceCalculator.DistanceResult> calculate(
            double originLat,
            double originLng,
            double destinationLat,
            double destinationLng) {
        if (apiKey == null || apiKey.isBlank()) {
            return Optional.empty();
        }

        RestTemplate restTemplate = restTemplateBuilder
                .setConnectTimeout(Duration.ofSeconds(5))
                .setReadTimeout(Duration.ofSeconds(8))
                .build();

        String uri = UriComponentsBuilder.fromUriString(baseUrl)
                .queryParam("point", originLat + "," + originLng)
                .queryParam("point", destinationLat + "," + destinationLng)
                .queryParam("vehicle", vehicle)
                .queryParam("key", apiKey)
                .build(true)
                .toUriString();

        try {
            ResponseEntity<GraphHopperRouteResponse> responseEntity =
                    restTemplate.getForEntity(uri, GraphHopperRouteResponse.class);
            GraphHopperRouteResponse response = responseEntity.getBody();
            if (response == null || response.paths() == null || response.paths().isEmpty()) {
                return Optional.empty();
            }

            GraphHopperPath firstPath = response.paths().getFirst();
            double distanceKm = firstPath.distance() / 1000.0;
            int estimatedDurationMin = distanceKm <= 0
                    ? 0
                    : Math.max(1, (int) Math.ceil(firstPath.time() / 60000.0));

            return Optional.of(new RouteDistanceCalculator.DistanceResult(distanceKm, estimatedDurationMin));
        } catch (RestClientException ex) {
            return Optional.empty();
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record GraphHopperRouteResponse(List<GraphHopperPath> paths) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record GraphHopperPath(double distance, long time) {
    }
}