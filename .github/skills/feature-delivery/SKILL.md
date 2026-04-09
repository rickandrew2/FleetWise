---
name: feature-delivery
description: Converts vague feature requests into scoped execution contracts with acceptance criteria, risk controls, and validation steps.
---

# Feature Delivery

Use this skill when users describe features informally and need a systematic execution path.

## Trigger Conditions

- User asks to "build", "add", "create", or "implement" a feature.
- Requirements are partial, broad, or non-technical.
- Scope and acceptance criteria are not explicit.

## Workflow

1. Parse objective from user language.
2. Define scope boundaries (in/out).
3. Generate acceptance criteria (2-5 concrete checks).
4. Derive implementation units in dependency order.
5. Define validation plan (unit/integration/e2e where relevant).
6. Identify risks and rollback notes.

## Output Contract

- Objective summary
- Scope boundaries
- Acceptance criteria
- Execution steps
- Verification steps
- Risks and assumptions

## Guardrails

- Ask minimal clarifications only when blocked.
- Do not execute destructive or broad changes without explicit scope.
- Keep plans deterministic and auditable.
