package com.fleetwise.report;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "reports.scheduler", name = "enabled", havingValue = "true")
public class MonthlyReportJobScheduler {

    private final ReportService reportService;

    @Scheduled(cron = "${reports.scheduler.monthly-cron}")
    public void runMonthlyReport() {
        log.info("Running scheduled monthly report generation");
        reportService.generateScheduledReport(ReportType.MONTHLY);
    }
}
