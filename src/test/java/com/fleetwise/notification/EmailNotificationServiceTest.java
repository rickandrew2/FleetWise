package com.fleetwise.notification;

import com.fleetwise.alert.Alert;
import com.fleetwise.alert.AlertType;
import com.fleetwise.report.ReportJob;
import com.fleetwise.report.ReportStatus;
import com.fleetwise.report.ReportType;
import com.fleetwise.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class EmailNotificationServiceTest {

    @Mock
    private JavaMailSender mailSender;

    private EmailNotificationService emailNotificationService;

    @BeforeEach
    void setUp() {
        emailNotificationService = new EmailNotificationService();
        ReflectionTestUtils.setField(emailNotificationService, "mailSender", mailSender);
        ReflectionTestUtils.setField(emailNotificationService, "emailNotificationsEnabled", true);
        ReflectionTestUtils.setField(emailNotificationService, "fromAddress", "noreply@fleetwise.test");
    }

    @Test
    void shouldSendAlertNotificationToPreferredEmail() {
        User user = new User();
        user.setEmail("driver@fleetwise.test");
        user.setNotificationEmail("alerts@fleetwise.test");
        user.setEmailNotificationsEnabled(true);

        Alert alert = new Alert();
        alert.setAlertType(AlertType.HIGH_COST);
        alert.setMessage("Fuel fill-up cost exceeded threshold");
        alert.setVehicleId(UUID.randomUUID());
        alert.setDriverId(UUID.randomUUID());

        emailNotificationService.sendAlertNotification(user, alert);

        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(messageCaptor.capture());

        SimpleMailMessage sent = messageCaptor.getValue();
        assertThat(sent.getTo()).containsExactly("alerts@fleetwise.test");
        assertThat(sent.getFrom()).isEqualTo("noreply@fleetwise.test");
        assertThat(sent.getSubject()).contains("HIGH COST");
        assertThat(sent.getText()).contains("Fuel fill-up cost exceeded threshold");
    }

    @Test
    void shouldSendWeeklyReportNotificationUsingAccountEmailFallback() {
        User user = new User();
        user.setEmail("manager@fleetwise.test");
        user.setNotificationEmail(null);
        user.setEmailNotificationsEnabled(true);

        ReportJob reportJob = new ReportJob();
        reportJob.setId(UUID.randomUUID());
        reportJob.setReportType(ReportType.WEEKLY);
        reportJob.setStatus(ReportStatus.COMPLETED);
        reportJob.setGeneratedAt(Instant.parse("2026-04-12T08:15:30Z"));
        reportJob.setFilePath("target/reports-output/weekly-report.zip");

        emailNotificationService.sendWeeklyReportNotification(user, reportJob);

        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(messageCaptor.capture());

        SimpleMailMessage sent = messageCaptor.getValue();
        assertThat(sent.getTo()).containsExactly("manager@fleetwise.test");
        assertThat(sent.getSubject()).contains("Weekly report is ready");
        assertThat(sent.getText()).contains(reportJob.getId().toString());
    }

    @Test
    void shouldSkipEmailWhenUserNotificationsDisabled() {
        User user = new User();
        user.setEmail("driver@fleetwise.test");
        user.setEmailNotificationsEnabled(false);

        Alert alert = new Alert();
        alert.setAlertType(AlertType.HIGH_COST);
        alert.setMessage("Fuel fill-up cost exceeded threshold");

        emailNotificationService.sendAlertNotification(user, alert);

        verify(mailSender, never()).send(org.mockito.ArgumentMatchers.any(SimpleMailMessage.class));
    }
}
