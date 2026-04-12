package com.fleetwise.common;

import com.fleetwise.alert.Alert;
import com.fleetwise.alert.AlertRepository;
import com.fleetwise.alert.AlertService;
import com.fleetwise.alert.AlertType;
import com.fleetwise.fuellog.FuelLog;
import com.fleetwise.fuellog.FuelLogRepository;
import com.fleetwise.route.RouteLog;
import com.fleetwise.route.RouteLogRepository;
import com.fleetwise.user.User;
import com.fleetwise.user.UserRepository;
import com.fleetwise.user.UserRole;
import com.fleetwise.vehicle.Vehicle;
import com.fleetwise.vehicle.VehicleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

@Component
@Profile("dev")
@RequiredArgsConstructor
@Slf4j
public class DevDataLoader implements ApplicationRunner {

    private static final String DEFAULT_PASSWORD = "password123";
    private static final BigDecimal MPG_TO_KM_PER_LITER = new BigDecimal("1.60934");

    private final UserRepository userRepository;
    private final VehicleRepository vehicleRepository;
    private final FuelLogRepository fuelLogRepository;
    private final RouteLogRepository routeLogRepository;
    private final AlertRepository alertRepository;
    private final AlertService alertService;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        Map<String, User> usersByEmail = seedUsers();
        Map<String, Vehicle> vehiclesByPlate = seedVehicles(usersByEmail);
        Map<UUID, BigDecimal> latestOdometerByVehicle = seedFuelLogs(usersByEmail, vehiclesByPlate);
        seedRouteLogs(usersByEmail, vehiclesByPlate);
        seedMaintenanceAlerts(latestOdometerByVehicle, vehiclesByPlate);

