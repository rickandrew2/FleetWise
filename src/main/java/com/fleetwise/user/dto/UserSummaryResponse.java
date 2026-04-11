package com.fleetwise.user.dto;

import com.fleetwise.user.User;

import java.util.UUID;

public record UserSummaryResponse(
        UUID id,
        String name,
        String email,
        String role) {

    public static UserSummaryResponse from(User user) {
        return new UserSummaryResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole().name());
    }
}
