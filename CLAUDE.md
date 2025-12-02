# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

JavaVacunas is a full-stack vaccination management system for Paraguay's PAI (Programa Ampliado de Inmunizaciones). It tracks child vaccination records, schedules, appointments, and vaccine inventory.

**Tech Stack:**
- **Backend**: Spring Boot 3.2.1, Java 17, Oracle Database 23c XE, JWT authentication
- **Frontend**: React 18, TypeScript, Vite, TanStack Query, Zustand, Tailwind CSS
- **Infrastructure**: Docker/Podman with compose

## Common Commands

### Backend Development

```bash
# Build the project
cd backend
mvn clean install

# Run backend (local dev - requires Oracle DB running)
mvn spring-boot:run

# Run with specific profile
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# Run unit tests only (excludes *IT.java)
mvn test

# Run all tests with coverage report
mvn clean verify jacoco:report

# View coverage report
open backend/target/site/jacoco/index.html

# Package without tests
mvn package -DskipTests

# Run a single test class
mvn test -Dtest=VaccineServiceTest

# Run a specific test method
mvn test -Dtest=VaccineServiceTest#shouldReturnAllVaccines
```

### Frontend Development

```bash
# Install dependencies
cd frontend
npm install

# Run dev server with hot reload
npm run dev

# Build for production
npm run build

# Preview production build
npm run preview

# Lint code
npm run lint
```

### Docker/Podman

```bash
# Start all services (uses .env.docker)
docker compose --env-file .env.docker up -d

# Start only database (for local dev)
docker compose up -d oracle-db

# Check service health
docker compose ps

# View logs (all services)
docker compose logs -f

# View logs (specific service)
docker compose logs -f backend

# Restart a service
docker compose restart backend

# Stop all services
docker compose down

# Stop and remove volumes (clean slate)
docker compose down -v

# Connect to Oracle DB
docker exec -it javacunas-oracle sqlplus JAVACUNAS/JavaCunas123@XEPDB1
```

### Test Credentials

- **Doctor**: `admin` / `admin123`
- **Nurse**: `nurse` / `admin123`
- **Parent**: `parent` / `admin123`

## Specialized Sub-Agents

JavaVacunas includes 12 specialized sub-agent definitions optimized for different development tasks. These agents have deep knowledge of the project's architecture, patterns, and conventions.

**Available Agents:**
1. **backend-feature-developer** - Spring Boot layered architecture expert
2. **api-rest-designer** - RESTful API design specialist
3. **database-migration-specialist** - Flyway and Oracle migration expert
4. **plsql-integration-developer** - PL/SQL procedures and integration
5. **jpa-security-specialist** - Spring Security and JWT authentication
6. **frontend-feature-developer** - React/TypeScript expert
7. **api-integration-developer** - Frontend-backend integration
8. **unit-test-engineer** - TDD and 90%+ coverage specialist
9. **integration-test-engineer** - Database integration testing
10. **code-review-specialist** - Standards and quality enforcement
11. **docker-cicd-specialist** - DevOps, Docker/Podman, GitHub Actions
12. **performance-optimization-specialist** - Backend/frontend optimization

**Location:** Each sub-agent is defined in its own file in `.claude/agents/` directory. Claude Code automatically discovers and loads these agents based on task requirements.

**How to Use:**
- Use the Task tool to invoke a specific sub-agent by referencing its name
- Claude Code will automatically select appropriate sub-agents based on task context
- Each sub-agent includes full system prompts, code patterns, and quality checklists

**Quick Selection Guide:**
- **Backend features**: api-rest-designer → database-migration-specialist → backend-feature-developer → unit-test-engineer
- **Frontend features**: api-integration-developer → frontend-feature-developer
- **Database work**: database-migration-specialist → plsql-integration-developer
- **Code review**: code-review-specialist → unit-test-engineer
- **DevOps**: docker-cicd-specialist → performance-optimization-specialist

## Architecture & Code Organization

### Backend Architecture (Spring Boot)

**Layered architecture with clear separation:**

```
Controller Layer (REST endpoints, @RestController)
    ↓
Service Layer (Business logic, @Service)
    ↓
Repository Layer (Data access, JpaRepository)
    ↓
Entity Layer (Domain models, @Entity)
```

