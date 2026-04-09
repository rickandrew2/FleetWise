# Subagents

Subagents are bounded agent processes that your orchestrator (main AI) can delegate tasks to with limited scope. Each subagent is defined as a single Markdown file with YAML frontmatter specifying its tools, model, and activation conditions.

## What Are Subagents?

- **Limited scope** — each subagent has one primary job and restricted tool access
- **Delegatable** — the orchestrator assigns tasks and subagents execute autonomously
- **Composable** — subagents work alongside skills; a subagent can invoke skills within its scope
- **Tool profile: copilot-compat** — YAML frontmatter is read natively by Claude Code; other tools treat the file as plain instructions

## Available Subagents

| Subagent | Model | Purpose |
|----------|-------|---------|
| `planner` | opus | Break down features into phased, ordered implementation plans |
| `architect` | opus | System design, ADRs, scalability trade-off analysis |
| `tdd-guide` | sonnet | Write tests first — Red-Green-Refactor lifecycle |
| `code-reviewer` | sonnet | Quality review with CRITICAL/HIGH/MEDIUM/LOW severity |
| `security-reviewer` | sonnet | OWASP Top 10 scan, secrets detection, auth review |
| `build-error-resolver` | sonnet | Fix build/type errors with minimal diff, no refactoring |
| `e2e-runner` | sonnet | Execute Playwright E2E test suites and report results |
| `refactor-cleaner` | sonnet | Remove dead code, unused deps, orphaned exports |
| `doc-updater` | haiku | Sync README, API docs, and codemaps after code changes |
| `performance-profiler` | sonnet | Diagnose latency and resource bottlenecks with measurable optimization plans |
| `dependency-auditor` | sonnet | Audit dependency risk, CVEs, and upgrade hygiene |
| `configuration-validator` | sonnet | Validate environment config, safety defaults, and deploy readiness |
| `database-migrator` | sonnet | Plan and review schema/data migrations with rollback and validation gates |
| `load-tester` | sonnet | Define load scenarios, thresholds, and release-quality performance gates |
| `compatibility-checker` | sonnet | Detect contract-breaking API changes and enforce versioning discipline |

## Model Selection Guide

| Model | When to use |
|-------|------------|
| `claude-opus-4-5` | Complex reasoning: feature planning, architecture decisions |
| `claude-sonnet-4-5` | Balanced: code review, security scan, testing, build fixes |
| `claude-haiku-4-5` | Lightweight: documentation sync, simple transformations |

## Invocation

### Claude Code (native)
Subagents are invoked automatically based on `description` matching, or explicitly:
```
/planner Build a multi-tenant authentication system
/code-reviewer Review the changes in src/auth/
```

### Other tools (Copilot, Cursor, Windsurf)
Reference the subagent file directly in your prompt or instruct your AI to follow the workflow defined in `.claude/agents/<name>.md`.

## File Structure

```
.claude/agents/
├── README.md                  ← This file
├── planner.md
├── architect.md
├── tdd-guide.md
├── code-reviewer.md
├── security-reviewer.md
├── build-error-resolver.md
├── e2e-runner.md
├── refactor-cleaner.md
├── doc-updater.md
├── performance-profiler.md
├── dependency-auditor.md
├── configuration-validator.md
├── database-migrator.md
├── load-tester.md
└── compatibility-checker.md
```

## Adding a New Subagent

```bash
agents-templated new-subagent <name>
```

Then add it to the Reference Index in `CLAUDE.md`.

## Guardrails

All subagents inherit the constraints from `CLAUDE.md`:
- Security and testing rules are non-overrideable
- Destructive actions require `CONFIRM-DESTRUCTIVE:<target>`
- Subagents must stop and report on failure, never silently retry
- No subagent may expose secrets, credentials, or PII
