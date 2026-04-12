package com.fleetwise.fuelprice;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class FuelPricePhScraper {

    private static final Pattern PRICE_PATTERN = Pattern.compile("(\\d{2,3}\\.\\d{1,2})");
    private static final ZoneId MANILA_ZONE = ZoneId.of("Asia/Manila");

    private final RestTemplate restTemplate;
    private final String sourceUrl;

    public FuelPricePhScraper(
            RestTemplateBuilder restTemplateBuilder,
            @Value("${fuel-prices.source.url:https://fuelprice.ph}") String sourceUrl) {
        this.restTemplate = restTemplateBuilder
                .setConnectTimeout(Duration.ofSeconds(6))
                .setReadTimeout(Duration.ofSeconds(10))
                .build();
        this.sourceUrl = sourceUrl;
    }

    public List<FuelPriceScrapedEntry> fetchLatestPrices() {
        String html = restTemplate.getForObject(sourceUrl, String.class);
        if (html == null || html.isBlank()) {
            throw new IllegalStateException("Fuel price source returned an empty response");
        }

        Document document = Jsoup.parse(html);
        Map<String, FuelPriceScrapedEntry> deduped = new LinkedHashMap<>();
        LocalDate effectiveDate = LocalDate.now(MANILA_ZONE);

        for (Element row : document.select("tr, li, p, div")) {
            String text = row.text();
            if (text == null || text.isBlank()) {
                continue;
            }

            Optional<FuelPriceType> fuelType = resolveFuelType(text);
            Optional<BigDecimal> price = extractPrice(text);
            if (fuelType.isEmpty() || price.isEmpty()) {
                continue;
            }

            String brand = resolveBrand(text).orElse("DOE Average");
            String key = brand + "|" + fuelType.get().name();
            deduped.putIfAbsent(key, new FuelPriceScrapedEntry(
                    fuelType.get(),
                    price.get(),
                    brand,
                    effectiveDate,
                    "DOE Weekly Advisory via fuelprice.ph"));
        }

        List<FuelPriceScrapedEntry> results = new ArrayList<>(deduped.values());
        if (results.isEmpty()) {
            throw new IllegalStateException("Unable to parse fuel prices from fuelprice.ph");
        }

        return results;
    }

    private Optional<FuelPriceType> resolveFuelType(String text) {
        String normalized = text.toUpperCase(Locale.ROOT);

        if (normalized.contains("DIESEL") && normalized.contains("PLUS")) {
            return Optional.of(FuelPriceType.DIESEL_PLUS);
        }
        if (normalized.contains("DIESEL") && !normalized.contains("BIODIESEL")) {
            return Optional.of(FuelPriceType.DIESEL);
        }
        if (normalized.contains("95") || normalized.contains("PREMIUM")) {
            return Optional.of(FuelPriceType.GASOLINE_95);
        }
        if (normalized.contains("91") || normalized.contains("REGULAR") || normalized.contains("UNLEADED")) {
            return Optional.of(FuelPriceType.GASOLINE_91);
        }

        return Optional.empty();
    }

    private Optional<String> resolveBrand(String text) {
        String normalized = text.toUpperCase(Locale.ROOT);
        if (normalized.contains("SHELL")) {
            return Optional.of("Shell");
        }
        if (normalized.contains("PETRON")) {
            return Optional.of("Petron");
        }
        if (normalized.contains("CALTEX")) {
            return Optional.of("Caltex");
        }
        if (normalized.contains("SEAOIL")) {
            return Optional.of("Seaoil");
        }
        if (normalized.contains("PTT")) {
            return Optional.of("PTT");
        }
        return Optional.empty();
    }

    private Optional<BigDecimal> extractPrice(String text) {
        Matcher matcher = PRICE_PATTERN.matcher(text);
        if (!matcher.find()) {
            return Optional.empty();
        }
        try {
            BigDecimal value = new BigDecimal(matcher.group(1));
            if (value.compareTo(BigDecimal.ZERO) <= 0) {
                return Optional.empty();
            }
            return Optional.of(value.setScale(2, RoundingMode.HALF_UP));
        } catch (NumberFormatException ex) {
            return Optional.empty();
        }
    }
}
