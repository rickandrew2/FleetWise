package com.fleetwise.user;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "users")
public class User {

    @Id
    private UUID id;

    @Column(length = 100)
    private String name;

    @Column(length = 100, nullable = false, unique = true)
    private String email;

    @Column(name = "notification_email", length = 100)
    private String notificationEmail;

    @Column(name = "email_notifications_enabled", nullable = false)
    private Boolean emailNotificationsEnabled;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserRole role;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    public void prePersist() {
        if (id == null) {
            id = UUID.randomUUID();
        }
        if (emailNotificationsEnabled == null) {
            emailNotificationsEnabled = Boolean.FALSE;
        }
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }
}