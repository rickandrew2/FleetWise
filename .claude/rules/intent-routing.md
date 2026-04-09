---
title: "Intent Routing & Command Selection"
description: "Apply when determining which rule applies, routing to correct execution pathway, or making command selection decisions"
version: "3.0.0"
tags: ["routing", "deterministic", "workflow", "commands"]
---

## Purpose

Provide deterministic routing from user intent to the correct execution pathway, with minimal clarification and strict safety behavior.

## Deterministic Routing Contract

1. Normalize input (trim, collapse whitespace, preserve semantic tokens).
2. Detect explicit slash command first.
3. If no slash command, run implicit intent routing against known command set.
4. Select exactly one command only when confidence is sufficient.
5. If ambiguous, return blocked output with decision-critical missing fields.
6. Never execute destructive actions without confirmation token.

## Confidence & Ambiguity Rules

- High confidence: execute selected contract.
- Medium confidence: execute with explicit assumptions in output.
- Low confidence: return `status: blocked` with 1-2 critical clarifications.
- Multi-intent input: split into ordered tasks only when boundaries are explicit; otherwise block.

## Minimal Clarification Policy

- Ask questions only when unsafe or logically blocked.
- Ask at most 2 questions per command cycle.
- Questions must be single-purpose and decision-critical.

## Structured Output Defaults

All routed executions must return schema-compliant output:
- `command`, `execution_id`, `mode`, `status`, `inputs`
- `prechecks`, `execution_log`, `artifacts`
- `risks`, `safety_checks`, `stop_condition`, `next_action`

## Safety Behavior

- Unknown slash command: structured error and stop.
- Ambiguous non-slash intent: blocked with minimal missing inputs.
- High-risk actions: blocked until explicit confirmation token is present.

## Guardrails Cross-Reference

When intent involves scope expansion, destructive actions, or agent behavioral safety, apply `agents/rules/guardrails.mdc` in addition to the primary route:

- Scope creep detected → Guardrails § Scope Control
- Destructive/irreversible action → Guardrails § Hard Stops + Reversibility Principle
- Agent accessing external systems beyond task scope → Guardrails § Minimal Footprint
- Repeated failure / silent retry → Guardrails § No Autonomous Escalation
