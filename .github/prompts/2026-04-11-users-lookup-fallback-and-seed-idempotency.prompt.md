---
agent: agent
description: Keep logs and alerts pages usable when users lookup fails and add dev seeder idempotency coverage.
---

## Context

Fuel Logs, Routes, and Alerts pages started failing with full-page 404 errors when the users lookup endpoint was unavailable in some runtime modes. The page behavior should degrade gracefully so core data still loads. A focused backend integration test is also needed to verify dev seeding is idempotent across repeated runs.

## Decision

Treat `/api/users` as an optional enhancement for filter UX rather than a hard dependency for page rendering. Keep primary queries (`vehicles`, `fuel logs`, `routes`, `alerts`, `stats`) as blocking dependencies and show a small helper note when users lookup fails. Add an integration test that executes `DevDataLoader` twice and asserts data counts do not increase on the second run.

## Steps

1. Update `fuel-logs-page.tsx` to remove users-query from blocking error handling and add fallback driver options.
2. Update `routes-page.tsx` with the same non-blocking users-query behavior.
3. Update `alerts-page.tsx` with the same non-blocking users-query behavior.
4. Add `DevDataLoaderIntegrationTest` to run seeding twice and assert idempotency via repository counts.
5. Run targeted frontend tests and backend test for validation.

## Acceptance Criteria

- Fuel Logs, Routes, and Alerts pages render even if `/api/users` fails.
- Core page errors only appear for core page data failures, not users filter lookup failures.
- Dev seeder integration test proves second run does not increase users, vehicles, fuel logs, routes, or alerts counts.
- Targeted frontend and backend tests pass.

## Status

- [ ] Not started / [ ] In progress / [x] Complete
      Blockers (if any):
- None
