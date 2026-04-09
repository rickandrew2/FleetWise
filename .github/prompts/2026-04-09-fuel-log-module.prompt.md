---
agent: agent
description: Implement Module 3 Fuel Log Service (entity, migration, API, service logic, tests).
---

## Context
Auth and vehicle modules are in place with integration tests passing. The next roadmap slice is Fuel Log Service for recording fill-ups and tracking cost data.

## Decision
Implement a focused Fuel Log vertical slice with secure access and core business rules only.

In scope:
- Flyway migration for `fuel_logs`
- Entity + repository
- DTOs for create/read
- Service for create/list/get/delete and total cost auto-computation
- Controller endpoints with role guards
- Integration tests for auth/authorization and create/list/read/delete behavior

Out of scope:
- Alert engine integration
- Advanced stats/aggregation endpoint
- Route coupling and efficiency inference

## Steps
1. Add `V3__create_fuel_logs.sql` migration with FK references to `vehicles` and `users`.
2. Create `fuellog` package with entity/repository.
3. Add DTOs with validation boundaries.
4. Implement service layer logic:
- normalize values
- compute `totalCost = litersFilled * pricePerLiter`
- enforce referenced vehicle/driver existence as needed
5. Implement controller endpoints and role authorization.
6. Add integration tests using existing auth flow.
7. Run `mvnw test` and fix any regressions.
8. Update changelog and memory notes if new recurring issues appear.

## Acceptance Criteria
1. `POST /api/fuel-logs` persists a log and returns computed total cost.
2. `GET /api/fuel-logs` returns existing logs for authorized roles.
3. `GET /api/fuel-logs/{id}` returns a single log or 404.
4. `DELETE /api/fuel-logs/{id}` is restricted to `ADMIN`.
5. Test suite passes with no new failures.

## Status
- [ ] Not started
- [x] In progress
- [ ] Complete
Blockers (if any): None
