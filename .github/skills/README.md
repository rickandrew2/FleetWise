# Agent Skills

This directory contains custom skills for your AI agents. Skills extend agent capabilities with specialized knowledge, workflows, and tools for specific domains.

## What is a Skill?

A **skill** is a reusable module that:
- Defines domain-specific knowledge or workflows
- Extends agent capabilities for specialized tasks
- Can be shared across your project or published for others

**Rules** define *how to behave*; **Skills** define *how to execute specific tasks*.

## Getting Started

### Finding Existing Skills

Use the built-in `find-skills` skill to discover skills from the open ecosystem:

```bash
npx skills find [query]
```

Examples:
- `npx skills find react testing`
- `npx skills find deployment`
- `npx skills find accessibility`

Browse more at: https://skills.sh/

### Creating a Custom Skill

To create a new skill for your specific domain:

1. **Create a new folder** in this directory:
   ```
   .github/skills/my-custom-skill/
   ```

2. **Create a SKILL.md file** with metadata and instructions:
   ```markdown
   ---
   name: my-custom-skill
   description: Brief description of what this skill does
   ---

   # My Custom Skill

   Instructions and examples for using this skill...
   ```

3. **Reference it** in your agent documentation or `.cursorrules`

### Skill Structure

```
.github/skills/
├── find-skills/
│   └── SKILL.md              # Meta-skill for discovering skills
├── feature-delivery/
│   └── SKILL.md              # Systematic feature scoping and execution contracts
├── bug-triage/
│   └── SKILL.md              # Reproduction-first debugging and regression workflows
├── debug-skill/
│   └── SKILL.md              # Breakpoint-first debugging and execution tracing
├── secure-code-guardian/
│   └── SKILL.md              # Secure-by-default coding for auth and input boundaries
├── feature-forge/
│   └── SKILL.md              # Requirements forging before implementation starts
├── error-patterns/
│   └── SKILL.md              # Persistent debugging memory and known-fix application
├── app-hardening/
│   └── SKILL.md              # Risk-based hardening and obfuscation guidance
├── ui-ux-pro-max/
│   └── SKILL.md              # Premium UI/UX implementation patterns
├── emilkowalski-skill/
│   └── SKILL.md              # Frontend polish and animation quality checks
├── raphaelsalaja-userinterface-wiki/
│   └── SKILL.md              # UI hierarchy and interface best-practice playbook
├── my-custom-skill/
│   └── SKILL.md              # Your custom skill
└── README.md                 # This file
```

## Best Practices

1. **Keep skills focused** - Each skill should handle one domain or workflow
2. **Document clearly** - Include examples and use cases in SKILL.md
3. **Name descriptively** - Use kebab-case names that reflect the skill's purpose
4. **Version your skills** - Include version info in the metadata
5. **Test before sharing** - Ensure skills work as documented

## Common Skill Categories

Consider creating skills for:

| Category        | Use Case                                 |
| --------------- | ---------------------------------------- |
| Web Development | Framework-specific patterns and guidelines |
| Testing         | Testing strategies and examples          |
| DevOps          | Deployment, CI/CD, infrastructure        |
| Documentation   | Doc generation, README templates         |
| Code Quality    | Code review, refactoring patterns        |
| Design          | UI/UX, accessibility, design systems    |
| Productivity    | Workflows, automation, git patterns      |

## Recommended Baseline Skills

- `feature-delivery`: Use when user asks are broad and you need objective, scope, acceptance criteria, and validation plan.
- `bug-triage`: Use for defects and regressions requiring reproducible evidence, root-cause isolation, and minimal safe patches.
- `debug-skill`: Use for breakpoint-driven debugging, execution tracing, and variable-state inspection.
- `secure-code-guardian`: Use for secure-by-default implementation across auth, secrets, and untrusted input.
- `feature-forge`: Use before coding to turn rough asks into execution-ready requirements and acceptance criteria.
- `error-patterns`: Use when errors repeat to apply known fixes from lessons-learned and automatically record new resolutions.
- `app-hardening`: Use for high-risk/distributed runtimes to enforce hardening profile, obfuscation decisions, and release evidence.
- `shadcn-ui`: Use for shadcn/ui installation, component composition, form patterns, and theme customization.
- `emilkowalski-skill`: Use for frontend animation, micro-interaction quality, and visual polish passes.
- `raphaelsalaja-userinterface-wiki`: Use for interface hierarchy, readability, and UX consistency audits.

## Using Skills in Your Project

Skills can be referenced in all your AI assistant configuration files:

### Cursor IDE (`.cursorrules`)
```
When the user asks about [domain], use the [skill-name] skill from .github/skills/[skill-name]/SKILL.md
```

### Claude (`CLAUDE.md`)
```markdown
## When Working on [Domain]

Reference the [skill-name] skill in `.github/skills/[skill-name]/SKILL.md` for patterns and guidance.
```

### GitHub Copilot (`.github/copilot-instructions.md`)
```markdown
When helping with [domain-specific task], reference the [skill-name] skill from `.github/skills/[skill-name]/SKILL.md`
```

### Documentation (`AGENTS.MD`)
```markdown
- When working with [domain], see the [skill-name] skill in `.github/skills/[skill-name]/SKILL.md`
```

**All AI assistants support skill references.** Keep your team aligned by linking to skill files across your configuration files.

3. **Share with your team**:
   - Commit to version control
   - Document installation in your README

## Example: Creating a Fastapi Skill

```markdown
---
name: fastapi-patterns
description: FastAPI development patterns and best practices for your projects
---

# FastAPI Patterns

Use this skill when working with FastAPI projects.

## Recommended Patterns

### Request Validation
- Use Pydantic models for all request bodies
- Validate query parameters with proper typing

### Error Handling
- Return meaningful HTTP status codes
- Document error responses in OpenAPI

### Testing
- Use TestClient for API testing
- Mock external dependencies

## Project Structure

```
src/
├── api/
│   ├── v1/
│   │   ├── users/
│   │   └── posts/
│   └── dependencies.py
├── models/
├── schemas/
├── database/
└── main.py
```
```

## Resources

- [Skills Documentation](https://skills.sh/)
- [Creating Skills Guide](https://skills.sh/docs/creating-skills)
- [Browse Popular Skills](https://skills.sh/explore)
