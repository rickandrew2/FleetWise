package com.fleetwise.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateUserNotificationPreferencesRequest(
        @NotNull(message = "emailNotificationsEnabled is required") Boolean emailNotificationsEnabled,
        @Email(message = "notificationEmail must be a valid email") @Size(max = 100, message = "notificationEmail must not exceed 100 characters") String notificationEmail) {
}
