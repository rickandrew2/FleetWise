---
agent: agent
description: Implement dev-only seed data loader and form/filter UX improvements for FleetWise.
---

## Context

FleetWise needs deterministic local development seed data and safer, user-friendly form UX. The current frontend still exposes raw UUID and EPA ID inputs, and currency display is inconsistent with Philippine peso requirements. Backend already has route/fuel alert logic and EPA lookup endpoint, which we will reuse/extend.

## Decision

Implement in two strict phases:

1. Backend dev-only seeding first with idempotent guards and realistic Philippine logistics data.
2. Frontend UX/API improvements next, including users dropdown source endpoint, EPA lookup selection flow, auto-filled driver identity, UUID filter replacement, and full peso currency consistency.

## Steps

1. Add dev-only ApplicationRunner seeder with idempotent checks.
2. Seed users/vehicles/fuel logs/route logs and fire alerts as required.
3. Add users list endpoint for ADMIN/FLEET_MANAGER.
4. Extend EPA lookup response with MPG data.
5. Update frontend API/types for users and EPA lookup.
6. Implement Add Vehicle lookup UX (no visible EPA ID input).
7. Auto-fill driver in Fuel Log and Route forms from authenticated user.
8. Replace UUID filters with dropdowns for fuel/routes/alerts.
9. Centralize peso currency formatting and apply across frontend.
10. Update tests and run backend/frontend validation.

## Acceptance Criteria

- Dev profile startup seeds exactly the requested baseline and reruns without duplicate inserts.
- Route seeding triggers overconsumption alerts via existing AlertService rules.
- Two MAINTENANCE_DUE alerts are present for qualifying vehicles.
- Add Vehicle uses lookup flow and never exposes EPA ID input directly.
- Fuel/Route create forms no longer allow raw driver UUID entry; they submit current user driver ID.
- Fuel/Route/Alerts filters use dropdown selectors for vehicle and driver.
- Cost displays use Philippine peso symbol (PHP formatting) across dashboard and logs.
- Tests/build/lint pass after changes.

## Status

- [ ] Not started / [ ] In progress / [x] Complete
      Blockers (if any):
- None.
