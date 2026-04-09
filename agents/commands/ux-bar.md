# /ux-bar

## A. Intent
Assess UX quality bar and guarantee core interaction states are defined before build.

## B. When to Use
- Use when UX quality and accessibility need pre-implementation validation.
- Do not use as a replacement for visual design exploration.

## C. Context Assumptions
- Scope and architecture are available.
- Primary user flows are identified.
- Design references exist.

## D. Required Inputs
| Input | Type | Example |
|---------------------|------------|----------------------------------|
| `ux_goal` | string | "reduce checkout friction" |
| `flows` | string[] | ["cart", "payment", "confirmation"] |
| `design_artifact` | artifact | wireframes, Figma link, screenshots |

## E. Pre-Execution Guards <- fail fast, check ALL before running
- [ ] critical flows are enumerated
- [ ] interaction states include loading/error/empty
- [ ] accessibility checks are defined

## F. Execution Flow
1. Review flows and interaction states.
2. Evaluate accessibility and usability risks.
3. Score UX gaps by severity.
4. Decision point ->
   - condition A -> critical UX gap -> block readiness
   - condition B ->  manageable gaps -> continue.
5. Build prioritized improvement list.
6. Emit UX quality package.

## G. Output Schema

```json
{
  "ux_review_id": "string",
  "ux_gaps": ["array","of","strings"],
  "severity": "low | medium | high",
  "blocker": "string | null"
}
```

## H. Output Target
- Default delivery: stdout
- Override flag: --output=<target>

## I. Stop Conditions <- abort with error message, never emit partial output
- critical flow lacks defined interaction states
- accessibility baseline is not addressed

## J. Safety Constraints
- Hard block: hard block on unaddressed critical accessibility failures
- Warn only: warn when medium UX issues are deferred
