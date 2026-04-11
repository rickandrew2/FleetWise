# FleetWise System Guide (For Node.js / TypeScript / Next.js Developers)

This guide explains how FleetWise is built, using comparisons to the stack you already know.

Scope note: this document focuses on the application itself (API, security, data, modules, request flow). It intentionally does not explain agent/AI-skill documentation.

Quick companion:

- If you want the simplest possible version first, read `agent-docs/NODEJS_TO_SPRING_ELI5_CHEATSHEET.md`.
- If you want a visual 2-minute request flow, read `agent-docs/REQUEST_FLOW_VISUAL_QUICKSTART.md`.

## 1. What You Are Looking At

FleetWise backend is a Spring Boot 3.5.13 API using Java 25, PostgreSQL, Flyway migrations, JWT auth, and role-based authorization.

Architecture style: Layered Modular Monolith.

- Layered: Controller -> Service -> Repository -> Entity/DTO
- Modular: code grouped by feature (`auth`, `user`, `vehicle`, `common`)
- Monolith: one deployable backend process

If you come from Node/Next:

- Think of this as one backend app with clear folders per domain, similar to a well-structured Express + Prisma codebase.

## 2. Tech Stack (Actual Dependencies in This Project)

From `pom.xml`:

- Spring Boot 3.5.13
- Java 25
- `spring-boot-starter-web` (REST API)
- `spring-boot-starter-security` (authn/authz)
- `spring-boot-starter-data-jpa` (ORM/data layer)
- `spring-boot-starter-validation` (request validation)
- Flyway + PostgreSQL driver (schema migrations)
- JJWT (`io.jsonwebtoken`) for token creation/validation
- Springdoc OpenAPI UI (`/swagger-ui.html`)
- H2 + Spring test dependencies for tests

## 3. Quick Mental Mapping (Node/Next/Prisma -> Spring)

| Your Familiar Stack | FleetWise Spring Equivalent | What It Means |
|---|---|---|
| Next.js API route / Express route | `@RestController` method | HTTP entry point |
| Middleware chain | Security filter chain + `JwtAuthFilter` | Cross-cutting auth checks |
| Service layer in TS | `@Service` class | Business rules/orchestration |
| Prisma model | JPA `@Entity` class | Table mapping object |
| Prisma client calls | `JpaRepository` methods | DB access abstraction |
| Zod/class-validator DTO | Java `record` DTO + `@Valid` + Jakarta constraints | Request contract validation |
| Error middleware | `@RestControllerAdvice` | Centralized API error responses |
| `.env` config | `application.yml` + env variables | Runtime configuration |

## 4. Project Structure You Should Care About

## Top level

- `src/main/java/com/fleetwise`: backend source code
- `src/main/resources`: configuration and migrations
- `src/test`: test code + test config
- `docker-compose.yml`: local Postgres
- `.env`, `.env.example`: local runtime env values
- `mvnw`, `mvnw.cmd`, `.mvn/`: Maven wrapper
- `agent-docs/`: developer docs like this guide

## Main Java packages

- `auth/`: login/register, JWT service/filter, Spring Security config
- `user/`: user entity/repository/role enum
- `vehicle/`: vehicle CRUD + EPA FuelEconomy integration
- `common/`: health/protected sample endpoints + global exception handler

## 5. Framework Basics: Spring Boot + Jakarta (Plain-English)

### Spring Boot

Spring Boot wires your app automatically based on dependencies and annotations.

Key startup file:

- `FleetwiseApplication.java` with `@SpringBootApplication`

Think of this as the app bootstrap that auto-registers routes/services/repos and starts the HTTP server.

### Jakarta packages in this project

Spring Boot 3 uses Jakarta namespaces (not older `javax` for most server APIs).

You will see:

- `jakarta.validation.*`: request validation annotations (`@NotBlank`, `@Email`, `@Size`, etc.)
- `jakarta.persistence.*`: JPA ORM annotations (`@Entity`, `@Table`, `@Column`, etc.)
- `jakarta.servlet.*`: servlet filter interfaces used by JWT filter

## 6. Request Lifecycle (Real Flow in FleetWise)

Example: `POST /api/vehicles`

1. Request enters Spring Security filter chain.
2. `JwtAuthFilter` checks `Authorization: Bearer ...`, validates token, sets authenticated user in security context.
3. `VehicleController.createVehicle(...)` receives body, runs `@Valid` checks on `VehicleUpsertRequest`.
4. `@PreAuthorize` checks role (`ADMIN` or `FLEET_MANAGER`).
5. `VehicleService.createVehicle(...)` applies business rules (plate normalization, uniqueness checks, EPA enrichment if `epaVehicleId` is present).
6. `VehicleRepository.save(...)` persists entity through JPA/Hibernate.
7. Controller returns `VehicleResponse` DTO.
8. If any known exception occurs, `GlobalExceptionHandler` shapes consistent JSON error output.

This is conceptually very close to: Express middleware -> controller -> service -> Prisma -> response.

## 7. Auth and Security Design

Security config (`SecurityConfig`):

- Stateless sessions (`SessionCreationPolicy.STATELESS`)
- CSRF disabled (appropriate for stateless token API)
- Public routes allowed:
	- `/api/auth/**`
	- `/api/public/**`
	- Swagger routes
- Everything else requires authentication

JWT flow:

