---
name: secure-code-guardian
description: Enforces secure-by-default implementation for auth, credentials, and untrusted input paths with OWASP-aligned controls.
---

# Secure Code Guardian

Use this skill when building or reviewing features that touch authentication, secrets, session logic, or user-controlled input.

## Trigger Conditions

- User asks for secure implementation, auth, JWT, password handling, or OWASP checks.
- New endpoints, forms, or integrations expose untrusted input boundaries.
- A feature handles sensitive data, identity, or permissions.

## Workflow

1. Map trust boundaries, actors, and attack surface.
2. Define authentication and authorization requirements.
3. Apply input validation and injection defenses at boundaries.
4. Enforce credential/secrets handling and safe logging rules.
5. Add abuse protection controls (rate limiting, lockouts, replay defenses where relevant).
6. Define security tests and residual risk before ship recommendation.

## Output Contract

- Attack surface map
- Security requirements
- Applied controls by boundary
- OWASP alignment notes
- Security validation checklist
- Residual risks and mitigations
- Ship/block recommendation

## Guardrails

- Never trust client-side validation alone.
- Never expose secrets, credentials, or sensitive payloads in logs.
- Block release recommendation if critical controls are missing.
