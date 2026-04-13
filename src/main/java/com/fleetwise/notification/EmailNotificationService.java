package com.fleetwise.notification;

import com.fleetwise.alert.Alert;
import com.fleetwise.report.ReportJob;
import com.fleetwise.user.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class EmailNotificationService {

    @Autowired(required = false)
    private JavaMailSender mailSender;

    @Value("${notifications.email.enabled:true}")
    private boolean emailNotificationsEnabled;

    @Value("${notifications.email.from:noreply@fleetwise.local}")
    private String fromAddress;

    public void sendAlertNotification(User user, Alert alert) {
        if (!canNotify(user)) {
            return;
        }

        String recipient = resolveRecipient(user);
        String subject = "[FleetWise] New " + alert.getAlertType().name().replace('_', ' ') + " alert";
        String body = "Hello,\n\n"
                + "A new alert has been generated in FleetWise.\n\n"
                + "Alert type: " + alert.getAlertType().name() + "\n"
                + "Message: " + alert.getMessage() + "\n"
                + "Vehicle ID: " + safeValue(alert.getVehicleId() == null ? null : alert.getVehicleId().toString())
                + "\n"
                + "Driver ID: " + safeValue(alert.getDriverId() == null ? null : alert.getDriverId().toString())
                + "\n\n"
                + "Please sign in to FleetWise to review this alert.\n";

        sendMailSafely(recipient, subject, body);
    }

    public void sendWeeklyReportNotification(User user, ReportJob reportJob) {
        if (!canNotify(user)) {
            return;
        }

        String recipient = resolveRecipient(user);
        String subject = "[FleetWise] Weekly report is ready";
        String body = "Hello,\n\n"
                + "Your weekly FleetWise report has been generated.\n\n"
                + "Report job ID: " + reportJob.getId() + "\n"
                + "Status: " + reportJob.getStatus() + "\n"
                + "Generated at: "
                + safeValue(reportJob.getGeneratedAt() == null ? null : reportJob.getGeneratedAt().toString()) + "\n"
                + "File path: " + safeValue(reportJob.getFilePath()) + "\n\n"
                + "You can download the report from the Reports page in FleetWise.\n";

        sendMailSafely(recipient, subject, body);
    }

    private boolean canNotify(User user) {
        return emailNotificationsEnabled
                && Boolean.TRUE.equals(user.getEmailNotificationsEnabled())
                && resolveRecipient(user) != null;
    }

    private String resolveRecipient(User user) {
        String candidate = user.getNotificationEmail();
        if (candidate != null && !candidate.isBlank()) {
            return candidate;
        }

        String fallback = user.getEmail();
        if (fallback == null || fallback.isBlank()) {
            return null;
        }
        return fallback;
    }

    private void sendMailSafely(String recipient, String subject, String body) {
        if (mailSender == null) {
            log.debug("Skipping email send because JavaMailSender is not configured");
            return;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromAddress);
            message.setTo(recipient);
            message.setSubject(subject);
            message.setText(body);
            mailSender.send(message);
        } catch (Exception ex) {
            log.warn("Email notification send failed for recipient={}", recipient, ex);
        }
    }

    private String safeValue(String value) {
        return value == null || value.isBlank() ? "N/A" : value;
    }
}
