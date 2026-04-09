package com.fleetwise.dashboard;

import com.fleetwise.dashboard.dto.CostTrendPointResponse;
import com.fleetwise.dashboard.dto.DashboardSummaryResponse;
import com.fleetwise.dashboard.dto.TopDriverResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/summary")
    @PreAuthorize("hasAnyRole('ADMIN','FLEET_MANAGER')")
    public DashboardSummaryResponse getSummary() {
        return dashboardService.getSummary();
    }

    @GetMapping("/top-drivers")
    @PreAuthorize("hasAnyRole('ADMIN','FLEET_MANAGER')")
    public List<TopDriverResponse> getTopDrivers() {
        return dashboardService.getTopDrivers();
    }

    @GetMapping("/cost-trend")
    @PreAuthorize("hasAnyRole('ADMIN','FLEET_MANAGER')")
    public List<CostTrendPointResponse> getCostTrend() {
        return dashboardService.getCostTrend();
    }
}
