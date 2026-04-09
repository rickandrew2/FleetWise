# /perf

## A. Intent
Define and execute deterministic performance optimization workflow against known baselines.

## B. When to Use
- Use when improving latency, throughput, or resource efficiency.
- Do not use for one-off smoke checks; use /perf-scan for quick regression comparison.

## C. Context Assumptions
- Baseline metrics exist or can be captured.
- Performance target is defined.
- Measurement method is repeatable.

## D. Required Inputs
| Input | Type | Example |
|---------------------|------------|----------------------------------|
| `performance_goal` | string | "p95 latency under 200ms" |
| `baseline_metrics` | string[] | ["p95=260ms", "cpu=70%"] |
| `benchmark_artifact` | artifact | profiling report or benchmark output |

## E. Pre-Execution Guards <- fail fast, check ALL before running
- [ ] goal is measurable
- [ ] baseline metric set is present
- [ ] benchmark method is consistent

## F. Execution Flow
1. Capture or validate baseline metrics.
2. Apply targeted optimization changes.
3. Measure post-change metrics.
4. Decision point ->
   - condition A -> target unmet -> iterate optimization
   - condition B ->  target met -> continue.
5. Summarize gains, tradeoffs, and risks.
6. Emit performance optimization report.

## G. Output Schema

```json
{
  "perf_run_id": "string",
  "metrics": ["array","of","strings"],
  "impact": "low | medium | high",
  "regression": "string | null"
}
```

## H. Output Target
- Default delivery: stdout
- Override flag: --output=<target>

## I. Stop Conditions <- abort with error message, never emit partial output
- baseline cannot be measured reliably
- measurement method is non-deterministic

## J. Safety Constraints
- Hard block: do not trade correctness/security for performance gains
- Warn only: warn when gains are within noise threshold
