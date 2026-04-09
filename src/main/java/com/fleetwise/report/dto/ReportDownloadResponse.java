package com.fleetwise.report.dto;

import java.time.Instant;
import java.util.UUID;

public record ReportDownloadResponse(
        UUID reportId,
        String filePath,
        Instant generatedAt,
        String note) {
}
