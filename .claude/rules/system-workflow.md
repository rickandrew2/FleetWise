---
title: "System Workflow Orchestration"
description: "Apply when planning delivery phases, defining acceptance criteria, establishing rollback procedures, or orchestrating multi-step workflows"
version: "3.0.0"
tags: ["workflow", "gates", "delivery", "governance"]
---

## Purpose

Define a repeatable lifecycle so all work is traceable, verifiable, and releasable.

## Workflow Phases

1. Discover
2. Plan
3. Implement
4. Verify
5. Release

## Phase Requirements

### 1) Discover
- Capture objective, scope boundaries, constraints, and risk profile.
- Produce: context summary + assumptions.

### 2) Plan
- Break work into atomic units and dependency order.
- Define acceptance criteria and rollback considerations.
- Produce: execution plan with checkpoints.

### 3) Implement
- Apply smallest safe changes within declared scope.
- Keep changes deterministic and reversible when possible.
- Produce: change summary and affected files.

### 4) Verify
- Run relevant tests/checks (targeted first, broad second).
- Confirm security and regression impact.
- Produce: validation evidence.

### 5) Release
- Check gates: tests, security posture, migration safety, rollback readiness.
- Produce: release decision + rollout/rollback steps.

## Required Delivery Artifacts

- Objective and scope
- Acceptance criteria
- Risk register
- Validation evidence
- Rollback strategy (for non-trivial changes)

## Gate Rules

- Fail any critical gate -> `status: blocked`.
- Missing rollback for risky changes -> blocked.
- Missing validation evidence -> blocked.

## Rollback Requirements

- Identify rollback trigger conditions.
- Provide explicit rollback steps.
- Keep backups/version references for destructive changes.