        log.info("Dev seed complete. users={}, vehicles={}, fuelLogs={}, routeLogs={}, alerts={}",
                userRepository.count(),
                vehicleRepository.count(),
                fuelLogRepository.count(),
                routeLogRepository.count(),
                alertRepository.count());
    }

    private Map<String, User> seedUsers() {
        upsertUser("Fleet Admin", "admin@fleetwise.local", UserRole.ADMIN);
        upsertUser("Fleet Manager", "manager@fleetwise.local", UserRole.FLEET_MANAGER);
        upsertUser("Driver One", "driver1@fleetwise.local", UserRole.DRIVER);
        upsertUser("Driver Two", "driver2@fleetwise.local", UserRole.DRIVER);

        Map<String, User> usersByEmail = new HashMap<>();
        userRepository.findAll().forEach(user -> usersByEmail.put(user.getEmail().toLowerCase(Locale.ROOT), user));
        return usersByEmail;
    }

    private User upsertUser(String name, String email, UserRole role) {
        return userRepository.findByEmailIgnoreCase(email)
                .orElseGet(() -> {
                    User user = new User();
                    user.setName(name);
                    user.setEmail(email.toLowerCase(Locale.ROOT));
                    user.setPasswordHash(passwordEncoder.encode(DEFAULT_PASSWORD));
                    user.setRole(role);
                    return userRepository.save(user);
                });
    }

    private Map<String, Vehicle> seedVehicles(Map<String, User> usersByEmail) {
        User driver1 = usersByEmail.get("driver1@fleetwise.local");
        User driver2 = usersByEmail.get("driver2@fleetwise.local");

        List<VehicleSeedSpec> specs = List.of(
                new VehicleSeedSpec("ABC-1234", "Toyota", "Hilux", 2020, "DIESEL", 80.0, 46269, 22.0,
                        driver1 == null ? null : driver1.getId()),
                new VehicleSeedSpec("XYZ-5678", "Mitsubishi", "L300", 2019, "DIESEL", 65.0, 43572, 18.0,
                        driver1 == null ? null : driver1.getId()),
                new VehicleSeedSpec("DEF-9012", "Isuzu", "D-Max", 2021, "DIESEL", 76.0, 47891, 24.0,
                        driver2 == null ? null : driver2.getId()),
                new VehicleSeedSpec("GHI-3456", "Ford", "Ranger", 2020, "DIESEL", 80.0, 46187, 23.0,
                        driver2 == null ? null : driver2.getId()),
                new VehicleSeedSpec("JKL-7890", "Toyota", "Hi-Ace", 2018, "GASOLINE_91", 70.0, 40761, 20.0, null));

        for (VehicleSeedSpec spec : specs) {
            if (vehicleRepository.existsByPlateNumberIgnoreCase(spec.plateNumber())) {
                continue;
            }

            Vehicle vehicle = new Vehicle();
            vehicle.setPlateNumber(spec.plateNumber());
            vehicle.setMake(spec.make());
            vehicle.setModel(spec.model());
            vehicle.setYear(spec.year());
            vehicle.setFuelType(spec.fuelType());
            vehicle.setTankCapacityLiters(toDecimal(spec.tankCapacityLiters(), 2));
            vehicle.setEpaVehicleId(spec.epaVehicleId());
            vehicle.setCombinedMpg(toDecimal(spec.combinedMpg(), 2));
            vehicle.setCityMpg(toDecimal(spec.combinedMpg() - 2.0, 2));
            vehicle.setHighwayMpg(toDecimal(spec.combinedMpg() + 3.0, 2));
            vehicle.setAssignedDriverId(spec.assignedDriverId());
            vehicleRepository.save(vehicle);
        }

        Map<String, Vehicle> vehiclesByPlate = new HashMap<>();
        vehicleRepository.findAll()
                .forEach(vehicle -> vehiclesByPlate.put(vehicle.getPlateNumber().toUpperCase(Locale.ROOT), vehicle));
        return vehiclesByPlate;
    }

    private Map<UUID, BigDecimal> seedFuelLogs(Map<String, User> usersByEmail, Map<String, Vehicle> vehiclesByPlate) {
        User manager = usersByEmail.get("manager@fleetwise.local");
        User driver1 = usersByEmail.get("driver1@fleetwise.local");
        User driver2 = usersByEmail.get("driver2@fleetwise.local");

        Map<String, UUID> driverByPlate = Map.of(
                "ABC-1234", driver1 == null ? null : driver1.getId(),
                "XYZ-5678", driver1 == null ? null : driver1.getId(),
                "DEF-9012", driver2 == null ? null : driver2.getId(),
                "GHI-3456", driver2 == null ? null : driver2.getId(),
                "JKL-7890", manager == null ? null : manager.getId());

        List<StationSpec> stations = List.of(
                new StationSpec("Petron SLEX", 14.290611, 121.085170),
                new StationSpec("Shell Alabang", 14.417850, 121.041990),
                new StationSpec("Caltex Batangas", 13.756500, 121.058300),
                new StationSpec("PTT Lipa", 13.941100, 121.163100),
                new StationSpec("Seaoil Sto. Tomas", 14.104300, 121.171500));

        List<LocalDate> schedule = List.of(
                LocalDate.of(2025, 11, 6),
                LocalDate.of(2025, 12, 4),
                LocalDate.of(2026, 1, 8),
                LocalDate.of(2026, 2, 6),
                LocalDate.of(2026, 3, 6),
                LocalDate.of(2026, 4, 2),
                LocalDate.of(2026, 4, 10));

        Map<String, Integer> baseOdometerByPlate = Map.of(
                "ABC-1234", 39800,
                "XYZ-5678", 39600,
                "DEF-9012", 28600,
                "GHI-3456", 33500,
                "JKL-7890", 19800);

        Set<String> existingKeys = new HashSet<>();
        for (FuelLog existing : fuelLogRepository.findAll()) {
            existingKeys.add(fuelLogKey(existing.getVehicleId(), existing.getLogDate()));
        }

        Random random = new Random(20260411L);
        Map<UUID, BigDecimal> latestOdometerByVehicle = new HashMap<>();

        List<Vehicle> seedVehicles = new ArrayList<>(List.of(
                vehiclesByPlate.get("ABC-1234"),
                vehiclesByPlate.get("XYZ-5678"),
                vehiclesByPlate.get("DEF-9012"),
                vehiclesByPlate.get("GHI-3456"),
                vehiclesByPlate.get("JKL-7890")));
        seedVehicles.removeIf(vehicle -> vehicle == null);

        int inserted = 0;
        for (Vehicle vehicle : seedVehicles) {
            String plate = vehicle.getPlateNumber().toUpperCase(Locale.ROOT);
            UUID driverId = driverByPlate.get(plate);
            if (driverId == null) {
                continue;
            }

            int odometer = baseOdometerByPlate.getOrDefault(plate, 20000);
            for (int i = 0; i < schedule.size(); i++) {
                LocalDate logDate = schedule.get(i).plusDays((long) i * 2);
                String key = fuelLogKey(vehicle.getId(), logDate);
                int odometerIncrement = ("ABC-1234".equals(plate) || "XYZ-5678".equals(plate))
                        ? 1500 + random.nextInt(501)
                        : 800 + random.nextInt(1201);
                odometer += odometerIncrement;
                latestOdometerByVehicle.put(vehicle.getId(), toDecimal((double) odometer, 2));

                if (existingKeys.contains(key)) {
                    continue;
                }

                int stationIndex = Math.floorMod(i + plate.hashCode(), stations.size());
                StationSpec station = stations.get(stationIndex);
                double maxLiters = Math.min(65.0, toDouble(vehicle.getTankCapacityLiters()) * 0.92);
                double liters = randomBetween(random, 30.0, maxLiters);
                boolean isDiesel = "DIESEL".equalsIgnoreCase(vehicle.getFuelType())
                        || "DIESEL_PLUS".equalsIgnoreCase(vehicle.getFuelType());
                double price = isDiesel
                        ? randomBetween(random, 62.0, 72.0)
                        : randomBetween(random, 68.0, 78.0);

                FuelLog logEntry = new FuelLog();
                logEntry.setVehicleId(vehicle.getId());
                logEntry.setDriverId(driverId);
                logEntry.setLogDate(logDate);
                logEntry.setOdometerReadingKm(toDecimal((double) odometer, 2));
                logEntry.setLitersFilled(toDecimal(liters, 2));
                logEntry.setPricePerLiter(toDecimal(price, 2));
                logEntry.setTotalCost(toDecimal(liters * price, 2));
                logEntry.setStationName(station.name());
                logEntry.setStationLat(toDecimal(station.lat(), 7));
                logEntry.setStationLng(toDecimal(station.lng(), 7));
                logEntry.setNotes("Seeded dev data entry");

                FuelLog saved = fuelLogRepository.save(logEntry);
                alertService.checkFuelLog(saved);
                inserted++;
                existingKeys.add(key);
            }
        }

        log.info("Fuel log seed inserted {} new records", inserted);
        return latestOdometerByVehicle;
    }

    private void seedRouteLogs(Map<String, User> usersByEmail, Map<String, Vehicle> vehiclesByPlate) {
        User manager = usersByEmail.get("manager@fleetwise.local");
        User driver1 = usersByEmail.get("driver1@fleetwise.local");
        User driver2 = usersByEmail.get("driver2@fleetwise.local");

        List<RoutePairSpec> routePairs = List.of(
                new RoutePairSpec("Lipa City", 13.9411, 121.1631, "Batangas Port", 13.7565, 121.0583, 35.0, 55),
                new RoutePairSpec("Sto. Tomas", 14.1043, 121.1715, "Alabang", 14.4195, 121.0433, 42.0, 70),
                new RoutePairSpec("Batangas City", 13.7565, 121.0583, "Manila", 14.5995, 120.9842, 110.0, 170),
                new RoutePairSpec("Lipa City", 13.9411, 121.1631, "Lucena", 13.9317, 121.6170, 68.0, 110),
                new RoutePairSpec("Calamba", 14.2117, 121.1653, "Batangas City", 13.7565, 121.0583, 55.0, 90));

        List<Vehicle> routeVehicles = new ArrayList<>(List.of(
                vehiclesByPlate.get("ABC-1234"),
                vehiclesByPlate.get("XYZ-5678"),
                vehiclesByPlate.get("DEF-9012"),
                vehiclesByPlate.get("GHI-3456"),
                vehiclesByPlate.get("JKL-7890")));
        routeVehicles.removeIf(vehicle -> vehicle == null);

        Map<String, UUID> driverByPlate = Map.of(
                "ABC-1234", driver1 == null ? null : driver1.getId(),
                "XYZ-5678", driver1 == null ? null : driver1.getId(),
                "DEF-9012", driver2 == null ? null : driver2.getId(),
                "GHI-3456", driver2 == null ? null : driver2.getId(),
                "JKL-7890", manager == null ? null : manager.getId());

        Set<String> overconsumptionIndexes = Set.of("2", "8", "15");

        Set<String> existingKeys = new HashSet<>();
        for (RouteLog existing : routeLogRepository.findAll()) {
            existingKeys.add(routeLogKey(existing.getVehicleId(), existing.getTripDate(),
                    existing.getOriginLabel(), existing.getDestinationLabel()));
        }

        Random random = new Random(20260412L);
        LocalDate startDate = LocalDate.of(2026, 2, 3);
        int inserted = 0;

        for (int i = 0; i < 20; i++) {
            RoutePairSpec pair = routePairs.get(i % routePairs.size());
            Vehicle vehicle = routeVehicles.get(i % routeVehicles.size());
            UUID driverId = driverByPlate.get(vehicle.getPlateNumber());
            if (driverId == null) {
                continue;
            }

            LocalDate tripDate = startDate.plusDays((long) i * 3);
            String key = routeLogKey(vehicle.getId(), tripDate, pair.originLabel(), pair.destinationLabel());
            if (existingKeys.contains(key)) {
                continue;
            }

            BigDecimal distanceKm = toDecimal(pair.distanceKm(), 2);
            BigDecimal expectedFuel = calculateExpectedFuel(distanceKm, vehicle.getCombinedMpg());
            if (expectedFuel == null) {
                continue;
            }

            double multiplier = overconsumptionIndexes.contains(String.valueOf(i))
                    ? randomBetween(random, 1.31, 1.39)
                    : randomBetween(random, 1.05, 1.10);
            BigDecimal actualFuel = toDecimal(expectedFuel.doubleValue() * multiplier, 2);
            BigDecimal efficiencyScore = expectedFuel.compareTo(BigDecimal.ZERO) == 0
                    ? null
                    : actualFuel.divide(expectedFuel, 2, RoundingMode.HALF_UP);

            RouteLog routeLog = new RouteLog();
            routeLog.setVehicleId(vehicle.getId());
            routeLog.setDriverId(driverId);
            routeLog.setTripDate(tripDate);
            routeLog.setOriginLabel(pair.originLabel());
            routeLog.setOriginLat(toDecimal(pair.originLat(), 7));
            routeLog.setOriginLng(toDecimal(pair.originLng(), 7));
            routeLog.setDestinationLabel(pair.destinationLabel());
            routeLog.setDestinationLat(toDecimal(pair.destinationLat(), 7));
            routeLog.setDestinationLng(toDecimal(pair.destinationLng(), 7));
            routeLog.setDistanceKm(distanceKm);
            routeLog.setEstimatedDurationMin(pair.estimatedDurationMin());
            routeLog.setExpectedFuelLiters(expectedFuel);
            routeLog.setActualFuelUsedLiters(actualFuel);
            routeLog.setEfficiencyScore(efficiencyScore);

            RouteLog savedRoute = routeLogRepository.save(routeLog);
            alertService.checkRouteLog(savedRoute);
            inserted++;
            existingKeys.add(key);
        }

        log.info("Route log seed inserted {} new records", inserted);
    }

    private void seedMaintenanceAlerts(Map<UUID, BigDecimal> latestOdometerByVehicle,
            Map<String, Vehicle> vehiclesByPlate) {
        List<UUID> seededMaintenanceVehicles = alertRepository.findAll().stream()
                .filter(alert -> alert.getAlertType() == AlertType.MAINTENANCE_DUE)
                .map(Alert::getVehicleId)
                .filter(id -> id != null)
                .distinct()
                .toList();

        List<Map.Entry<UUID, BigDecimal>> candidates = latestOdometerByVehicle.entrySet().stream()
                .filter(entry -> entry.getValue() != null && entry.getValue().compareTo(new BigDecimal("50000")) > 0)
                .sorted(Comparator.comparing(Map.Entry<UUID, BigDecimal>::getValue).reversed())
                .toList();

        int created = 0;
        for (Map.Entry<UUID, BigDecimal> candidate : candidates) {
            if (created >= 2) {
                break;
            }
            if (seededMaintenanceVehicles.contains(candidate.getKey())) {
                continue;
            }

            Vehicle vehicle = vehiclesByPlate.values().stream()
                    .filter(item -> item.getId().equals(candidate.getKey()))
                    .findFirst()
                    .orElse(null);
            if (vehicle == null) {
                continue;
            }

            Alert alert = new Alert();
            alert.setVehicleId(vehicle.getId());
            alert.setDriverId(vehicle.getAssignedDriverId());
            alert.setAlertType(AlertType.MAINTENANCE_DUE);
            alert.setMessage("Vehicle exceeded 50,000 km since last service window");
            alert.setThresholdValue(new BigDecimal("50000.00"));
            alert.setActualValue(candidate.getValue().setScale(2, RoundingMode.HALF_UP));
            alert.setIsRead(false);
            alertRepository.save(alert);
            created++;
        }

        log.info("Maintenance alert seed inserted {} new records", created);
    }

    private BigDecimal calculateExpectedFuel(BigDecimal distanceKm, BigDecimal combinedMpg) {
        if (distanceKm == null || combinedMpg == null || combinedMpg.compareTo(BigDecimal.ZERO) <= 0) {
            return null;
        }

        BigDecimal divisor = combinedMpg.multiply(MPG_TO_KM_PER_LITER);
        if (divisor.compareTo(BigDecimal.ZERO) <= 0) {
            return null;
        }

        return distanceKm.divide(divisor, 2, RoundingMode.HALF_UP);
    }

    private String fuelLogKey(UUID vehicleId, LocalDate logDate) {
        return vehicleId + "|" + logDate;
    }

    private String routeLogKey(UUID vehicleId, LocalDate tripDate, String originLabel, String destinationLabel) {
        return vehicleId + "|" + tripDate + "|" + originLabel + "|" + destinationLabel;
    }

    private BigDecimal toDecimal(double value, int scale) {
        return BigDecimal.valueOf(value).setScale(scale, RoundingMode.HALF_UP);
    }

    private double toDouble(BigDecimal value) {
        return value == null ? 0.0 : value.doubleValue();
    }

    private double randomBetween(Random random, double min, double max) {
        return min + ((max - min) * random.nextDouble());
    }

    private record VehicleSeedSpec(
            String plateNumber,
            String make,
            String model,
            int year,
            String fuelType,
            double tankCapacityLiters,
            int epaVehicleId,
            double combinedMpg,
            UUID assignedDriverId) {
    }

    private record StationSpec(String name, double lat, double lng) {
    }

    private record RoutePairSpec(
            String originLabel,
            double originLat,
            double originLng,
            String destinationLabel,
            double destinationLat,
            double destinationLng,
            double distanceKm,
            int estimatedDurationMin) {
    }
}
