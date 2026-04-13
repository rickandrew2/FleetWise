package com.fleetwise.user;

import com.fleetwise.user.dto.UpdateUserNotificationPreferencesRequest;
import com.fleetwise.user.dto.UserNotificationPreferencesResponse;
import com.fleetwise.user.dto.UserSummaryResponse;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<UserSummaryResponse> getUserSummaries() {
        return userRepository.findAll().stream()
                .sorted(Comparator
                        .comparing((User user) -> safeLower(user.getName()))
                        .thenComparing(user -> safeLower(user.getEmail())))
                .map(UserSummaryResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public UserNotificationPreferencesResponse getNotificationPreferences(String currentEmail) {
        User user = getCurrentUser(currentEmail);
        return UserNotificationPreferencesResponse.from(user);
    }

    @Transactional
    public UserNotificationPreferencesResponse updateNotificationPreferences(
            String currentEmail,
            UpdateUserNotificationPreferencesRequest request) {
        User user = getCurrentUser(currentEmail);
        user.setEmailNotificationsEnabled(Boolean.TRUE.equals(request.emailNotificationsEnabled()));
        user.setNotificationEmail(normalizeNotificationEmail(request.notificationEmail()));
        User savedUser = userRepository.save(user);
        return UserNotificationPreferencesResponse.from(savedUser);
    }

    private User getCurrentUser(String email) {
        return userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new EntityNotFoundException("Current user not found"));
    }

    private String normalizeNotificationEmail(String value) {
        if (value == null) {
            return null;
        }

        String normalized = value.trim().toLowerCase();
        return normalized.isBlank() ? null : normalized;
    }

    private String safeLower(String value) {
        return value == null ? "" : value.toLowerCase();
    }
}
