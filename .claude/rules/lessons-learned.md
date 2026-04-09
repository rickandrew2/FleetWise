---
alwaysApply: true
title: "Lessons Learned"
description: "Apply before debugging: check known error patterns first, then record every new resolved fix"
version: "1.0.0"
tags: ["debugging", "memory", "lessons", "error-patterns"]
---

# Lessons Learned - Persistent Error Memory

## Purpose

This rule is the agent's long-term memory for recurring errors and fixes.
Always check `memory.md` in project root before debugging. If a matching error exists, apply that known fix first.

## Required Workflow

1. Before debugging, check `memory.md` for matching symptoms.
2. If a match is found, apply the known fix immediately.
3. If no match is found, debug using the `error-patterns` skill checklist.
4. After every successful fix, append a new lesson entry to `memory.md`.

## Lesson Entry Format

```markdown
### [CATEGORY] Short title of the error
- **Symptom**: What the error looked like (message, behavior)
- **Root Cause**: Why it happened
- **Fix**: Exact steps or code that resolved it
- **Avoid**: What NOT to do next time
- **Date**: YYYY-MM-DD
```

Allowed categories: `[BUILD]` `[DB]` `[API/AUTH]` `[UI]` `[TYPE]` `[CONFIG]` `[OTHER]`

## Enforcement

- This rule is non-overrideable.
- Lesson recording is mandatory after each resolved error.
- Do not remove existing lessons unless explicitly requested.

## Known Lessons

<!-- New lessons are appended below this line. Do not remove this comment. -->

Entries are maintained in `memory.md` at project root.
