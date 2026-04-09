package com.fleetwise.alert;

import com.fleetwise.alert.dto.AlertResponse;
import com.fleetwise.alert.dto.AlertUnreadCountResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/alerts")
@RequiredArgsConstructor
public class AlertController {

    private final AlertService alertService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','FLEET_MANAGER','DRIVER')")
    public List<AlertResponse> getAlerts(Authentication authentication,
            @RequestParam(required = false) AlertType alertType,
            @RequestParam(required = false) UUID vehicleId,
            @RequestParam(required = false) UUID driverId,
            @RequestParam(required = false) Boolean isRead) {
        return alertService.getAlerts(authentication.getName(), alertType, vehicleId, driverId, isRead);
    }

    @PutMapping("/{id}/read")
    @PreAuthorize("hasAnyRole('ADMIN','FLEET_MANAGER','DRIVER')")
    public AlertResponse markAlertAsRead(Authentication authentication, @PathVariable UUID id) {
        return alertService.markAlertAsRead(authentication.getName(), id);
    }

    @GetMapping("/unread-count")
    @PreAuthorize("hasAnyRole('ADMIN','FLEET_MANAGER','DRIVER')")
    public AlertUnreadCountResponse getUnreadCount(Authentication authentication,
            @RequestParam(required = false) UUID driverId) {
        return alertService.getUnreadCount(authentication.getName(), driverId);
    }
}
