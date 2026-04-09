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
public class WeeklyReportJobScheduler {

    private final ReportService reportService;

    @Scheduled(cron = "${reports.scheduler.weekly-cron}")
    public void runWeeklyReport() {
        log.info("Running scheduled weekly report generation");
        reportService.generateScheduledReport(ReportType.WEEKLY);
    }
}
