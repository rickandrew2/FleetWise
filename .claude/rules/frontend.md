---
title: "Frontend Development Guidelines"
description: "Apply when building UI components, designing pages, creating forms, or implementing accessibility and responsive interfaces"
alwaysApply: true
version: "3.0.0"
tags: ["frontend", "ui", "ux", "security", "accessibility"]
globs:
  - "src/**/*"
  - "components/**/*"
  - "public/**/*"
---

# Frontend Development Guidelines

Technology-agnostic patterns for building secure, accessible user interfaces.

## Core Principles

### User Experience
- **Responsive design** - Works on mobile, tablet, and desktop
- **Progressive enhancement** - Content works without JavaScript when possible
- **Fast performance** - Minimize blocking resources, lazy load non-critical content
- **Accessibility** - WCAG 2.1 AA compliance for all users
- **Error handling** - Clear, helpful error messages and graceful degradation

### Security
- **Input validation** - Validate all user input on client and server
- **Output encoding** - Escape data based on context (HTML, URL, CSS, JavaScript)
- **No sensitive data** - Never include passwords, tokens, or secrets in client code
- **Content Security Policy** - Restrict which resources can load
- **HTTPS only** - Enforce in production environments

### Accessibility
- **Semantic HTML** - Use appropriate elements for structure and meaning
- **ARIA attributes** - Include when semantic HTML is insufficient
- **Keyboard navigation** - Full functionality with keyboard only
- **Color contrast** - WCAG AA standard contrast ratios
- **Focus indicators** - Clear visual indicator of focused element
- **Alt text** - Descriptive alt text for all meaningful images
- **Form labels** - Associated labels for all form inputs

## Component Development

### Component Structure Pattern

Component responsibilities:
1. Clear purpose - One thing the component does
2. Props interface - Document all inputs clearly
3. Error handling - Handle invalid inputs gracefully
4. Accessibility - Include ARIA and semantic attributes
5. Testing - Can be tested in isolation

### Component Patterns (Language-Agnostic)

Component Naming:
- File names: kebab-case (user-profile, navigation-bar)
- Component names: PascalCase (UserProfile, NavigationBar)

Component Organization:
- Props at top
- State management
- Effects/lifecycle
- Event handlers
- Render/JSX at bottom

### Form Development

Form Validation Pattern:
1. Define validation schema (JavaScript, Zod, Yup, etc.)
2. Validate on change for feedback
3. Validate on blur for efficiency
4. Show field-specific errors
5. Disable submit until valid
6. Validate on server before processing

Form Accessibility:
- Label associated with each input
- Error messages linked to field
- Required fields marked clearly
- Tab order logical and visible
- Focus management in forms
- Error summary at top (optional)

### Responsive Design Patterns

Breakpoint-based design:
- Mobile first approach
- Add complexity at larger sizes
- Use media queries or responsive framework
- Test on actual devices
- Common breakpoints:
  - Mobile: 0-480px
  - Tablet: 481-768px
  - Desktop: 769-1024px
  - Wide: 1025px+

### State Management

Choose ONE approach for consistency:
- **Client-side**: Component state (useState, reactive variables)
- **Context/Provider**: Shared state across components
- **Centralized**: Redux, Vuex, Pinia, or similar
- **Server state**: Load from API, cache locally
- **Local storage**: Persistent browser storage

Keep state:
- As local as possible
- Close to where used
- Immutable when possible
- Easy to reason about

## Design System & Styling

### CSS Approach Options

Choose ONE approach:
- **Utility classes** (Tailwind CSS, UnoCSS)
- **Styled components** (styled-components, Emotion)
- **CSS Modules** (scoped styles)
- **BEM Convention** (Block-Element-Modifier)
- **Atomic CSS** (Atomic design principles)

Styling Principles:
- Consistent spacing using scale (4px, 8px, 12px, etc.)
- Limited color palette
- Consistent font sizing
- Consistent border radius
- Consistent shadows
- Dark/light mode support when needed

### Design Tokens

Define design system tokens:
- Colors: primary, secondary, danger, success, warning
- Typography: font families, sizes, weights, line heights
- Spacing: margin/padding scale (4px, 8px, 16px, 32px, etc.)
- Shadows: subtle, regular, strong
- Border radius: small, medium, large
- Animations: duration, easing

Use tokens consistently:
- In components
- In styles
- In tests
- In documentation

## Performance Optimization

### Image Optimization
- Use appropriate formats (WebP, JPEG, PNG)
- Provide multiple sizes (srcset)
- Lazy load below the fold
- Responsive images
- Optimize file size

### Code Splitting
- Split by route (page-based code splitting)
- Split by feature (feature-based code splitting)
- Split third-party libraries
- Load libraries on demand

### Caching Strategy
- Cache static assets (images, fonts, CSS, JS)
- Cache API responses appropriately
- Use service workers for offline support
- Clear cache on deployment

## Accessibility Checklist

Before releasing any UI:

- [ ] Semantic HTML is used correctly
- [ ] All images have descriptive alt text
- [ ] Color contrast meets WCAG AA standards
- [ ] Keyboard navigation works completely
- [ ] Focus indicators are visible
- [ ] Form inputs have associated labels
- [ ] Error messages are clear and specific
- [ ] Headings have correct hierarchy
- [ ] ARIA attributes used when appropriate
- [ ] Mobile layout is responsive
- [ ] Text is readable at smaller sizes
- [ ] Content structure makes sense without CSS
- [ ] No information conveyed by color alone

## Browser Compatibility

Support strategy:
- Define minimum versions to support
- Use feature detection or progressive enhancement
- Test critical paths in target browsers
- Provide fallbacks for older browsers
- Use polyfills when appropriate

## Testing Frontend Components

Unit tests:
- Component renders with props
- Event handlers work correctly
- Error states display properly
- Accessibility attributes present

Integration tests:
- Form submission workflow
- Navigation between views
- State updates across components
- API integration

E2E tests:
- Critical user workflows
- Happy path scenarios
- Common error scenarios
- Accessibility workflows

---

Remember: Build user interfaces that work for everyone, load quickly, and provide excellent user experience.
