---
agent: agent
description: Scaffold fleetwise-web and deliver Phase 1 frontend slice (auth bootstrap, login, protected shell, dashboard).
---

## Context

FleetWise backend is stable and now exposes frontend-ready auth bootstrap data (`/api/protected/me` returns status/email/role/userId) with CORS support. The next phase is to start a decoupled frontend implementation in `fleetwise-web` using React + Vite + TypeScript + Tailwind + shadcn/ui.

## Decision

Implement the first frontend deliverable as a focused slice:

- Scaffold `fleetwise-web` with required stack and secure dependency posture.
- Add API/auth foundation with JWT bootstrap against backend contract.
- Ship login page, protected app shell, and dashboard page using backend dashboard endpoints.

Security decision:

- Use pinned Axios version from official npm registry, verify with audit, and keep lockfile for integrity tracking.

## Steps

1. Scaffold Vite React TypeScript app in `fleetwise-web`.
2. Install frontend dependencies: Router, Query, Axios (pinned), Recharts, shadcn dependencies.
3. Configure Tailwind and initialize shadcn/ui with base components.
4. Implement API client and auth bootstrap (`/api/protected/me`).
5. Implement login workflow (`/api/auth/login`) and token persistence.
6. Implement protected route/app shell with role-aware dashboard access.
7. Implement dashboard KPIs + top drivers + cost trend chart.
8. Run lint/build/tests and fix any issues.

## Acceptance Criteria

1. `fleetwise-web` runs and builds with React + Vite + TypeScript + Tailwind + shadcn/ui.
2. Axios is pinned to a specific version and dependency audit completes without high/critical production vulnerabilities.
3. Login works against backend and stores JWT for subsequent API calls.
4. On refresh, app bootstrap validates session via `/api/protected/me`.
5. Protected shell blocks unauthorized users and redirects to login.
6. Dashboard shows summary, top drivers, and cost trend from backend endpoints.

## Status

- [ ] Not started
- [x] In progress
- [ ] Complete
      Blockers (if any):
- None.
