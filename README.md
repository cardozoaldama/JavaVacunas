# JavaVacunas

A modern vaccination management system for children in Paraguay, following the PAI (Expanded Program on Immunization) schedule established by the Ministry of Public Health and Social Welfare.

## Overview

JavaVacunas is a full-stack application designed to help healthcare facilities manage infant vaccination records, appointments, and inventory. The system supports healthcare workers and parents/guardians in tracking vaccination schedules and ensuring children receive timely immunizations.

## Features

- **Child Management**: Register and maintain comprehensive child profiles
- **Vaccination Records**: Track administered vaccines with batch numbers and dates
- **Appointment Scheduling**: Manage vaccination appointments for healthcare facilities
- **Paraguay PAI Schedule**: Built-in official vaccination calendar
- **Vaccine Inventory**: Monitor vaccine stock, batch numbers, and expiration dates
- **Role-Based Access**: Separate interfaces for medical staff and parents
- **Audit Trail**: Complete history of all vaccination activities

## Technology Stack

### Backend
- Java 17+
- Spring Boot 3.2+
- Spring Data JPA
- Spring Security with JWT
- Oracle Database 23c XE
- Flyway for database migrations
- Maven

### Frontend
- React 18
- TypeScript
- Vite
- TanStack Query (React Query)
- Tailwind CSS
- Axios

### Infrastructure
- Podman/Docker for containerization
- Oracle 23c XE in container

## Project Structure

```
JavaVacunas/
├── backend/           # Spring Boot application
├── frontend/          # React application
├── docker/            # Container configurations
├── docs/              # Project documentation
├── LICENSE            # GNU GPL v3
└── README.md
```

## Prerequisites

- Java 17 or higher
- Node.js 18+ and npm
- Maven 3.8+
- Podman or Docker
- Git

## Getting Started

### 1. Clone the Repository

```bash
git clone <repository-url>
cd JavaVacunas
```

### 2. Start the Database

```bash
podman-compose up -d
```

### 3. Run the Backend

```bash
cd backend
mvn spring-boot:run
```

The API will be available at `http://localhost:8080`

### 4. Run the Frontend

```bash
cd frontend
npm install
npm run dev
```

The application will be available at `http://localhost:5173`

## API Documentation

Once the backend is running, access the interactive API documentation at:
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/api-docs`

## User Roles

- **DOCTOR/NURSE**: Administer and record vaccinations, manage appointments
- **PARENT**: View child vaccination history, schedule appointments

## Paraguay Vaccination Schedule

The system includes the complete PAI (Programa Ampliado de Inmunizaciones) schedule:

- Birth: BCG, Hepatitis B
- 2 months: Pentavalent, IPV, Rotavirus, Pneumococcal
- 4 months: Pentavalent, IPV, Rotavirus, Pneumococcal
- 6 months: Pentavalent, IPV, Pneumococcal
- 12 months: MMR, Yellow Fever
- 18 months: Varicella, MMR, DPT, bOPV
- 4 years: bOPV
- 11 years: HPV (girls)

## Development

### Testing

JavaVacunas maintains comprehensive test coverage with a goal of 90%+ for production readiness.

#### Test Infrastructure

- **Unit Tests**: JUnit 5, Mockito, AssertJ
- **Integration Tests**: TestContainers with Oracle 23c XE
- **Code Coverage**: JaCoCo
- **CI/CD**: GitHub Actions
- **Code Quality**: SonarCloud

#### Running Tests

**Unit Tests** (fast, in-memory H2):
```bash
cd backend
mvn test
```

**Integration Tests** (with TestContainers Oracle):
```bash
cd backend
mvn verify
```

**All Tests with Coverage**:
```bash
cd backend
mvn clean verify jacoco:report
```

View coverage report: `backend/target/site/jacoco/index.html`

**Coverage Thresholds**:
- Line Coverage: 90%
- Branch Coverage: 85%

#### Test Structure

```
backend/src/test/java/
├── py/gov/mspbs/javacunas/
│   ├── BaseUnitTest.java           # Base for unit tests
│   ├── BaseIT.java                 # Base for integration tests
│   ├── service/                    # Service layer tests
│   ├── controller/                 # API endpoint tests
│   ├── security/                   # Security tests
│   └── config/
│       └── TestContainersConfiguration.java
```

#### Test Naming Conventions

- Unit tests: `*Test.java` (e.g., `AuthServiceTest.java`)
- Integration tests: `*IT.java` (e.g., `ChildRepositoryIT.java`)

#### Continuous Integration

Every push and pull request triggers:
- Build compilation
- Unit tests
- Integration tests
- Code coverage analysis
- SonarCloud quality scan
- Security vulnerability checks

### Database Migrations

Database schema is managed by Flyway. Migrations are in `backend/src/main/resources/db/migration/`

## Contributing

This is a free and open-source project licensed under GNU GPL v3. Contributions are welcome.

## License

This project is licensed under the GNU General Public License v3.0 - see the [LICENSE](LICENSE) file for details.

## Acknowledgments

- Ministry of Public Health and Social Welfare of Paraguay (MSPBS)
- PAI (Expanded Program on Immunization) Paraguay
