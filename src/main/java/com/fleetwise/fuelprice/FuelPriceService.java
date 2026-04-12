package com.fleetwise.fuelprice;

import com.fleetwise.fuelprice.dto.FuelPriceCurrentResponse;
import com.fleetwise.fuelprice.dto.FuelPriceHistoryPointResponse;
import com.fleetwise.fuelprice.dto.FuelPriceManualEntryRequest;
import com.fleetwise.fuelprice.dto.FuelPriceManualUpdateRequest;
import com.fleetwise.fuelprice.dto.FuelPriceUpdateResultResponse;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class FuelPriceService {

    private static final ZoneId MANILA_ZONE = ZoneId.of("Asia/Manila");

    private final FuelPriceHistoryRepository fuelPriceHistoryRepository;
    private final FuelPricePhScraper fuelPricePhScraper;

    @Value("${fuel-prices.stale-after-days:8}")
    private int staleAfterDays;

    @Transactional(readOnly = true)
    public List<FuelPriceCurrentResponse> getCurrentPrices() {
        List<FuelPriceCurrentResponse> responses = new ArrayList<>();
        for (FuelPriceType fuelPriceType : FuelPriceType.values()) {
            fuelPriceHistoryRepository.findLatestEffectiveDateByFuelType(fuelPriceType)
                    .ifPresent(latestDate -> responses.add(buildCurrentResponse(fuelPriceType, latestDate)));
        }

        return responses.stream()
                .sorted(Comparator.comparing(FuelPriceCurrentResponse::fuelType))
                .toList();
    }

    @Transactional(readOnly = true)
    public FuelPriceCurrentResponse getCurrentPrice(FuelPriceType fuelType) {
        LocalDate latestDate = fuelPriceHistoryRepository.findLatestEffectiveDateByFuelType(fuelType)
                .orElseThrow(() -> new EntityNotFoundException("No fuel price data found for " + fuelType));
        return buildCurrentResponse(fuelType, latestDate);
    }

    @Transactional(readOnly = true)
    public List<FuelPriceHistoryPointResponse> getHistoryLastSixMonths() {
        LocalDate startDate = LocalDate.now(MANILA_ZONE).minusMonths(6);
        List<FuelPriceHistory> rows = fuelPriceHistoryRepository.findHistorySince(startDate);

        Map<FuelPriceType, Map<LocalDate, List<BigDecimal>>> grouped = new EnumMap<>(FuelPriceType.class);
        for (FuelPriceHistory row : rows) {
            grouped.computeIfAbsent(row.getFuelType(), ignored -> new LinkedHashMap<>())
                    .computeIfAbsent(row.getEffectiveDate(), ignored -> new ArrayList<>())
                    .add(row.getPricePerLiter());
        }

        List<FuelPriceHistoryPointResponse> points = new ArrayList<>();
        grouped.forEach((fuelType, byDate) -> byDate.forEach((effectiveDate, prices) -> {
            BigDecimal average = prices.stream()
                    .reduce(BigDecimal.ZERO, BigDecimal::add)
                    .divide(BigDecimal.valueOf(prices.size()), 2, RoundingMode.HALF_UP);
            points.add(new FuelPriceHistoryPointResponse(fuelType, effectiveDate, average.doubleValue()));
        }));

        return points.stream()
                .sorted(Comparator.comparing(FuelPriceHistoryPointResponse::effectiveDate)
                        .thenComparing(FuelPriceHistoryPointResponse::fuelType))
                .toList();
    }

    @Transactional
    public FuelPriceUpdateResultResponse manualUpdate(FuelPriceManualUpdateRequest request) {
        List<FuelPriceHistory> records = request.entries().stream()
                .map(entry -> toHistoryRow(request, entry))
                .toList();

        fuelPriceHistoryRepository.saveAll(records);

        return new FuelPriceUpdateResultResponse(
                records.size(),
                request.effectiveDate(),
                false,
                "Manual fuel price update saved successfully");
    }

    @Transactional
    public FuelPriceUpdateResultResponse triggerUpdate() {
        return runScheduledUpdate();
    }

    @Transactional
    public FuelPriceUpdateResultResponse runScheduledUpdate() {
        try {
            List<FuelPriceScrapedEntry> scrapedEntries = fuelPricePhScraper.fetchLatestPrices();
            List<FuelPriceHistory> rows = scrapedEntries.stream()
                    .map(this::toHistoryRow)
                    .toList();

            fuelPriceHistoryRepository.saveAll(rows);

            LocalDate effectiveDate = scrapedEntries.getFirst().effectiveDate();
            return new FuelPriceUpdateResultResponse(
                    rows.size(),
                    effectiveDate,
                    false,
                    "Fuel prices updated from fuelprice.ph");
        } catch (Exception ex) {
            log.warn("Fuel price scrape failed: {}", ex.getMessage());
            if (fuelPriceHistoryRepository.findLatestEffectiveDate().isEmpty()) {
                throw new IllegalStateException("Fuel price update failed and no fallback data is available", ex);
            }
            return new FuelPriceUpdateResultResponse(
                    0,
                    null,
                    true,
                    "Update failed. Serving last successful fuel prices.");
        }
    }

    private FuelPriceCurrentResponse buildCurrentResponse(FuelPriceType fuelType, LocalDate latestDate) {
        List<FuelPriceHistory> rows = fuelPriceHistoryRepository.findByFuelTypeAndEffectiveDateOrderByBrandAsc(fuelType,
                latestDate);
        if (rows.isEmpty()) {
            throw new EntityNotFoundException("No fuel price entries found for " + fuelType + " on " + latestDate);
        }

        BigDecimal averagePrice = rows.stream()
                .map(FuelPriceHistory::getPricePerLiter)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(rows.size()), 2, RoundingMode.HALF_UP);

        String source = rows.getFirst().getSource();
        boolean stale = latestDate.isBefore(LocalDate.now(MANILA_ZONE).minusDays(staleAfterDays));

        return new FuelPriceCurrentResponse(
                fuelType,
                averagePrice.doubleValue(),
                latestDate,
                source,
                stale);
    }

    private FuelPriceHistory toHistoryRow(FuelPriceScrapedEntry entry) {
        FuelPriceHistory history = new FuelPriceHistory();
        history.setFuelType(entry.fuelType());
        history.setPricePerLiter(entry.pricePerLiter().setScale(2, RoundingMode.HALF_UP));
        history.setBrand(cleanBrand(entry.brand()));
        history.setEffectiveDate(entry.effectiveDate());
        history.setSource(entry.source());
        return history;
    }

    private FuelPriceHistory toHistoryRow(FuelPriceManualUpdateRequest request, FuelPriceManualEntryRequest entry) {
        FuelPriceHistory history = new FuelPriceHistory();
        history.setFuelType(entry.fuelType());
        history.setPricePerLiter(entry.pricePerLiter().setScale(2, RoundingMode.HALF_UP));
        history.setBrand(cleanBrand(entry.brand()));
        history.setEffectiveDate(request.effectiveDate());
        history.setSource(request.source().trim());
        return history;
    }

    private String cleanBrand(String brand) {
        if (brand == null || brand.isBlank()) {
            return "DOE Average";
        }
        return brand.trim();
    }
}
