package com.fleetwise.fuellog;

import com.fleetwise.fuellog.dto.FuelLogResponse;
import com.fleetwise.fuellog.dto.FuelLogStatsResponse;
import com.fleetwise.fuellog.dto.FuelLogUpsertRequest;
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
@RequestMapping("/api/fuel-logs")
@RequiredArgsConstructor
public class FuelLogController {

    private final FuelLogService fuelLogService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','FLEET_MANAGER','DRIVER')")
    public List<FuelLogResponse> getFuelLogs(Authentication authentication,
            @RequestParam(required = false) UUID vehicleId,
            @RequestParam(required = false) UUID driverId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return fuelLogService.getFuelLogs(authentication.getName(), vehicleId, driverId, startDate, endDate);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN','FLEET_MANAGER','DRIVER')")
    public FuelLogResponse createFuelLog(Authentication authentication,
            @Valid @RequestBody FuelLogUpsertRequest request) {
        return fuelLogService.createFuelLog(authentication.getName(), request);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','FLEET_MANAGER','DRIVER')")
    public FuelLogResponse getFuelLogById(Authentication authentication, @PathVariable UUID id) {
        return fuelLogService.getFuelLogById(authentication.getName(), id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteFuelLog(@PathVariable UUID id) {
        fuelLogService.deleteFuelLog(id);
    }

    @GetMapping("/stats")
    @PreAuthorize("hasAnyRole('ADMIN','FLEET_MANAGER','DRIVER')")
    public FuelLogStatsResponse getFuelLogStats(Authentication authentication,
            @RequestParam(required = false) UUID vehicleId,
            @RequestParam(required = false) UUID driverId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return fuelLogService.getFuelLogStats(authentication.getName(), vehicleId, driverId, startDate, endDate);
    }
}
