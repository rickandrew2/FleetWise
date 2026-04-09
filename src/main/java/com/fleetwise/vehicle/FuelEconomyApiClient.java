package com.fleetwise.vehicle;

import com.fleetwise.vehicle.dto.EpaVehicleOption;
import com.fleetwise.vehicle.dto.FuelEconomyVehicleData;

import java.util.List;
import java.util.Optional;

public interface FuelEconomyApiClient {

    List<EpaVehicleOption> lookupVehicleOptions(int year, String make, String model);

    Optional<FuelEconomyVehicleData> fetchVehicleData(int epaVehicleId);
}