- Register: creates user, hashes password with BCrypt, default role is `DRIVER`
- Login: verifies password, generates JWT with subject = email
- Filter: token parsed/verified each request, then Spring Security identity is set

Authorization style:

- Method-level guards via `@PreAuthorize`
- Example in vehicle module:
	- Read/create/update: `ADMIN` or `FLEET_MANAGER`
	- Delete: `ADMIN` only

Role mapping detail:

- Roles are stored as enum values (`ADMIN`, `FLEET_MANAGER`, `DRIVER`)
- Granted authorities are created as `ROLE_<ENUM>` (Spring convention)

## 8. Data Layer Design (Prisma Mindset Translation)

### Entities

Entity classes are your table-mapped models:

- `User` -> `users`
- `Vehicle` -> `vehicles`

JPA annotations define DB mapping:

- `@Entity`, `@Table`, `@Id`, `@Column`, `@Enumerated`
- `@PrePersist` hooks set IDs/timestamps before insert

### Repositories

`UserRepository` and `VehicleRepository` extend `JpaRepository<T, ID>`.

You get CRUD methods automatically, plus derived query methods by naming convention, for example:

- `findByEmailIgnoreCase(...)`
- `existsByPlateNumberIgnoreCase(...)`

Think of this as generated data access APIs similar to how Prisma client exposes typed model operations.

### Migrations (Flyway)

Schema is created by SQL migration files in order:

- `V1__create_users.sql`
- `V2__create_vehicles.sql`

`spring.jpa.hibernate.ddl-auto=validate` means Hibernate verifies schema compatibility but does not auto-create/modify tables. Flyway is the schema source of truth.

## 9. Validation and Error Handling

Request DTOs use Jakarta validation constraints. Example checks in current codebase:

- Register email must be valid and max length 100
- Password min/max length enforced
- Vehicle year range 1980..2100
- Plate/make/model required and size-limited

Controller params annotated with `@Valid` trigger automatic validation.

When validation fails:

- `GlobalExceptionHandler` returns `400` with structured `fieldErrors`

Other handled cases:

- `EntityNotFoundException` -> `404`
- `IllegalArgumentException` -> `400`
- `BadCredentialsException` -> `401`

## 10. External Integration (Vehicle EPA Lookup)

Vehicle module has an external API client abstraction:

- `FuelEconomyApiClient` interface
- `FuelEconomyGovApiClient` implementation using `RestTemplate`

Capabilities:

- Lookup EPA vehicle options by year/make/model
- Fetch MPG/fuel data by EPA vehicle ID

Hardening already present in this client:

- connect/read timeouts
- safe XML parser settings (XXE protections)
- graceful fallback to empty results on remote failures

## 11. Configuration and Environments

Main config file: `src/main/resources/application.yml`

Important config groups:

- datasource from env (`DB_URL`, `DB_USERNAME`, `DB_PASSWORD`)
- JWT secret and expiration from env
- Flyway enabled
- Swagger UI path

Profile overrides:

- `application-dev.yml`: SQL logging enabled
- `application-prod.yml`: quieter logging

Test config:

- `src/test/resources/application.yml` uses in-memory H2 in PostgreSQL mode + Flyway + test JWT secret

## 12. Current API Areas

### Auth

- `POST /api/auth/register`
- `POST /api/auth/login`

### Common

- `GET /api/public/health` (public)
- `GET /api/protected/me` (requires valid JWT)

### Vehicle

- `GET /api/vehicles`
- `POST /api/vehicles`
- `GET /api/vehicles/{id}`
- `PUT /api/vehicles/{id}`
- `DELETE /api/vehicles/{id}`
- `POST /api/vehicles/lookup-epa`

## 13. Testing Layout and What Is Covered

Current tests in `src/test/java/com/fleetwise`:

- `auth/AuthIntegrationTest.java`
- `auth/JwtServiceTest.java`
- `vehicle/VehicleControllerIntegrationTest.java`

This gives a mix of integration and focused unit coverage around security/token behavior and vehicle endpoints.

## 14. Build and Run Commands (Windows-Friendly)

From project root:

- Run app: `mvnw.cmd spring-boot:run`
- Run tests: `mvnw.cmd test`

Swagger docs UI when app is running:

- `http://localhost:8080/swagger-ui.html`

## 15. How to Add a New Module (Recommended Order)

Use this same flow for future modules like fuel logs, routes, alerts, reports:

1. Add Flyway migration SQL in `src/main/resources/db/migration`.
2. Add entity and repository in `src/main/java/com/fleetwise/<module>`.
3. Add request/response DTOs in `dto/` subpackage.
4. Add service methods for business rules.
5. Add controller endpoints and authorization annotations.
6. Add/update global error handling only if a new reusable error pattern appears.
7. Add integration tests under `src/test/java/com/fleetwise/<module>`.

## 16. If You Are New to Spring: What to Learn First

For fastest ramp-up with your current background, focus in this order:

1. Controller + DTO + validation (`@RestController`, `@Valid`, Jakarta constraints)
2. Service + transactions (`@Service`, `@Transactional`)
3. Repository + entity mapping (`JpaRepository`, `@Entity`, `@Column`)
4. Security flow (JWT filter + `SecurityConfig` + `@PreAuthorize`)
5. Flyway migrations and schema lifecycle

If you already understand layered Node APIs, this codebase should feel familiar quickly. The biggest differences are annotation-driven wiring and Spring Security conventions.
