---
agent: agent
description: Implement Phase 1 UX sequence in order: Routes table sorting/pagination, Alerts bulk triage, then mobile navigation drawer.
---

## Context

User requested the next implementation slices in strict order after Fuel Logs completion: improve Routes list usability, then Alerts triage speed, then mobile navigation ergonomics.

## Decision

Apply minimal-diff frontend-only improvements in strict sequence with validation after each slice. Keep API contracts and backend behavior unchanged.

## Steps

1. Add Routes table sorting + pagination controls.
2. Validate frontend lint/build.
3. Add Alerts bulk triage actions (select unread, bulk mark read).
4. Validate frontend lint/build.
5. Implement mobile navigation drawer in app shell for small screens.
6. Validate frontend lint/build.

## Acceptance Criteria

- Routes page supports sorting and pagination with page size selector.
- Alerts page supports bulk unread selection and bulk mark-as-read action.
- Mobile viewport uses a drawer-style navigation instead of horizontal scroll tabs.
- Existing role-based access and core workflows remain intact.
- Frontend lint and build pass after all three slices.

## Status

- [ ] Not started
- [ ] In progress
- [x] Complete
Blockers (if any):
- None
