# /release

## A. Intent
Generate deterministic release decision package with rollout and rollback readiness.

## B. When to Use
- Use when deciding whether to ship to production.
- Do not use before release-ready checks are complete.

## C. Context Assumptions
- Release candidate is identified.
- Pre-release checks are completed.
- Rollback strategy is defined.

## D. Required Inputs
| Input | Type | Example |
|---------------------|------------|----------------------------------|
| `version` | string | "v2.4.0" |
| `gate_results` | string[] | ["tests-pass", "risk-review-pass"] |
| `release_artifacts` | artifact | release notes draft, migration plan |

## E. Pre-Execution Guards <- fail fast, check ALL before running
- [ ] version is valid and unique
- [ ] required gates are complete
- [ ] rollback plan exists

## F. Execution Flow
1. Collect gate outputs and release artifacts.
2. Validate rollout and rollback prerequisites.
3. Classify release risk.
4. Decision point ->
   - condition A -> high unresolved risk -> block release
   - condition B ->  acceptable risk -> continue.
5. Build release decision and communication payload.
6. Emit release package.

## G. Output Schema

```json
{
  "release_id": "string",
  "gates": ["array","of","strings"],
  "release_risk": "low | medium | high",
  "rollback_status": "string | null"
}
```

## H. Output Target
- Default delivery: stdout
- Override flag: --output=<target>

## I. Stop Conditions <- abort with error message, never emit partial output
- required gate missing or failed
- rollback plan is undefined

## J. Safety Constraints
- Hard block: hard block on unresolved high-severity release risks
- Warn only: warn when rollout is phased due to uncertainty