**Package structure:**
- `controller/` - REST endpoints with `@RestController`, validation with `@Valid`
- `service/` - Business logic, transactional operations
- `repository/` - JPA repositories extending `JpaRepository`
- `entity/` - JPA entities with Lombok annotations
- `dto/` - Data Transfer Objects for API requests/responses
- `security/` - JWT authentication: `JwtTokenProvider`, `JwtAuthenticationFilter`, `CustomUserDetailsService`
- `exception/` - Custom exceptions and `@RestControllerAdvice` global handler
- `config/` - Spring configuration classes

**Key patterns:**
- **Authentication**: Stateless JWT with HS256, 24-hour expiration, Bearer token in Authorization header
- **Authorization**: Role-based with `@PreAuthorize` annotations (DOCTOR, NURSE, PARENT roles)
- **Data access**: JPA with custom `@Query` annotations, JOIN FETCH for eager loading to prevent N+1
- **DTO mapping**: Manual mapping methods in services (no MapStruct mappers generated)
- **Exception handling**: `GlobalExceptionHandler` with `@RestControllerAdvice` returns consistent `ErrorResponse`
- **Validation**: Jakarta Bean Validation with `@Valid` in controllers
- **Transactions**: `@Transactional` on service methods, `readOnly=true` for queries
- **PL/SQL integration**: `PlSqlVaccinationService` calls Oracle stored procedures via `EntityManager.createStoredProcedureQuery()`
- **Soft deletes**: Entities have `deletedAt` timestamp, queries filter with `IS NULL`
- **Audit trail**: `@PrePersist`/`@PreUpdate` callbacks set `createdAt`/`updatedAt`

**Database migrations (Flyway):**
- Location: `backend/src/main/resources/db/migration/`
- Pattern: `V{number}__{description}.sql`
- **NEVER modify existing migrations** - create new ones instead
- Test locally before committing
- Migrations run automatically on application startup

### Frontend Architecture (React/TypeScript)

**Feature-organized structure:**

```
/src
├── api/          - API client modules (authApi, childrenApi, etc.)
├── pages/        - Route-level components
├── components/   - Reusable UI components
├── store/        - Zustand auth state with persistence
├── lib/          - Shared utilities (api-client config)
└── types/        - TypeScript interfaces
```

**Key patterns:**
- **State management**: Zustand for auth (persisted), TanStack Query for server state
- **API client**: Axios with interceptors (auto-inject Bearer token, handle 401)
- **Forms**: react-hook-form with custom validation (Zod installed but not actively used)
- **Routing**: React Router v6 with `PrivateRoute` wrapper checking auth state
- **Role-based UI**: Conditional rendering based on `user?.role` (DOCTOR, NURSE, PARENT)
- **Server state**: TanStack Query with `refetchOnWindowFocus: false`, manual cache invalidation
- **Styling**: Tailwind CSS utility-first approach
- **Icons**: Lucide React
- **Dates**: date-fns with Spanish locale
- **Language**: All UI text in Spanish

### Testing Strategy

**Unit Tests (`*Test.java`):**
- Extend `BaseUnitTest` (Mockito, no Spring context, H2 in-memory)
- Test service layer in isolation
- Mock dependencies with `@Mock` and `@InjectMocks`
- Use AssertJ for assertions
- Pattern: Given-When-Then with `@Nested` and `@DisplayName`

**Integration Tests (`*IT.java`):**
- Extend `AbstractOracleIntegrationTest` (TestContainers 2.0+ with Oracle 23c Free)
- Test repository layer with real Oracle database
- Use `@ServiceConnection` annotation (Spring Boot 3.2+) for automatic DataSource configuration
- Container reuse enabled for improved test performance (requires `testcontainers.reuse.enable=true`)
- Database cleanup after each test via `@AfterEach` for test isolation
- Docker image: `gvenzl/oracle-free:23.3-slim-faststart` (30-60 second startup)
- **Completed integration tests:**
  - `UserRepositoryIT` - User entity and authentication queries
  - `VaccineRepositoryIT` - Vaccine CRUD and active vaccines
  - `ChildRepositoryIT` - Child entity with soft delete and guardian relationships
  - `GuardianRepositoryIT` - Guardian entity with many-to-many child relationships
  - `VaccinationRecordRepositoryIT` - Vaccination records with child/vaccine relationships
  - `AppointmentRepositoryIT` - Appointments with complex joins through guardians

