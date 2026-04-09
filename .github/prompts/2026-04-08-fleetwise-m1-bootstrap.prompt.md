---
agent: agent
description: Milestone 1 bootstrap plan for FleetWise Spring Boot foundation (local runtime, DB, auth baseline).
---

## Context
FleetWise currently has strong documentation but no runnable Java code. The immediate goal is to produce a resume-ready first milestone that proves backend foundation skills: Spring Boot project setup, PostgreSQL/PostGIS infrastructure, schema migration, and secure auth baseline.

## Decision
Implement Milestone 1 as a narrow vertical slice:
- In scope: Maven scaffold, app config, Dockerized Postgres/PostGIS, Flyway users migration, basic auth (register/login), JWT security boundary, and minimal tests.
- Out of scope: FuelEconomy integration, GraphHopper routing, alert engine, reporting jobs.

Why: This sequence minimizes cognitive load for a first real Java project while still creating a production-style baseline.

## Steps
1. Create Maven project foundation (`pom.xml`, wrapper scripts, app entrypoint).
2. Add runtime config (`application.yml`, profile-safe env bindings, OpenAPI baseline).
3. Add local infra (`docker-compose.yml`, optional `.env.example`, `.gitignore` safety).
4. Add data layer for users (`User` entity, `UserRole`, repository, Flyway migration).
5. Add auth module (`AuthController`, DTOs, `AuthService`, `JwtService`, `SecurityConfig`, `JwtAuthFilter`).
6. Add minimal protected endpoint for verification.
7. Add tests for auth flow and JWT utility.
8. Run compile/test and refine docs to reflect milestone state.

## Acceptance Criteria
1. Application starts successfully with Java 17 and Maven.
2. Docker PostgreSQL/PostGIS service starts and app connects without manual DB changes.
3. Flyway creates users table with UUID PK and unique email.
4. `POST /api/auth/register` and `POST /api/auth/login` work end-to-end.
5. A protected endpoint returns 401 without token and 200 with valid JWT.
6. `mvn test` passes for initial auth/JWT tests.

## Status
- [ ] Not started
- [x] In progress
- [ ] Complete
Blockers (if any): None
