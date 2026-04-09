# /task

## A. Intent
Convert approved plans into deterministic, execution-ready task batches.

## B. When to Use
- Use when a plan exists and work must be distributed to implementers.
- Do not use before planning is complete.

## C. Context Assumptions
- An approved plan exists.
- Owners and execution context are known.
- Dependencies can be sequenced.

## D. Required Inputs
| Input | Type | Example |
|---------------------|------------|----------------------------------|
| `plan_id` | string | "plan-2026-04-03" |
| `task_scope` | string[] | ["api", "ui", "tests"] |
| `source_artifacts` | artifact | plan file path or issue board URL |

## E. Pre-Execution Guards <- fail fast, check ALL before running
- [ ] plan exists and is approved
- [ ] task scope maps to explicit plan items
- [ ] dependency order is resolvable

## F. Execution Flow
1. Read plan outputs and dependency graph.
2. Split work into atomic tasks.
3. Assign execution order and ownership metadata.
4. Decision point ->
   - condition A -> blocking dependency found -> move item to blocked queue
   - condition B ->  no blockers -> keep in active queue.
5. Build task package with acceptance checks.
6. Emit task list and execution order.

## G. Output Schema

```json
{
  "task_batch_id": "string",
  "tasks": ["array","of","strings"],
  "priority": "low | medium | high",
  "blocker": "string | null"
}
```

## H. Output Target
- Default delivery: stdout
- Override flag: --output=<target>

## I. Stop Conditions <- abort with error message, never emit partial output
- plan_id is missing or invalid
- critical dependency cannot be resolved

## J. Safety Constraints
- Hard block: no task emission without traceability to a plan item
- Warn only: warn when owner assignment is temporary
