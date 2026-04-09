---
name: emilkowalski-skill
description: Frontend interaction and motion quality checks for polished, minimal UI.
---

# Emil Kowalski Frontend Polish

Use this skill when refining frontend motion, interaction quality, and visual clarity.

## Trigger Conditions

- User asks to improve UI quality, animations, or perceived smoothness.
- User wants cleaner interactions without heavy redesign.
- Interface feels functional but not polished.

## Workflow

1. Baseline the current interaction quality.
2. Remove accidental motion (janky or decorative-only animation).
3. Define motion hierarchy: page, section, element, micro-interaction.
4. Tune timing and easing with consistency.
5. Validate accessibility: reduced-motion support and focus visibility.
6. Verify mobile and desktop interaction parity.

## Frontend Quality Checklist

- Animation duration is intentional and consistent.
- Entry and exit transitions communicate state changes.
- Hover/focus/active states are visually distinct.
- Touch targets remain accessible on mobile.
- Layout and typography remain readable during motion.

## Output Contract

- Interaction problems found
- Priority fixes (high/medium/low)
- Motion system decisions (duration, easing, delay)
- Accessibility checks performed
- Before/after verification notes

## Guardrails

- Do not add animation where no UX value exists.
- Prefer CSS/transform-based transitions over layout-thrashing properties.
- Always provide a reduced-motion fallback.
- Keep performance budget visible (FPS, input latency, layout shifts).

## Upstream Reference

- Original package command:
  - npx skills add emilkowalski/skill
