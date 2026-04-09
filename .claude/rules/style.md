---
title: "Code Style and Standards"
description: "Apply when organizing code, naming variables/functions, improving clarity, formatting with consistency, and maintaining code quality"
alwaysApply: true
version: "3.0.0"
tags: ["style", "formatting", "naming", "quality"]
globs:
  - "**/*"
---

# Code Style and Standards

Language and framework-agnostic standards for clean, maintainable code.

## General Code Quality

### Core Principles

- **Readability**: Code should be easy to understand without extensive comments
- **Consistency**: Follow the same patterns throughout the codebase
- **Simplicity**: Prefer simple solutions to complex ones
- **SOLID principles**: Single responsibility, Open/closed, Liskov, Interface segregation, Dependency inversion
- **DRY**: Don''t Repeat Yourself - extract common patterns

### Code Review Standards

Code should:
- Do one thing well
- Be testable
- Have no dead code
- Follow established patterns
- Include appropriate comments
- Have meaningful names
- Handle errors appropriately
- Consider security implications
- Consider performance implications

## Naming Conventions

### File Naming

Choose from established conventions:

**Option 1: kebab-case (Recommended)**
- File names: `user-profile.ts`, `navigation-bar.tsx`, `date-utils.js`
- Matches URLs and file systems
- Language-agnostic
- Easy to type and read

**Option 2: snake_case**
- File names: `user_profile.py`, `date_utils.go`
- Common in Python, Go, and Ruby
- Matches language conventions

**Option 3: PascalCase**
- File names: `UserProfile.tsx`, `DateUtils.ts`
- Common in C# and some JavaScript frameworks
- Use consistently if chosen

**Consistency rule**: Pick ONE and use it throughout the entire project.

### Code Naming

Consistent naming patterns:

**Variables and Functions**
- Use camelCase: `getUserData`, `isLoading`, `handleSubmit`
- Use English words (even in non-English projects)
- Be descriptive: `user` is worse than `currentUser`
- Avoid abbreviations: `getDesc` vs `getDescription`
- Prefix booleans with is/has: `isActive`, `hasPermission`

**Constants**
- Use UPPER_SNAKE_CASE: `MAX_RETRIES`, `API_BASE_URL`
- Immutable values that don''t change
- Global configuration values
- Magic numbers should become constants

**Classes/Types/Interfaces**
- Use PascalCase: `UserProfile`, `ApiResponse`, `DatabaseConfig`
- Descriptive and specific names
- Avoid generic names like `Data`, `Info`, `Object`

**Enums**
- Type name PascalCase: `UserStatus`
- Values UPPER_SNAKE_CASE: `ACTIVE`, `INACTIVE`
- Example: `UserStatus.ACTIVE`, `Theme.DARK_MODE`

### Examples

Good naming:
```
 getUserById()
 isValidEmail()
 parseJsonResponse()
 calculateProposedDiscount()
 MAXIMUM_RETRY_ATTEMPTS
 ValidationError
```

Bad naming:
```
 get()
 foo(), bar(), x
 process()
 u1, u2, s
 var1, var2
 handleIt()
```

## Formatting

### Consistent Formatting

Use automated tools:
- **Prettier**: JavaScript/TypeScript/CSS/YAML
- **Black**: Python
- **gofmt**: Go
- **rustfmt**: Rust
- **clang-format**: C/C++
- Your language''s standard formatter

Benefits:
- No debates about formatting
- Quick review of actual changes
- Consistent codebase

### Line Length

- Target: 80-120 characters
- Maximum: 120-140 characters
- Avoid scrolling horizontally

### Indentation

Choose ONE and use consistently:
- **2 spaces**: JavaScript, YAML, some Python
- **4 spaces**: Python, Java, most languages
- **Tabs**: If configured, consistently

### Comments & Documentation

