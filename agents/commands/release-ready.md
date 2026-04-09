# /release-ready

## A. Intent
Validate pre-release readiness gates and guarantee deploy prerequisites are satisfied.

## B. When to Use
- Use after implementation/tests/risk review and before final release decision.
- Do not use to execute deployment itself.

## C. Context Assumptions
- Release candidate exists.
- All required gate outputs are available.
- Rollout and rollback plans are drafted.

## D. Required Inputs
| Input | Type | Example |
|---------------------|------------|----------------------------------|
| `candidate_version` | string | "v2.4.0-rc1" |
| `gate_artifacts` | string[] | ["test", "risk-review", "perf-scan"] |
| `release_artifact` | artifact | release checklist, migration plan |

## E. Pre-Execution Guards <- fail fast, check ALL before running
- [ ] mandatory gates are present
- [ ] critical blockers are resolved
- [ ] rollback steps are executable

## F. Execution Flow
1. Collect gate evidence for candidate.
2. Validate checklist completion.
3. Verify rollout and rollback readiness.
4. Decision point ->
   - condition A -> missing mandatory gate -> block readiness
   - condition B ->  all gates complete -> continue.
5. Assemble readiness summary and open items.
6. Emit release-ready report.

## G. Output Schema

```json
{
  "readiness_id": "string",
  "gate_status": ["array","of","strings"],
  "readiness_risk": "low | medium | high",
  "blocker": "string | null"
}
```

## H. Output Target
- Default delivery: stdout
- Override flag: --output=<target>

## I. Stop Conditions <- abort with error message, never emit partial output
- mandatory gate missing or failed
- rollback execution path is unverified

## J. Safety Constraints
- Hard block: hard block when release checklist is incomplete on critical items
- Warn only: warn when non-critical items are deferred
