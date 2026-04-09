---
name: app-hardening
description: Applies risk-based application hardening guidance including obfuscation decisions, integrity controls, and release evidence.
---

# App Hardening

Use this skill for security hardening of distributed applications and sensitive client-side logic.

## Trigger Conditions

- User asks for hardening, anti-tamper, reverse-engineering resistance, or secure release posture.
- Project includes mobile/desktop/browser-delivered code with high-value logic.

## Decision Matrix

Apply hardening when:
- Threat model includes tampering/repackaging/hooking, or
- Client runtime is untrusted, or
- Business/IP impact is high.

Keep baseline controls regardless:
- AuthN/AuthZ, validation, secrets hygiene, monitoring.

## Workflow

1. Define threat model and assets at risk.
2. Select hardening profile by risk tier.
3. Choose controls (obfuscation, integrity checks, runtime protections).
4. Integrate into build/release pipeline.
5. Run post-hardening functional + performance validation.
6. Produce release evidence and rollback path.

## Required Evidence

- Hardening profile selection rationale
- Verification results (functional + performance)
- Symbol/mapping artifact access policy
- Rollback steps and trigger conditions

## Guardrails

- Never treat obfuscation as a standalone security solution.
- Never store secrets in client code.
- Block release when hardening-required profile lacks evidence.