Good comments explain:
- **Why**: The reason for this code
- **Complex logic**: How it works if not obvious
- **Edge cases**: Special handling and why
- **Warnings**: Performance implications or gotchas

Bad comments:
```
 // Increment x
x = x + 1

 // Loop through users
for (user in users) { ... }

 // This is a variable
let name = getUserName()
```

Good comments:
```
 // Increment retry counter before exponential backoff
retryCount = retryCount + 1

 // Process users in batches to prevent memory overload
for (batch in users.batches()) { ... }

 // Fetch fresh name from API to avoid stale cache
let name = getUserName()
```

### Comments vs Code

Let code speak for itself:

Instead of:
```
// Get user age
int age = currentYear - birthYear
```

Write:
```
int age = calculateAge(currentYear, birthYear)
```

## Code Organization

### Module Organization

Order within files:
1. **Imports/Dependencies** - at top
2. **Constants** - module-level constants
3. **Types/Interfaces** - type definitions
4. **Functions/Classes** - main code (public first, private after)
5. **Exports** - explicit exports at end

### Function Organization

Function structure:
1. **Parameters** - at top
2. **Early returns** - validate inputs early
3. **Logic** - main implementation
4. **Return** - return result

### Class/Object Organization

Class structure:
1. **Constructor/initializer**
2. **Public methods**
3. **Private methods**
4. **Getters/setters**
5. **Static methods** (if applicable)

## Type Definitions

### Clear Type Contracts

Document what your types expect:

```
Define data structures clearly:
- What fields are required
- What data types
- What values are valid
- Any constraints or rules
```

### Avoid Magic Values

Instead of magic numbers/strings:
```
 if (status === 1) { ... }
 if (timeout === 60000) { ... }

 const ACTIVE_STATUS = 1
 const DEFAULT_TIMEOUT_MS = 60000
 if (status === ACTIVE_STATUS) { ... }
```

## Error Handling

### Clear Error Messages

Error messages should:
- Describe what went wrong
- Explain why (if not obvious)
- Suggest how to fix
- Use user-friendly language

Good error messages:
- "Email format is invalid. Please enter a valid email address."
- "Password must be at least 8 characters."
- "User with this email already exists."

Bad error messages:
- "Error"
- "Invalid input"
- "Stack trace here..."

### Error Handling Pattern

Proper error handling:
1. Catch/handle errors explicitly
2. Log error with context
3. Return appropriate error response
4. Never expose sensitive data
5. Include error code for client handling

## Security in Code

### Dangerous Patterns to Avoid

Never:
-  Store secrets in code
-  Log sensitive data
-  Dynamic SQL/query construction
-  Eval user input
-  Hardcoded API keys
-  Return sensitive data in errors
-  Skip input validation

Always:
-  Validate all inputs
-  Use parameterized queries
-  Hash sensitive data
-  Use environment variables for secrets
-  Log security events
-  Encode output appropriately

## Performance Considerations

### Avoid N+1 Queries

Good:
```
Get all users once with their posts
Result: 1 query
```

Bad:
```
For each user (loop):
  Get posts for that user
Result: 1 + N queries
```

### Avoid Premature Optimization

Focus on:
1. **Correct code first** - Works before optimizing
2. **Readable code** - Easy to understand
3. **Profile critical paths** - Where time is spent
4. **Optimize bottlenecks** - Only where it matters

## Testing

### Code for Testability

Testable code:
- Has clear inputs and outputs
- Is easy to mock dependencies
- Has single responsibility
- Doesn''t depend on side effects
- Has deterministic behavior

### Test Naming

Test names describe what is being tested:
```
 testEmailValidationAcceptsValidEmail()
 testUserCreationReturnsUserWithId()
 testPasswordResetTokenExpiresAfter24Hours()

 test1()
 testFunction()
 testWorks()
```

---

Remember: **Write code for humans first, computers second.**
Clean code is maintainable code, and maintainable code is more secure, performant, and bug-free.
