# JavaVacunas Agent Selection Workflow

Quick reference guide for selecting the right specialized agent for your task.

## Decision Tree

### 1. What are you building?

#### A. Backend Feature
**Workflow:**
```
1. api-rest-designer
   ↓ Design REST endpoints and DTOs

2. database-migration-specialist
   ↓ Create Flyway migrations if schema changes needed

3. backend-feature-developer
   ↓ Implement controllers, services, repositories, entities

4. plsql-integration-developer (if needed)
   ↓ Create stored procedures for complex transactions

5. jpa-security-specialist (if needed)
   ↓ Add authentication/authorization

6. unit-test-engineer
   ↓ Write unit tests (90%+ coverage)

7. code-review-specialist
   ↓ Review before committing
```

**Example:** Creating vaccine inventory management
- api-rest-designer: Design `/api/v1/vaccine-inventory` endpoints
- database-migration-specialist: Create `vaccine_inventory` table
- backend-feature-developer: Implement CRUD operations
- plsql-integration-developer: Create FIFO inventory selection procedure
- unit-test-engineer: Test all service methods
- code-review-specialist: Final review

---

#### B. Frontend Feature
**Workflow:**
```
1. api-integration-developer
   ↓ Create API client module and TanStack Query hooks

2. frontend-feature-developer
   ↓ Build React components, pages, forms

3. code-review-specialist
   ↓ Review before committing
```

**Example:** Creating children management page
- api-integration-developer: Create `childrenApi.ts` with CRUD methods
- frontend-feature-developer: Build `ChildrenPage.tsx`, `CreateChildModal.tsx`
- code-review-specialist: Check TypeScript types, Spanish UI text

---

#### C. Full-Stack Feature
**Workflow:**
```
Backend:
1. api-rest-designer → 2. database-migration-specialist → 3. backend-feature-developer → 4. unit-test-engineer

Frontend:
5. api-integration-developer → 6. frontend-feature-developer

Final:
7. code-review-specialist
```

---

### 2. What are you modifying?

#### A. Database Schema
**Agent:** database-migration-specialist
- Creating new tables
- Adding columns to existing tables
- Creating indexes or constraints
- **Remember:** NEVER modify existing migrations!

#### B. PL/SQL Procedures
**Agent:** plsql-integration-developer
- Complex multi-step transactions
- FIFO inventory operations
- Database-side business rules
- Reporting functions

#### C. Authentication/Security
**Agent:** jpa-security-specialist
- Adding new user roles
- Implementing authorization rules
- JWT token modifications
- Security filter configuration

#### D. API Endpoints
**Agent:** api-rest-designer
- Designing new REST endpoints
- Creating DTOs
- Adding validation rules
- Swagger documentation

---

### 3. What are you testing?

#### A. Unit Tests
**Agent:** unit-test-engineer
- Service layer testing
- Achieving 90%+ coverage
- TDD methodology
- Given-When-Then structure

#### B. Integration Tests (when re-enabled)
**Agent:** integration-test-engineer
- Repository layer testing
- Database constraints
- Flyway migration validation

---

### 4. What are you deploying?

#### A. Docker/Container Configuration
**Agent:** docker-cicd-specialist
- Docker Compose setup
- Dockerfile creation
- Health check configuration
- Environment variable management

#### B. CI/CD Pipeline
**Agent:** docker-cicd-specialist
- GitHub Actions workflows
- Build automation
- Test automation
- Deployment configuration

---

### 5. What are you optimizing?

#### A. Backend Performance
**Agent:** performance-optimization-specialist
- Fixing N+1 query problems
- Adding database indexes
- Connection pool tuning
- Batch operations

#### B. Frontend Performance
**Agent:** performance-optimization-specialist
- TanStack Query caching
- React rendering optimization
- Code splitting
- Lazy loading

---

### 6. What are you reviewing?

**Agent:** code-review-specialist
- Pull request reviews
- Code quality checks
- Standards compliance
- Security vulnerability scanning

---

## Common Task Flows

### Adding a New Entity (Child, Vaccine, Appointment, etc.)

