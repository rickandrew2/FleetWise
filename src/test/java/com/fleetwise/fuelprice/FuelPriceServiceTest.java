package com.fleetwise.fuelprice;

import com.fleetwise.fuelprice.dto.FuelPriceCurrentResponse;
import com.fleetwise.fuelprice.dto.FuelPriceUpdateResultResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FuelPriceServiceTest {

    @Mock
    private FuelPriceHistoryRepository fuelPriceHistoryRepository;

    @Mock
    private FuelPricePhScraper fuelPricePhScraper;

    @InjectMocks
    private FuelPriceService fuelPriceService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(fuelPriceService, "staleAfterDays", 8);
    }

    @Test
    void shouldAverageCurrentPriceAcrossBrands() {
        LocalDate effectiveDate = LocalDate.of(2026, 4, 7);

        when(fuelPriceHistoryRepository.findLatestEffectiveDateByFuelType(FuelPriceType.DIESEL))
                .thenReturn(Optional.of(effectiveDate));
        when(fuelPriceHistoryRepository.findByFuelTypeAndEffectiveDateOrderByBrandAsc(FuelPriceType.DIESEL,
                effectiveDate))
                .thenReturn(List.of(
                        history(FuelPriceType.DIESEL, "Shell", "70.00", effectiveDate),
                        history(FuelPriceType.DIESEL, "Petron", "72.00", effectiveDate)));

        FuelPriceCurrentResponse response = fuelPriceService.getCurrentPrice(FuelPriceType.DIESEL);

        assertThat(response.pricePerLiter()).isEqualTo(71.0);
        assertThat(response.fuelType()).isEqualTo(FuelPriceType.DIESEL);
        assertThat(response.effectiveDate()).isEqualTo(effectiveDate);
        assertThat(response.source()).isEqualTo("DOE Weekly Advisory");
    }

    @Test
    void shouldReturnFallbackResultWhenScrapeFailsAndDataExists() {
        doThrow(new IllegalStateException("Source unavailable")).when(fuelPricePhScraper).fetchLatestPrices();
        when(fuelPriceHistoryRepository.findLatestEffectiveDate()).thenReturn(Optional.of(LocalDate.of(2026, 4, 7)));

        FuelPriceUpdateResultResponse response = fuelPriceService.runScheduledUpdate();

        assertThat(response.fallbackUsed()).isTrue();
        assertThat(response.message()).contains("last successful fuel prices");
    }

    private FuelPriceHistory history(FuelPriceType fuelType, String brand, String price, LocalDate effectiveDate) {
        FuelPriceHistory history = new FuelPriceHistory();
        history.setFuelType(fuelType);
        history.setBrand(brand);
        history.setPricePerLiter(new BigDecimal(price));
        history.setEffectiveDate(effectiveDate);
        history.setSource("DOE Weekly Advisory");
        return history;
    }
}
