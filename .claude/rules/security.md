---
title: "Security Rules & Best Practices"
description: "Apply when implementing authentication, authorization, API validation, secrets management, or protecting against OWASP Top 10 vulnerabilities"
alwaysApply: true
version: "3.0.0"
tags: ["security", "auth", "validation", "secrets", "owasp"]
---

# Security Rules & Best Practices

You are an expert security-focused developer implementing security-first development regardless of technology stack.

## Core Security Principles

- **Defense in Depth**: Implement multiple layers of security controls
- **Least Privilege**: Grant minimum necessary permissions for each role/service
- **Input Validation**: Validate all inputs at boundaries with appropriate schema validation
- **Output Encoding**: Encode all outputs to prevent injection attacks
- **Secure by Default**: Choose secure configurations over convenience
- **Fail Securely**: Default to denial, require explicit allow

## Authentication & Authorization Patterns

### Session Management Best Practices

Implement secure session handling regardless of framework:
- **Session storage**: Use secure cookies (HttpOnly, Secure, SameSite) or JWT tokens
- **Token expiration**: Implement appropriate timeout for token validity
- **Refresh tokens**: Separate long-lived and short-lived tokens
- **Session invalidation**: Properly terminate sessions on logout
- **CSRF protection**: Verify request origin for state-changing operations

### Authentication Patterns (Language-Agnostic)

Authentication Flow:
1. User provides credentials (username/password)
2. Validate credentials against hashed password in database
3. Create session or JWT token with user identity
4. Return session/token to client with secure flags
5. Client includes token/session in subsequent requests
6. Server validates token/session on each request
7. Extract user identity from verified token/session

**Key Requirements:**
- Hash passwords with strong algorithms (bcrypt, scrypt, argon2)
- Salt rounds: 12+ for bcrypt
- Never store plaintext passwords
- Implement rate limiting on authentication endpoints
- Log authentication attempts (failures especially)
- Provide clear error messages that don't leak user information

### Authorization Patterns (Language-Agnostic)

Authorization Flow:
1. Extract user identity from authenticated session/token
2. For each protected operation:
   a. Check if user is authenticated
   b. Retrieve user''s roles/permissions from database
   c. Verify required permissions for operation
   d. Allow or deny based on permission check
3. Log authorization checks (especially failures)
4. Return appropriate error for denied access

**Role-Based Access Control (RBAC):**
- Define roles with clear permissions (admin, moderator, user, guest)
- Assign roles to users (can be multiple)
- Check user''s roles before allowing sensitive operations
- Implement at API boundaries and critical business logic
- Audit role changes and permission checks

## Input Validation & Sanitization

### Validation Strategy (Technology-Agnostic)

Validate all user inputs at API boundaries:

Validation Process:
1. Define schema/rules for each input
   - Data type (string, number, boolean, date, etc.)
   - Length constraints (min/max)
   - Format requirements (regex patterns)
   - Allowed values (whitelist)
   - Required fields
   
2. Validate incoming data against schema
   - Reject invalid data with clear error messages
   - Never modify user input, reject or accept
   
3. Transform/normalize data if needed
   - Trim whitespace
   - Convert to lowercase/uppercase as appropriate
   - Parse dates to consistent format
   
4. Return validation result
   - Success with validated data
   - Failure with specific field errors

**Common Validation Rules:**
- Email: Valid format using standardized validation, convert to lowercase
- Password: Minimum 8 characters, complexity requirements (uppercase, lowercase, number, special)
- Names: 2-100 characters, alphanumeric with spaces allowed
- URLs: Valid URL format for whitelist only
- Phone: Valid format for your region
- User IDs: UUID v4 format or similar non-sequential identifiers

### Special Input Handling

**File Uploads:**
- Validate file content type (MIME type check from file content, not extension)
- Enforce size limits (max upload size)
- Scan for malware when possible
- Store outside web root
- No execute permission on upload directory
- Serve with inline=false to force download

**API Parameters:**
- Validate query parameters with same rules as request body
- Whitelist allowed parameters per endpoint
- Validate pagination limits (max 100-1000 items)
- Validate sort fields against whitelist

**Database Queries:**
- Use ORM/parameterized queries only (never string concatenation)
- Filter results to authenticated user''s data
- Limit results with pagination
- Monitor for suspicious query patterns

## Rate Limiting & DoS Protection

### Rate Limiting Strategy

Implement rate limiting on sensitive endpoints:

Rate Limiting Levels:
- Authentication endpoints: 5 attempts per 15 minutes per IP
- API endpoints: 100-1000 requests per hour per user/IP
- Public endpoints: 1000+ requests per hour per IP
- Critical operations: 1 attempt per minute per user

