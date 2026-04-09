---
name: raphaelsalaja-userinterface-wiki
description: UI and UX best-practice playbook for stronger information architecture, readability, and usability.
---

# User Interface Wiki Playbook

Use this skill when improving UI clarity, hierarchy, and interaction ergonomics.

## Trigger Conditions

- User requests a frontend cleanup or UX review.
- Interface has weak visual hierarchy or navigation confusion.
- Product needs consistent UI patterns across pages.

## Workflow

1. Audit hierarchy: headings, spacing, grouping, and contrast.
2. Audit navigation and task flow for top user goals.
3. Standardize component patterns (cards, forms, tables, dialogs).
4. Tighten copy and labels for faster comprehension.
5. Validate responsive behavior at mobile, tablet, and desktop widths.
6. Validate accessibility semantics and keyboard flow.

## Interface Quality Checklist

- Primary action is obvious in each view.
- Navigation labels are unambiguous.
- Form errors are clear, contextual, and actionable.
- Empty/loading/error states are explicit.
- Density and spacing are consistent across sections.

## Output Contract

- UX issues grouped by severity
- IA and layout improvements
- Component consistency decisions
- Accessibility and responsiveness findings
- Validation steps and acceptance criteria

## Guardrails

- Avoid visual changes that break existing design-system tokens.
- Keep interaction patterns predictable and learnable.
- Optimize for readability before decoration.
- Confirm improvements with at least one critical user journey.

## Upstream Reference

- Original package command:
  - npx skills add raphaelsalaja/userinterface-wiki
