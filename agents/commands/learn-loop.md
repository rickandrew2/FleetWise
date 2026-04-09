# /learn-loop

## A. Intent
Capture deterministic retrospective outcomes and convert lessons into next-cycle actions.

## B. When to Use
- Use after delivery milestones, incidents, or release cycles.
- Do not use for pre-implementation planning.

## C. Context Assumptions
- Cycle outcome data is available.
- Owners for follow-up actions can be assigned.
- Retrospective scope is defined.

## D. Required Inputs
| Input | Type | Example |
|---------------------|------------|----------------------------------|
| `cycle_name` | string | "Sprint 18" |
| `observations` | string[] | ["test flakiness", "scope churn"] |
| `evidence_artifact` | artifact | metrics dashboard, incident notes, PR links |

## E. Pre-Execution Guards <- fail fast, check ALL before running
- [ ] observations are evidence-backed
- [ ] action owners can be assigned
- [ ] follow-up window is defined

## F. Execution Flow
1. Collect outcomes, wins, and misses.
2. Identify root process issues and patterns.
3. Prioritize actionable improvements.
4. Decision point ->
   - condition A -> no actionable item -> request clearer observations
   - condition B ->  actionable set ready -> continue.
5. Map actions to owners and timelines.
6. Emit learn-loop action report.

## G. Output Schema

```json
{
  "loop_id": "string",
  "actions": ["array","of","strings"],
  "urgency": "low | medium | high",
  "blocker": "string | null"
}
```

## H. Output Target
- Default delivery: stdout
- Override flag: --output=<target>

## I. Stop Conditions <- abort with error message, never emit partial output
- retrospective inputs are anecdotal without evidence
- no owner can be assigned to critical actions

## J. Safety Constraints
- Hard block: hard block on publishing blame-focused output without actionable remediation
- Warn only: warn when metrics are incomplete but direction is still usable
