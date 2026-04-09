# /plan

## A. Intent
Build a deterministic implementation plan with scoped phases and acceptance checks.

## B. When to Use
- Use when a feature or change request is approved for planning before coding starts.
- Do not use for post-incident debugging; use /debug-track instead.

## C. Context Assumptions
- Problem statement and objective are available.
- Primary stakeholders and delivery window are known.
- Scope boundaries can be explicitly defined.

## D. Required Inputs
| Input | Type | Example |
|---------------------|------------|----------------------------------|
| `objective` | string | "Ship onboarding v2" |
| `constraints` | string[] | ["2-week deadline", "no schema rewrite"] |
| `references` | artifact | PRD link, issue URL, screenshot |

## E. Pre-Execution Guards <- fail fast, check ALL before running
- [ ] objective is non-empty and testable
- [ ] constraints are explicit and non-contradictory
- [ ] required references are accessible

## F. Execution Flow
1. Collect requirements and constraints.
2. Split work into ordered phases and milestones.
3. Attach measurable acceptance criteria per phase.
4. Decision point ->
   - condition A -> phase risk > threshold -> add mitigation gate
   - condition B ->  otherwise -> continue with baseline plan.
5. Assemble plan artifacts and dependency map.
6. Emit final plan package.

## G. Output Schema

```json
{
  "plan_id": "string",
  "phases": ["array","of","strings"],
  "risk_level": "low | medium | high",
  "notes": "string | null"
}
```

## H. Output Target
- Default delivery: stdout
- Override flag: --output=<target>

## I. Stop Conditions <- abort with error message, never emit partial output
- any guard in section E fails
- acceptance criteria cannot be made measurable

## J. Safety Constraints
- Hard block: no hidden scope expansion beyond declared boundaries
- Warn only: allow proceed with warning when estimate confidence is low
