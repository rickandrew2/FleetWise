package com.fleetwise.alert;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AlertRepository extends JpaRepository<Alert, UUID> {

    @Query("""
            select a from Alert a
            where (:alertType is null or a.alertType = :alertType)
              and (:vehicleId is null or a.vehicleId = :vehicleId)
              and (:driverId is null or a.driverId = :driverId)
              and (:isRead is null or a.isRead = :isRead)
            order by a.triggeredAt desc
            """)
    List<Alert> findFiltered(
            @Param("alertType") AlertType alertType,
            @Param("vehicleId") UUID vehicleId,
            @Param("driverId") UUID driverId,
            @Param("isRead") Boolean isRead);

    Optional<Alert> findByIdAndDriverId(UUID id, UUID driverId);

    @Query("""
            select count(a) from Alert a
            where a.isRead = false
              and (:driverId is null or a.driverId = :driverId)
            """)
    long countUnread(@Param("driverId") UUID driverId);
}
