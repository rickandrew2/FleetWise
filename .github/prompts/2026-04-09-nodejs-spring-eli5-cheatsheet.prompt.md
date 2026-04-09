---
agent: agent
description: Add a one-page Node.js to Spring Boot ELI5 cheat sheet and connect it to existing docs.
---

## Context

The user requested a very simple explanation of Java and Spring concepts (folders, packages, DTO, etc.) using a Node.js/TypeScript/Next.js mental model.

## Decision

Create a dedicated one-page cheat sheet in agent-docs that explains core concepts in plain language and includes direct Node.js mappings, then link it from the existing project guide.

## Steps

1. Draft one-page cheat sheet with ELI5 metaphor + concept mapping.
2. Explain folder structure (`src/main/java`, `com`, package paths) and common Spring annotations.
3. Add practical request flow and first steps for building a feature.
4. Link the cheat sheet from the project folder guide.

## Acceptance Criteria

- Cheat sheet is beginner-friendly and short enough to use as a quick reference.
- Includes clear explanations for package/folder basics and DTO/service/repository/entity roles.
- Includes Node.js/TypeScript analogies for each key concept.
- Existing docs point to the new cheat sheet.

## Status

- [ ] Not started / [ ] In progress / [x] Complete
      Blockers (if any):
- None.
