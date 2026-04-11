---
agent: agent
description: Add a root-level command to run FleetWise backend + frontend together, similar to monorepo dev orchestration.
---

## Context

Developers currently start services separately (Docker DB, Spring Boot API, and Vite frontend). The request is to support a single root command, similar to Turborepo ergonomics.

## Decision

Add a root Node scripts entrypoint using `concurrently` so one command launches DB bootstrap, backend, and frontend dev server in parallel.

## Steps

1. Create root `package.json` with `dev`, `dev:backend`, `dev:frontend`, and `dev:db` scripts.
2. Add `concurrently` as root dev dependency.
3. Add optional VS Code task `FleetWise: Run Full Stack` to mirror the same behavior from Tasks UI.
4. Update root README quick-start with one-command flow.
5. Add strict Java 25 + Maven 3.9+ enforcement and JDK toolchain selection in `pom.xml`.
6. Add root `test` and `build` script orchestration.
7. Validate command wiring and quality/build script execution.

## Acceptance Criteria

1. Running `npm run dev` from repository root starts DB up, backend, and frontend processes.
2. Developers can still run backend/frontend scripts independently.
3. Running `npm run test` executes backend tests and frontend lint checks.
4. Running `npm run build` produces backend and frontend build artifacts.
5. Documentation reflects the new command surface and Java enforcement requirements.
6. Existing stack remains unchanged (no migration to Turborepo required).

## Status

- [ ] Not started
- [ ] In progress
- [x] Complete
      Blockers (if any):
- None.
