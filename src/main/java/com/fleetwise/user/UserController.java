package com.fleetwise.user;

import com.fleetwise.user.dto.UpdateUserNotificationPreferencesRequest;
import com.fleetwise.user.dto.UserNotificationPreferencesResponse;
import com.fleetwise.user.dto.UserSummaryResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','FLEET_MANAGER')")
    public List<UserSummaryResponse> getUsers() {
        return userService.getUserSummaries();
    }

    @GetMapping("/me/preferences")
    @PreAuthorize("hasAnyRole('ADMIN','FLEET_MANAGER','DRIVER')")
    public UserNotificationPreferencesResponse getNotificationPreferences(Authentication authentication) {
        return userService.getNotificationPreferences(authentication.getName());
    }

    @PutMapping("/me/preferences")
    @PreAuthorize("hasAnyRole('ADMIN','FLEET_MANAGER','DRIVER')")
    public UserNotificationPreferencesResponse updateNotificationPreferences(
            Authentication authentication,
            @Valid @RequestBody UpdateUserNotificationPreferencesRequest request) {
        return userService.updateNotificationPreferences(authentication.getName(), request);
    }
}
