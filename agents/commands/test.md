# /test

## A. Intent
Run deterministic validation and gate release readiness based on test evidence.

## B. When to Use
- Use when validating behavior after implementation or before merge/release.
- This command now includes quality-gate behavior; do not use /quality-gate.

## C. Context Assumptions
- Test targets are defined.
- Required environments are available.
- Pass/fail criteria are explicit.

## D. Required Inputs
| Input | Type | Example |
|---------------------|------------|----------------------------------|
| `test_scope` | string | "api regression" |
| `test_suites` | string[] | ["unit", "integration", "critical-flow"] |
| `evidence_artifact` | artifact | test report path or CI run URL |

## E. Pre-Execution Guards <- fail fast, check ALL before running
- [ ] test scope is non-empty
- [ ] critical test suites are executable
- [ ] pass criteria are declared

## F. Execution Flow
1. Collect test targets and runtime config.
2. Execute suites in deterministic order.
3. Aggregate results and failures.
4. Decision point ->
   - condition A -> critical failures present -> gate = blocked
   - condition B ->  no critical failures -> gate = pass.
5. Build validation evidence and remediation list.
6. Emit test and gate report.

## G. Output Schema

```json
{
  "test_run_id": "string",
  "results": ["array","of","strings"],
  "gate_status": "low | medium | high",
  "blocker": "string | null"
}
```

## H. Output Target
- Default delivery: stdout
- Override flag: --output=<target>

## I. Stop Conditions <- abort with error message, never emit partial output
- critical test suite cannot run
- result schema cannot be produced

## J. Safety Constraints
- Hard block: hard block when critical flow tests fail
- Warn only: warn when flaky tests are excluded with justification
