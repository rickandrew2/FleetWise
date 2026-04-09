---
name: error-patterns
description: >
  Activate this skill whenever something is broken, failing, erroring, or crashing, especially
  for build errors, type errors, database/migration failures, API/auth issues, and UI bugs.
  Also activate when the user says "fix this", "it's broken", "I'm getting an error", or
  "this keeps happening".
---

# Error Patterns

You are debugging with persistent memory. Every fix should be recorded so it does not need to be solved twice.

## Step 1 - Check Lessons First (Always)

Before writing any fix, read `.claude/rules/lessons-learned.md`.

- Search for symptoms that match the current error.
- If a match is found, apply the known fix directly and state that the known fix was used.
- If no match is found, continue to Step 2.

## Step 2 - Diagnose by Category

### [BUILD] Build / Type Errors
- Read the full error trace, not just the first line.
- Check for version mismatches, missing imports, circular dependencies, and config issues.
- Verify lock file and dependency sync.

### [DB] Database / Migration Errors
- Check whether the migration already ran.
- Verify DB URL and environment target.
- Check for schema drift between model and database.
- Avoid destructive migrations without a backup.

### [API/AUTH] API / Auth Errors
- Check token expiry, missing headers, and wrong scopes.
- Verify environment variables for keys/secrets.
- Check CORS and middleware order.
- Log request/response safely (no secrets).

### [UI] Frontend / UI Bugs
- Check hydration mismatches.
- Verify null/undefined guards and prop contracts.
- Check list keys and class conflicts.
- Confirm loading and error states are handled.

## Step 3 - Apply the Fix

- Make the minimum safe change needed.
- Do not expand scope beyond the reported error.

## Step 4 - Record the Lesson (Mandatory)

After every successful fix, append to `.claude/rules/lessons-learned.md` using:

```markdown
### [CATEGORY] Short title of the error
- **Symptom**: What the error looked like (message, behavior)
- **Root Cause**: Why it happened
- **Fix**: Exact steps or code that resolved it
- **Avoid**: What NOT to do next time
- **Date**: YYYY-MM-DD
```

## Step 5 - User Confirmation Message

After fixing and recording, respond with one of:

- `Fixed. I've saved this to lessons-learned so we won't have to solve it again.`
- `Found this in lessons-learned and applied the known fix.`
