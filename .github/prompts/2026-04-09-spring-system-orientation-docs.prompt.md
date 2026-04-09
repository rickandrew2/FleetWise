---
agent: agent
description: Rewrite the system folder guide into a beginner-friendly Spring Boot orientation for a Node.js/TypeScript full-stack developer.
---

## Context

The current project folder guide is concise but does not fully bridge concepts for a developer coming from Node.js, TypeScript, Next.js, and Prisma.

## Decision

Rewrite the guide to explain architecture, request lifecycle, Spring Boot and Jakarta annotations, security flow, data layer, migrations, configuration, and testing using direct concept mappings to Node/Next/Prisma.

## Steps

1. Read source-of-truth files (pom, controllers, services, repositories, entities, migrations, config, tests).
2. Expand and restructure the folder guide into a practical onboarding document.
3. Add cross-framework mapping examples and first-week implementation checklist.
4. Verify the content matches current code and modules only.

## Acceptance Criteria

- Guide explains each major system part in plain language.
- Guide includes direct Node/Next/Prisma analogies.
- Guide documents current modules, request flow, persistence flow, and security flow.
- Guide avoids agent/AI-skill internals and stays focused on application architecture.

## Status

- [ ] Not started / [ ] In progress / [x] Complete
      Blockers (if any):
- None.
