---
agent: agent
description: Upgrade FleetWise Java runtime to the latest LTS version with full compile and test validation.
---

## Context

FleetWise currently targets Java 17 in Maven build properties. The user requested an upgrade to the latest Java LTS version.

## Decision

Upgrade Java runtime target to Java 25 (latest LTS) and align build tooling/runtime compatibility as needed. Keep changes minimal and focused on Java runtime compatibility, then validate by compiling main and test code and running the full test suite.

## Steps

1. Generate and review upgrade plan from the Java-upgrade workflow template.
2. Establish baseline compile/test results before making runtime changes.
3. Update runtime/build configuration for Java 25 compatibility.
4. Resolve compile/test regressions and validate 100% test pass.
5. Produce upgrade summary and follow-up recommendations.

## Acceptance Criteria

- Project runtime target is Java 25.
- Build tooling is compatible with Java 25.
- `mvn clean test-compile` succeeds.
- `mvn clean test` succeeds with 100% pass rate.

## Status

- [ ] Not started / [x] In progress / [ ] Complete
      Blockers (if any):
- None
