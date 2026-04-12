---
agent: agent
description: Use CloudBeaver as the Docker-based PostgreSQL browser UI.
---

## Context
The project currently uses Adminer for database browsing, but the user prefers a richer UI similar to Prisma Studio.

## Decision
Use CloudBeaver as the primary Docker web UI for PostgreSQL browsing and remove pgAdmin from the stack.

## Steps
1. Add CloudBeaver service in docker-compose.yml.
2. Start CloudBeaver and verify browser reachability.
3. Configure and verify PostgreSQL connection inside CloudBeaver.
4. Remove pgAdmin service once CloudBeaver is confirmed working.

## Acceptance Criteria
- Docker Compose includes CloudBeaver and no pgAdmin service.
- Running docker compose up -d starts PostgreSQL and CloudBeaver.
- CloudBeaver is available in browser and connects to fleetwise PostgreSQL.

## Status
- [ ] Not started  /  [ ] In progress  /  [x] Complete
Blockers (if any):
- None
