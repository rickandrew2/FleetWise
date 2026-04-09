# /fix

## A. Intent
Apply the smallest safe code fix with regression evidence and bounded impact.

## B. When to Use
- Use after root cause is confirmed and a targeted fix is required.
- Do not use when defect cause is still speculative.

## C. Context Assumptions
- Issue is reproducible or sufficiently evidenced.
- Root cause has been identified.
- Regression checks are available.

## D. Required Inputs
| Input | Type | Example |
|---------------------|------------|----------------------------------|
| `defect_id` | string | "BUG-142" |
| `affected_paths` | string[] | ["src/auth.ts", "tests/auth.test.ts"] |
| `evidence` | artifact | stack trace, failing test output, screenshot |

## E. Pre-Execution Guards <- fail fast, check ALL before running
- [ ] root cause evidence is present
- [ ] fix scope is bounded
- [ ] regression checks are defined

## F. Execution Flow
1. Read defect evidence and failing paths.
2. Implement minimal change set.
3. Run targeted validations.
4. Decision point ->
   - condition A -> validation fails -> iterate fix or abort
   - condition B ->  validation passes -> continue.
5. Prepare change rationale and impact summary.
6. Emit fix package with evidence.

## G. Output Schema

```json
{
  "fix_id": "string",
  "changed_files": ["array","of","strings"],
  "risk": "low | medium | high",
  "rollback_note": "string | null"
}
```

## H. Output Target
- Default delivery: stdout
- Override flag: --output=<target>

## I. Stop Conditions <- abort with error message, never emit partial output
- no verified root-cause evidence
- regression validation unavailable for critical path

## J. Safety Constraints
- Hard block: no broad refactor inside fix-only workflow
- Warn only: warn when temporary workaround is used
