package com.fleetwise.report.dto;

import com.fleetwise.report.ReportType;
import jakarta.validation.constraints.NotNull;

public record GenerateReportRequest(@NotNull ReportType reportType) {
}
