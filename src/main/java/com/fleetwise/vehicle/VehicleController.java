package com.fleetwise.vehicle;

import com.fleetwise.vehicle.dto.EpaLookupRequest;
import com.fleetwise.vehicle.dto.EpaVehicleOption;
import com.fleetwise.vehicle.dto.VehicleResponse;
import com.fleetwise.vehicle.dto.VehicleUpsertRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/vehicles")
@RequiredArgsConstructor
public class VehicleController {

    private final VehicleService vehicleService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','FLEET_MANAGER')")
    public List<VehicleResponse> getAllVehicles() {
        return vehicleService.getAllVehicles();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN','FLEET_MANAGER')")
    public VehicleResponse createVehicle(@Valid @RequestBody VehicleUpsertRequest request) {
        return vehicleService.createVehicle(request);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','FLEET_MANAGER')")
    public VehicleResponse getVehicleById(@PathVariable UUID id) {
        return vehicleService.getVehicleById(id);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','FLEET_MANAGER')")
    public VehicleResponse updateVehicle(@PathVariable UUID id, @Valid @RequestBody VehicleUpsertRequest request) {
        return vehicleService.updateVehicle(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteVehicle(@PathVariable UUID id) {
        vehicleService.deleteVehicle(id);
    }

    @PostMapping("/lookup-epa")
    @PreAuthorize("hasAnyRole('ADMIN','FLEET_MANAGER')")
    public List<EpaVehicleOption> lookupEpa(@Valid @RequestBody EpaLookupRequest request) {
        return vehicleService.lookupEpaOptions(request);
    }
}