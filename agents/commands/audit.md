# /audit

## A. Intent
Produce a deterministic risk and compliance audit with prioritized findings.

## B. When to Use
- Use before release or for targeted quality/security reviews.
- Do not use as a substitute for implementation planning.

## C. Context Assumptions
- Audit scope is defined.
- Relevant artifacts are available.
- Severity rubric is agreed.

## D. Required Inputs
| Input | Type | Example |
|---------------------|------------|----------------------------------|
| `audit_scope` | string | "authentication flows" |
| `checklist` | string[] | ["security", "tests", "rollback"] |
| `evidence_set` | artifact | PR diff, logs, reports |

## E. Pre-Execution Guards <- fail fast, check ALL before running
- [ ] scope is explicit
- [ ] evidence artifacts are accessible
- [ ] severity model is available

## F. Execution Flow
1. Collect scoped evidence and standards.
2. Evaluate checks against evidence.
3. Classify findings by severity.
4. Decision point ->
   - condition A -> critical unresolved finding -> block recommendation
   - condition B ->  no critical blocker -> continue.
5. Assemble remediation actions and owners.
6. Emit audit report.

## G. Output Schema

```json
{
  "audit_id": "string",
  "findings": ["array","of","strings"],
  "severity": "low | medium | high",
  "blocker": "string | null"
}
```

## H. Output Target
- Default delivery: stdout
- Override flag: --output=<target>

## I. Stop Conditions <- abort with error message, never emit partial output
- scope cannot be determined
- critical evidence is missing

## J. Safety Constraints
- Hard block: hard block when critical finding lacks mitigation
- Warn only: warn when medium findings are deferred
