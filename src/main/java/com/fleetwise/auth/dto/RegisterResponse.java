package com.fleetwise.auth.dto;

public record RegisterResponse(
                String id,
                String email,
                String role) {
}