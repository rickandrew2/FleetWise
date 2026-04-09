package com.fleetwise.report;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

@Component
public class ExcelReportGenerator {

    private static final DateTimeFormatter TS_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss 'UTC'")
            .withZone(ZoneOffset.UTC);

    public void generate(Path outputFile, ReportSnapshot snapshot) {
        try {
            Files.createDirectories(outputFile.getParent());
            try (Workbook workbook = new XSSFWorkbook();
                    OutputStream outputStream = Files.newOutputStream(outputFile)) {
                createSummarySheet(workbook, snapshot);
                createTopRoutesSheet(workbook, snapshot);
                workbook.write(outputStream);
            }
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to generate Excel report", ex);
        }
    }

    private void createSummarySheet(Workbook workbook, ReportSnapshot snapshot) {
        Sheet summarySheet = workbook.createSheet("Summary");
        int rowIndex = 0;
        rowIndex = addRow(summarySheet, rowIndex, "Report Type", snapshot.reportType().name());
        rowIndex = addRow(summarySheet, rowIndex, "Generated At", TS_FORMAT.format(snapshot.generatedAt()));
        rowIndex = addRow(summarySheet, rowIndex, "Total Vehicles", String.valueOf(snapshot.totalVehicles()));
        rowIndex = addRow(summarySheet, rowIndex, "Total Fuel Cost", toMoney(snapshot.totalFuelCost()));
        rowIndex = addRow(summarySheet, rowIndex, "Average Efficiency Score",
                snapshot.averageEfficiencyScore() == null ? "N/A" : snapshot.averageEfficiencyScore().toString());
        addRow(summarySheet, rowIndex, "Active Alerts", String.valueOf(snapshot.activeAlertsCount()));
        summarySheet.autoSizeColumn(0);
        summarySheet.autoSizeColumn(1);
    }

    private void createTopRoutesSheet(Workbook workbook, ReportSnapshot snapshot) {
        Sheet routesSheet = workbook.createSheet("Top Inefficient Routes");

        Row header = routesSheet.createRow(0);
        header.createCell(0).setCellValue("Route ID");
        header.createCell(1).setCellValue("Vehicle ID");
        header.createCell(2).setCellValue("Driver ID");
        header.createCell(3).setCellValue("Efficiency Score");

        int rowIndex = 1;
        for (ReportSnapshot.TopInefficientRoute route : snapshot.topInefficientRoutes()) {
            Row row = routesSheet.createRow(rowIndex++);
            row.createCell(0).setCellValue(route.routeId().toString());
            row.createCell(1).setCellValue(route.vehicleId().toString());
            row.createCell(2).setCellValue(route.driverId().toString());
            row.createCell(3)
                    .setCellValue(route.efficiencyScore() == null ? "N/A" : route.efficiencyScore().toString());
        }

        for (int i = 0; i < 4; i++) {
            routesSheet.autoSizeColumn(i);
        }
    }

    private int addRow(Sheet sheet, int rowIndex, String key, String value) {
        Row row = sheet.createRow(rowIndex);
        row.createCell(0).setCellValue(key);
        row.createCell(1).setCellValue(value);
        return rowIndex + 1;
    }

    private String toMoney(BigDecimal value) {
        if (value == null) {
            return "0.00";
        }
        return value.setScale(2, RoundingMode.HALF_UP).toString();
    }
}
