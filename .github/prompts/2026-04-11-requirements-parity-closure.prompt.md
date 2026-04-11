---
agent: agent
description: Close backend parity gaps from README audit with minimal, test-backed changes.
---

## Context
The latest audit found partial parity with FleetWise requirements: route distance still uses Haversine only, `GET /api/vehicles/{id}/stats` is missing, and `GET /api/routes/top-inefficient` is missing.

## Decision
Implement a minimal parity closure patch with safe defaults and no broad refactor:
1. Add a GraphHopper-backed distance calculator that is enabled only when API key exists, with automatic fallback to existing Haversine calculator.
2. Add vehicle stats endpoint and service response DTO using existing fuel/route data.
3. Add top-inefficient routes endpoint using existing repository query.
4. Add focused integration tests for the new endpoints and role behavior.

## Steps
1. Add route distance providers (Haversine provider + GraphHopper client + fallback calculator wiring).
2. Add vehicle stats DTO/service/controller endpoint `GET /api/vehicles/{id}/stats`.
3. Add route endpoint `GET /api/routes/top-inefficient` and service method.
4. Update app config docs/properties for optional GraphHopper settings.
5. Add/adjust integration tests for new endpoints.
6. Run backend tests and fix any regressions.

## Acceptance Criteria
- `GET /api/vehicles/{id}/stats` returns aggregate vehicle metrics for authorized roles.
- `GET /api/routes/top-inefficient` returns ordered route logs and respects role constraints.
- Route creation uses GraphHopper when configured and gracefully falls back when unavailable.
- Existing tests remain green and new endpoint tests pass.

## Status
- [ ] Not started  /  [ ] In progress  /  [x] Complete
Blockers (if any):
- None
