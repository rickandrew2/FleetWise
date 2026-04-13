package com.fleetwise.weather;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.Duration;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class WeatherApiClient {

    private final RestTemplateBuilder restTemplateBuilder;

    @Value("${weather.open-meteo.base-url:https://api.open-meteo.com/v1/forecast}")
    private String baseUrl;

    public Optional<WeatherSnapshot> fetchTripWeather(double latitude, double longitude, LocalDate tripDate) {
        RestTemplate restTemplate = restTemplateBuilder
                .setConnectTimeout(Duration.ofSeconds(5))
                .setReadTimeout(Duration.ofSeconds(8))
                .build();

        String uri = UriComponentsBuilder.fromUriString(baseUrl)
                .queryParam("latitude", latitude)
                .queryParam("longitude", longitude)
                .queryParam("daily", "weathercode,temperature_2m_max")
                .queryParam("timezone", "Asia/Manila")
                .queryParam("start_date", tripDate)
                .queryParam("end_date", tripDate)
                .build(true)
                .toUriString();

        try {
            ResponseEntity<OpenMeteoResponse> responseEntity =
                    restTemplate.getForEntity(uri, OpenMeteoResponse.class);
            OpenMeteoResponse response = responseEntity.getBody();
            if (response == null || response.daily() == null) {
                return Optional.empty();
            }

            OpenMeteoDaily daily = response.daily();
            if (daily.weathercode() == null || daily.weathercode().isEmpty()) {
                return Optional.empty();
            }

            Integer weatherCode = daily.weathercode().getFirst();
            Double temperature = null;
            if (daily.temperature2mMax() != null && !daily.temperature2mMax().isEmpty()) {
                temperature = daily.temperature2mMax().getFirst();
            }

            return Optional.of(new WeatherSnapshot(mapWeatherCode(weatherCode), weatherCode, temperature));
        } catch (RestClientException ex) {
            return Optional.empty();
        }
    }

    private String mapWeatherCode(Integer weatherCode) {
        if (weatherCode == null) {
            return "Unknown";
        }

        if (weatherCode == 0) {
            return "Clear Sky";
        }

        if (weatherCode >= 1 && weatherCode <= 3) {
            return "Partly Cloudy";
        }

        if (weatherCode == 45 || weatherCode == 48) {
            return "Foggy";
        }

        if (weatherCode >= 51 && weatherCode <= 67) {
            return "Rainy";
        }

        if (weatherCode >= 80 && weatherCode <= 82) {
            return "Rain Showers";
        }

        if (weatherCode >= 95 && weatherCode <= 99) {
            return "Thunderstorm";
        }

        return "Unknown";
    }

    public record WeatherSnapshot(String weatherCondition, Integer weatherCode, Double temperatureCelsius) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record OpenMeteoResponse(OpenMeteoDaily daily) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record OpenMeteoDaily(
            List<Integer> weathercode,
            @JsonProperty("temperature_2m_max")
            List<Double> temperature2mMax) {
    }
}
