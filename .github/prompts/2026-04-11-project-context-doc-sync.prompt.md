---
agent: agent
description: Synchronize core FleetWise docs with the new general project context narrative.
---

## Context
The project now has a refined business-level context in PROJECT_CONTEXT.md that explains FleetWise in plain language for logistics teams in the Philippines. Existing docs still mix technical-first and scaffolded content, and the frontend README is still a Vite template.

## Decision
Update only documentation surfaces that should reflect the new context without changing product scope or implementation:
1. Root README for onboarding flow and context-first reading order.
2. FleetWise_README for product overview alignment.
3. fleetwise-web README for accurate frontend usage and scope.

## Steps
1. Review current README files and verify current commands and frontend behavior from source.
2. Insert a general explanation section and links to PROJECT_CONTEXT.md where appropriate.
3. Replace stale frontend template README content with FleetWise-specific documentation.
4. Ensure no unverified claims are added and keep docs concise.

## Acceptance Criteria
- Root README clearly points readers to PROJECT_CONTEXT.md before implementation details.
- FleetWise_README project overview reflects the business problem, users, and boundaries from PROJECT_CONTEXT.md.
- fleetwise-web README documents real scripts, environment variable behavior, and current page scope.
- Documentation statements match existing source behavior.

## Status
- [ ] Not started  /  [ ] In progress  /  [x] Complete
Blockers (if any):
- None