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

### [UI] Hook order regression from conditional render placement
- **Symptom**: New module pages crashed during tests with `Rendered more hooks than during the previous render` and lint flagged `react-hooks/rules-of-hooks`.
- **Root Cause**: `useMemo` was declared after early loading/error returns, so hook call order changed between renders.
- **Fix**: Move all hooks (including memoized maps) above conditional returns and memoize directly from stable query references.
- **Avoid**: Never place hook declarations below any possible return path in React components.
- **Date**: 2026-04-11

### [TYPE] TanStack Query mutation function test assertions changed
- **Symptom**: Vitest assertions on mocked mutation functions failed even though UI actions executed correctly.
- **Root Cause**: TanStack Query mutation functions received a second context argument, so strict `toHaveBeenCalledWith(singleArg)` assertions no longer matched.
- **Fix**: Assert on the first call first argument (`mock.calls[0][0]`) instead of exact full argument list.
- **Avoid**: Do not assume mutation functions are invoked with variables only when testing Query v5 hooks.
- **Date**: 2026-04-11

### [UI] Shared Badge component only supports limited variants
- **Symptom**: Build failed with TypeScript error when using `variant="destructive"` on `Badge`.
- **Root Cause**: The local `Badge` implementation exposes only `default`, `secondary`, and `outline` variants.
- **Fix**: Use supported variant and apply destructive styling via `className` when needed.
- **Avoid**: Do not introduce unsupported variant names without extending component variant definitions.
- **Date**: 2026-04-11

### [UI] Form label collisions broke RTL selectors
- **Symptom**: Frontend tests failed with "Found multiple elements with the text of: Vehicle" after adding new filter dropdowns.
- **Root Cause**: Both filter controls and create-form fields used the same label text, making broad `getByLabelText('Vehicle')` selectors ambiguous.
- **Fix**: Scope selectors to the intended element (for example `getByLabelText('Vehicle', { selector: 'select#vehicleId' })`) in form submission tests.
- **Avoid**: Do not rely on unscoped label queries when pages contain repeated labels across filters and forms.
- **Date**: 2026-04-11

### [BUILD] Dev seeder crashed due negative list index
- **Symptom**: Spring context startup failed with `ArrayIndexOutOfBoundsException: Index -3 out of bounds for length 5` in `DevDataLoader.seedFuelLogs`.
- **Root Cause**: Station index used `%` with a negative `plate.hashCode()`, producing a negative result for list indexing.
- **Fix**: Replaced modulo indexing with `Math.floorMod(i + plate.hashCode(), stations.size())` before reading the station list.
- **Avoid**: Do not use raw `%` for collection indexes when the left operand can be negative.
- **Date**: 2026-04-11

### [UI] Optional directory lookups should not block core pages
- **Symptom**: Fuel Logs, Routes, and Alerts rendered full-page errors when `/api/users` returned 404, even though core page data endpoints were available.
- **Root Cause**: Pages treated the users lookup as a required query and included it in blocking error state.
- **Fix**: Made users lookup non-blocking, kept core queries as gatekeepers, and added a small helper message/fallback driver option when user directory fetch fails.
- **Avoid**: Do not fail entire page renders for secondary enrichment/filter queries.
- **Date**: 2026-04-11

### [UI] Avoid sync setState in effects for derived pagination
- **Symptom**: Frontend lint failed with `react-hooks/set-state-in-effect` after pagination changes.
- **Root Cause**: Component used `useEffect` only to clamp page state (`setCurrentPage(totalPages)`), creating an avoidable cascade render pattern.
- **Fix**: Removed the effect and used a derived `normalizedCurrentPage` value for rendering, slicing, and pagination controls.
- **Avoid**: Do not use effects to sync state that can be derived directly during render.
- **Date**: 2026-04-11

### [CONFIG] pgAdmin container restart loop from invalid default email domain
- **Symptom**: `fleetwise-pgadmin` kept restarting and logs reported `does not appear to be a valid email address`.
- **Root Cause**: `PGADMIN_DEFAULT_EMAIL` default used `admin@fleetwise.local`, which pgAdmin's validator rejected.
- **Fix**: Changed Docker Compose default to a valid email (`admin@fleetwise.com`) and recreated the pgAdmin container.
- **Avoid**: Do not use `.local` placeholder domains for pgAdmin default login email unless confirmed accepted by the current image validator.
- **Date**: 2026-04-12

### [CONFIG] pgAdmin login spinner from CSRF bootstrap mismatch on localhost
- **Symptom**: Login page stayed on spinner or returned `The CSRF token is missing. You need to refresh the page.` in some browser sessions.
- **Root Cause**: In server mode, pgAdmin login bootstrap occasionally hit CSRF/session mismatch with cached browser session state.
- **Fix**: Set `PGADMIN_CONFIG_SERVER_MODE: "False"` in Docker Compose for local development and recreated the pgAdmin container.
- **Avoid**: For local single-user Docker setups, prefer pgAdmin desktop mode to avoid login bootstrap/CSRF friction.
- **Date**: 2026-04-12