**Coverage requirements:**
- Line coverage: ≥ 90%
- Branch coverage: ≥ 85%
- JaCoCo enforces thresholds in build
- View report: `backend/target/site/jacoco/index.html`

**Test structure:**
```java
@DisplayName("ServiceName Tests")
class ServiceNameTest extends BaseUnitTest {
    @Mock private DependencyRepository repository;
    @InjectMocks private ServiceName service;

    @Nested
    @DisplayName("Method Name Tests")
    class MethodNameTests {
        @Test
        @DisplayName("Should do something when condition")
        void shouldDoSomethingWhenCondition() {
            // Given
            when(repository.method()).thenReturn(value);

            // When
            Result result = service.method();

            // Then
            assertThat(result).isNotNull();
            verify(repository).method();
        }
    }
}
```

### API Testing (Bruno)

**Location:** `api-tests/` directory contains comprehensive Bruno API test collection

**Collection Statistics:**
- **49 test files** covering all REST API endpoints
- **8 test categories**: Authentication, Vaccines, Children, Users, Appointments, Vaccinations, Schedules, Inventory
- **3 roles tested**: DOCTOR, NURSE, PARENT with role-based access control verification
- **Complete CRUD workflows** with state management and transitions

**Quick Start:**
```bash
# Install Bruno (one-time)
# Download from: https://www.usebruno.com/
# Or: npm install -g @usebruno/cli

# Start the backend
docker compose --env-file .env.docker up -d

# Open collection in Bruno
# File > Open Collection > Select api-tests/

# Select environment (local or docker)

# Run authentication tests first
# This automatically saves JWT tokens to environment variables
```

**Key Features:**
- **Automatic Token Management**: Login tests save JWT tokens (doctorToken, nurseToken, parentToken)
- **Dynamic ID Capture**: Test entities automatically save IDs for dependent tests
- **Comprehensive Assertions**: Status codes, response structure, data types, business rules
- **Role-Based Testing**: Success/forbidden scenarios for each role
- **Educational Design**: Clear names, sequential execution, realistic data

**Test Categories:**
1. `auth/` - Login and registration (6 tests)
2. `vaccines/` - Vaccine catalog operations (5 tests)
3. `children/` - Child management CRUD (9 tests)
4. `users/` - User management queries (5 tests)
5. `appointments/` - Appointment lifecycle (8 tests)
6. `vaccinations/` - Vaccination records (5 tests)
7. `schedules/` - Paraguay PAI schedule (3 tests)
8. `inventory/` - Vaccine inventory (6 tests)

**Example Workflow - Complete Patient Journey:**
```
1. auth/login-doctor.bru
2. children/create-child.bru (saves testChildId)
3. schedules/get-paraguay-schedule.bru
4. appointments/create-appointment.bru (saves testAppointmentId)
5. auth/login-parent.bru
6. appointments/confirm-appointment.bru
7. auth/login-nurse.bru
8. vaccinations/create-vaccination-record.bru
9. vaccinations/get-child-vaccination-history.bru
```

**Documentation:**
- `api-tests/README.md` - Complete collection overview and usage guide
- `api-tests/TEACHER_GUIDE.md` - Lesson plans, demo scenarios, exercises for educators
- `api-tests/SUMMARY.md` - Quick reference with statistics and test coverage matrix

**Command Line Usage:**
```bash
# Run entire collection
bru run api-tests --env local

# Run specific folder
bru run api-tests/auth --env local

# Run with output
bru run api-tests --env local --output results.json
```

**Educational Use:**
- Students learn REST API fundamentals, HTTP methods, authentication
- Teachers demonstrate CRUD operations, RBAC, state machines
- Developers verify API functionality and test role permissions

## Important Development Notes

### Security & Authentication

