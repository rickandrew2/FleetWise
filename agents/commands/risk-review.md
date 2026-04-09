# /risk-review

## A. Intent
Perform deterministic release risk review with mitigation and rollback readiness.

## B. When to Use
- Use before merge or release approval on non-trivial changes.
- Do not use as a substitute for running tests.

## C. Context Assumptions
- Change set is available.
- Validation evidence exists.
- Deployment context is known.

## D. Required Inputs
| Input | Type | Example |
|---------------------|------------|----------------------------------|
| `change_set` | string | "payment retry + queue changes" |
| `validation_status` | string[] | ["unit pass", "integration pass"] |
| `deployment_artifact` | artifact | rollout plan, env config snapshot |

## E. Pre-Execution Guards <- fail fast, check ALL before running
- [ ] change set is fully described
- [ ] validation status is current
- [ ] rollback path is defined

## F. Execution Flow
1. Inspect behavior deltas and blast radius.
2. Rank risks by impact and likelihood.
3. Validate mitigations and rollback readiness.
4. Decision point ->
   - condition A -> high unresolved risk -> block recommendation
   - condition B ->  acceptable risk -> continue.
5. Build release risk summary and actions.
6. Emit risk review report.

## G. Output Schema

```json
{
  "review_id": "string",
  "risks": ["array","of","strings"],
  "risk_level": "low | medium | high",
  "recommendation": "string | null"
}
```

## H. Output Target
- Default delivery: stdout
- Override flag: --output=<target>

## I. Stop Conditions <- abort with error message, never emit partial output
- high-severity risk has no mitigation
- rollback readiness is undefined

## J. Safety Constraints
- Hard block: hard block on unresolved high-severity risks
- Warn only: warn when medium risks are accepted with owner
