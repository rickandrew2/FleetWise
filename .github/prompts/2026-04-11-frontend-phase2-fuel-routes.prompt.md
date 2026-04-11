---
agent: agent
description: Implement FleetWise frontend Phase 2 Fuel Logs and Routes modules with backend contract parity, role-aware actions, and validation gates.
---

## Context

Phase 1 shipped frontend foundations, role-aware navigation, dashboard hardening, and vehicles CRUD. Fuel Logs and Routes remain placeholders even though backend contracts are stable and validated. This phase delivers production-ready interfaces for both modules while keeping backend untouched.

## Decision

Implement Fuel Logs and Routes with minimal-diff contract-safe integration: shared typed API methods with runtime schema parsing, filters/stats/create/delete flows, and role-aware delete restrictions. Reuse established page-state/toast/auth patterns and add focused module tests.

## Steps

1. Extend frontend API types and API client methods for fuel logs/routes list, stats, create, and delete flows.
2. Implement Fuel Logs page with filters, stats summary, list table, create form, and admin-only delete behavior.
3. Implement Routes page with filters, stats summary, list table, create form, and admin-only delete behavior.
4. Wire app routes from placeholders to real module pages behind existing role guards.
5. Add focused frontend tests for Fuel Logs and Routes critical create-path behavior.
6. Run lint/build/tests and resolve issues.

## Acceptance Criteria

- Fuel Logs route is fully functional for list/filter/stats/create, with delete restricted to ADMIN UX.
- Routes route is fully functional for list/filter/stats/create, with delete restricted to ADMIN UX.
- Shared API contract parsing safely validates new module payloads before app consumption.
- Module pages handle loading, empty, and error states explicitly.
- Frontend lint/build/tests pass after changes.

## Status

- [ ] Not started
- [ ] In progress
- [x] Complete
Blockers (if any):
- None
