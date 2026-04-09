---
title: "AI / LLM Integration"
description: "Apply when integrating LLMs, RAG pipelines, prompt engineering, or building AI-powered features. Covers safety, cost, and quality"
version: "3.0.0"
tags: ["ai", "llm", "openai", "anthropic", "rag", "prompt-engineering", "safety"]
alwaysApply: false
globs: ["**/*llm*", "**/*openai*", "**/*anthropic*", "**/*langchain*", "**/*rag*", "**/ai/**"]
---

## Purpose

Govern LLM integrations safely: prevent prompt injection, enforce cost boundaries, define fallback behavior, and ensure model outputs are validated before use in any user-facing or downstream context.

## Security Requirements

1. **Prompt injection prevention** — Never interpolate raw user input directly into system prompts. Delimit user content explicitly (e.g., `<user_input>…</user_input>` tags or equivalent structural separation).
2. **Output validation** — Treat all LLM outputs as untrusted data. Validate schema, sanitize before rendering in UI, and never execute LLM-generated code without a human or automated review gate.
3. **Secret isolation** — API keys must live in environment variables only. Never log full request/response payloads that may contain sensitive user data.
4. **Rate limiting** — Apply per-user and global rate limits on all LLM-backed endpoints to prevent abuse and runaway costs.

## Cost Controls

- Set explicit `max_tokens` on every API call — never rely on model defaults.
- Log token usage per request; alert on anomalies (> 2× rolling baseline).
- Prefer streaming for long generations to enable early cancellation.
- Use smaller/cheaper models for classification, routing, or validation tasks; reserve large models for generation.

## Model Selection

| Task | Preferred approach |
|------|--------------------|
| Classification / intent detection | Small fast model or fine-tuned classifier |
| Retrieval-augmented generation | Embed → retrieve → generate pipeline |
| Code generation | Model with strong code benchmarks; always review output |
| Summarization | Mid-tier model with explicit length constraints |
| Production generation | Model with provider SLA; never experimental endpoints in prod |

## Fallback & Reliability

- Every LLM call must have a timeout and retry with exponential backoff (max 3 retries).
- Define a graceful degradation path for every LLM-powered feature (static response, cached answer, or user-facing degradation message).
- Do not block critical user flows on LLM availability.

## RAG Pipeline Rules

- Chunk documents at semantic boundaries (paragraph, section), not arbitrary byte offsets.
- Score retrieved chunks; discard chunks below relevance threshold before injecting into prompt.
- Cite sources in output when content is retrieved — never present retrieved facts as model-generated knowledge.

## Evaluation Requirements

- New LLM features must include an evaluation suite before production: minimum 20 representative examples with expected outputs.
- Track: accuracy, latency (p50/p95), token cost per request, failure rate.
- Accuracy regressions > 5% block promotion to production.
