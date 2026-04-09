package com.fleetwise.auth.dto;

public record AuthResponse(
                String token,
                long expiresInMs,
                String email,
                String role) {
}