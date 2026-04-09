---
agent: agent
description: Add a tiny visual quickstart doc that explains one real API request flow in ELI5 style for Node.js developers new to Spring.
---

## Context

User requested a very simple visual explanation after receiving the ELI5 cheat sheet, specifically asking to proceed with a tiny visual request-flow page.

## Decision

Create a compact visual onboarding doc using one real endpoint (`POST /api/vehicles`) and map each Spring component to familiar Node.js concepts.

## Steps

1. Create a one-page visual doc in agent-docs.
2. Include simple analogy and request flow from filter to controller to service to repository.
3. Include a short "where this lives in code" section.
4. Link from existing docs for discoverability.

## Acceptance Criteria

- New doc is short and readable in 2 minutes.
- Uses one real request flow from current implementation.
- Includes Node.js concept mapping.
- Linked from current onboarding docs.

## Status

- [ ] Not started / [ ] In progress / [x] Complete
      Blockers (if any):
- None.
