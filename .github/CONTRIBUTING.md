# Contributing to JavaVacunas

Thank you for your interest in contributing to JavaVacunas, a vaccination management system for Paraguay's PAI (Programa Ampliado de Inmunizaciones).

## Table of Contents

- [Code of Conduct](#code-of-conduct)
- [Getting Started](#getting-started)
- [Development Environment Setup](#development-environment-setup)
- [How to Contribute](#how-to-contribute)
- [Coding Standards](#coding-standards)
- [Commit Guidelines](#commit-guidelines)
- [Pull Request Process](#pull-request-process)
- [Reporting Bugs](#reporting-bugs)
- [Suggesting Enhancements](#suggesting-enhancements)

## Code of Conduct

This project and everyone participating in it is governed by our [Code of Conduct](CODE_OF_CONDUCT.md). By participating, you are expected to uphold this code.

## Getting Started

JavaVacunas is a monorepo containing:
- **Backend**: Spring Boot 3.2 with Java 17, Oracle Database 23c XE
- **Frontend**: React 18 with TypeScript, Vite, and Tailwind CSS

Before contributing, please familiarize yourself with:
- Spring Boot and Spring Security best practices
- React and TypeScript conventions
- Oracle Database and SQL fundamentals
- JWT authentication patterns

## Development Environment Setup

### Prerequisites

- Java Development Kit (JDK) 17 or higher
- Maven 3.8+
- Node.js 18+ and npm
- Podman or Docker with podman-compose or docker-compose
- Git

### Setup Steps

1. Fork and clone the repository:
```bash
git clone https://github.com/YOUR_USERNAME/JavaVacunas.git
cd JavaVacunas
```

2. Start the Oracle database:
```bash
podman-compose up -d oracle
```

3. Build and run the backend:
```bash
cd backend
mvn clean install
mvn spring-boot:run
```

4. Install and run the frontend:
```bash
cd frontend
npm install
npm run dev
```

5. Access the application:
- Frontend: http://localhost:5173
- Backend API: http://localhost:8080
- Swagger UI: http://localhost:8080/swagger-ui/index.html

### Test Credentials

- Doctor: `admin` / `admin123`
- Nurse: `nurse` / `admin123`
- Parent: `parent` / `admin123`

## How to Contribute

1. Check existing [issues](https://github.com/cardozoaldama/JavaVacunas/issues) or create a new one
2. Fork the repository
3. Create a feature branch from `main`
4. Make your changes following our coding standards
5. Test your changes thoroughly
6. Commit your changes following our commit guidelines
7. Push to your fork
8. Submit a pull request

## Coding Standards

### General

- All code, comments, documentation, and commit messages must be in English
- Frontend user interface text must be in Spanish
- No emojis in code, comments, or commit messages
- Follow the existing code style and patterns
- Write clear, self-documenting code
- Add comments only where logic is not self-evident

### Backend (Java/Spring Boot)

- Follow Java naming conventions (PascalCase for classes, camelCase for methods/variables)
- Use Spring Boot best practices and design patterns
- Implement proper exception handling
- Use DTOs for API responses
- Validate input at controller level with `@Valid`
- Use constructor injection for dependencies
- Keep controllers thin, business logic in services
- Write database migrations with Flyway (never modify existing migrations)
- Use JPA annotations appropriately
- Follow RESTful API design principles

### Frontend (React/TypeScript)

- Use TypeScript strict mode
- Follow React hooks best practices
- Use functional components exclusively
- Implement proper error boundaries
- Keep components small and focused
- Use TanStack Query for server state
- Use Zustand for client state
- Follow Tailwind CSS utility-first approach
- Ensure responsive design
- All user-facing text must be in Spanish

### Database

- Use snake_case for table and column names
- Include appropriate indexes
- Use foreign key constraints
- Document complex queries
- Never store sensitive data in plain text
- Use Flyway migrations for all schema changes

## Commit Guidelines

### Commit Messages

- Write commit messages in English
- Keep the subject line under 72 characters
- Do not use conventional commit prefixes (no `feat:`, `fix:`, etc.)
- Use imperative mood ("Add feature" not "Added feature")
- Capitalize the first letter
- Do not end the subject line with a period
- Separate subject from body with a blank line
- Use the body to explain what and why (not how)
- Make atomic commits (one logical change per commit)

### Examples

**Good:**
```
Add vaccine inventory low stock alerts

Implements automated alerts when vaccine stock falls below 10 units.
This helps medical staff proactively reorder vaccines before running out.
```

**Bad:**
```
feat: added some stuff and fixed bugs
```

## Pull Request Process

1. Ensure your code follows all coding standards
2. Update documentation if you changed APIs or added features
3. Add or update tests as appropriate
4. Ensure all tests pass
5. Update the README.md if necessary
6. Request review from maintainers
7. Address any review feedback promptly
8. Squash commits if requested

### Pull Request Title

- Follow the same guidelines as commit messages
- Be descriptive and concise
- No emojis

### Pull Request Description

- Describe what changes you made
- Explain why you made these changes
- Reference related issues (e.g., "Closes #123")
- Include screenshots for UI changes
- List any breaking changes

## Reporting Bugs

Before reporting a bug:
- Check existing issues to avoid duplicates
- Verify the bug exists in the latest version
- Collect relevant information (logs, screenshots, steps to reproduce)

When reporting, include:
- Clear, descriptive title
- Expected behavior
- Actual behavior
- Steps to reproduce
- Environment details (OS, Java version, Node version, browser)
- Relevant logs or error messages
- Screenshots if applicable

## Suggesting Enhancements

Enhancement suggestions are welcome. Please provide:
- Clear description of the proposed feature
- Rationale for why this enhancement would be useful
- Possible implementation approach
- Any potential drawbacks or concerns

## Testing

JavaVacunas follows Test-Driven Development (TDD) principles with a target of 90%+ code coverage.

### Testing Strategy

**Unit Tests** (`*Test.java`):
- Test service layer business logic in isolation
- Use Mockito to mock dependencies
- Fast execution with H2 in-memory database
- Extend `BaseUnitTest` class
- Focus on edge cases, error handling, and validation

**Integration Tests** (`*IT.java`):
- Test repository layer with real Oracle database
- Use TestContainers for database provisioning
- Test database constraints, triggers, and migrations
- Extend `BaseIT` class
- Slower but ensure Oracle-specific behavior works

**API Tests**:
- Test REST endpoints with MockMvc
- Verify HTTP status codes and response structure
- Test authentication and authorization
- Validate request/response JSON
- Test error scenarios

### Writing Unit Tests

Example structure:
```java
@DisplayName("ServiceName Tests")
class ServiceNameTest extends BaseUnitTest {

    @Mock
    private DependencyRepository repository;

    @InjectMocks
    private ServiceName service;

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

### Writing Integration Tests

Example structure:
```java
@DisplayName("RepositoryName Integration Tests")
class RepositoryNameIT extends BaseIT {

    @Autowired
    private RepositoryName repository;

    @Test
    @DisplayName("Should find entity by criteria")
    void shouldFindEntityByCriteria() {
        // Given - setup test data
        Entity entity = createTestEntity();
        repository.save(entity);

        // When
        Optional<Entity> result = repository.findByCriteria(criteria);

        // Then
        assertThat(result).isPresent();
    }
}
```

### Test Best Practices

1. **Given-When-Then Pattern**: Structure tests clearly
2. **Descriptive Names**: Use `@DisplayName` with clear descriptions
3. **One Assertion Focus**: Test one thing at a time
4. **Mock External Dependencies**: Don't call real APIs or databases in unit tests
5. **Test Edge Cases**: Null values, empty lists, boundary conditions
6. **Test Error Scenarios**: Exceptions, validation failures, business rule violations
7. **Verify Interactions**: Use `verify()` to ensure mocks were called correctly
8. **Clean Test Data**: Integration tests clean up after themselves (see `BaseIT.cleanUp()`)

### Running Tests Locally

Before pushing code:

```bash
# Run all tests with coverage
cd backend
mvn clean verify jacoco:report

# View coverage report
open target/site/jacoco/index.html
```

Ensure coverage meets thresholds:
- Line coverage: >= 90%
- Branch coverage: >= 85%

### Test Coverage Requirements

All contributions must maintain or improve code coverage:
- New services: 90%+ coverage required
- New controllers: 90%+ coverage required
- Bug fixes: Add regression tests
- New features: Tests before implementation (TDD)

### CI/CD Test Execution

GitHub Actions automatically runs:
1. Unit tests on every push
2. Integration tests on every push
3. Coverage analysis
4. SonarCloud quality scan
5. Fails build if coverage drops below 90%

## Questions?

If you have questions about contributing, feel free to:
- Open an issue with the "question" label
- Review existing documentation in the repository
- Check the README.md for general project information

## License

By contributing to JavaVacunas, you agree that your contributions will be licensed under the GNU General Public License v3.0.

Thank you for contributing to improve vaccination management in Paraguay!
