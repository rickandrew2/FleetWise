# FleetWise Memory

This file stores lessons learned for recurring issues and fixes.

## How to Use

1. Before debugging, check existing entries for a matching symptom.
2. If a match exists, apply the known fix first.
3. After any successful new fix, append a new entry using the template below.

## Lesson Entry Template

### [CATEGORY] Short title of the error
- **Symptom**: What the error looked like (message, behavior)
- **Root Cause**: Why it happened
- **Fix**: Exact steps or code that resolved it
- **Avoid**: What NOT to do next time
- **Date**: YYYY-MM-DD

Allowed categories: [BUILD] [DB] [API/AUTH] [UI] [TYPE] [CONFIG] [OTHER]

## Known Lessons

### [CONFIG] Prompt frontmatter mode key deprecated
- **Symptom**: Prompt file validation reported "The 'mode' attribute has been deprecated. Please rename it to 'agent'."
- **Root Cause**: New prompt metadata schema no longer accepts `mode: agent`.
- **Fix**: Replace `mode: agent` with `agent: true` in `.prompt.md` frontmatter.
- **Avoid**: Do not use legacy `mode` key when creating new prompt files.
- **Date**: 2026-04-08

### [CONFIG] Docker Compose version field warning
- **Symptom**: `docker compose config` warned that the `version` attribute is obsolete and ignored.
- **Root Cause**: Compose V2 no longer requires the top-level `version` key.
- **Fix**: Removed the `version` field from `docker-compose.yml` and validated with `docker compose config`.
- **Avoid**: Do not add `version` in new Compose files unless explicitly required by older tooling.
- **Date**: 2026-04-08

### [CONFIG] Winget cannot find Apache.Maven package
- **Symptom**: `winget install -e --id Apache.Maven` returns "No package found matching input criteria."
- **Root Cause**: Some winget indexes/sources do not include that package ID consistently.
- **Fix**: Use Maven Wrapper (`mvnw`/`mvnw.cmd`) as the primary build path and treat global Maven install as optional fallback.
- **Avoid**: Do not block project setup on global Maven installation when wrapper can be used.
- **Date**: 2026-04-08

### [BUILD] POM XML parse failed on ampersand
- **Symptom**: Maven failed with `Non-parseable POM ... entity reference names can not start with character ' '`.
- **Root Cause**: Unescaped `&` in `pom.xml` description text.
- **Fix**: Replace `&` with `&amp;` in XML content.
- **Avoid**: Escape XML special characters in POM fields (`&`, `<`, `>`).
- **Date**: 2026-04-08

### [CONFIG] PowerShell cannot run local mvnw without path prefix
- **Symptom**: `mvnw.cmd` showed `is not recognized` even though the file exists in current directory.
- **Root Cause**: PowerShell does not execute commands from current directory unless explicitly prefixed.
- **Fix**: Run Maven Wrapper as `./mvnw` (bash) or `.\\mvnw.cmd` (PowerShell).
- **Avoid**: Do not run `mvnw.cmd` without `.\\` in PowerShell.
- **Date**: 2026-04-08

### [CONFIG] JWT_SECRET placeholder unresolved at startup
- **Symptom**: App startup failed with `Could not resolve placeholder 'JWT_SECRET'`.
- **Root Cause**: Spring Boot does not automatically read `.env` unless explicitly imported.
- **Fix**: Added `spring.config.import: optional:file:.env[.properties]` in `application.yml`.
- **Avoid**: If using `.env` files, always configure Spring config import for local runs.
- **Date**: 2026-04-08

### [CONFIG] Make mvnw work in future PowerShell sessions
- **Symptom**: Alias worked in one terminal but failed in newly opened terminals.
- **Root Cause**: `Set-Alias` is session-scoped unless added to PowerShell profile.
- **Fix**: Added `function mvnw { & (Join-Path (Get-Location) 'mvnw.cmd') @args }` to `$PROFILE`, reloaded profile, and verified `mvnw -version`.
- **Avoid**: Do not rely on session-only alias when you need persistent command behavior.
- **Date**: 2026-04-08

### [TYPE] Hibernate schema validation mismatch on DECIMAL columns
- **Symptom**: Tests failed with `Schema-validation: wrong column type ... found DECIMAL, but expecting FLOAT` for `vehicles` MPG columns.
- **Root Cause**: JPA entity used `Double` while migration defined `DECIMAL`.
- **Fix**: Changed entity numeric fields to `BigDecimal` and converted at service DTO boundaries.
- **Avoid**: Keep Java numeric types aligned with SQL column definitions, especially when using `ddl-auto=validate`.
- **Date**: 2026-04-09

### [DB] Integration tests fail after adding new foreign keys
- **Symptom**: `DataIntegrityViolationException` when `VehicleControllerIntegrationTest` cleanup deleted vehicles before dependent fuel logs.
- **Root Cause**: New FK from `fuel_logs.vehicle_id` to `vehicles.id` enforced referential integrity.
- **Fix**: Delete dependent records first in test setup (`fuelLogRepository.deleteAll()` before `vehicleRepository.deleteAll()`).
- **Avoid**: Revisit test fixture cleanup order after introducing new FK constraints.
- **Date**: 2026-04-09

