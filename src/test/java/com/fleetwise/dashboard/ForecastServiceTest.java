package com.fleetwise.dashboard;

import com.fleetwise.dashboard.dto.DashboardForecastResponse;
import com.fleetwise.fuelprice.FuelPriceHistory;
import com.fleetwise.fuelprice.FuelPriceHistoryRepository;
import com.fleetwise.fuelprice.FuelPriceType;
import com.fleetwise.fuellog.FuelLog;
import com.fleetwise.fuellog.FuelLogRepository;
import com.fleetwise.vehicle.Vehicle;
import com.fleetwise.vehicle.VehicleRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ForecastServiceTest {

    @Mock
    private FuelLogRepository fuelLogRepository;

    @Mock
    private VehicleRepository vehicleRepository;

    @Mock
    private FuelPriceHistoryRepository fuelPriceHistoryRepository;

    @InjectMocks
    private ForecastService forecastService;

    @Test
    void shouldForecastNextMonthCostWithHighConfidence() {
        UUID dieselVehicleId = UUID.randomUUID();
        UUID gasolineVehicleId = UUID.randomUUID();

        Vehicle dieselVehicle = new Vehicle();
        dieselVehicle.setId(dieselVehicleId);
        dieselVehicle.setFuelType("DIESEL");

        Vehicle gasolineVehicle = new Vehicle();
        gasolineVehicle.setId(gasolineVehicleId);
        gasolineVehicle.setFuelType("GASOLINE_91");

        LocalDate now = LocalDate.now();
        FuelLog dieselMonth1 = fuelLog(dieselVehicleId, now.minusMonths(1), "100.00");
        FuelLog dieselMonth2 = fuelLog(dieselVehicleId, now.minusMonths(2), "100.00");
        FuelLog dieselMonth3 = fuelLog(dieselVehicleId, now.minusMonths(3), "100.00");
        FuelLog gasolineMonth1 = fuelLog(gasolineVehicleId, now.minusMonths(1), "50.00");
        FuelLog gasolineMonth2 = fuelLog(gasolineVehicleId, now.minusMonths(2), "50.00");
        FuelLog gasolineMonth3 = fuelLog(gasolineVehicleId, now.minusMonths(3), "50.00");

        when(fuelLogRepository.findAll()).thenReturn(List.of(
                dieselMonth1,
                dieselMonth2,
                dieselMonth3,
                gasolineMonth1,
                gasolineMonth2,
                gasolineMonth3));

        when(vehicleRepository.findAllById(any())).thenReturn(List.of(dieselVehicle, gasolineVehicle));

        LocalDate currentPriceDate = now.minusDays(1);
        LocalDate previousPriceDate = now.minusMonths(3);

        when(fuelPriceHistoryRepository.findLatestEffectiveDateByFuelType(FuelPriceType.DIESEL))
                .thenReturn(Optional.of(currentPriceDate));
        when(fuelPriceHistoryRepository.findLatestEffectiveDateByFuelType(FuelPriceType.GASOLINE_91))
                .thenReturn(Optional.of(currentPriceDate));
        when(fuelPriceHistoryRepository.findLatestEffectiveDateByFuelTypeOnOrBefore(eq(FuelPriceType.DIESEL), any()))
                .thenReturn(Optional.of(previousPriceDate));
        when(fuelPriceHistoryRepository.findLatestEffectiveDateByFuelTypeOnOrBefore(eq(FuelPriceType.GASOLINE_91), any()))
                .thenReturn(Optional.of(previousPriceDate));

        when(fuelPriceHistoryRepository.findByFuelTypeAndEffectiveDateOrderByBrandAsc(FuelPriceType.DIESEL, currentPriceDate))
                .thenReturn(List.of(fuelPrice(FuelPriceType.DIESEL, currentPriceDate, "70.00")));
        when(fuelPriceHistoryRepository.findByFuelTypeAndEffectiveDateOrderByBrandAsc(FuelPriceType.DIESEL, previousPriceDate))
                .thenReturn(List.of(fuelPrice(FuelPriceType.DIESEL, previousPriceDate, "65.00")));
        when(fuelPriceHistoryRepository.findByFuelTypeAndEffectiveDateOrderByBrandAsc(FuelPriceType.GASOLINE_91, currentPriceDate))
                .thenReturn(List.of(fuelPrice(FuelPriceType.GASOLINE_91, currentPriceDate, "75.00")));
        when(fuelPriceHistoryRepository.findByFuelTypeAndEffectiveDateOrderByBrandAsc(FuelPriceType.GASOLINE_91, previousPriceDate))
                .thenReturn(List.of(fuelPrice(FuelPriceType.GASOLINE_91, previousPriceDate, "70.00")));

        DashboardForecastResponse response = forecastService.forecastNextMonthCost();

        assertThat(response.confidenceLevel()).isEqualTo("HIGH");
        assertThat(response.basedOnMonths()).isEqualTo(3);
        assertThat(response.avgLitersPerMonth()).isEqualTo(150.0);
        assertThat(response.projectedCost()).isGreaterThan(11000.0);
    }

    private FuelLog fuelLog(UUID vehicleId, LocalDate date, String liters) {
        FuelLog fuelLog = new FuelLog();
        fuelLog.setVehicleId(vehicleId);
        fuelLog.setLogDate(date);
        fuelLog.setLitersFilled(new BigDecimal(liters));
        return fuelLog;
    }

    private FuelPriceHistory fuelPrice(FuelPriceType fuelType, LocalDate date, String price) {
        FuelPriceHistory history = new FuelPriceHistory();
        history.setFuelType(fuelType);
        history.setEffectiveDate(date);
        history.setPricePerLiter(new BigDecimal(price));
        history.setBrand("Test");
        history.setSource("Test");
        return history;
    }
}
