package com.fleetwise.fuelprice;

import com.fleetwise.fuelprice.dto.FuelPriceCurrentResponse;
import com.fleetwise.fuelprice.dto.FuelPriceHistoryPointResponse;
import com.fleetwise.fuelprice.dto.FuelPriceManualUpdateRequest;
import com.fleetwise.fuelprice.dto.FuelPriceUpdateResultResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Fuel Prices")
@RestController
@RequestMapping("/api/fuel-prices")
@RequiredArgsConstructor
public class FuelPriceController {

    private final FuelPriceService fuelPriceService;

    @Operation(summary = "Get current prices for all fuel types")
    @GetMapping("/current")
    @PreAuthorize("hasAnyRole('ADMIN','FLEET_MANAGER','DRIVER')")
    public List<FuelPriceCurrentResponse> getCurrentPrices() {
        return fuelPriceService.getCurrentPrices();
    }

    @Operation(summary = "Get current price for one fuel type")
    @GetMapping("/current/{fuelType}")
    @PreAuthorize("hasAnyRole('ADMIN','FLEET_MANAGER','DRIVER')")
    public FuelPriceCurrentResponse getCurrentPriceByType(@PathVariable FuelPriceType fuelType) {
        return fuelPriceService.getCurrentPrice(fuelType);
    }

    @Operation(summary = "Get fuel price history for the last 6 months")
    @GetMapping("/history")
    @PreAuthorize("hasAnyRole('ADMIN','FLEET_MANAGER','DRIVER')")
    public List<FuelPriceHistoryPointResponse> getHistory() {
        return fuelPriceService.getHistoryLastSixMonths();
    }

    @Operation(summary = "Manually update current fuel prices")
    @PostMapping("/manual-update")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    public FuelPriceUpdateResultResponse manualUpdate(@Valid @RequestBody FuelPriceManualUpdateRequest request) {
        return fuelPriceService.manualUpdate(request);
    }

    @Operation(summary = "Trigger fuel price update from source")
    @PostMapping("/trigger-update")
    @PreAuthorize("hasRole('ADMIN')")
    public FuelPriceUpdateResultResponse triggerUpdate() {
        return fuelPriceService.triggerUpdate();
    }
}