### [OTHER] Auth integration test collided with seeded users from other module tests
- **Symptom**: Full suite run failed with `400` on register in `AuthIntegrationTest`, while targeted auth tests previously passed.
- **Root Cause**: Hardcoded email (`driver@fleetwise.test`) conflicted with users seeded by other integration tests sharing the same application context/database.
- **Fix**: Generate a unique test email per run using `UUID` in `AuthIntegrationTest` request payloads and assertions.
- **Avoid**: Do not hardcode globally reused identifiers in integration tests that run with shared test state.
- **Date**: 2026-04-09

### [BUILD] Missing report dependencies caused baseline compile failure
- **Symptom**: `mvn clean test-compile` failed with missing packages `org.apache.poi.*` and `com.lowagie.text.*`.
- **Root Cause**: `pom.xml` did not include `poi-ooxml` and `openpdf` dependencies required by report generator classes already in source.
- **Fix**: Added `org.apache.poi:poi-ooxml:5.3.0` and `com.github.librepdf:openpdf:1.3.41` back to `pom.xml` and re-ran compile.
- **Avoid**: Keep dependency declarations aligned with newly added feature modules before merging to main branch.
- **Date**: 2026-04-10

### [BUILD] Service-repository contract drift broke compilation
- **Symptom**: Compile failed with `cannot find symbol method findTopInefficient(...)` in `ReportService`.
- **Root Cause**: `ReportService` called `RouteLogRepository.findTopInefficient` but repository interface did not define the method.
- **Fix**: Added `findTopInefficient(UUID, Pageable)` JPQL query method to `RouteLogRepository`.
- **Avoid**: Add or update repository interfaces in the same change set when introducing new service query calls.
- **Date**: 2026-04-10

### [BUILD] Java 25 required explicit Lombok processor configuration
- **Symptom**: Java 25 compile surfaced Lombok processing failures until processor wiring was explicit.
- **Root Cause**: Implicit annotation processing setup was brittle under the upgraded toolchain.
- **Fix**: Added `lombok.version` property, pinned Lombok `1.18.44`, and configured `maven-compiler-plugin` `annotationProcessorPaths` for Lombok.
- **Avoid**: For latest JDK upgrades, explicitly configure annotation processors instead of relying on implicit behavior.
- **Date**: 2026-04-10

### [CONFIG] React Router v6 minor version carried known high CVE
- **Symptom**: `npm audit --omit=dev` reported 3 high vulnerabilities after installing `react-router-dom@6.30.1`.
- **Root Cause**: `react-router-dom@6.30.1` depends on vulnerable `@remix-run/router` versions (open redirect/XSS advisory path).
- **Fix**: Upgraded and pinned to patched `react-router-dom@6.30.3` and re-ran `npm audit` to confirm zero high/critical vulnerabilities.
- **Avoid**: Do not pin older v6 patch releases without running `npm audit` immediately; prefer latest patched v6 patchline.
- **Date**: 2026-04-10

### [CONFIG] Strict Java enforcement requires Maven toolchains entry
- **Symptom**: Maven validate/build failed with toolchain selection errors after enabling strict Java 25 enforcement.
- **Root Cause**: `maven-toolchains-plugin` was configured to require JDK 25, but no matching `~/.m2/toolchains.xml` entry existed.
- **Fix**: Added a JDK 25 toolchain definition in `~/.m2/toolchains.xml` and verified Maven resolved `JDK[C:/Users/Acer/.jdk/jdk-25]` during `test` and `package`.
- **Avoid**: Do not enable strict toolchain enforcement without documenting and provisioning `toolchains.xml` on developer machines and CI agents.
- **Date**: 2026-04-10

### [CONFIG] Root dev orchestration stopped after DB bootstrap success
- **Symptom**: `npm run dev` started services, then backend/frontend were terminated right after `dev:db` exited with code 0.
- **Root Cause**: Root `dev` script used `concurrently -k`, which kills sibling processes when any command exits, including a successful one-shot DB bootstrap command.
- **Fix**: Removed `-k` from the root `dev` script so `dev:db` can finish while backend/frontend continue running.
- **Avoid**: Do not use `-k` in mixed one-shot + long-running orchestration commands unless all commands are expected to stay alive.
- **Date**: 2026-04-10

### [TYPE] Zod coerce schema caused React Hook Form resolver mismatch
- **Symptom**: Frontend build failed with TypeScript error in `vehicles-page.tsx` because `zodResolver(vehicleFormSchema)` inferred `year` input as `unknown` while the form type expected `number`.
- **Root Cause**: `z.coerce.number()` changed Zod input/output typing in a way that conflicted with generic type inference in `useForm<VehicleFormValues>`.
- **Fix**: Switched `year` field schema to validated string input, then converted to number in payload mapping (`toVehiclePayload`).
- **Avoid**: In typed RHF forms, avoid mixed input/output coercion unless using explicit `useForm` generic input/output signatures.
- **Date**: 2026-04-11
