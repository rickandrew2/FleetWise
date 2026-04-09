---
title: "Application Hardening & Obfuscation"
description: "Apply when building distributed apps, protecting IP logic, preparing production releases, or hardening against reverse engineering"
version: "3.0.0"
tags: ["hardening", "obfuscation", "anti-tamper", "security"]
---

## Purpose

Define a technology-agnostic hardening baseline for distributed applications and high-value logic.

## Core Principles

- Hardening is defense-in-depth, not a replacement for secure design.
- Obfuscation increases reverse-engineering cost but does not eliminate risk.
- Keep authentication, authorization, secrets management, and validation as primary controls.

## Risk-Based Applicability

Use hardening when one or more conditions apply:
- Client-distributed app (mobile/desktop/browser-delivered logic)
- High-value IP in client-side logic
- Elevated tampering, hooking, or repackaging threat model
- High-risk business operations exposed in untrusted runtime

## Hardening Control Set

- Code obfuscation/minification hardening as applicable
- Runtime integrity/tamper detection where platform allows
- Debug/instrumentation resistance where legally and technically appropriate
- Binary/artifact integrity verification in delivery pipeline
- Secure handling of symbol/mapping artifacts

## Obfuscation Guidance

- Apply near build/release stage, not as source-authoring substitute.
- Validate behavior after obfuscation with full regression checks.
- Enforce performance and startup budget checks post-hardening.
- Maintain reproducible builds and deterministic config snapshots.

## Verification Requirements

- Functional regression tests pass on hardened build.
- Performance budgets remain within accepted thresholds.
- Crash/debug workflow documented with restricted symbol access.
- Release notes include hardening profile and known tradeoffs.

## Safety Constraints

- Do not claim obfuscation as complete protection.
- Do not rely on obfuscation to protect embedded secrets.
- Block release if hardening-required profile lacks verification evidence.
