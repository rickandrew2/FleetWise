# Deterministic Slash Command Contracts

This directory is the modular source of truth for slash-command execution contracts.

- Global protocol and safety framework: `AGENTS.MD` → `Deterministic Slash Command System Standard`
- Global response schema: `agents/commands/SCHEMA.md`
- Command contracts:
  - `plan.md`
  - `task.md`
  - `fix.md`
  - `audit.md`
  - `perf.md`
  - `test.md`
  - `pr.md`
  - `release.md`
  - `docs.md`
  - `problem-map.md`
  - `scope-shape.md`
  - `arch-check.md`
  - `ux-bar.md`
  - `debug-track.md`
  - `risk-review.md`
  - `release-ready.md`
  - `learn-loop.md`

Execution requirements:
- Parse slash commands deterministically.
- Return structured output only.
- No conversational fallback in slash mode.
- Enforce destructive confirmation token: `CONFIRM-DESTRUCTIVE:<target>`.
- Enforce unique command purpose for each primary workflow command.

## Command Integrity Guards

- Primary workflow commands must have unique purpose identifiers.
- Duplicate command purpose definitions fail CLI startup validation.
- Deprecated aliases are not part of the active command surface.

## Publish Inclusion

The npm package includes command contracts from both:

- `agents/commands/` (root mirror)
- `templates/agents/commands/` (scaffold source)

## Workflow Command Mapping

Use these lifecycle commands as the recommended specialist sequence:

- `problem-map` -> `plan.md`
- `scope-shape` -> `plan.md`
- `arch-check` -> `plan.md`
- `ux-bar` -> `plan.md`
- `debug-track` -> `fix.md`
- `risk-review` -> `audit.md`
- `perf` -> `perf.md`
- `release-ready` -> `release.md`
- `docs` -> `docs.md`
- `learn-loop` -> `task.md`

The CLI command `agents-templated workflow` prints this lifecycle in order:

Think -> Plan -> Build -> Review -> Test -> Ship -> Reflect

