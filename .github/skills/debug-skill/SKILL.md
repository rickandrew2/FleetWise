---
name: debug-skill
description: Enables breakpoint-driven debugging workflows with execution tracing, state inspection, and root-cause proof before patching.
---

# Debug Skill

Use this skill when the user is stuck on a bug and needs debugger-style investigation instead of print-guessing.

## Trigger Conditions

- User asks to debug, set breakpoints, step through code, or inspect runtime state.
- A bug is intermittent, difficult to localize, or likely branch/state dependent.
- Existing logs are insufficient to prove root cause.

## Workflow

1. Confirm a reproducible scenario and expected vs actual behavior.
2. Define a checkpoint plan (breakpoints/log points/frames).
3. Trace execution order across checkpoints.
4. Capture variable-state transitions at failure boundaries.
5. Prove root cause with execution evidence.
6. Apply the smallest safe patch and run focused regressions.

## Output Contract

- Reproduction summary
- Checkpoint plan
- Execution trace highlights
- State snapshots at critical steps
- Root-cause finding
- Minimal patch plan
- Regression checks and residual risk

## Guardrails

- Do not ship speculative fixes without trace-backed evidence.
- Do not skip regression validation after state-sensitive fixes.
- If reproduction fails, stop and request missing runtime context.
