---
agent: agent
description: Implement Module 2 Vehicle Registry baseline with role-protected CRUD and FuelEconomy lookup stub-ready integration.
---

## Context

Milestone 1 auth foundation is complete and validated. Next implementation slice is Module 2: Vehicle Registry, aligned with FleetWise roadmap and source-of-truth contracts.

## Decision

Implement Vehicle Registry as a production-style baseline now:

- Add `vehicle` data model, repository, service, and controller.
- Protect endpoints by role (`FLEET_MANAGER`, `ADMIN` for write/list).
- Include a FuelEconomy client contract and lookup endpoint with safe fallback behavior.

Out of scope for this slice:

- Full FuelEconomy model matching all response variants.
- Route/fuel analytics coupling.

## Steps

1. Add domain model: `Vehicle` entity and repository.
2. Add DTOs and validation for create/update/lookups.
3. Add service layer with CRUD logic and role-safe behavior.
4. Add FuelEconomy API client abstraction and initial implementation.
5. Add Vehicle controller endpoints and method-level authorization.
6. Add integration tests for authorization + happy-path CRUD.
7. Run tests with Maven Wrapper and patch any failing paths.
8. Update README change log with module progress.

## Acceptance Criteria

1. `POST /api/vehicles` creates vehicle with validated payload.
2. `GET /api/vehicles` and `GET /api/vehicles/{id}` return saved vehicles.
3. Manager/admin authorization enforced for vehicle management endpoints.
4. `/api/vehicles/lookup-epa` endpoint returns lookup response (or safe error handling).
5. Integration tests pass via `mvnw test`.

## Status

- [ ] Not started
- [x] In progress
- [ ] Complete
      Blockers (if any): None
