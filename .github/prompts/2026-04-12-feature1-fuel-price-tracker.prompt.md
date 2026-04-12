---
agent: agent
description: Implement Feature 1 live PH fuel price tracker end-to-end (backend, frontend, tests).
---

## Context

FleetWise needs a production-grade live Philippine fuel price tracker with weekly updates, dashboard visibility, and fuel-log auto-fill support. Existing vehicle fuel types are free text, so canonical fuel type normalization is required for deterministic behavior.

## Decision

Implement Feature 1 only in this cycle. Introduce a dedicated fuel price module with Tuesday scheduled updates (scrape + resilient fallback), strict canonical fuel type handling, admin manual update/trigger endpoints, and frontend widget + auto-fill + trend sparkline.

## Steps

1. Add DB migrations for fuel price history plus vehicle fuel_type normalization/constraints.
2. Add backend fuel price domain (entity, enum, repository, DTOs, service, controller).
3. Add Jsoup dependency and scheduler/config wiring (including @EnableScheduling).
4. Implement scrape flow with stale-last-success fallback and admin manual update/trigger APIs.
5. Add Swagger @Operation annotations for all new endpoints.
6. Add frontend API types/clients and dashboard fuel price widget + trend sparkline.
7. Add fuel log auto-fill behavior by selected vehicle fuel type with advisory/stale labels.
8. Add backend + frontend tests for Feature 1 behavior.
9. Run tests and update docs/config references.

## Acceptance Criteria

- [ ] Tuesday scheduled fuel price update runs and persists prices.
- [ ] Scrape failure still returns last successful prices with stale metadata.
- [ ] New fuel price endpoints work with correct auth (admin-only writes).
- [ ] Dashboard shows current PH fuel prices + 8-week Diesel/Gasoline 91 sparkline.
- [ ] Fuel log form auto-fills price per liter from current fuel type price.
- [ ] Feature 1 tests pass (backend + frontend) with no regressions.

## Status

- [ ] Not started
- [x] In progress
- [ ] Complete
      Blockers (if any):
- None
