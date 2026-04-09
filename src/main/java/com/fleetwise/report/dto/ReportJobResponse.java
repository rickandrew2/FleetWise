package com.fleetwise.report.dto;

import com.fleetwise.report.ReportStatus;
import com.fleetwise.report.ReportType;

import java.time.Instant;
import java.util.UUID;

public record ReportJobResponse(
        UUID id,
        ReportType reportType,
        ReportStatus status,
        String filePath,
        Instant generatedAt,
        Instant createdAt) {
}
