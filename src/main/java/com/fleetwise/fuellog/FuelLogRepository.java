package com.fleetwise.fuellog;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FuelLogRepository extends JpaRepository<FuelLog, UUID> {

    @Query("""
            select f from FuelLog f
            where (:vehicleId is null or f.vehicleId = :vehicleId)
              and (:driverId is null or f.driverId = :driverId)
              and (:startDate is null or f.logDate >= :startDate)
              and (:endDate is null or f.logDate <= :endDate)
            order by f.logDate desc, f.createdAt desc
            """)
    List<FuelLog> findFiltered(
            @Param("vehicleId") UUID vehicleId,
            @Param("driverId") UUID driverId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    Optional<FuelLog> findByIdAndDriverId(UUID id, UUID driverId);

    @Query("""
            select coalesce(sum(f.litersFilled), 0) from FuelLog f
            where f.logDate = :logDate
              and f.vehicleId = :vehicleId
              and f.driverId = :driverId
            """)
    BigDecimal sumLitersFilledForTrip(
            @Param("logDate") LocalDate logDate,
            @Param("vehicleId") UUID vehicleId,
            @Param("driverId") UUID driverId);
}
