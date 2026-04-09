## Project Guidelines

This is a **technology-agnostic development template** with enterprise-grade patterns for security, testing, and developer experience.
These guidelines are for both humans and AI assistants working with any technology stack.

- Canonical AI policy source lives in `CLAUDE.md`.
- **Agent responsibilities** and MCP integration are documented in `AGENTS.MD`.
- **Detailed implementation rules** live in `.claude/rules/*.md` files.
- **Custom skills** for domain-specific tasks are organized in `.github/skills/` (see [Skills Guide](../.github/skills/README.md)).

Read this file first to understand the architecture, then consult `AGENTS.MD` for agent delegation.

---

## Technology Stack Selection

This template is designed to work with **any modern technology stack**. Choose the technologies that best fit your project requirements:

### Frontend Framework Options

#### Option A: React Ecosystem
- ✅ **Best for**: Complex UIs, large teams, extensive ecosystem
- ✅ **Variants**: Next.js (full-stack), Create React App, Vite + React
- ✅ **Use when**: Building complex SPAs or full-stack applications

#### Option B: Vue.js Ecosystem  
- ✅ **Best for**: Progressive adoption, gentle learning curve
- ✅ **Variants**: Nuxt.js (full-stack), Vue CLI, Vite + Vue
- ✅ **Use when**: Migrating existing apps or rapid prototyping

#### Option C: Angular
- ✅ **Best for**: Enterprise applications, TypeScript-first development
- ✅ **Features**: Built-in TypeScript, comprehensive CLI, enterprise patterns
- ✅ **Use when**: Building large-scale enterprise applications

#### Option D: Svelte/SvelteKit
- ✅ **Best for**: Performance-critical applications, smaller bundle sizes
- ✅ **Features**: Compile-time optimization, minimal runtime overhead
- ✅ **Use when**: Performance is the primary concern

#### Option E: Traditional Server-Side
- ✅ **Best for**: SEO-critical sites, progressive enhancement
- ✅ **Variants**: Django templates, Rails views, PHP templates
- ✅ **Use when**: Building traditional web applications with SSR

### Backend Framework Options

#### Option A: Node.js
- ✅ **Frameworks**: Express, Fastify, Koa, Next.js API routes
- ✅ **Best for**: JavaScript/TypeScript full-stack development
- ✅ **Use when**: Team has JavaScript expertise, need API + frontend

#### Option B: Python
- ✅ **Frameworks**: Django, FastAPI, Flask
- ✅ **Best for**: Data-heavy applications, AI/ML integration, rapid development
- ✅ **Use when**: Building APIs with complex business logic

#### Option C: Go
- ✅ **Frameworks**: Gin, Echo, Fiber
- ✅ **Best for**: High-performance APIs, microservices
- ✅ **Use when**: Performance and concurrency are critical

#### Option D: Rust
- ✅ **Frameworks**: Actix-web, Warp, Rocket
- ✅ **Best for**: System-level performance, memory safety
- ✅ **Use when**: Building high-performance, secure backends

#### Option E: Java/Kotlin
- ✅ **Frameworks**: Spring Boot, Ktor, Quarkus
- ✅ **Best for**: Enterprise applications, existing Java ecosystem
- ✅ **Use when**: Working in enterprise Java environments

### Database Strategy Options

#### Option A: SQL Databases
- ✅ **Databases**: PostgreSQL, MySQL, SQLite
- ✅ **ORMs**: Prisma, TypeORM, Sequelize (JS), SQLAlchemy (Python), GORM (Go)
- ✅ **Best for**: Complex relationships, ACID transactions, reporting

#### Option B: NoSQL Databases
- ✅ **Databases**: MongoDB, DynamoDB, CouchDB
- ✅ **ODMs**: Mongoose, AWS SDK, PouchDB
- ✅ **Best for**: Flexible schemas, horizontal scaling, document storage

#### Option C: Cloud-Native Solutions
- ✅ **Options**: Supabase, Firebase, AWS RDS, Azure SQL, PlanetScale
- ✅ **Best for**: Rapid development, managed infrastructure, built-in features
- ✅ **Use when**: Want managed database with additional services

### Authentication Options

#### Option A: Self-Managed
- ✅ **Solutions**: NextAuth.js, Passport.js, Django Auth, custom JWT
- ✅ **Best for**: Custom requirements, full control, specific workflows
- ✅ **Use when**: Need custom authentication logic

#### Option B: Authentication as a Service
- ✅ **Solutions**: Auth0, Firebase Auth, AWS Cognito, Supabase Auth
- ✅ **Best for**: Rapid development, enterprise SSO, compliance requirements
- ✅ **Use when**: Want managed authentication with social providers

---

## Core Architecture Principles

### Security-First Development
- **Input Validation**: Validate all user inputs at application boundaries
- **Authentication**: Implement secure session management and MFA where appropriate
- **Authorization**: Role-based access control with proper middleware
- **Data Protection**: Encrypt sensitive data, sanitize outputs, secure error handling
- **Rate Limiting**: Protect against DoS attacks with appropriate limiting strategies

### Performance Optimization
- **Asset Optimization**: Compress images, minify code, implement caching
- **Loading Strategies**: Lazy loading, code splitting, progressive loading
- **Database Optimization**: Efficient queries, connection pooling, caching layers
- **Monitoring**: Performance metrics, error tracking, user experience monitoring

