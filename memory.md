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
