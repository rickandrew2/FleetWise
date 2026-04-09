# /pr

## A. Intent
Prepare a deterministic pull request package with implementation and validation evidence.

## B. When to Use
- Use after code changes and validation are complete and review package is needed.
- Do not use when critical findings are still unresolved.

## C. Context Assumptions
- Change set exists.
- Validation evidence is available.
- Linked issue/task context is known.

## D. Required Inputs
| Input | Type | Example |
|---------------------|------------|----------------------------------|
| `change_summary` | string | "add retry policy to webhook worker" |
| `linked_items` | string[] | ["ISSUE-18", "TASK-42"] |
| `validation_evidence` | artifact | test report, benchmark output, screenshot |

## E. Pre-Execution Guards <- fail fast, check ALL before running
- [ ] change summary is complete
- [ ] linked items are resolvable
- [ ] validation evidence is present

## F. Execution Flow
1. Collect changed files and linked references.
2. Summarize intent, impact, and scope.
3. Attach validation and risk evidence.
4. Decision point ->
   - condition A -> critical blocker open -> abort PR package
   - condition B ->  no blocker -> continue.
5. Build reviewer checklist and rollout notes.
6. Emit PR payload.

## G. Output Schema

```json
{
  "title": "string",
  "files_changed": ["array","of","strings"],
  "risk_assessment": "low | medium | high",
  "blockers": "string | null"
}
```

## H. Output Target
- Default delivery: stdout
- Override flag: --output=<target>

## I. Stop Conditions <- abort with error message, never emit partial output
- validation evidence missing for critical changes
- open critical findings remain unresolved

## J. Safety Constraints
- Hard block: hard block if critical issues remain
- Warn only: warn when non-critical follow-up items are deferred