### Type Safety & Validation
- **Static Typing**: Use TypeScript, Flow, or language-native typing systems
- **Runtime Validation**: Schema validation at API boundaries and form inputs
- **API Contracts**: OpenAPI/GraphQL schemas for API documentation and validation
- **Database Schemas**: Proper constraints and validation at the database level

### Testing Strategy
- **Unit Testing**: Test individual functions and components (80% of tests)
- **Integration Testing**: Test API endpoints and database operations (15% of tests)
- **End-to-End Testing**: Test critical user journeys (5% of tests)
- **Accessibility Testing**: Automated WCAG compliance checking
- **Security Testing**: Input validation and authentication flow testing

### Developer Experience
- **Development Tools**: Hot reload, debugging tools, comprehensive logging
- **Code Quality**: Linting, formatting, pre-commit hooks, automated quality gates
- **Documentation**: API docs, component storybooks, architectural decision records
- **Deployment**: CI/CD pipelines, environment management, rollback strategies

---

## Project Structure Patterns

### Feature-Based Structure
Organize code by business domain rather than technical layer:

```
src/
├── features/
│   ├── auth/
│   │   ├── components/
│   │   ├── services/
│   │   ├── types/
│   │   └── tests/
│   └── dashboard/
│       ├── components/
│       ├── services/
│       ├── types/
│       └── tests/
├── shared/
│   ├── components/
│   ├── utilities/
│   ├── types/
│   └── constants/
└── tests/
    ├── integration/
    └── e2e/
```

### Technology-Specific Adaptations

**React/Vue/Angular Projects:**
- Component-based architecture with proper state management
- Shared component library with consistent design tokens
- Custom hooks/composables for reusable logic

**Backend API Projects:**
- Service layer for business logic
- Repository pattern for data access
- Middleware for cross-cutting concerns

**Full-Stack Projects:**
- Shared types between frontend and backend
- API route organization matching frontend features
- Consistent error handling patterns

---

## Quality Standards

### Code Quality
- **Consistency**: Follow established patterns throughout the codebase
- **Readability**: Clear naming, proper documentation, logical organization
- **Maintainability**: Modular design, separation of concerns, SOLID principles
- **Performance**: Efficient algorithms, appropriate caching, resource optimization

### Security Standards
- **OWASP Top 10**: Address all major web application security risks
- **Input Validation**: Comprehensive validation with appropriate error handling
- **Authentication**: Secure session management with proper token handling
- **Authorization**: Granular permissions with proper access control
- **Data Protection**: Encryption at rest and in transit, secure data handling

### Testing Standards
- **Coverage**: Maintain >80% test coverage for business logic
- **Reliability**: Tests should be deterministic and fast
- **Maintainability**: Clear test organization and proper mocking patterns
- **Accessibility**: All user-facing features tested for WCAG compliance

### Documentation Standards
- **API Documentation**: Complete endpoint documentation with examples
- **Component Documentation**: Props, usage examples, accessibility notes
- **Architecture Documentation**: Decision records, system diagrams, setup guides
- **User Documentation**: Feature guides, troubleshooting, FAQ

---

## Getting Started

### 1. Choose Your Stack
Review the options above and select technologies that fit your:
- **Team expertise** and learning preferences  
- **Project requirements** and performance needs
- **Deployment environment** and infrastructure constraints
- **Timeline** and development velocity requirements

### 2. Adapt the Template
- Update `.claude/rules/*.md` files with technology-specific patterns
- Keep `.cursorrules`, `.github/copilot-instructions.md`, `AGENTS.MD`, and `CLAUDE.md` as minimal wrappers that point to `CLAUDE.md`
- Update `CLAUDE.md` with stack-specific guidelines
- Create appropriate configuration files for your chosen tools

### 3. Implement Core Patterns
- Set up your chosen security patterns (validation, auth, rate limiting)
- Implement testing tools and maintain quality gates
- Configure development tools (linting, formatting, debugging)
- Set up deployment pipelines and environment management

### 4. Follow Agent Patterns
- Use the agents defined in `AGENTS.MD` for specialized tasks
- Maintain consistency with established patterns
- Regular code reviews using `ReviewerAgent` patterns
- Document architectural decisions as you build

---

## Maintenance & Evolution

### Regular Reviews
- **Security audits**: Regular dependency updates and vulnerability scanning
- **Performance monitoring**: Track metrics and optimize bottlenecks
- **Code quality**: Regular refactoring and technical debt management
- **Documentation**: Keep docs current with code changes

### Technology Updates
- **Dependencies**: Regular updates with proper testing
- **Framework versions**: Planned upgrades with migration strategies
- **Security patches**: Immediate application of critical security updates
- **Performance improvements**: Adoption of new optimization techniques

### How We Improve This Package (Maintainers)
When improving the agents-templated package itself, maintainers use **NotebookLM** (research and best-practice discourse) and **Context7** (up-to-date library and framework docs) to gather insights. Use both to improve the **system itself**—templates, rules, and skills—by querying Cursor rules best practices, agent-rules patterns, and template/scaffolding guides, then refining rules, skills, and template content. Run the audit dimensions (docs, rules, CLI, presets, validate/doctor, tests), prioritize changes, then validate with `agents-templated validate` and `doctor`. See the README section "Improvement and Maintenance" for the full process.

This template provides a solid foundation while remaining flexible for any technology stack you choose to implement.
