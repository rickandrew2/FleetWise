# Technology-Agnostic Development Template

This template has been installed by the agents-templated npm package.

## What's Included

Depending on what you installed, you may have:

- **AGENTS.MD**: Generic compatibility wrapper for AI assistants
- **ARCHITECTURE.md**: Project guidelines and architecture
- **CLAUDE.md**: Canonical policy source (single source of truth)
- **.claude/rules/**: Rule modules (`*.md`)
- **.github/skills/**: Skill modules (`*/SKILL.md`)
- **CLAUDE.md**: Claude compatibility wrapper
- **.github/copilot-instructions.md**: GitHub Copilot compatibility wrapper
- **.cursorrules**: Cursor compatibility wrapper

## Installation Options

If you're missing some components, you can install them:

```bash
# Install everything
agents-templated init --all

# Or install specific components
agents-templated init --docs      # Documentation files only
agents-templated init --rules     # Agent rules only
agents-templated init --skills    # Skills only
agents-templated init --github    # GitHub Copilot config only

# List available components
agents-templated list
```

## Rules and Skills

- **Rules** (`.claude/rules/*.md`): Define *how to behave* - patterns, principles, and standards for your team
- **Skills** (`.github/skills/*/SKILL.md`): Define *how to execute specific tasks* - domain-specific workflows and specialized knowledge

### Using Skills in Your AI Assistants

Skills can be referenced in all AI configuration files:

**In `.cursorrules` (Cursor IDE):**
```
When the user asks about [domain], use the [skill-name] skill from .github/skills/[skill-name]/SKILL.md
```

**In `CLAUDE.md` (Claude):**
```

When working on [domain-specific task], reference the [skill-name] skill in .github/skills/[skill-name]/SKILL.md
```

**In `.github/copilot-instructions.md` (GitHub Copilot):**
```
When helping with [domain-specific task], reference the [skill-name] skill from .github/skills/[skill-name]/SKILL.md
```

All wrappers point to `CLAUDE.md`, and skills can be referenced from any assistant through that canonical policy. Create custom skills in `.github/skills/` to extend capabilities across your entire team.

## Getting Started

1. Review AGENTS.MD for AI assistance guidance
2. Review ARCHITECTURE.md for overall project guidelines
3. Adapt the rules to your specific technology stack
4. Create custom skills in `.github/skills/` for your domain
5. Configure your AI assistants (Cursor, Copilot, Claude, generic agents) to reference your skills

## Documentation

For full documentation, visit: https://github.com/rickandrew2/agents-templated

