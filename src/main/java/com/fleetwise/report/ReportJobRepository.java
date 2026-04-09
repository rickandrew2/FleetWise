package com.fleetwise.report;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ReportJobRepository extends JpaRepository<ReportJob, UUID> {
}