1. **database-migration-specialist**
   - Create migration: `V{N}__create_{entity}_table.sql`
   - Define columns, indexes, foreign keys
   - Test migration locally

2. **backend-feature-developer**
   - Create `@Entity` class with Lombok annotations
   - Create `JpaRepository` interface
   - Create DTOs (CreateRequest, Response DTO)
   - Create Service with CRUD operations
   - Create Controller with REST endpoints

3. **unit-test-engineer**
   - Write service tests (90%+ coverage)
   - Test edge cases and errors

4. **api-integration-developer**
   - Create API client module (e.g., `childrenApi.ts`)
   - Create TanStack Query hooks

5. **frontend-feature-developer**
   - Create list page component
   - Create detail page component
   - Create create/edit modal

6. **code-review-specialist**
   - Review all changes

---

### Adding Role-Based Feature

1. **jpa-security-specialist**
   - Define authorization rules
   - Add @PreAuthorize annotations
   - Configure SecurityConfig if needed

2. **backend-feature-developer**
   - Implement business logic
   - Filter data by user role in service layer

3. **frontend-feature-developer**
   - Add conditional rendering based on role
   - Hide/show UI elements

4. **unit-test-engineer**
   - Test authorization logic
   - Test with different roles

---

### Database Migration + Complex Transaction

1. **database-migration-specialist**
   - Create table migration
   - Plan PL/SQL procedure structure

2. **plsql-integration-developer**
   - Write stored procedure/function
   - Create Java wrapper service

3. **backend-feature-developer**
   - Integrate PL/SQL service into controller/service layer

4. **unit-test-engineer**
   - Mock PL/SQL calls in unit tests

5. **integration-test-engineer** (when enabled)
   - Test actual stored procedure execution

---

### Performance Problem

1. **performance-optimization-specialist**
   - Profile the application
   - Identify bottlenecks
   - Implement optimizations:
     - Add JOIN FETCH to queries
     - Add database indexes
     - Configure caching
     - Optimize React rendering

2. **code-review-specialist**
   - Verify optimizations don't break functionality

3. **unit-test-engineer**
   - Ensure tests still pass

---

### CI/CD Setup

1. **docker-cicd-specialist**
   - Configure Docker Compose
   - Create Dockerfiles
   - Set up health checks
   - Create GitHub Actions workflows

2. **backend-feature-developer**
   - Ensure Spring Boot Actuator configured

3. **frontend-feature-developer**
   - Ensure build process works in Docker

---

## Agent Cheat Sheet

| Task | Primary Agent | Supporting Agents |
|------|---------------|-------------------|
| New REST endpoint | api-rest-designer | backend-feature-developer, unit-test-engineer |
| New database table | database-migration-specialist | backend-feature-developer |
| Complex transaction | plsql-integration-developer | database-migration-specialist |
| New React page | frontend-feature-developer | api-integration-developer |
| API integration | api-integration-developer | frontend-feature-developer |
| Authentication | jpa-security-specialist | backend-feature-developer |
| Unit testing | unit-test-engineer | - |
| Code review | code-review-specialist | - |
| Docker setup | docker-cicd-specialist | - |
| Performance issue | performance-optimization-specialist | code-review-specialist |
| Full feature | Multiple agents in sequence | See workflows above |

---

## Tips for Success

1. **Follow the workflow** - Don't skip steps, especially testing and code review
2. **One agent at a time** - Complete each agent's work before moving to next
3. **Check the checklist** - Each agent has a quality checklist in AGENTS.md
4. **Read the patterns** - Each agent defines specific patterns to follow
5. **Test as you go** - Don't wait until the end to write tests (TDD)
6. **Review before commit** - Always use code-review-specialist before committing

---

## Quick Invocation Template

```
Use Task tool with prompt:

"Act as the [AGENT-NAME] for JavaVacunas.

[Copy the 'Full Agent Prompt' from .claude/AGENTS.md]

Your task: [Describe specific task in detail]"
```

Replace `[AGENT-NAME]` with one of the 12 agents and describe your specific task.
