---
title: "Testing Guidelines & Best Practices"
description: "Apply when adding tests, verifying coverage, or validating quality before deployment. Always required for business logic and critical flows"
version: "3.0.0"
tags: ["testing", "quality", "coverage", "e2e", "a11y"]
---

# Testing Guidelines & Best Practices

Comprehensive testing patterns for maintaining quality across any technology stack.

## Core Testing Principles

- **Test Pyramid**: More unit tests (80%), fewer integration tests (15%), minimal E2E tests (5%)
- **Arrange-Act-Assert**: Structure tests clearly with setup, action, and verification
- **Descriptive Names**: Test names should describe expected behavior
- **Independent Tests**: Each test should be able to run in isolation
- **Fast Feedback**: Unit tests should run quickly (<100ms each)
- **Deterministic**: Tests should pass/fail consistently, not randomly

## Unit Testing

Unit tests verify individual functions, methods, and components in isolation.

### Unit Test Pattern

Unit Test Structure:
1. Arrange
   - Set up test data
   - Mock dependencies
   - Configure test environment

2. Act
   - Call the function/method being tested
   - Perform the action

3. Assert
   - Verify the result
   - Check side effects
   - Validate behavior

### What to Unit Test

- Business logic functions (validation, calculations, transformations)
- Utility functions (helpers, formatters, parsers)
- Service/class methods with clear inputs and outputs
- Error handling and edge cases
- Complex conditional logic

### Unit Test Examples (Language-Agnostic)

**Example 1: Validation Function**
Test: Email validation function
- Test valid email returns true
- Test invalid email format returns false
- Test empty string returns false
- Test whitespace is trimmed
- Test case-insensitive comparison

**Example 2: Business Logic**
Test: Calculate discount price
- Test standard discount percentage
- Test no discount
- Test maximum discount cap
- Test negative prices handled gracefully
- Test rounding is correct

**Example 3: Error Handling**
Test: Parse JSON data
- Test valid JSON parses successfully
- Test invalid JSON throws appropriate error
- Test missing required fields throws error
- Test extra fields are handled
- Test error messages are helpful

### Mocking & Test Doubles

Use test doubles (mocks, stubs, fakes) for external dependencies:

Mocking Strategy:
1. Identify external dependencies
   - Database calls
   - API calls
   - File system access
   - Time/date functions
   - Random number generation

2. Replace with appropriate test double
   - Stub: Return fixed value
   - Mock: Verify it was called correctly
   - Fake: Simplified working implementation

3. Verify interactions when appropriate
   - Was the dependency called?
   - Was it called with correct arguments?
   - Was it called the right number of times?

## Integration Testing

Integration tests verify that multiple components work together correctly.

### What to Integration Test

- API endpoints (request  response)
- Database operations (save, query, delete)
- Service-to-service interactions
- Authentication and authorization flows
- Error scenarios and edge cases

### Integration Test Pattern

Integration Test Structure:
1. Setup
   - Create test database/data
   - Mock external services if needed
   - Set up test user/session

2. Execute
   - Make API call or trigger operation
   - Interact with multiple components

3. Verify
   - Check response status and data
   - Verify database changes
   - Check side effects

### Integration Test Examples

**Example 1: User Creation API**
Test: POST /api/users with valid data
- Validate request body
- Create user in database
- Return created user with ID
- User can be retrieved afterward
- Password is hashed (not plaintext)

**Example 2: Authentication Flow**
Test: User login
- Validate credentials
- Create session/token
- Return session/token to client
- Can use session to access protected endpoints
- Session is invalidated on logout

**Example 3: Error Handling**
Test: Create user with duplicate email
- Database rejects duplicate
- API returns 400 Bad Request
- Error message is appropriate
- No partial data is written

## End-to-End Testing

E2E tests verify complete user journeys across the entire application.

### What to E2E Test

- Critical user workflows (login, purchase, form submission)
- Happy path scenarios
- Common error scenarios
- Cross-browser compatibility (if applicable)
- Accessibility requirements

### E2E Test Examples

**Example 1: User Registration**
Test: New user can register successfully
1. Navigate to signup page
2. Fill in email, password, name
3. Click submit button
4. Wait for success message
5. Redirect to dashboard
6. User can access protected content

**Example 2: Login with Invalid Credentials**
Test: Login with wrong password
1. Navigate to login page
2. Enter email and wrong password
3. Click login button
4. See error message "Invalid credentials"
5. Still on login page (not redirected)
6. Can try again

**Example 3: Multi-step Workflow**
Test: User can complete purchase
1. Navigate to product page
2. Add item to cart
3. Navigate to checkout
4. Enter shipping information
5. Enter payment information
6. Submit order
7. See confirmation page
8. Receive confirmation email

