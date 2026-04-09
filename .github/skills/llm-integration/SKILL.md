---
name: llm-integration
description: LLM integration patterns — prompt engineering, RAG pipelines, tool use, evaluation harnesses, and prompt injection defense.
---

# LLM Integration

Use this skill when building, debugging, or reviewing AI/LLM-powered features.

## Trigger Conditions

- User is integrating an LLM (OpenAI, Anthropic, Gemini, local models) into an application.
- Prompt engineering, system prompt design, or output parsing is discussed.
- RAG (retrieval-augmented generation) architecture is needed.
- Evaluation, benchmarking, or quality measurement of an LLM feature is requested.
- Prompt injection risks are identified or suspected.
- Tool use / function calling patterns are being designed.

## Workflow

### Prompt Engineering

1. Separate system prompt (policy/persona) from user content (data) — never merge them raw.
2. Use structured output formats (JSON mode, XML tags) for parseable responses.
3. Specify output constraints explicitly: length, format, forbidden content.
4. Test prompts against adversarial and edge-case inputs before shipping.

### RAG Pipeline

1. Chunk source documents at semantic boundaries (paragraph, section heading).
2. Embed chunks with a consistent model; store in a vector DB with source metadata.
3. At query time: embed query → retrieve top-k chunks → score → discard below threshold.
4. Inject retrieved chunks into prompt with clear source attribution markers.
5. Cite sources in final output — never present retrieved facts as model knowledge.

### Tool Use / Function Calling

1. Define tool schemas with strict input types (JSON Schema).
2. Validate all tool call arguments before executing — treat as untrusted input.
3. Never expose filesystem paths, shell commands, or credentials via tool definitions.
4. Log all tool invocations for auditability.

### Evaluation

1. Define an eval set (minimum 20 examples) with inputs and expected outputs before launch.
2. Track: accuracy, latency p50/p95, token cost per request, failure rate.
3. Run evals on every prompt change before deploying to production.
4. Block production promotion if accuracy regresses > 5% vs. baseline.

## Output Contract

- Prompt template with annotated sections (system / context / user)
- RAG pipeline diagram or pseudocode (if applicable)
- Tool schema definitions (if applicable)
- Evaluation plan with metrics and pass/fail thresholds
- Identified injection risks and mitigations

## Guardrails

- Never interpolate raw user input into system prompts without sanitization and clear structural delimiting.
- Never execute LLM-generated code without a human or automated review gate.
- Always set explicit token limits — never rely on model defaults.
- Never log payloads that may contain PII or credentials.
- Apply `agents/rules/ai-integration.mdc` for all cost, fallback, and safety decisions.
