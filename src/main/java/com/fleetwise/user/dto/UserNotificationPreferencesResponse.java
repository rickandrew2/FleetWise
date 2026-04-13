package com.fleetwise.user.dto;

import com.fleetwise.user.User;

public record UserNotificationPreferencesResponse(
        String accountEmail,
        String notificationEmail,
        String effectiveNotificationEmail,
        boolean emailNotificationsEnabled) {

    public static UserNotificationPreferencesResponse from(User user) {
        String notificationEmail = user.getNotificationEmail();
        String effectiveNotificationEmail = notificationEmail == null || notificationEmail.isBlank() ? user.getEmail()
                : notificationEmail;

        return new UserNotificationPreferencesResponse(
                user.getEmail(),
                notificationEmail,
                effectiveNotificationEmail,
                Boolean.TRUE.equals(user.getEmailNotificationsEnabled()));
    }
}
