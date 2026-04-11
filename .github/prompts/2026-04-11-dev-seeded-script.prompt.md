---
agent: agent
description: Add a one-command seeded dev startup script for local development.
---

## Context
The user currently starts locally with `npm run dev` and wants an explicit command that guarantees backend startup with the `dev` profile so the dev seeder runs.

## Decision
Add dedicated npm scripts in root `package.json`:
1. `dev:backend:seeded` runs Spring Boot with dev profile.
2. `dev:seeded` starts DB + seeded backend + frontend in parallel.

## Steps
1. Update root scripts in `package.json`.
2. Verify the new script names are available via `npm run` listing.

## Acceptance Criteria
- `npm run dev:seeded` exists.
- Backend in that flow starts with `dev` profile and triggers `DevDataLoader`.
- Existing scripts remain unchanged.

## Status
- [ ] Not started  /  [ ] In progress  /  [x] Complete
Blockers (if any):
- None
