package com.fleetwise.route;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RouteLogRepository extends JpaRepository<RouteLog, UUID> {

    @Query("""
            select r from RouteLog r
            where (:vehicleId is null or r.vehicleId = :vehicleId)
              and (:driverId is null or r.driverId = :driverId)
              and (:startDate is null or r.tripDate >= :startDate)
              and (:endDate is null or r.tripDate <= :endDate)
            order by r.tripDate desc, r.createdAt desc
            """)
    List<RouteLog> findFiltered(
            @Param("vehicleId") UUID vehicleId,
            @Param("driverId") UUID driverId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    Optional<RouteLog> findByIdAndDriverId(UUID id, UUID driverId);
}