- JWT secret must be ≥256 bits (configured via `JWT_SECRET` env var)
- Passwords hashed with BCrypt in `User` entity
- Security filter: `JwtAuthenticationFilter` validates token on every request
- 401 responses automatically clear auth state and redirect to login (frontend)
- CORS configured in `SecurityConfig` for frontend origin

### Database & ORM

- **Oracle-specific**: Uses Oracle 23c XE, some features not compatible with other databases
- Hibernate `ddl-auto: none` - schema managed by Flyway only
- Connection pool: HikariCP with 10 max, 5 min idle connections
- Batch inserts/updates enabled (`batch_size: 20`)
- Lazy loading default for `@ManyToOne`, use JOIN FETCH when needed
- Stored procedures in `V13__create_plsql_procedures.sql` for complex operations

### Environment Configuration

Two profiles:
- **docker**: Uses `application-docker.yml`, database URL points to `oracle-db` service
- **dev/local**: Uses `application.yml`, database URL points to `localhost`

Environment variables:
- `SPRING_DATASOURCE_URL` - Database connection string
- `SPRING_DATASOURCE_USERNAME` - Database user
- `SPRING_DATASOURCE_PASSWORD` - Database password
- `JWT_SECRET` - JWT signing key (≥256 bits)
- `SPRING_PROFILES_ACTIVE` - Active profile (docker, dev, prod)
- `VITE_API_BASE_URL` - Frontend API endpoint

### Code Style & Conventions

**From CONTRIBUTING.md:**
- All code, comments, and commits in **English**
- Frontend UI text in **Spanish**
- **NO emojis** in code, comments, or commits
- Commit messages: Imperative mood, no conventional commit prefixes, <72 chars
- Use Lombok for boilerplate (`@Data`, `@Builder`, `@AllArgsConstructor`, etc.)
- Constructor injection for dependencies
- Keep controllers thin, business logic in services
- Follow existing patterns in the codebase

### Common Pitfalls

1. **Flyway migrations**: Never modify existing migrations - create new ones
2. **Integration tests**: Now enabled with TestContainers 2.0+, requires Docker 29.x API and `testcontainers.reuse.enable=true` in `~/.testcontainers.properties`
3. **Annotation processing**: IDE must enable annotation processors for Lombok/MapStruct
4. **Oracle startup**: First container launch takes 3-5 minutes, subsequent runs 30-60 seconds with reuse
5. **Environment files**: Use `.env.docker` for compose, `.env` for local dev
6. **Test isolation**: Unit tests use H2, not Oracle - be aware of dialect differences
7. **FIFO inventory**: PL/SQL procedures handle vaccine inventory with FIFO selection
8. **Soft deletes**: Always check `deletedAt IS NULL` in queries when needed

### API Design

- Base path: `/api/v1`
- RESTful conventions: GET, POST, PUT, DELETE
- Response format: JSON with DTOs (not entities directly)
- Error responses: Consistent `ErrorResponse` with timestamp, status, message
- Validation errors: Include field-level details
- Documentation: Swagger UI at `/swagger-ui.html`

### Role-Based Access

Three roles with different permissions:

- **DOCTOR**: Full access - create/update/delete children, vaccines, appointments, inventory
- **NURSE**: Read/write access - administer vaccines, manage appointments, view inventory
- **PARENT**: Read-only - view own children, vaccination history, schedule appointments

Check role in:
- Backend: `@PreAuthorize("hasAnyRole('DOCTOR', 'NURSE')")` on controller methods
- Frontend: `user?.role === 'DOCTOR' || user?.role === 'NURSE'` for conditional rendering

### Paraguay PAI Vaccination Schedule

Implemented in `VaccinationSchedule` entity with country code "PY":
- Birth: BCG, Hepatitis B
- 2 months: Pentavalent, IPV, Rotavirus, Pneumococcal
- 4 months: Pentavalent, IPV, Rotavirus, Pneumococcal
- 6 months: Pentavalent, IPV, Pneumococcal
- 12 months: MMR, Yellow Fever
- 18 months: Varicella, MMR, DPT, bOPV
- 4 years: bOPV
- 11 years: HPV (girls)

Service method: `getParaguaySchedule()` filters by country code "PY"