**Implementation approach:**
- Use Redis or similar for distributed rate limiting
- Track by IP address, user ID, or API key depending on endpoint
- Return 429 (Too Many Requests) when limit exceeded
- Include retry-after header in response
- Log rate limit violations

## OWASP Top 10 Protection

### A01: Broken Access Control
-  Implement authentication on all protected endpoints
-  Check authorization for current user before returning data
-  Use role-based access control with clear permission model
-  Validate user can only access their own data
-  Log authorization failures for security monitoring

### A02: Cryptographic Failures
-  Hash passwords with strong algorithms (bcrypt, scrypt, argon2)
-  Use HTTPS in production for all communications
-  Store secrets in environment variables, never in code
-  Use secure cookie flags (HttpOnly, Secure, SameSite)
-  Encrypt sensitive data at rest when appropriate

### A03: Injection
-  Use ORM/parameterized queries only (no string concatenation)
-  Validate all inputs with schema/whitelist approach
-  Encode output data in appropriate context (HTML, URL, etc.)
-  Implement Content Security Policy headers
-  Use prepared statements for database queries

### A04: Insecure Design
-  Implement authentication and authorization from start
-  Rate limit authentication endpoints
-  Validate input at every API boundary
-  Design with fail-secure defaults
-  Implement comprehensive error handling

### A05: Security Misconfiguration
-  Run only necessary services/features
-  Implement all security headers (CSP, X-Frame-Options, HSTS, etc.)
-  Update frameworks and dependencies regularly
-  Remove default accounts and credentials
-  Validate environment variables at startup

### A06: Vulnerable Components
-  Keep dependencies updated with security patches
-  Use lock files (package-lock.json, requirements.txt, etc.)
-  Regularly scan dependencies for vulnerabilities
-  Remove unused dependencies
-  Only use supported versions of frameworks

### A07: Authentication Failure
-  Implement strong password requirements
-  Use rate limiting on authentication endpoints
-  Hash passwords securely (bcrypt, scrypt, argon2)
-  Implement session timeouts
-  Log authentication attempts

### A08: Data Integrity Failures
-  Validate all input data with schema validation
-  Implement proper error handling
-  Log integrity violation attempts
-  Use strong checksums/hashes for critical data
-  Implement database constraints

### A09: Logging and Monitoring Failure
-  Log authentication and authorization events
-  Log sensitive operations and data access
-  Monitor for suspicious patterns
-  Keep logs secure and don''t expose sensitive info
-  Alert on security-relevant events

### A10: SSRF (Server-Side Request Forgery)
-  Validate and whitelist URLs that application accesses
-  Implement network-level restrictions
-  Disable unused URL schemes
-  Implement timeouts on external requests
-  Log and monitor external API calls

## Environment & Secrets Management

### Environment Variables

Environment Variable Validation Pattern:
1. Define all required variables with types
2. Load from .env file or environment not from code
3. Validate all variables exist at startup
4. Validate all variables are correct type/format
5. Provide clear error if validation fails
6. Never log or expose environment variables

**Sensitive Variables:**
- Database credentials (URL, passwords)
- API keys and secrets
- Encryption keys
- Authentication secrets (session secret, JWT secret)
- Third-party service credentials

No sensitive variables should ever be:
- Committed to version control
- Logged (even in debug logs)
- Exposed in error messages
- Included in client-side code
- Transmitted in URLs

## Application Hardening & Obfuscation

- Treat obfuscation as **defense in depth**, not a replacement for authentication, authorization, secrets hygiene, or validation.
- Apply hardening selectively by risk profile, especially for distributed clients (mobile/desktop/browser) and high-value IP logic.
- Integrate hardening into build/release flow and require post-hardening functional regression and performance checks.
- Protect symbol/mapping/debug artifacts with restricted access and secure retention policies.
- Require reproducible builds and a clear rollback path for hardened releases.

## Security Checklist

Before deploying to production:

- [ ] All inputs validated with schema validation
- [ ] Password hashing implemented with strong algorithm
- [ ] Rate limiting on authentication endpoints
- [ ] HTTPS enforced in production
- [ ] Security headers configured
- [ ] Environment variables validated at startup
- [ ] No sensitive data in logs
- [ ] Database queries use ORM (no raw SQL)
- [ ] Authorization checks on protected endpoints
- [ ] Error messages don''t leak sensitive information
- [ ] External API calls use HTTPS
- [ ] Dependencies scanned for vulnerabilities
- [ ] Unused dependencies removed
- [ ] Dead code removed
- [ ] Secrets never committed to version control
- [ ] Session/token timeouts configured
- [ ] CSRF protection implemented (if applicable)

---

Remember: **Security must be built in from the beginning, not added as an afterthought.**
Every feature should consider security implications, and every developer should understand these principles.
