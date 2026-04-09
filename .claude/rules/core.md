---
title: "Core Project Guidelines"
description: "Apply to all architecture, type safety, code organization, and enterprise quality standards. Foundation for every feature"
alwaysApply: true
version: "3.0.0"
tags: ["architecture", "core", "enterprise", "technology-agnostic"]
globs:
  - "*"
---

## Developer Identity

- The AI assistant should provide clear, step-by-step solutions
- Focus on security-first thinking in all recommendations
- Provide concise but comprehensive instructions
- Adapt all patterns to the chosen technology stack

## Architecture Principles

### Security-First Development
- **Validate all inputs** at application boundaries with appropriate validation libraries
- **Authenticate and authorize** every protected endpoint with secure session management
- **Rate limit** public endpoints to prevent abuse and DoS attacks
- **Sanitize outputs** to prevent injection attacks and data leaks
- **Use encryption** for sensitive data in transit and at rest
- **Log security events** appropriately without exposing sensitive information

### Performance-Focused
- **Optimize asset delivery** with compression, caching, and lazy loading
- **Monitor performance metrics** specific to your platform (Web Vitals, response times, etc.)
- **Implement appropriate caching strategies** for your data freshness requirements
- **Optimize critical paths** - database queries, API responses, asset loading
- **Profile and benchmark** before and after optimization changes

### Type Safety & Validation
- **Use strong typing systems** available in your chosen language
- **Implement runtime validation** for all external/user-provided data
- **Validate at boundaries** - API endpoints, form submissions, configuration
- **Document type contracts** between system components
- **Generate types** from schemas when possible (OpenAPI, GraphQL, database schemas)

### Testing Strategy
- **Unit tests** for business logic and utility functions (80% of test effort)
- **Integration tests** for API endpoints and data access layers (15% of test effort)
- **E2E tests** for critical user journeys and workflows (5% of test effort)
- **Accessibility tests** for all user-facing interfaces
- **Performance tests** for critical paths and known bottlenecks
- **Security tests** for authentication, authorization, and validation flows

## Code Quality Standards

### Architecture & Organization
- **Feature-based structure** - organize code by business domain, not just technical layer
- **Separation of concerns** - keep UI, business logic, and data access separate
- **DRY principle** - don't repeat yourself, extract common patterns
- **SOLID principles** - single responsibility, open/closed, Liskov substitution, interface segregation, dependency inversion
- **Consistent naming** conventions throughout the codebase

### Code Style
- **Consistent formatting** with automated tools (prettier, black, gofmt, rustfmt, etc.)
- **Linting rules** enforced via pre-commit hooks or CI/CD
- **Clear comments** for complex logic, architectural decisions, and gotchas
- **Self-documenting code** with meaningful variable and function names
- **No magic numbers** - extract constants with descriptive names

### Component/Module Standards (Framework-Specific)
- **Single responsibility** - each component/module should have one clear purpose
- **Reusable units** - build components/modules that can be used in multiple places
- **Proper error handling** for production resilience
- **Accessibility attributes** included for user-facing components
- **Proper documentation** of public APIs and interfaces

### Database Standards
- **Choose ONE approach** and maintain consistency (SQL with ORM, NoSQL, managed service, etc.)
- **Schema migrations only** for schema changes (never manual database edits in production)
- **ORM/ODM patterns** to prevent injection attacks (no raw SQL unless performance-critical)
- **Connection pooling** for all production deployments
- **Audit logs** for sensitive data operations
- **Proper indexing** to prevent N+1 query problems

## Development Workflow

### Quality Gates
- **Automated formatting** checks before code submission
- **Type checking** must pass (strict mode where available)
- **Linting** must pass with zero critical violations
- **Unit tests** must pass with adequate coverage (>80% for business logic)
- **Integration tests** must pass for API and data layer changes
- **Security scans** must pass without high-severity issues
- **Code review** approval required before merging

### Environment Management
- **Environment variables** with runtime validation at startup
- **Secrets management** never in code, version control, or logs
- **Database migrations** applied consistently across environments
- **Feature flags** for gradual rollouts and A/B testing
- **Health checks** for deployment validation and monitoring

### Performance Standards
- **Monitor resource usage** - memory, CPU, disk, network
- **Track performance metrics** specific to your platform
- **Profile critical paths** regularly to identify bottlenecks
- **Implement caching** appropriately for your use case
- **Alert on regressions** when performance degrades

## Technology Stack Selection

This template is technology-agnostic. Choose your stack based on:
- **Team expertise** and preferred languages/frameworks
- **Project requirements** - performance needs, scalability, real-time features
- **Deployment environment** - cloud platform, container requirements, resources
- **Time constraints** - rapid prototyping vs. long-term maintenance

### Frontend Tiers
- **Full-stack frameworks**: Next.js, Nuxt, SvelteKit, Remix
- **Component libraries**: React, Vue, Angular, Svelte, Solid
- **Traditional**: Server-side rendering with templates (Django, Rails, Laravel)
- **Mobile-first**: React Native, Flutter, Kotlin, Swift

### Backend Tiers
- **Node.js**: Express, Fastify, NestJS, Koa
- **Python**: Django, FastAPI, Flask
- **Go**: Gin, Echo, Fiber
- **Rust**: Actix-web, Axum, Rocket
- **Java**: Spring Boot, Quarkus, Ktor
- **Other**: Ruby on Rails, C#/.NET, PHP, etc.

### Database Tiers
- **SQL**: PostgreSQL, MySQL, SQLite, SQL Server
- **NoSQL**: MongoDB, DynamoDB, CouchDB, Firestore
- **Cloud-native**: Supabase, Firebase, AWS RDS, Azure SQL
- **Search**: Elasticsearch, Meilisearch, Algolia

### Authentication
- **Self-managed**: Custom implementation with JWT, sessions, or cookies
- **Framework-integrated**: NextAuth, Passport, Django's auth system
- **SaaS**: Auth0, Firebase Auth, AWS Cognito, Supabase Auth

## Maintenance & Evolution

### Regular Reviews
- **Dependency updates** - keep packages current with security patches
- **Security audits** - regular vulnerability scanning and code review
- **Performance monitoring** - track metrics and address regressions
- **Code quality** - refactoring and technical debt management
- **Documentation** - keep docs current with code changes

### Technology Updates
- **Framework versions** - planned upgrades with migration strategies
- **Critical patches** - immediate application of security updates
- **Feature adoption** - stay current with language/framework improvements
- **Deprecation management** - handle deprecated APIs and patterns

## Documentation and Research

When deciding what to recommend or implement:

- **Local rules** (this file and other `agents/rules/*.mdc`): Use as the primary source for architecture, security, testing, and style. Follow these when the stack is generic or already covered.
- **Context7 MCP**: Use for **up-to-date library and framework documentation**. When suggesting or implementing patterns for a specific stack (e.g. Next.js, Express, React), query Context7 (`resolve-library-id` then `query-docs`) so recommendations match current APIs and official guidance.
- **NotebookLM MCP**: Use for **research and best-practice discourse** (e.g. OWASP, building-systems advice, template design). Query when adding or changing guidelines, or when the audit/planning needs external best practices. Not for real-time API docs—use Context7 for that.

## Communication & Collaboration

- When providing solutions, be explicit about what to change
- Show code examples in the language/framework being used
- Focus on actionable steps, minimize unnecessary explanation
- Explain security implications of code changes
- Flag performance implications of design decisions

---

Remember: This template adapts to **any modern technology stack** while maintaining **enterprise-grade quality standards**.
Choose your technologies wisely, apply these principles consistently, and keep your codebase maintainable.
