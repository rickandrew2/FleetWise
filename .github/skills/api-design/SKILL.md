---
name: api-design
description: REST and GraphQL API design — resource modeling, OpenAPI specs, versioning strategy, error contracts, pagination, and security patterns.
---

# API Design

Use this skill when designing, reviewing, or documenting REST or GraphQL APIs.

## Trigger Conditions

- User asks to design, build, or review an API endpoint or service.
- Requests involve routes, schemas, data contracts, or API versioning.
- Pagination, error handling, or authentication patterns are discussed.
- OpenAPI / Swagger spec generation is needed.
- Breaking change management or deprecation strategy is required.

## Workflow

### REST APIs

1. Define resource hierarchy and URL structure (`/resources/{id}/sub-resources`).
2. Apply correct HTTP methods (GET/POST/PUT/PATCH/DELETE) with idempotency notes.
3. Design request/response schemas with explicit, versioned types.
4. Define the error contract: `{ error: { code, message, details } }` with HTTP status mapping.
5. Choose pagination strategy: cursor-based for large/real-time datasets; offset for simple cases.
6. Document authentication scheme (Bearer token, API key, OAuth2 scopes) per endpoint.
7. Generate OpenAPI 3.1 spec.

### GraphQL APIs

1. Design schema types, queries, mutations, and subscriptions.
2. Apply DataLoader pattern to prevent N+1 queries.
3. Define error types in schema (not just HTTP-layer errors).
4. Enforce query depth and complexity limits to prevent abuse.
5. Document field-level deprecation strategy (`@deprecated` directive with migration notes).

### Versioning

- Prefer URI versioning (`/v1/`, `/v2/`) for REST; field deprecation for GraphQL.
- Never mutate an existing contract in place — breaking changes require a new version.
- Maintain prior version for at least one deprecation cycle with migration docs.

## Output Contract

- Resource or type definitions
- Endpoint / operation list with method, path, auth requirement
- Request/response schema examples (JSON)
- Error code reference table
- Pagination strategy description
- OpenAPI 3.1 spec (REST) or SDL schema (GraphQL)

## Guardrails

- Never expose internal stack traces or DB column names in error responses.
- Always validate input at the API boundary — never trust client-supplied data.
- Do not design endpoints that require admin-level credentials from the client.
- Rate limit all public-facing endpoints.
- Apply `agents/rules/security.mdc` for all auth and input handling decisions.
