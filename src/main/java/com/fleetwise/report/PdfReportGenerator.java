package com.fleetwise.report;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
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
public class PdfReportGenerator {

    private static final DateTimeFormatter TS_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss 'UTC'")
            .withZone(ZoneOffset.UTC);

    public void generate(Path outputFile, ReportSnapshot snapshot) {
        try {
            Files.createDirectories(outputFile.getParent());
            try (OutputStream outputStream = Files.newOutputStream(outputFile)) {
                Document document = new Document();
                PdfWriter.getInstance(document, outputStream);
                document.open();

                document.add(new Paragraph("FleetWise " + snapshot.reportType().name() + " Report"));
                document.add(new Paragraph("Generated: " + TS_FORMAT.format(snapshot.generatedAt())));
                document.add(new Paragraph(" "));

                PdfPTable summaryTable = new PdfPTable(2);
                summaryTable.setWidthPercentage(100);
                addSummaryRow(summaryTable, "Total Vehicles", String.valueOf(snapshot.totalVehicles()));
                addSummaryRow(summaryTable, "Total Fuel Cost", toMoney(snapshot.totalFuelCost()));
                addSummaryRow(summaryTable, "Average Efficiency Score",
                        snapshot.averageEfficiencyScore() == null ? "N/A"
                                : snapshot.averageEfficiencyScore().toString());
                addSummaryRow(summaryTable, "Active Alerts", String.valueOf(snapshot.activeAlertsCount()));
                document.add(summaryTable);

                document.add(new Paragraph(" "));
                document.add(new Paragraph("Top Inefficient Routes"));

                PdfPTable routesTable = new PdfPTable(4);
                routesTable.setWidthPercentage(100);
                routesTable.addCell(headerCell("Route ID"));
                routesTable.addCell(headerCell("Vehicle ID"));
                routesTable.addCell(headerCell("Driver ID"));
                routesTable.addCell(headerCell("Efficiency"));

                for (ReportSnapshot.TopInefficientRoute route : snapshot.topInefficientRoutes()) {
                    routesTable.addCell(route.routeId().toString());
                    routesTable.addCell(route.vehicleId().toString());
                    routesTable.addCell(route.driverId().toString());
                    routesTable.addCell(route.efficiencyScore() == null ? "N/A" : route.efficiencyScore().toString());
                }

                if (snapshot.topInefficientRoutes().isEmpty()) {
                    PdfPCell noDataCell = new PdfPCell(new Phrase("No route data available"));
                    noDataCell.setColspan(4);
                    routesTable.addCell(noDataCell);
                }

                document.add(routesTable);
                document.close();
            }
        } catch (IOException | DocumentException ex) {
            throw new IllegalStateException("Failed to generate PDF report", ex);
        }
    }

    private void addSummaryRow(PdfPTable table, String key, String value) {
        table.addCell(headerCell(key));
        table.addCell(value);
    }

    private PdfPCell headerCell(String value) {
        PdfPCell cell = new PdfPCell(new Phrase(value));
        cell.setPadding(4f);
        return cell;
    }

    private String toMoney(BigDecimal value) {
        if (value == null) {
            return "0.00";
        }
        return value.setScale(2, RoundingMode.HALF_UP).toString();
    }
}
