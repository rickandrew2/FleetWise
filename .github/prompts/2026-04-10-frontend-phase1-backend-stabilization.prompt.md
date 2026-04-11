---
agent: agent
description: Phase 1 backend stabilization for frontend kickoff (CORS + identity contract + tests).
---

## Context

Frontend implementation is starting against existing FleetWise backend contracts. The backend is stable, but two integration blockers exist for a decoupled frontend repo: cross-origin browser requests and limited identity payload from the protected bootstrap endpoint.

## Decision

Implement only minimal backend changes needed to unblock frontend Phase 1:

1. Add configurable CORS support in security config for local/prod web origins.
2. Expand protected identity response to include role and userId (in addition to status/email) so frontend can rehydrate auth state after refresh.
3. Extend integration tests to lock these behaviors.

This avoids broad backend scope and keeps the API reliable for upcoming fleetwise-web implementation.

## Steps

1. Add CORS configuration bean and enable `http.cors()` in security filter chain.
2. Add `frontend.allowed-origins` configuration in `application.yml` with env override.
3. Update protected endpoint payload in `ProtectedController`.
4. Update auth integration test assertions for new identity fields.
5. Add CORS preflight integration test for protected endpoint.
6. Run targeted tests and then full test task if needed.

## Acceptance Criteria

1. Browser preflight `OPTIONS` request from configured origin receives successful response with `Access-Control-Allow-Origin`.
2. `GET /api/protected/me` with valid JWT returns `status`, `email`, `role`, and `userId`.
3. Existing auth integration flow still passes.
4. No regressions in backend test suite.

## Status

- [ ] Not started
- [ ] In progress
- [x] Complete
      Blockers (if any):
- None currently.
