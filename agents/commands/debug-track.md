# /debug-track

## A. Intent
Run root-cause-first debugging workflow and guarantee evidence-backed defect diagnosis.

## B. When to Use
- Use when behavior is broken, failing, or regressing in runtime.
- Do not use to apply speculative patches without diagnosis.

## C. Context Assumptions
- Defect symptom is captured.
- A reproduction path is available or can be derived.
- Runtime context is accessible.

## D. Required Inputs
| Input | Type | Example |
|---------------------|------------|----------------------------------|
| `defect_symptom` | string | "payment retries loop forever" |
| `repro_steps` | string[] | ["submit order", "disconnect network"] |
| `runtime_artifact` | artifact | error logs, trace screenshot, failing test |

## E. Pre-Execution Guards <- fail fast, check ALL before running
- [ ] reproduction path is actionable
- [ ] evidence can be collected at runtime
- [ ] investigation scope is bounded

## F. Execution Flow
1. Reproduce issue and capture trace.
2. Follow execution and state transitions.
3. Confirm root cause with evidence.
4. Decision point ->
   - condition A -> root cause unverified -> continue investigation
   - condition B ->  verified -> continue.
5. Draft minimal patch strategy and checks.
6. Emit debug investigation report.

## G. Output Schema

```json
{
  "debug_id": "string",
  "evidence": ["array","of","strings"],
  "certainty": "low | medium | high",
  "root_cause": "string | null"
}
```

## H. Output Target
- Default delivery: stdout
- Override flag: --output=<target>

## I. Stop Conditions <- abort with error message, never emit partial output
- issue cannot be reproduced with available context
- root cause cannot be evidenced

## J. Safety Constraints
- Hard block: hard block on symptom-only fixes without diagnosis
- Warn only: warn when reproduction is intermittent
