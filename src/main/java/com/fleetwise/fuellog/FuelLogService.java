package com.fleetwise.fuellog;

import com.fleetwise.fuellog.dto.FuelLogResponse;
import com.fleetwise.fuellog.dto.FuelLogStatsResponse;
import com.fleetwise.fuellog.dto.FuelLogUpsertRequest;
import com.fleetwise.user.User;
import com.fleetwise.user.UserRepository;
import com.fleetwise.user.UserRole;
import com.fleetwise.vehicle.VehicleRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FuelLogService {

    private final FuelLogRepository fuelLogRepository;
    private final VehicleRepository vehicleRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<FuelLogResponse> getFuelLogs(String currentEmail,
            UUID vehicleId,
            UUID driverId,
            LocalDate startDate,
            LocalDate endDate) {
        User currentUser = getCurrentUser(currentEmail);
        UUID effectiveDriverId = resolveDriverFilter(currentUser, driverId);

        return fuelLogRepository.findFiltered(vehicleId, effectiveDriverId, startDate, endDate)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public FuelLogResponse getFuelLogById(String currentEmail, UUID id) {
        User currentUser = getCurrentUser(currentEmail);

        FuelLog fuelLog;
        if (currentUser.getRole() == UserRole.DRIVER) {
            fuelLog = fuelLogRepository.findByIdAndDriverId(id, currentUser.getId())
                    .orElseThrow(() -> new EntityNotFoundException("Fuel log not found"));
        } else {
            fuelLog = fuelLogRepository.findById(id)
                    .orElseThrow(() -> new EntityNotFoundException("Fuel log not found"));
        }

        return toResponse(fuelLog);
    }

    @Transactional
    public FuelLogResponse createFuelLog(String currentEmail, FuelLogUpsertRequest request) {
        User currentUser = getCurrentUser(currentEmail);

        if (!vehicleRepository.existsById(request.vehicleId())) {
            throw new EntityNotFoundException("Vehicle not found");
        }

        UUID effectiveDriverId = resolveDriverForCreate(currentUser, request.driverId());
        if (!userRepository.existsById(effectiveDriverId)) {
            throw new EntityNotFoundException("Driver not found");
        }

        BigDecimal litersFilled = toMoney(request.litersFilled());
        BigDecimal pricePerLiter = toMoney(request.pricePerLiter());
        BigDecimal totalCost = litersFilled.multiply(pricePerLiter).setScale(2, RoundingMode.HALF_UP);

        FuelLog fuelLog = new FuelLog();
        fuelLog.setVehicleId(request.vehicleId());
        fuelLog.setDriverId(effectiveDriverId);
        fuelLog.setLogDate(request.logDate());
        fuelLog.setOdometerReadingKm(toDecimal(request.odometerReadingKm(), 2));
        fuelLog.setLitersFilled(litersFilled);
        fuelLog.setPricePerLiter(pricePerLiter);
        fuelLog.setTotalCost(totalCost);
        fuelLog.setStationName(trimToNull(request.stationName()));
        fuelLog.setStationLat(toDecimal(request.stationLat(), 7));
        fuelLog.setStationLng(toDecimal(request.stationLng(), 7));
        fuelLog.setNotes(trimToNull(request.notes()));

        return toResponse(fuelLogRepository.save(fuelLog));
    }

    @Transactional
    public void deleteFuelLog(UUID id) {
        if (!fuelLogRepository.existsById(id)) {
            throw new EntityNotFoundException("Fuel log not found");
        }
        fuelLogRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public FuelLogStatsResponse getFuelLogStats(String currentEmail,
            UUID vehicleId,
            UUID driverId,
            LocalDate startDate,
            LocalDate endDate) {
        User currentUser = getCurrentUser(currentEmail);
        UUID effectiveDriverId = resolveDriverFilter(currentUser, driverId);

        List<FuelLog> logs = fuelLogRepository.findFiltered(vehicleId, effectiveDriverId, startDate, endDate);

        long totalLogs = logs.size();
        BigDecimal totalCost = logs.stream()
                .map(FuelLog::getTotalCost)
                .filter(value -> value != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);

        BigDecimal totalLiters = logs.stream()
                .map(FuelLog::getLitersFilled)
                .filter(value -> value != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal avgLiters = totalLogs == 0
                ? BigDecimal.ZERO
                : totalLiters.divide(BigDecimal.valueOf(totalLogs), 2, RoundingMode.HALF_UP);

        return new FuelLogStatsResponse(totalLogs, totalCost.doubleValue(), avgLiters.doubleValue());
    }

    private User getCurrentUser(String email) {
        return userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new EntityNotFoundException("Current user not found"));
    }

    private UUID resolveDriverFilter(User currentUser, UUID requestedDriverId) {
        if (currentUser.getRole() == UserRole.DRIVER) {
            if (requestedDriverId != null && !requestedDriverId.equals(currentUser.getId())) {
                throw new IllegalArgumentException("Drivers can only access their own fuel logs");
            }
            return currentUser.getId();
        }
        return requestedDriverId;
    }

    private UUID resolveDriverForCreate(User currentUser, UUID requestedDriverId) {
        if (currentUser.getRole() == UserRole.DRIVER) {
            if (requestedDriverId != null && !requestedDriverId.equals(currentUser.getId())) {
                throw new IllegalArgumentException("Drivers can only create their own fuel logs");
            }
            return currentUser.getId();
        }

        if (requestedDriverId == null) {
            return currentUser.getId();
        }
        return requestedDriverId;
    }

    private FuelLogResponse toResponse(FuelLog fuelLog) {
        return new FuelLogResponse(
                fuelLog.getId(),
                fuelLog.getVehicleId(),
                fuelLog.getDriverId(),
                fuelLog.getLogDate(),
                toDouble(fuelLog.getOdometerReadingKm()),
                toDouble(fuelLog.getLitersFilled()),
                toDouble(fuelLog.getPricePerLiter()),
                toDouble(fuelLog.getTotalCost()),
                fuelLog.getStationName(),
                toDouble(fuelLog.getStationLat()),
                toDouble(fuelLog.getStationLng()),
                fuelLog.getNotes(),
                fuelLog.getCreatedAt());
    }

    private BigDecimal toMoney(Double value) {
        return toDecimal(value, 2);
    }

    private BigDecimal toDecimal(Double value, int scale) {
        if (value == null) {
            return null;
        }
        return BigDecimal.valueOf(value).setScale(scale, RoundingMode.HALF_UP);
    }

    private Double toDouble(BigDecimal value) {
        return value == null ? null : value.doubleValue();
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
