package com.fleetwise.route;

import com.fleetwise.route.dto.DriverEfficiencyProfileResponse;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/drivers")
@RequiredArgsConstructor
public class DriverEfficiencyController {

    private final RouteLogService routeLogService;

    @GetMapping("/{driverId}/efficiency-profile")
    @Operation(summary = "Get a driver's 30-day efficiency profile")
    @PreAuthorize("hasAnyRole('ADMIN','FLEET_MANAGER','DRIVER')")
    public DriverEfficiencyProfileResponse getDriverEfficiencyProfile(
            Authentication authentication,
            @PathVariable UUID driverId) {
        return routeLogService.getDriverEfficiencyProfile(authentication.getName(), driverId);
    }
}
