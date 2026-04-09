# /problem-map

## A. Intent
Frame the real user problem and guarantee a clear problem statement before planning.

## B. When to Use
- Use at the start of a feature cycle when pain points are unclear or broad.
- Do not use for implementation details or code-level debugging.

## C. Context Assumptions
- User objective is available.
- Stakeholder context can be collected.
- Outcome can be stated as a measurable problem.

## D. Required Inputs
| Input | Type | Example |
|---------------------|------------|----------------------------------|
| `user_problem` | string | "onboarding drop-off at step 2" |
| `signals` | string[] | ["support tickets", "analytics"] |
| `evidence_artifact` | artifact | funnel screenshot, issue links |

## E. Pre-Execution Guards <- fail fast, check ALL before running
- [ ] problem statement is concrete
- [ ] signals support the stated pain
- [ ] scope remains problem-focused

## F. Execution Flow
1. Collect user pain signals and context.
2. Synthesize candidate problem statements.
3. Validate statement against evidence.
4. Decision point ->
   - condition A -> weak evidence -> request stronger signals
   - condition B ->  strong evidence -> continue.
5. Produce framed problem and success criteria.
6. Emit problem map package.

## G. Output Schema

```json
{
  "problem_id": "string",
  "core_pains": ["array","of","strings"],
  "urgency": "low | medium | high",
  "unknowns": "string | null"
}
```

## H. Output Target
- Default delivery: stdout
- Override flag: --output=<target>

## I. Stop Conditions <- abort with error message, never emit partial output
- problem statement remains vague
- evidence contradicts proposed framing

## J. Safety Constraints
- Hard block: hard block on fabricated assumptions presented as facts
- Warn only: warn when evidence quality is limited
