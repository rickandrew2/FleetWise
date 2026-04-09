---
agent: agent
description: Upgrade reporting to real file generation with job lifecycle and scheduler, then harden API contracts for frontend handoff.
---

## Context

FleetWise backend modules are implemented and tests are passing. Reporting is currently metadata-only and does not generate real files. The next phase is to make reporting production-realistic and tighten backend contracts before starting React UI work.

## Decision

Implement real report artifact generation (PDF + Excel packaged as ZIP), track report job lifecycle (PENDING/RUNNING/COMPLETED/FAILED), add schedulers for weekly/monthly generation with safe defaults, and keep endpoint contracts deterministic for frontend consumption.

## Steps

1. Add required dependencies and configuration for PDF/Excel generation and scheduling.
2. Implement report generators and file storage strategy under a configurable reports output path.
3. Refactor report service to lifecycle states with robust failure handling.
4. Upgrade download endpoint from metadata response to actual file download stream.
5. Add/adjust integration tests for report lifecycle and file download behavior.
6. Run full tests and update source-of-truth README and lessons learned.

## Acceptance Criteria

1. POST `/api/reports/generate` creates real report artifacts and persists correct status transitions.
2. GET `/api/reports/{id}/download` returns downloadable file content for completed jobs.
3. Scheduler components exist for weekly and monthly report generation and can be toggled by config.
4. Integration tests cover authz plus report generate/list/download flow.
5. Full `mvnw test` passes with no regressions.

## Status

- [ ] Not started
- [ ] In progress
- [x] Complete
      Blockers (if any):
- None.
