---
agent: agent
description: Implement FleetWise frontend Phase 3 Alerts and Reports modules, then apply route-level code-splitting optimization.
---

## Context

Phase 2 delivered Fuel Logs and Routes with stable backend parity. Alerts and Reports are still placeholders in the frontend despite implemented backend endpoints and DTOs. The user also requested optimization follow-up for current frontend bundle size warnings.

## Decision

Implement Alerts and Reports pages with contract-safe typed API methods and resilient loading/empty/error UX, preserving role guards and secure behavior. Follow with route-level lazy loading to reduce initial bundle size and improve load performance.

## Steps

1. Extend API type contracts for alerts and reports request/response payloads.
2. Add schema-validated API methods for alerts list/read/unread-count and reports list/generate/download.
3. Implement Alerts page with filters, unread summary, and mark-as-read action.
4. Implement Reports page with generation flow, status table, and secure download action.
5. Replace alerts/reports route placeholders with real pages and add focused tests.
6. Apply route-level lazy loading with fallback UI in router and validate lint/tests/build.

## Acceptance Criteria

- Alerts and Reports routes are no longer placeholders and integrate with stable backend endpoints.
- Alerts supports filtering and mark-as-read flow with unread count display.
- Reports supports listing, report generation, and completed-report download behavior.
- Frontend routing uses lazy-loaded page bundles for optimization.
- Frontend lint, tests, and build pass after changes.

## Status

- [ ] Not started
- [ ] In progress
- [x] Complete
Blockers (if any):
- None
