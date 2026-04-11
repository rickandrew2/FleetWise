package com.fleetwise.route;

import com.fleetwise.route.dto.RouteLogResponse;
import com.fleetwise.route.dto.RouteLogStatsResponse;
import com.fleetwise.route.dto.RouteLogUpsertRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/routes")
@RequiredArgsConstructor
public class RouteLogController {

    private final RouteLogService routeLogService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','FLEET_MANAGER','DRIVER')")
    public List<RouteLogResponse> getRouteLogs(Authentication authentication,
            @RequestParam(required = false) UUID vehicleId,
            @RequestParam(required = false) UUID driverId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return routeLogService.getRouteLogs(authentication.getName(), vehicleId, driverId, startDate, endDate);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN','FLEET_MANAGER','DRIVER')")
    public RouteLogResponse createRouteLog(Authentication authentication,
            @Valid @RequestBody RouteLogUpsertRequest request) {
        return routeLogService.createRouteLog(authentication.getName(), request);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','FLEET_MANAGER','DRIVER')")
    public RouteLogResponse getRouteLogById(Authentication authentication, @PathVariable UUID id) {
        return routeLogService.getRouteLogById(authentication.getName(), id);
    }

    @GetMapping("/top-inefficient")
    @PreAuthorize("hasAnyRole('ADMIN','FLEET_MANAGER','DRIVER')")
    public List<RouteLogResponse> getTopInefficientRoutes(Authentication authentication,
            @RequestParam(required = false) UUID driverId,
            @RequestParam(required = false) Integer limit) {
        return routeLogService.getTopInefficientRoutes(authentication.getName(), driverId, limit);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteRouteLog(@PathVariable UUID id) {
        routeLogService.deleteRouteLog(id);
    }

    @GetMapping("/stats")
    @PreAuthorize("hasAnyRole('ADMIN','FLEET_MANAGER','DRIVER')")
    public RouteLogStatsResponse getRouteLogStats(Authentication authentication,
            @RequestParam(required = false) UUID vehicleId,
            @RequestParam(required = false) UUID driverId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return routeLogService.getRouteLogStats(authentication.getName(), vehicleId, driverId, startDate, endDate);
    }
}
