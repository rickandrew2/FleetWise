---
agent: agent
description: Implement FleetWise frontend Phase 1 foundations, role-aware auth UX, dashboard hardening, and vehicles integration with validation gates.
---

## Context

Backend contracts are stable and backend orchestration is already validated. Frontend has auth bootstrap, protected routes, shell layout, and dashboard, but operational modules are still placeholders. The immediate objective is to ship production-quality frontend foundations and the first fully integrated module with secure, resilient UX.

## Decision

Implement a scoped Phase 1 with minimal diffs: harden shared frontend infrastructure, enforce role-aware navigation/access, improve dashboard resiliency, and replace Vehicles placeholder with full CRUD UI against stable backend APIs. Add frontend test harness and critical-flow tests to enforce lint/build/test quality gates.

## Steps

1. Add shared UI and API resilience primitives (toasts, page states, safe error handling).
2. Add role-aware access checks in routing/navigation and improve expired-session UX.
3. Harden dashboard loading/empty/error/null-safe behavior with reusable primitives.
4. Implement Vehicles module UI with list/create/edit/delete, validation, and role-based actions.
5. Add frontend testing setup and critical-path tests for auth/dashboard/vehicles.
6. Run and record lint/build/tests and manual critical-path checks.

## Acceptance Criteria

- Vehicles route is no longer placeholder and supports list/create/edit/delete with backend integration.
- Role-aware route and navigation behavior reflects backend roles (ADMIN, FLEET_MANAGER, DRIVER).
- Major Phase 1 screens have explicit loading, empty, success, and error states.
- API failures are safely handled and surfaced without leaking sensitive details.
- Frontend lint and build pass, and added tests pass.

## Status

- [ ] Not started
- [ ] In progress
- [x] Complete
Blockers (if any):
- None
