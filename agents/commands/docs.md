# /docs

## A. Intent
Create deterministic documentation outputs aligned with current implementation behavior.

## B. When to Use
- Use when generating or updating docs as a direct deliverable.
- Do not use for release decision making.

## C. Context Assumptions
- Source behavior is known.
- Target audience is defined.
- Doc destination is available.

## D. Required Inputs
| Input | Type | Example |
|---------------------|------------|----------------------------------|
| `doc_scope` | string | "API auth endpoints" |
| `source_refs` | string[] | ["src/auth.ts", "openapi.yaml"] |
| `doc_artifact` | artifact | existing README path or docs URL |

## E. Pre-Execution Guards <- fail fast, check ALL before running
- [ ] scope is explicit
- [ ] source refs are accessible
- [ ] destination path is writable

## F. Execution Flow
1. Collect implementation references.
2. Draft structured documentation content.
3. Validate examples and references.
4. Decision point ->
   - condition A -> mismatch with implementation -> revise doc content
   - condition B ->  aligned -> continue.
5. Assemble final documentation package.
6. Emit documentation output.

## G. Output Schema

```json
{
  "doc_id": "string",
  "updated_sections": ["array","of","strings"],
  "confidence": "low | medium | high",
  "gap": "string | null"
}
```

## H. Output Target
- Default delivery: file
- Override flag: --output=<target>

## I. Stop Conditions <- abort with error message, never emit partial output
- source references are unavailable
- critical behavior cannot be documented accurately

## J. Safety Constraints
- Hard block: hard block on knowingly incorrect implementation claims
- Warn only: warn when sections remain TODO with owner
