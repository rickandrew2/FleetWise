package com.fleetwise.fuelprice;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "fuel-prices.scheduler", name = "enabled", havingValue = "true", matchIfMissing = true)
public class FuelPriceUpdateJob {

    private final FuelPriceService fuelPriceService;

    @Scheduled(cron = "${fuel-prices.scheduler.cron:0 0 8 * * TUE}", zone = "${fuel-prices.scheduler.zone:Asia/Manila}")
    public void runTuesdayUpdate() {
        log.info("Running scheduled fuel price update");
        fuelPriceService.runScheduledUpdate();
    }
}
