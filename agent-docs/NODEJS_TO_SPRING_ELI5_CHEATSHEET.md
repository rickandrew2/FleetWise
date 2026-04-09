# Node.js to Spring Boot ELI5 Cheat Sheet

This page is for developers who know Node.js, TypeScript, Next.js, and Prisma but are new to Java + Spring Boot.

Goal: understand the codebase quickly without deep Java theory.

Need a visual first:

- Read `agent-docs/REQUEST_FLOW_VISUAL_QUICKSTART.md`.

## 1. Imagine a Toy City

Think of the backend as a toy city where each building has one job.

- Controller = front desk (takes requests, gives responses)
- Service = brain room (business rules)
- Repository = database robot (save/find data)
- Entity = database shape (table-mapped object)
- DTO = delivery box (data going in or out)
- Security filter = gate guard (checks token before entry)

If you understand Express middleware + route + service + Prisma, you already know the pattern.

## 2. Folder Names That Look Weird at First

### Why src/main/java?

Maven project convention:

- src/main/java = production Java code
- src/main/resources = config and SQL migrations
- src/test/java = tests
- src/test/resources = test configs

### Why com/fleetwise?

In Java, folders match package names.

- package com.fleetwise.auth; means file must live under com/fleetwise/auth
- com is a naming convention from reverse domain names
- It prevents naming collisions across libraries/projects

Simple view:

- com.fleetwise.auth.AuthController
  - com = top-level namespace style
  - fleetwise = project namespace
  - auth = feature module
  - AuthController = class name

## 3. Fast Mapping: Node vs Spring

| Node.js / Next.js world | Spring world | Meaning |
|---|---|---|
| API route handler | Rest controller method | Handles HTTP request |
| Middleware | Filter chain | Runs before controller |
| Service/use-case file | Service class | Business logic |
| Prisma model | Entity class | DB-mapped object |
| Prisma client | JpaRepository | DB read/write abstraction |
| Zod schema / DTO type | Java record DTO + validation annotations | Input/output contract |
| Error middleware | Global exception handler | Unified error responses |
| .env | application.yml + env variables | Runtime config |

## 4. What Is a Class, Record, Interface?

### Class

A blueprint for an object with fields and methods.

- Used for services, controllers, entities

### Record

A compact immutable data holder.

- Great for DTOs (request/response payloads)
- Similar to defining a fixed TypeScript type for transport data

### Interface

A contract saying what methods must exist, without implementation details.

- Similar to a TypeScript interface conceptually

## 5. Core Spring Annotations You Will See

- @SpringBootApplication: app entry point
- @RestController: class exposes HTTP endpoints
- @RequestMapping: base URL path
- @GetMapping/@PostMapping/@PutMapping/@DeleteMapping: route methods
- @Service: business logic bean
- @Entity: DB-mapped model
- @Table/@Column: table and column mapping
- @Valid: validate request body DTO
- @PreAuthorize: role/permission guard
- @Transactional: DB operation boundary

Think of annotations as labels that tell Spring what each class does.

## 6. DTO, Entity, Repository (Most Important)

### DTO

Data Transfer Object: box for API data.

- Request DTO: incoming body from client
- Response DTO: outgoing body to client
- Keeps API contract clean and stable

### Entity

Represents a DB row.

- Mapped to a table
- Not always ideal to return directly to API clients

### Repository

Database access layer.

- Save entity
- Find entity
- Query by fields

This separation is like keeping API types and DB models separate in a clean Node codebase.

## 7. ELI5 Request Flow (Real Life)

When client calls POST /api/vehicles:

1. Gate guard checks token (security filter).
2. Front desk receives request (controller).
3. Box is checked for valid fields (DTO validation).
4. Brain applies rules (service).
5. Database robot stores row (repository).
6. Response box sent back (response DTO).
7. If something breaks, error manager formats a clean error JSON.

## 8. Java Basics You Will Meet Daily

- package: namespace for classes
- import: bring another class into scope
- public: visible from other packages
- private: visible only inside class
- enum: fixed set of values (like role names)
- UUID: unique id type
- Optional<T>: maybe value exists, maybe not

## 9. Validation (Like Zod but Annotation-Based)

On request DTO fields, you can see:

- @NotBlank
- @Email
- @Size(min=..., max=...)
- @Min / @Max

Then controller uses @Valid so invalid input is rejected automatically.

## 10. Security in One Minute

- Login endpoint verifies email/password
- Server creates JWT
- Client sends JWT in Authorization header
- Filter validates JWT each request
- Role checks decide if endpoint is allowed

Very similar to JWT middleware in Express.

## 11. Where to Put New Code

For a new module:

1. Add SQL migration in src/main/resources/db/migration
2. Add entity + repository in src/main/java/com/fleetwise/<module>
3. Add DTO records in dto subpackage
4. Add service logic
5. Add controller endpoints
6. Add tests in src/test/java/com/fleetwise/<module>

## 12. 30-Minute Learning Path

If overwhelmed, learn in this order:

1. Read one controller
2. Read the service it calls
3. Read repository and entity
4. Read DTO validation
5. Read security config and JWT filter

Repeat this loop for each module.

## 13. Mini Glossary

- Bean: object managed by Spring container
- Dependency Injection: Spring gives classes the dependencies they need
- Spring container: runtime registry that creates and wires beans
- JPA: Java persistence standard used for ORM behavior
- Hibernate: JPA implementation used under Spring Data JPA
- Flyway: migration runner for versioned SQL schema

You are closer than it feels. The architecture is the same idea you already use in Node: route -> logic -> data.