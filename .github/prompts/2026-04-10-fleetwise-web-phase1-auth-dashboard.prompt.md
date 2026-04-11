---
agent: agent
description: FleetWise web Phase 1 implementation (secure deps, auth bootstrap, login, protected shell, dashboard).
---

## Context

Backend contracts are ready for frontend kickoff. We need to deliver the first working web slice in a new `fleetwise-web` app: foundation stack, secure dependency choices, auth bootstrap against `/api/auth/login` and `/api/protected/me`, then dashboard rendering from `/api/dashboard/*` endpoints.

## Decision

Implement the first deliverable as a single vertical slice with secure-by-default client behavior:

1. Pin and audit Axios to an exact vetted version (no floating range).
2. Set up Tailwind + shadcn/ui patterns for accessible UI primitives.
3. Build auth bootstrap and protected routing before dashboard UI.
4. Render dashboard summary/top drivers/cost trend with robust loading/error states.

## Steps

1. Install and pin dependencies: router, query, axios, form validation, charting, tailwind/shadcn support libs.
2. Configure Tailwind, PostCSS, aliases, base styles, and reusable UI primitives.
3. Implement env config + Axios client with token injection and normalized API error handling.
4. Implement auth store/context + bootstrap flow from `/api/protected/me` and guarded routes.
5. Build login page and app shell navigation.
6. Build dashboard data hooks and UI cards/table/chart.
7. Run lint and build checks.

## Acceptance Criteria

1. `fleetwise-web` starts and builds successfully.
2. Login with valid credentials stores token and resolves `/api/protected/me` bootstrap state.
3. Protected routes redirect unauthenticated users to login.
4. Dashboard shows summary, top drivers, and cost trend from live backend.
5. Axios is pinned to exact version and dependency audit reports no high/critical vulnerabilities.

## Status

- [ ] Not started
- [ ] In progress
- [x] Complete
      Blockers (if any):
- None currently.
