# /arch-check

## A. Intent
Validate architecture readiness and implementation constraints before build begins.

## B. When to Use
- Use after scope lock and before implementation starts.
- Do not use for post-release retrospectives.

## C. Context Assumptions
- Scope is frozen for current increment.
- Architecture options are documented.
- Test strategy can be defined.

## D. Required Inputs
| Input | Type | Example |
|---------------------|------------|----------------------------------|
| `architecture_goal` | string | "multi-tenant API isolation" |
| `design_options` | string[] | ["shared schema", "schema-per-tenant"] |
| `design_artifact` | artifact | ADR doc, sequence diagram |

## E. Pre-Execution Guards <- fail fast, check ALL before running
- [ ] design options are comparable
- [ ] key edge cases are identified
- [ ] test strategy exists for selected design

## F. Execution Flow
1. Review architecture options and constraints.
2. Evaluate edge cases and failure modes.
3. Validate selected option against requirements.
4. Decision point ->
   - condition A -> critical gap found -> block readiness
   - condition B ->  no critical gaps -> continue.
5. Build architecture decision and test implications.
6. Emit architecture readiness report.

## G. Output Schema

```json
{
  "architecture_id": "string",
  "decisions": ["array","of","strings"],
  "risk_level": "low | medium | high",
  "blocker": "string | null"
}
```

## H. Output Target
- Default delivery: stdout
- Override flag: --output=<target>

## I. Stop Conditions <- abort with error message, never emit partial output
- selected architecture lacks testable validation path
- critical edge case has no mitigation

## J. Safety Constraints
- Hard block: hard block on architecture with unresolved critical failure modes
- Warn only: warn when non-critical tradeoffs are accepted