### [CONFIG] pgAdmin desktop mode caused 401 Unauthorized in normal browser
- **Symptom**: `http://localhost:5050` returned plain `401 Unauthorized` in real browsers while integrated session seemed to work.
- **Root Cause**: For this containerized setup, forcing `PGADMIN_CONFIG_SERVER_MODE=False` made standard browser access fail consistently.
- **Fix**: Removed `PGADMIN_CONFIG_SERVER_MODE` override, recreated pgAdmin, and used standard server-mode login page.
- **Avoid**: Do not force desktop mode in this Docker setup unless fully validating regular browser access routes.
- **Date**: 2026-04-12

### [CONFIG] pgAdmin infinite spinner with RequireJS mismatch in normal browser
- **Symptom**: Login page stayed on infinite spinner and browser console showed `Uncaught Error: Mismatched anonymous define()` from `require.min.js`.
- **Root Cause**: Browser-side module bootstrap conflict (stale cached assets and/or extension-injected scripts) while server assets were healthy.
- **Fix**: Kept pgAdmin in server mode, added `PGADMIN_CONFIG_SEND_FILE_MAX_AGE_DEFAULT: 0`, exposed a fresh origin port `5051`, and reopened pgAdmin from that new origin.
- **Avoid**: If RequireJS mismatch appears again, use a clean origin/profile (new port or incognito with extensions disabled) before assuming backend failure.
- **Date**: 2026-04-12

### [DB] New integration tests must clear dependent tables before users
- **Symptom**: Backend suite failed in `FuelPriceControllerIntegrationTest` setup with `Referential integrity constraint violation ... FK_FUEL_LOGS_DRIVER` when deleting from `users`.
- **Root Cause**: Shared Spring test context kept seeded rows in dependent tables (`fuel_logs`, `route_logs`, `alerts`, `vehicles`) that still referenced `users`.
- **Fix**: In setup, delete tables in dependency order (`alerts`, `route_logs`, `fuel_logs`, `vehicles`, feature table) before `userRepository.deleteAll()`.
- **Avoid**: Do not delete `users` first in integration tests that run in a shared in-memory DB context.
- **Date**: 2026-04-12

### [CONFIG] Persistent pgAdmin spinner resolved by CloudBeaver fallback
- **Symptom**: pgAdmin kept spinning in normal browser profiles even after cache and origin workarounds.
- **Root Cause**: Client-side RequireJS/bootstrap conflicts can persist per browser profile and are hard to fully eliminate quickly.
- **Fix**: Added `cloudbeaver` service in Docker Compose (`localhost:8978`) as the primary browser DB UI fallback.
- **Avoid**: Do not block local DB browsing workflows on pgAdmin-only path when browser-specific script conflicts persist.
- **Date**: 2026-04-12

### [UI] Optional feature widgets must not block dashboard
- **Symptom**: Dashboard rendered full-page `Unable to load dashboard` with 404 after adding fuel-price widgets.
- **Root Cause**: Optional fuel-price queries were included in the dashboard's blocking error/loading gates.
- **Fix**: Restrict blocking gates to core dashboard queries only (`summary`, `top-drivers`, `cost-trend`) and render fuel widgets with non-blocking fallback messaging.
- **Avoid**: Do not include optional enrichment queries in top-level page failure gates.
- **Date**: 2026-04-12

### [UI] Dashboard fallback test was too strict for async label state
- **Symptom**: `dashboard-page.test.tsx` failed with "Unable to find ... Estimated from recent fill-up logs" while the UI rendered "Recent fill-up fallback".
- **Root Cause**: The test expected only the final fallback label, but the component can validly render an interim label before fallback queries finish.
- **Fix**: Relaxed assertion to accept either valid label using regex (`/Recent fill-up fallback|Estimated from recent fill-up logs/`).
- **Avoid**: Do not assert a single static label when component copy can legitimately vary across async fallback states.
- **Date**: 2026-04-12

### [BUILD] Optional email infrastructure should not block app startup
- **Symptom**: Full backend test suite failed to boot Spring context with `No qualifying bean of type 'org.springframework.mail.javamail.JavaMailSender' available` after adding notification service.
- **Root Cause**: `EmailNotificationService` used required constructor injection for `JavaMailSender`, but mail auto-configuration was not guaranteed in every environment.
- **Fix**: Switched to optional injection (`@Autowired(required = false)`), guarded send path when mail sender is absent, and kept send failures non-blocking.
- **Avoid**: Do not hard-require infrastructure beans for auxiliary features; provide a safe no-op path when integrations are unavailable.
- **Date**: 2026-04-12

### [API/AUTH] New endpoint returns 404 on stale backend runtime
- **Symptom**: Settings page showed `Unable to load settings` with `Request failed with status code 404` for `/api/users/me/preferences`.
- **Root Cause**: Browser was hitting an older Spring Boot process that did not include the new endpoint/migration yet.
- **Fix**: Stopped the process on port 8080, restarted backend from current source (`mvnw.cmd spring-boot:run`), and verified endpoint presence via `/v3/api-docs`.
- **Avoid**: After adding new controllers/routes, always restart the backend process and verify OpenAPI paths before debugging frontend code.
- **Date**: 2026-04-12
