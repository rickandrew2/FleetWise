---
title: "Development Workflow Guidelines"
description: "Apply to optimize development process, running pre-commit checks, enforcing quality gates, and keeping project healthy"
alwaysApply: false
version: "3.0.0"
tags: ["workflow", "quality", "validation", "commit"]
globs:
  - "**/*"
---

# Development Workflow Guidelines

Technology-agnostic suggestions for keeping the project healthy and maintaining quality before commits.

## When This Rule Applies

Use this guidance when the user asks about workflow, pre-commit checks, or keeping the project validated. Do not force these steps on every change—treat them as recommended practices.

## Project Health (agents-templated)

If this project was scaffolded with agents-templated:

- **Periodically**: Run `agents-templated validate` to ensure required docs and rules are present; run `agents-templated doctor` for environment and configuration recommendations.
- **After pulling template updates**: Run `agents-templated update --check-only` to see available updates, then `agents-templated update` if you want to apply them (with backup).

These commands help keep agent rules, documentation, and security patterns in sync.

## Pre-Commit Quality Gates

Before committing, consider running (in order):

1. **Lint** – Your stack’s linter (e.g. ESLint, Pylint, golangci-lint) to catch style and simple issues.
2. **Type check** – If applicable (TypeScript, mypy, etc.) to catch type errors.
3. **Tests** – At least the fast test suite (e.g. unit tests) so regressions are caught early.
4. **Project validation** – For agents-templated projects, `agents-templated validate` can confirm setup is intact.

For high-risk or distributed-client releases, add hardening-specific checks before merge/release:

5. **Hardening profile selection** – Document threat model and selected profile from `agents/rules/hardening.mdc`.
6. **Post-hardening verification** – Run functional regression and performance checks on hardened artifacts.
7. **Artifact controls** – Verify restricted handling for symbol/mapping/debug artifacts and confirm rollback path.

Do not commit with failing lint or tests unless the user explicitly requests a WIP commit (e.g. with `--no-verify` or a clear “work in progress” message).

## Commit Messages

- Prefer clear, descriptive messages (e.g. conventional commits: `feat:`, `fix:`, `docs:`).
- Reference issues or tickets when relevant (e.g. “Closes #123”).
- Keep the body focused on what changed and why, not how.

## Summary

- Use **agents-templated validate** and **doctor** to maintain project setup and get recommendations.
- Run **lint**, **type check**, and **tests** before committing when possible.
- For hardening-required releases, include profile rationale, post-hardening verification evidence, and rollback readiness.
- Write clear commit messages and reference issues where applicable.
