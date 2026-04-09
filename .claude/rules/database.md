---
title: "Database and Data Layer Guidelines"
description: "Apply when designing schema, building data access layers, optimizing queries, or managing database operations"
alwaysApply: true
version: "3.0.0"
tags: ["database", "backend", "data", "orm", "schema"]
globs:
  - "src/models/**/*"
  - "src/data/**/*"
---

# Database and Data Layer Guidelines

Technology-agnostic patterns for secure, maintainable data access.

## Core Principles

### Design First
- **Schema design** - Think through relationships and constraints
- **Normalization** - Minimize data duplication and anomalies
- **Scalability** - Design for growth and performance
- **Consistency** - ACID transactions when needed (SQL) or eventual consistency patterns (NoSQL)
- **Security** - Access controls at database and application level

### Access Patterns
- **ORM/ODM only** - Use abstraction layer, never raw SQL/queries
- **Parameterized queries** - Prevent injection attacks
- **Query optimization** - Avoid N+1 problems and slow queries
- **Connection pooling** - Manage resources efficiently
- **Error handling** - Graceful degradation when database unavailable

### Migration Strategy
- **Version control** - All schema changes tracked
- **Idempotent migrations** - Can run multiple times safely
- **Reversible migrations** - Can rollback if needed
- **Testing** - Test migrations in all environments
- **Documentation** - Document significant schema decisions

## Database Strategy Options

### SQL Databases (PostgreSQL, MySQL, SQLite, SQL Server)

Characteristics:
- Structured schema with defined tables and relationships
- ACID transactions for data consistency
- Powerful querying with SQL
- Good for relational data
- Mature tooling and libraries

Tools:
- ORM: Sequelize (Node.js), SQLAlchemy (Python), Hibernate (Java), Entity Framework (.NET)
- Query Builder: Knex.js (Node.js), SqlAlchemy Core (Python), QueryDSL (Java)
- Migration tools: Prisma Migrate, Flyway, Liquibase, Alembic

Best for:
- Complex relationships between entities
- Strong consistency requirements
- Transactional integrity
- Complex queries
- Team comfortable with SQL

### NoSQL Databases (MongoDB, DynamoDB, CouchDB, Firestore)

Characteristics:
- Flexible/schemaless documents
- Eventual consistency
- Good horizontal scaling
- Fast for simple lookups
- Dev-friendly for rapid iteration

Tools:
- ODM: Mongoose (MongoDB), AWS SDK (DynamoDB), Firebase SDK (Firestore)
- Schema validation: JSONSchema, Zod, Joi

Best for:
- Flexible/evolving data structures
- Horizontal scaling
- Fast document lookups
- Real-time updates
- Prototyping and MVPs

### Managed Database Services (Supabase, Firebase, AWS RDS)

Characteristics:
- Hosted by cloud provider
- Automatic backups and scaling
- Built-in authentication
- Real-time subscriptions (some)
- Reduced infrastructure complexity

Best for:
- Rapid prototyping
- Teams without DevOps expertise
- Projects needing built-in features
- Compliance-heavy requirements
- Teams preferring managed services

## Schema Design Patterns

### Core Tables/Collections

Base entities:
- **Users**: Authentication and identity data
- **Roles**: Permission groups
- **Audit logs**: Track changes for compliance

Related data:
- Define relationships clearly
- Use appropriate constraints (foreign keys, unique constraints)
- Add indexes for common queries
- Document connection between tables

### Timestamps

Include timestamps:
- **Created at**: When record created (immutable)
- **Updated at**: When record last modified
- **Deleted at**: For soft deletes (when applicable)

### IDs and Keys

ID strategy:
- **Primary key**: Uniquely identifies record
- **Foreign key**: References another table (relational)
- **Unique constraints**: Prevent duplicates
- **Non-sequential IDs**: Use UUID or similar for security

### Data Validation

At database level:
- Required fields (NOT NULL)
- Data type constraints
- Length constraints (for strings)
- Enum constraints (for limited values)
- Range constraints (for numbers)

At application level:
- Validate before insert/update
- Validate format (email, URL, etc.)
- Validate business rules
- Return clear error messages

## Query Patterns

### Efficient Queries

Do:
- Use WHERE clauses to filter
- Use indexes on filtered fields
- Use SELECT specific columns
- Use LIMIT for pagination
- Use JOIN for related data
- Use aggregation functions
- Batch operations when possible

Don't:
- SELECT *  (select all columns)
- N+1 queries (query in loop)
- Unindexed large scans
- Complex JOINs without indexes
- Missing WHERE clause
- Fetching unused data

### Pagination

Pagination pattern:
1. Accept limit and offset parameters
2. Validate reasonable limit (max 100-1000)
3. Query with LIMIT and OFFSET
4. Return total count optionally
5. Include nextPage/previousPage links

### Relationships

Query related data:
- One-to-many: JOIN or separate queries
- Many-to-many: Junction table with JOINs
- One-to-one: JOIN or embed in document
- Hierarchical: Recursive queries or denormalization

## Security

### Access Control

Implement at database:
- Row-level security (RLS in PostgreSQL, Firestore)
- Column-level access restrictions
- Database-level user permissions

Implement at application:
- Verify user owns resource before returning
- Check permissions before allowing modifications
- Log access to sensitive data
- Rate limit database operations

### Sensitive Data

Protect sensitive data:
- Hash passwords (never store plaintext)
- Encrypt PII at rest
- Never log sensitive data
- Mask sensitive data in backups
- Use parameterized queries to prevent injection

### Backup & Recovery

Backup strategy:
- Automated daily backups
- Test recovery procedures
- Retain backups for compliance period
- Encrypt backups at rest
- Store backups separately from primary database

## Performance

### Indexing

Index columns that are:
- Frequently filtered (WHERE clause)
- Used in JOINs
- Used in ORDER BY
- Used in DISTINCT

Don't over-index:
- Too many indexes slow down writes
- Indexes require storage space
- Maintain only used indexes

### Connection Management

Connection pooling:
- Reuse connections across requests
- Configure reasonable pool size (10-20 connections)
- Set idle timeout
- Monitor connection usage
- Alert on connection exhaustion

### Query Optimization

Optimize slow queries:
1. Profile query execution time
2. Check execution plan
3. Add missing indexes
4. Rewrite queries if needed
5. Consider denormalization if needed
6. Verify schema design

## Monitoring & Maintenance

### Health Checks

Monitor:
- Database connectivity
- Query performance (slow query logs)
- Connection pool status
- Storage space usage
- Backup completion
- Replication lag (if applicable)

### Database Maintenance

Regular tasks:
- Update statistics/analyze
- Vacuum/compact (depending on DB)
- Rebuild indexes if needed
- Clean up old audit logs
- Review and optimize slow queries

## Testing

### Test Data

Setup:
- Use separate test database
- Reset data before each test
- Use factories for consistent test data
- Seed with realistic data

Testing patterns:
- Test CRUD operations
- Test relationships
- Test constraints
- Test error cases
- Test performance

### Test Examples

Test: Create and retrieve user
1. Create user with valid data
2. Retrieve by ID
3. Verify data matches

Test: Enforce unique constraint
1. Create user with email
2. Attempt to create another with same email
3. Verify error is returned

Test: Cascade delete
1. Create parent and child records
2. Delete parent
3. Verify child is deleted

---

Remember: Good database design prevents bugs, improves performance, and makes your application maintainable.