## Accessibility Testing

Test that your application meets WCAG 2.1 AA standards.

### Automated Accessibility Testing

Use tools to scan for common accessibility violations:
- Color contrast issues
- Missing alt text for images
- Missing labels for form inputs
- Heading hierarchy problems
- Missing ARIA attributes

### Manual Accessibility Testing

- Keyboard navigation: Can user navigate with Tab and Enter?
- Screen reader: Does content make sense when read aloud?
- Visual clarity: Can text be read at smaller sizes?
- Color dependency: Is information conveyed without color alone?
- Responsiveness: Does layout work on different screen sizes?

## Performance Testing

Test performance requirements and regressions.

### Performance Metrics to Monitor

**Web Applications:**
- First Contentful Paint (FCP)
- Largest Contentful Paint (LCP)
- Cumulative Layout Shift (CLS)
- Time to Interactive (TTI)
- Page load time

**API Endpoints:**
- Response time (p50, p95, p99)
- Throughput (requests per second)
- Error rate
- Database query time
- Memory usage

### Performance Testing Examples

Test: API response time
- Create 100 users in database
- Query all users endpoint
- Measure response time
- Assert completes in <500ms

Test: Page load performance
- Navigate to homepage
- Measure First Contentful Paint
- Assert completes in <2 seconds

Test: Database query optimization
- Insert 10,000 records
- Run query with filter
- Verify query uses index
- Measure execution time
- Assert completes efficiently

## Hardening Verification Testing

When hardening/obfuscation is enabled (see `agents/rules/hardening.mdc`), require additional validation on hardened artifacts.

### Required Checks for Hardened Builds

- Run core functional regression tests against hardened output, not only non-hardened builds.
- Run performance checks and compare against accepted budgets (startup, latency, memory as applicable).
- Validate crash/debug workflow with restricted symbol/mapping artifact access controls.
- Confirm release evidence includes hardening profile rationale and rollback trigger/steps.

If these checks are missing for a hardening-required profile, release readiness should be treated as blocked.

## Security Testing

Test security requirements and threat scenarios.

### Security Tests

Test: Input Validation
- SQL injection payloads in fields
- XSS payloads in fields
- Extremely long strings
- Special characters
- Wrong data types

Test: Authentication
- Login with wrong password
- Access protected endpoint without credentials
- Use expired token
- Use modified token

Test: Authorization
- Access another user''s data
- Perform admin operation as regular user
- Modify resource of different user

Test: Rate Limiting
- Exceed rate limit on authentication
- Verify 429 response
- Verify retry-after header

## Test Organization

Organize tests logically for easy maintenance:

Project Structure:
tests/
 unit/
    utils/           # Utility function tests
    services/        # Business logic tests
    validators/      # Validation tests
    formatters/      # Formatter tests

 integration/
    api/            # API endpoint tests
    database/       # Database operation tests
    auth/           # Authentication tests
    services/       # Service integration tests

 e2e/
    auth/           # Authentication flows
    workflows/      # Critical user journeys
    forms/          # Form submission flows
    pages/          # Page components (if applicable)

 a11y/               # Accessibility tests
 performance/        # Performance tests
 fixtures/           # Test data
 setup.ts            # Test configuration

## Test Coverage Guidelines

### Coverage Targets

- **Business Logic**: 80%+ coverage required
- **Controllers/Routes**: 70%+ coverage
- **Utils/Helpers**: 90%+ coverage
- **UI Components**: 60%+ coverage
- **Overall**: 75%+ coverage target

### Coverage Measurement

- Generate coverage reports regularly
- Track coverage trends over time
- Identify uncovered critical code
- The goal is quality, not just high numbers

## Best Practices

### General Best Practices

- Write tests as you write code (TDD or regular)
- Make tests as simple as production code
- Use meaningful test names that describe behavior
- Keep tests focused on one thing
- Avoid test interdependencies (order shouldn''t matter)
- Clean up test data (database, files, etc.)
- Use version control for test data when needed

### Avoiding Common Mistakes

- Testing implementation details instead of behavior
- Writing tests that are brittle (break on minor changes)
- Tests that are slower than necessary
- Not testing error cases and edge cases
- Skipping security-related tests
- Skipping accessibility tests
- Not maintaining tests (let them rot)
- Testing too much in E2E when unit tests would work

---

Remember: **Tests are documentation** of how your code should behave.
Write clear, concise tests that describe expected behavior.
Good tests make refactoring and maintenance easier and reduce bugs in production.
