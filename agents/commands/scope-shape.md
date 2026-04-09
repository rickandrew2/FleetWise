# /scope-shape

## A. Intent
Constrain delivery scope to the smallest high-value reversible release.

## B. When to Use
- Use after problem framing and before architectural design.
- Do not use when requirements are already contractually frozen.

## C. Context Assumptions
- Problem map exists.
- Candidate feature list exists.
- Delivery constraints are known.

## D. Required Inputs
| Input | Type | Example |
|---------------------|------------|----------------------------------|
| `scope_goal` | string | "ship MVP in 2 weeks" |
| `candidate_items` | string[] | ["email login", "social login", "tutorial"] |
| `constraint_artifact` | artifact | timeline doc, staffing snapshot |

## E. Pre-Execution Guards <- fail fast, check ALL before running
- [ ] scope goal is explicit
- [ ] candidate items are ranked
- [ ] out-of-scope list can be produced

## F. Execution Flow
1. Rank candidate items by value and effort.
2. Draft in-scope and out-of-scope sets.
3. Check scope against constraints.
4. Decision point ->
   - condition A -> scope exceeds constraints -> trim to MVP
   - condition B ->  feasible scope -> continue.
5. Build scope rationale and tradeoffs.
6. Emit scope decision package.

## G. Output Schema

```json
{
  "scope_id": "string",
  "scope_in": ["array","of","strings"],
  "confidence": "low | medium | high",
  "scope_out": "string | null"
}
```

## H. Output Target
- Default delivery: stdout
- Override flag: --output=<target>

## I. Stop Conditions <- abort with error message, never emit partial output
- scope cannot satisfy constraints
- no explicit out-of-scope definition is produced

## J. Safety Constraints
- Hard block: hard block on hidden scope creep
- Warn only: warn when deferred items carry significant risk
