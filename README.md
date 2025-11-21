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

### Running Tests

Backend:
```bash
cd backend
mvn test
```

Frontend:
```bash
cd frontend
npm test
```

### Database Migrations

Database schema is managed by Flyway. Migrations are in `backend/src/main/resources/db/migration/`

## Contributing

This is a free and open-source project licensed under GNU GPL v3. Contributions are welcome.

## License

This project is licensed under the GNU General Public License v3.0 - see the [LICENSE](LICENSE) file for details.

## Acknowledgments

- Ministry of Public Health and Social Welfare of Paraguay (MSPBS)
- PAI (Expanded Program on Immunization) Paraguay
