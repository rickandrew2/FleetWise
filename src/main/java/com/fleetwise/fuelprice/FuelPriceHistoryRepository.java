package com.fleetwise.fuelprice;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FuelPriceHistoryRepository extends JpaRepository<FuelPriceHistory, UUID> {

    @Query("select max(f.effectiveDate) from FuelPriceHistory f")
    Optional<LocalDate> findLatestEffectiveDate();

    @Query("select max(f.effectiveDate) from FuelPriceHistory f where f.fuelType = :fuelType")
    Optional<LocalDate> findLatestEffectiveDateByFuelType(@Param("fuelType") FuelPriceType fuelType);

    @Query("""
            select max(f.effectiveDate) from FuelPriceHistory f
            where f.fuelType = :fuelType and f.effectiveDate <= :effectiveDate
            """)
    Optional<LocalDate> findLatestEffectiveDateByFuelTypeOnOrBefore(
            @Param("fuelType") FuelPriceType fuelType,
            @Param("effectiveDate") LocalDate effectiveDate);

    List<FuelPriceHistory> findByFuelTypeAndEffectiveDateOrderByBrandAsc(FuelPriceType fuelType,
            LocalDate effectiveDate);

    @Query("""
            select f from FuelPriceHistory f
            where f.effectiveDate >= :startDate
            order by f.effectiveDate asc, f.fuelType asc, f.brand asc
            """)
    List<FuelPriceHistory> findHistorySince(@Param("startDate") LocalDate startDate);
}
