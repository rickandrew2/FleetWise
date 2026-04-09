package com.fleetwise.report;

import com.fleetwise.alert.AlertRepository;
import com.fleetwise.fuellog.FuelLog;
import com.fleetwise.fuellog.FuelLogRepository;
import com.fleetwise.route.RouteLog;
import com.fleetwise.route.RouteLogRepository;
import com.fleetwise.report.dto.GenerateReportRequest;
import com.fleetwise.report.dto.ReportJobResponse;
import com.fleetwise.vehicle.VehicleRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
@Slf4j
@RequiredArgsConstructor
public class ReportService {

    private static final DateTimeFormatter FILE_TS_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss")
            .withZone(ZoneOffset.UTC);

    private final ReportJobRepository reportJobRepository;
    private final VehicleRepository vehicleRepository;
    private final FuelLogRepository fuelLogRepository;
    private final RouteLogRepository routeLogRepository;
    private final AlertRepository alertRepository;
    private final PdfReportGenerator pdfReportGenerator;
    private final ExcelReportGenerator excelReportGenerator;

    @Value("${reports.output-path:target/reports-output}")
    private String reportsOutputPath;

    @Transactional(readOnly = true)
    public List<ReportJobResponse> getReports() {
        return reportJobRepository.findAll().stream()
                .sorted(Comparator.comparing(ReportJob::getCreatedAt).reversed())
                .map(this::toResponse)
                .toList();
    }

    public ReportJobResponse generateReport(GenerateReportRequest request) {
        ReportJob reportJob = createPendingJob(request.reportType());
        return toResponse(generateArtifacts(reportJob));
    }

    public ReportJobResponse generateScheduledReport(ReportType reportType) {
        ReportJob reportJob = new ReportJob();
        reportJob.setReportType(reportType);
        reportJob.setStatus(ReportStatus.PENDING);
        ReportJob saved = reportJobRepository.save(reportJob);

        ReportJob completed = generateArtifacts(saved);
        return toResponse(completed);
    }

    @Transactional(readOnly = true)
    public DownloadableReport getDownloadableReport(UUID id) {
        ReportJob reportJob = reportJobRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Report not found"));

        if (reportJob.getStatus() != ReportStatus.COMPLETED || reportJob.getFilePath() == null) {
            throw new IllegalArgumentException("Report is not ready for download");
        }

        Path filePath = Paths.get(reportJob.getFilePath()).toAbsolutePath().normalize();
        Resource resource = new FileSystemResource(filePath);

        if (!resource.exists() || !resource.isReadable()) {
            throw new IllegalArgumentException("Report file is not available for download");
        }

        return new DownloadableReport(resource, filePath.getFileName().toString());
    }

    private ReportJob createPendingJob(ReportType reportType) {
        ReportJob reportJob = new ReportJob();
        reportJob.setReportType(reportType);
        reportJob.setStatus(ReportStatus.PENDING);
        return reportJobRepository.save(reportJob);
    }

    private ReportJob generateArtifacts(ReportJob reportJob) {
        try {
            reportJob.setStatus(ReportStatus.RUNNING);
            reportJobRepository.save(reportJob);

            Instant generatedAt = Instant.now();
            ReportSnapshot snapshot = buildSnapshot(reportJob.getReportType(), generatedAt);
            Path zipPath = generateZipArtifact(reportJob.getId(), snapshot);

            reportJob.setStatus(ReportStatus.COMPLETED);
            reportJob.setGeneratedAt(generatedAt);
            reportJob.setFilePath(zipPath.toString());
            return reportJobRepository.save(reportJob);
        } catch (Exception ex) {
            log.error("Report generation failed for reportJobId={}", reportJob.getId(), ex);
            reportJob.setStatus(ReportStatus.FAILED);
            reportJob.setGeneratedAt(Instant.now());
            reportJob.setFilePath(null);
            reportJobRepository.save(reportJob);
            throw new IllegalStateException("Report generation failed", ex);
        }
    }

    private ReportSnapshot buildSnapshot(ReportType reportType, Instant generatedAt) {
        long totalVehicles = vehicleRepository.count();
        BigDecimal totalFuelCost = fuelLogRepository.findAll().stream()
                .map(FuelLog::getTotalCost)
                .filter(value -> value != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);

        List<BigDecimal> efficiencies = routeLogRepository.findAll().stream()
                .map(RouteLog::getEfficiencyScore)
                .filter(value -> value != null)
                .toList();

        Double averageEfficiencyScore = null;
        if (!efficiencies.isEmpty()) {
            BigDecimal totalEfficiency = efficiencies.stream()
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            averageEfficiencyScore = totalEfficiency
                    .divide(BigDecimal.valueOf(efficiencies.size()), 2, RoundingMode.HALF_UP)
                    .doubleValue();
        }

        long activeAlertsCount = alertRepository.countUnread(null);

        List<ReportSnapshot.TopInefficientRoute> topRoutes = routeLogRepository
                .findTopInefficient(null, PageRequest.of(0, 5))
                .stream()
                .map(routeLog -> new ReportSnapshot.TopInefficientRoute(
                        routeLog.getId(),
                        routeLog.getVehicleId(),
                        routeLog.getDriverId(),
                        routeLog.getEfficiencyScore() == null ? null : routeLog.getEfficiencyScore().doubleValue()))
                .toList();

        return new ReportSnapshot(
                reportType,
                generatedAt,
                totalVehicles,
                totalFuelCost,
                averageEfficiencyScore,
                activeAlertsCount,
                topRoutes);
    }

    private Path generateZipArtifact(UUID reportJobId, ReportSnapshot snapshot) throws IOException {
        Path outputRoot = Paths.get(reportsOutputPath).toAbsolutePath().normalize();
        Files.createDirectories(outputRoot);

        Path reportJobDir = outputRoot.resolve(reportJobId.toString());
        Files.createDirectories(reportJobDir);

        String reportTypePart = snapshot.reportType().name().toLowerCase();
        String timestampPart = FILE_TS_FORMAT.format(snapshot.generatedAt());
        String baseName = reportTypePart + "-fleetwise-" + timestampPart;

        Path pdfPath = reportJobDir.resolve(baseName + ".pdf");
        Path excelPath = reportJobDir.resolve(baseName + ".xlsx");

        pdfReportGenerator.generate(pdfPath, snapshot);
        excelReportGenerator.generate(excelPath, snapshot);

        Path zipPath = outputRoot.resolve(baseName + ".zip");
        try (ZipOutputStream zipOutputStream = new ZipOutputStream(Files.newOutputStream(zipPath))) {
            addFileToZip(zipOutputStream, pdfPath, pdfPath.getFileName().toString());
            addFileToZip(zipOutputStream, excelPath, excelPath.getFileName().toString());
        }

        return zipPath;
    }

    private void addFileToZip(ZipOutputStream zipOutputStream, Path sourceFile, String entryName) throws IOException {
        zipOutputStream.putNextEntry(new ZipEntry(entryName));
        try (InputStream inputStream = Files.newInputStream(sourceFile)) {
            inputStream.transferTo(zipOutputStream);
        }
        zipOutputStream.closeEntry();
    }

    private ReportJobResponse toResponse(ReportJob reportJob) {
        return new ReportJobResponse(
                reportJob.getId(),
                reportJob.getReportType(),
                reportJob.getStatus(),
                reportJob.getFilePath(),
                reportJob.getGeneratedAt(),
                reportJob.getCreatedAt());
    }

    public record DownloadableReport(Resource resource, String filename) {
    }
}
