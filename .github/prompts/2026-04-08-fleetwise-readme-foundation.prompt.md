---
agent: true
description: Establish FleetWise_README.md as the canonical living project guide with a maintenance workflow.
---

## Context
FleetWise needs one stable reference document to guide implementation decisions while the project is being built module by module. The existing FleetWise README already has strong architecture, schema, and roadmap detail, but it does not explicitly define ownership and update workflow.

## Decision
Keep FleetWise_README.md as the single source of truth for project planning and implementation context, and add a lightweight update protocol so the document can be safely maintained as requirements evolve.

## Steps
1. Review current FleetWise_README.md content and preserve all technical sections.
2. Add a "How to Use This README" section with operational rules for daily development.
3. Add a simple "Change Log" section for future updates.
4. Verify structure/readability and keep edits minimal and scoped.

## Acceptance Criteria
- FleetWise_README.md clearly states it is the canonical project guide.
- FleetWise_README.md includes an explicit update workflow for when and how to edit it.
- FleetWise_README.md includes a change log table ready for future entries.
- Existing architecture/module/schema content remains intact.

## Status
- [ ] Not started
- [ ] In progress
- [x] Complete
Blockers (if any):
- None
