---
name: bug-triage
description: Reproduction-first bug workflow for reliable root-cause isolation, minimal patching, and regression protection.
---

# Bug Triage

Use this skill for defects, crashes, unexpected behavior, and regressions.

## Trigger Conditions

- User reports bug symptoms ("broken", "fails", "crash", "not working").
- There is a failing test or reproducible scenario.

## Workflow

1. Capture reproducible steps and expected vs actual behavior.
2. Confirm failure reproduction locally or from evidence.
3. Isolate probable subsystem and narrow root cause.
4. Apply smallest safe patch in bounded scope.
5. Add or run regression tests.
6. Validate fix and report residual risks.

## Output Contract

- Defect summary and reproduction
- Root cause hypothesis/finding
- Patch summary
- Validation evidence
- Regression coverage update

## Guardrails

- Do not patch without reproduction evidence unless explicitly approved.
- Avoid broad refactors in bug-fix flow.
- Block when scope or acceptance criteria are unsafe/unclear.
