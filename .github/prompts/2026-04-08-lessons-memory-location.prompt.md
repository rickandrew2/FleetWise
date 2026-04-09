---
agent: true
description: Move lessons learned storage to memory.md at project root.
---

## Context
Lessons learned entries were being appended in `.claude/rules/lessons-learned.md`, but project preference is to keep resolved lessons in a dedicated `memory.md` file at repository root.

## Decision
Use `memory.md` as the canonical lessons store and keep `.claude/rules/lessons-learned.md` as policy/instructions only.

## Steps
1. Create `memory.md` in project root with lesson template and current known lesson entry.
2. Update `.claude/rules/lessons-learned.md` workflow text to reference `memory.md`.
3. Remove migrated lesson entry from `.claude/rules/lessons-learned.md` to avoid duplication.

## Acceptance Criteria
- `memory.md` exists at project root.
- Current lesson entry is present in `memory.md`.
- `.claude/rules/lessons-learned.md` references `memory.md` for lesson checks and appends.
- No duplicate lesson entry remains in `.claude/rules/lessons-learned.md`.

## Status
- [ ] Not started
- [ ] In progress
- [x] Complete
Blockers (if any):
- None
