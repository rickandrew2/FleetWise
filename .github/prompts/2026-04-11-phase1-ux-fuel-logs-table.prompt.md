---
agent: agent
description: Start Phase 1 UX implementation by adding sorting and pagination controls to the Fuel Logs table.
---

## Context

FleetWise now has functional Fuel Logs workflows but list exploration degrades as records grow. The first implementation slice should improve operator speed with minimal risk by enhancing only the Fuel Logs table UX.

## Decision

Implement client-side sorting and pagination in Fuel Logs first, preserving existing API contracts and filters. This delivers immediate productivity gains while keeping scope narrow and reversible.

## Steps

1. Add sort state and pagination state to Fuel Logs page.
2. Add sortable column headers for date, liters, total cost, and odometer.
3. Compute sorted and paginated rows from current filtered result set.
4. Add pagination controls (page size, prev/next, range summary).
5. Keep delete actions and role rules unchanged.
6. Run frontend tests/build checks and fix any regressions.

## Acceptance Criteria

- Fuel Logs table supports sorting at least date, liters, total cost, and odometer.
- Fuel Logs table shows pagination controls with selectable page size.
- Empty and filtered states still work correctly.
- Existing create/delete flows remain unchanged.
- Frontend tests/build checks pass for modified scope.

## Status

- [ ] Not started
- [ ] In progress
- [x] Complete
Blockers (if any):
- None
