---
agent: agent
description: Implement Feature 5 email notifications with user preferences, backend delivery hooks, and frontend settings UI.
---

## Context

Feature 1 is complete and validated. The next required roadmap item is Feature 5: email notifications for alerts and weekly reports, controlled by per-user settings.

## Decision

Implement notification preferences directly on the existing users table (`notification_email`, `email_notifications_enabled`) and expose authenticated `/api/users/me/preferences` endpoints. Add a mail notifier service with failure-safe delivery (log-only on send failure) and integrate it into alert creation plus weekly report completion.

## Steps

1. Add persistence and API contracts: Flyway migration, user entity fields, DTOs, and user service/controller methods for get/update preferences.
2. Add notification pipeline: mail dependency/config, `EmailNotificationService`, and integration hooks in alert and report services.
3. Add frontend profile/settings page with route and nav entry, API client methods, and mutation/query state handling.
4. Add and/or adjust backend integration tests for preferences endpoints and ensure existing report/alert flows remain green.
5. Run backend and frontend tests to verify behavior and regressions.

## Acceptance Criteria

- Authenticated users can view and update their own email notification settings.
- Alerts trigger email sends only for opted-in users with a configured notification email.
- Weekly report completion triggers notification emails to opted-in recipients.
- Mail send failures do not break alert/report transactions.
- Backend and frontend test suites pass.

## Status

- [ ] Not started / [ ] In progress / [x] Complete
      Blockers (if any):
- None.
