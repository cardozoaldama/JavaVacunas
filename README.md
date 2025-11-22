# JavaVacunas

[![CI/CD Pipeline](https://github.com/cardozoaldama/JavaVacunas/actions/workflows/ci.yml/badge.svg)](https://github.com/cardozoaldama/JavaVacunas/actions/workflows/ci.yml)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=cardozoaldama_JavaVacunas&metric=alert_status)](https://sonarcloud.io/dashboard?id=cardozoaldama_JavaVacunas)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=cardozoaldama_JavaVacunas&metric=coverage)](https://sonarcloud.io/dashboard?id=cardozoaldama_JavaVacunas)
[![codecov](https://codecov.io/gh/cardozoaldama/JavaVacunas/branch/main/graph/badge.svg)](https://codecov.io/gh/cardozoaldama/JavaVacunas)
[![Bugs](https://sonarcloud.io/api/project_badges/measure?project=cardozoaldama_JavaVacunas&metric=bugs)](https://sonarcloud.io/dashboard?id=cardozoaldama_JavaVacunas)
[![Security Rating](https://sonarcloud.io/api/project_badges/measure?project=cardozoaldama_JavaVacunas&metric=security_rating)](https://sonarcloud.io/dashboard?id=cardozoaldama_JavaVacunas)
[![Maintainability Rating](https://sonarcloud.io/api/project_badges/measure?project=cardozoaldama_JavaVacunas&metric=sqale_rating)](https://sonarcloud.io/dashboard?id=cardozoaldama_JavaVacunas)
[![License: GPL v3](https://img.shields.io/badge/License-GPLv3-blue.svg)](https://www.gnu.org/licenses/gpl-3.0)
[![Java](https://img.shields.io/badge/Java-17-orange.svg)](https://openjdk.java.net/projects/jdk/17/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2-green.svg)](https://spring.io/projects/spring-boot)
[![React](https://img.shields.io/badge/React-18-blue.svg)](https://reactjs.org/)

A modern vaccination management system for children in Paraguay, following the PAI (Expanded Program on Immunization) schedule established by the Ministry of Public Health and Social Welfare.

## Table of Contents

- [Overview](#overview)
- [Features](#features)
- [Technology Stack](#technology-stack)
- [System Requirements](#system-requirements)
- [Quick Start](#quick-start)
- [Getting Started (Detailed)](#getting-started-detailed)
- [API Documentation](#api-documentation)
- [Useful Commands](#useful-commands)
- [Troubleshooting](#troubleshooting)
- [Paraguay Vaccination Schedule](#paraguay-vaccination-schedule)
- [Development](#development)
- [Contributing](#contributing)
- [License](#license)
- [Support](#support)

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
├── compose.yml        # Docker/Podman Compose configuration
├── .env.example       # Environment variables template
├── LICENSE            # GNU GPL v3
└── README.md
```

## System Requirements

### Software
- **Java**: JDK 17 or higher (OpenJDK recommended)
- **Node.js**: 18+ with npm
- **Maven**: 3.8+
- **Container Runtime**: Docker or Podman with docker-compose/podman-compose
- **Git**: For version control
- **IDE**: IntelliJ IDEA, Eclipse, or VS Code (with Java/Spring extensions)

### Hardware
- **RAM**: Minimum 8 GB (Oracle Database requires significant memory)
- **Disk Space**: 10 GB free space (includes Oracle DB, dependencies, and build artifacts)
- **CPU**: 2+ cores recommended

### Operating Systems
- Linux (Ubuntu 20.04+, Fedora 38+, etc.)
- macOS 12+
- Windows 10/11 with WSL2 (for Docker/Podman)

### Important Notes
- Oracle Database 21c XE requires accepting the [Oracle Technology Network License Agreement](https://www.oracle.com/downloads/licenses/standard-license.html)
- Lombok and MapStruct require IDE annotation processor configuration
- Integration tests are currently disabled due to TestContainers/Docker API compatibility (see [issue #32](https://github.com/cardozoaldama/JavaVacunas/pull/32))

## Quick Start

Get the application running in 3 simple steps:

```bash
# 1. Clone and configure
git clone https://github.com/cardozoaldama/JavaVacunas.git
cd JavaVacunas
cp .env.example .env

# 2. Start all services with Docker/Podman
docker compose up -d
# or
podman-compose up -d

# 3. Access the application
# Frontend: http://localhost:5173
# Backend API: http://localhost:8080
# Swagger UI: http://localhost:8080/swagger-ui.html
```

**Test Credentials:**
- Doctor: `admin` / `admin123`
- Nurse: `nurse` / `admin123`
- Parent: `parent` / `admin123`

> For local development without containers, see [Getting Started (Detailed)](#getting-started-detailed).

## Getting Started (Detailed)

### 1. Clone the Repository

```bash
git clone <repository-url>
cd JavaVacunas
```

### 2. Configure Environment Variables

Create a `.env` file in the project root:

```bash
cp .env.example .env
```

Edit `.env` and update values as needed. Key configurations:

```bash
# Database passwords
ORACLE_PWD=YourSecureOraclePassword
SPRING_DATASOURCE_PASSWORD=YourSecureDatabasePassword

# JWT secret (minimum 256 bits for HS256)
JWT_SECRET=YourVerySecureJWTSecretKeyMinimum256BitsForHS256

# Spring profile (docker, dev, prod)
SPRING_PROFILES_ACTIVE=docker

# API endpoint for frontend
VITE_API_BASE_URL=http://localhost:8080/api/v1
```

**Security Notes:**
- Never commit `.env` to version control (already in `.gitignore`)
- Change all default passwords in production
- Use strong JWT secret for production deployments

### 3. Development Mode Options

Choose between containerized or local development:

#### Option A: Full Containerized Setup (Recommended for Testing)

```bash
# Start all services (database, backend, frontend)
docker compose up -d

# View logs
docker compose logs -f

# Stop services
docker compose down
```

#### Option B: Local Development (Recommended for Active Development)

Start only the database in a container, run backend and frontend locally:

```bash
# Start Oracle database
docker compose up -d oracle-db

# Run backend (in new terminal)
cd backend
mvn spring-boot:run

# Run frontend (in new terminal)
cd frontend
npm install
npm run dev
```

Access points:
- Frontend: `http://localhost:5173`
- Backend API: `http://localhost:8080`
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- Oracle EM Express: `https://localhost:5500/em` (requires database startup)

## API Documentation

Interactive API documentation available when backend is running:
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/api-docs`

**User Roles:**
- **DOCTOR/NURSE**: Administer vaccinations, manage appointments
- **PARENT**: View child history, schedule appointments

## Useful Commands

### Container Management

```bash
# Start all services
docker compose up -d

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
```

### Backend Development

```bash
# Build the project
mvn clean install

# Run unit tests
mvn test

# Run with specific profile
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# Package without tests
mvn package -DskipTests

# Generate test coverage report
mvn clean verify jacoco:report
# View at: backend/target/site/jacoco/index.html
```

### Frontend Development

```bash
# Install dependencies
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

### Database Access

```bash
# Connect to Oracle DB in container
docker exec -it javacunas-oracle sqlplus JAVACUNAS/JavaCunas123@XEPDB1

# View database logs
docker compose logs -f oracle-db

# Backup database (container must be running)
docker exec javacunas-oracle ./backup.sh
```

## Troubleshooting

### Database Issues

**Oracle container fails to start or is unhealthy:**
```bash
# Check Oracle logs
docker compose logs oracle-db

# Common fix: Remove volume and restart
docker compose down -v
docker compose up -d oracle-db

# Wait for healthcheck (can take 3-5 minutes on first start)
docker compose ps
```

**Connection refused errors:**
- Database takes 2-3 minutes to initialize on first start
- Check health status: `docker compose ps`
- Verify credentials in `.env` match the database user

### Backend Issues

**Backend won't start - "Unable to acquire JDBC Connection":**
- Wait for Oracle to become healthy: `docker compose ps`
- Verify `SPRING_DATASOURCE_URL` points to correct database
- Check Oracle is accessible: `docker compose logs oracle-db`

**Flyway migration errors:**
- Never modify existing migrations
- If corrupted, clean database: `docker compose down -v`

**Lombok/MapStruct errors in IDE:**
- Enable annotation processing in IDE settings
- IntelliJ: Settings → Build → Compiler → Annotation Processors → Enable
- Eclipse: Project Properties → Java Compiler → Annotation Processing → Enable

### Frontend Issues

**API calls failing (CORS errors):**
- Verify backend is running: `curl http://localhost:8080/actuator/health`
- Check `VITE_API_BASE_URL` in `.env`
- Ensure Spring Security CORS configuration is correct

**Build errors:**
```bash
# Clear node_modules and reinstall
rm -rf node_modules package-lock.json
npm install
```

### General Issues

**Port already in use:**
```bash
# Check what's using port 8080
lsof -i :8080  # macOS/Linux
netstat -ano | findstr :8080  # Windows

# Change ports in .env file
BACKEND_PORT=8081
FRONTEND_PORT=5174
```

**Memory issues (Oracle):**
- Oracle requires minimum 8GB RAM
- Adjust Docker/Podman memory allocation in settings
- On Linux, check available memory: `free -h`

### Health Check Endpoints

Monitor service health:
```bash
# Backend health
curl http://localhost:8080/actuator/health

# Database health (from backend)
curl http://localhost:8080/actuator/health/db

# All actuator endpoints
curl http://localhost:8080/actuator
```

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

JavaVacunas follows TDD principles with 90%+ code coverage target.

**Test Infrastructure:**
- Unit Tests: JUnit 5, Mockito, AssertJ (in-memory H2)
- Integration Tests: TestContainers with Oracle 23c XE (currently disabled - see [#32](https://github.com/cardozoaldama/JavaVacunas/pull/32))
- Coverage: JaCoCo (90% line, 85% branch minimum)
- CI/CD: GitHub Actions with SonarCloud analysis

**Test Structure:**
- Unit tests: `*Test.java` (extends `BaseUnitTest`)
- Integration tests: `*IT.java` (extends `BaseIT`)
- Location: `backend/src/test/java/py/gov/mspbs/javacunas/`

**Run Tests:**
```bash
mvn test              # Unit tests only
mvn verify            # All tests
mvn clean verify jacoco:report  # With coverage report
```

View coverage: `backend/target/site/jacoco/index.html`

**CI Pipeline:**
Every push/PR triggers: build → tests → coverage → SonarCloud scan → security checks

### Database Migrations

Managed by Flyway in `backend/src/main/resources/db/migration/`

Rules:
- Never modify existing migrations
- Use sequential versioning: `V{number}__{description}.sql`
- Test migrations locally before committing

### IDE Setup

**IntelliJ IDEA:**
1. Import as Maven project
2. Enable annotation processing: Settings → Build → Compiler → Annotation Processors
3. Install Lombok plugin
4. Set Java SDK to 17+

**VS Code:**
1. Install extensions: Java Extension Pack, Spring Boot Extension Pack
2. Configure Java SDK in settings
3. Enable annotation processing in Java settings

### Code Style

- Follow existing patterns in the codebase
- Backend: Java conventions, Spring best practices
- Frontend: TypeScript strict mode, React hooks, functional components
- All code and commits in English; UI text in Spanish
- See [CONTRIBUTING.md](.github/CONTRIBUTING.md) for detailed guidelines

## Contributing

Contributions are welcome! This is a free and open-source project under GNU GPL v3.

**How to contribute:**
1. Read our [Contributing Guide](.github/CONTRIBUTING.md) and [Code of Conduct](.github/CODE_OF_CONDUCT.md)
2. Fork the repository and create a feature branch
3. Write tests for your changes (maintain 90%+ coverage)
4. Follow our coding standards and commit guidelines
5. Submit a pull request

**Quick Guidelines:**
- All code, comments, and commits in English
- Frontend UI text in Spanish
- No emojis in code or commits
- Use Test-Driven Development (TDD)
- Write atomic commits with clear messages

For detailed guidelines, testing requirements, and development workflow, see [CONTRIBUTING.md](.github/CONTRIBUTING.md).

**Test Credentials for Development:**
- Doctor: `admin` / `admin123`
- Nurse: `nurse` / `admin123`
- Parent: `parent` / `admin123`

## License

This project is licensed under the GNU General Public License v3.0 - see the [LICENSE](LICENSE) file for details.

By contributing, you agree that your contributions will be licensed under the same GPL v3 license.

## Acknowledgments

- Ministry of Public Health and Social Welfare of Paraguay (MSPBS)
- PAI (Expanded Program on Immunization) Paraguay
- Open source community and contributors

## Support

- **Documentation**: Check this README and [CONTRIBUTING.md](.github/CONTRIBUTING.md)
- **Issues**: Report bugs or request features via [GitHub Issues](https://github.com/cardozoaldama/JavaVacunas/issues)
- **Security**: Report vulnerabilities via [Security Policy](.github/SECURITY.md)
