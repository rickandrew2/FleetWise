---
title: "Planning Discipline"
description: "Apply when implementing features, designing systems, discussing architecture, or making implementation decisions. Produces reusable prompt plan files"
version: "3.0.0"
tags: ["planning", "workflow", "documentation", "prompts"]
alwaysApply: true
---

## Purpose

Ensure every feature discussion, design decision, or implementation produces a reusable prompt plan stored in `.github/prompts/`. Plans persist across sessions and serve as living context for future work — they are never discarded.

## When to Apply

This rule is always active. Trigger when:

- User asks to implement a new feature
- A design or architecture decision is being made
- A significant refactor is planned
- A bug fix requires non-trivial investigation or systemic changes
- A discussion produces decisions that affect future work

## Plan File Convention

**Location:** `.github/prompts/`
**Filename:** `YYYY-MM-DD-{feature-slug}.prompt.md`
**Format:** VS Code reusable prompt (`.prompt.md` — usable as an `@workspace` prompt in Copilot Chat)

## Required Sections

Each plan file must contain:

```
---
agent: agent
description: One-line summary of what this plan covers.
---

## Context
Brief background — what problem are we solving and why now.

## Decision
What we decided to do and the reasoning behind it (not just what, but why).

## Steps
Numbered implementation steps in dependency order.

## Acceptance Criteria
Concrete, testable outcomes that define "done".

## Status
- [ ] Not started  /  [ ] In progress  /  [x] Complete
Blockers (if any):
```

## Workflow

1. At the start of any feature discussion or implementation, create the plan file immediately.
2. Use the filename convention: `YYYY-MM-DD-{feature-slug}.prompt.md`.
3. Fill out **Context**, **Decision**, and **Steps** before starting implementation.
4. Update **Status** and **Acceptance Criteria** incrementally as work progresses.
5. Mark the plan complete when implementation is verified and accepted.

## Guardrails

- Do not skip plan creation for "small" features — small decisions accumulate into undocumented technical debt.
- Plans are never deleted — they form a historical record of architectural decisions.
- Plan files must not contain secrets, credentials, or PII.
- If a plan changes significantly mid-implementation, update it in place rather than creating a new one.
