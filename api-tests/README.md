# JavaVacunas API Test Collection

Comprehensive API test collection for the JavaVacunas vaccination management system using Bruno API client.

## Overview

This collection contains 50+ API tests covering all endpoints of the JavaVacunas system, organized by functional areas. The tests are designed for educational purposes, demonstrating REST API testing, authentication, role-based access control, and complete CRUD workflows.

## Prerequisites

1. Install Bruno API client
   - Download from: https://www.usebruno.com/
   - Or via npm: `npm install -g @usebruno/cli`

2. Start the JavaVacunas backend

   ```bash
   # Option 1: Using Docker Compose
   docker compose --env-file .env.docker up -d

   # Option 2: Local development
   cd backend
   mvn spring-boot:run
   ```

3. Ensure the API is accessible at `http://localhost:8080/api/v1`

## Collection Structure

```
api-tests/
├── auth/                    # Authentication tests
├── vaccines/                # Vaccine catalog tests
├── children/                # Child management tests
├── users/                   # User management tests
├── appointments/            # Appointment workflow tests
├── vaccinations/            # Vaccination record tests
├── schedules/               # Vaccination schedule tests
├── inventory/               # Vaccine inventory tests
└── environments/            # Environment configurations
```

## Quick Start

1. Open Bruno and import the collection
   - File > Open Collection
   - Select the `api-tests` directory

2. Select the environment
   - Choose `local` for local development
   - Choose `docker` for Docker Compose setup

3. Run the authentication tests first
   - Execute tests in the `auth/` folder
   - This will automatically save tokens to environment variables

4. Explore other test categories
   - Tests are numbered for sequential execution
   - Each folder contains related API tests

## Recent Improvements (December 2025)

### ✅ All Hardcoded IDs Removed

The test collection has been completely refactored to use **dynamic test data**:

**What was fixed:**
- ✅ **8 files** with hardcoded child IDs now use `{{testChildId}}`
- ✅ **3 files** with hardcoded vaccine IDs now use `{{testVaccineId}}`
- ✅ **Document numbers** are now timestamp-based (prevents duplicate conflicts)
- ✅ **Appointment dates** are now dynamically calculated (30 days in future)
- ✅ **Batch numbers** are now timestamp-based (prevents duplicate conflicts)

**Benefits:**
- ✨ **Portable:** Tests work on any machine/database without seed data
- ✨ **No conflicts:** Can run tests multiple times without cleanup
- ✨ **Isolated:** Each test run creates fresh test data
- ✨ **Reliable:** No dependencies on hardcoded database IDs

### New Test Runner Script

Run all tests in the correct order with one command:

```bash
# Make script executable (one-time)
chmod +x api-tests/run-all-tests.sh

# Run all tests
./api-tests/run-all-tests.sh

# Or specify environment
./api-tests/run-all-tests.sh docker
```

The script:
- ✅ Checks if backend is running
- ✅ Runs tests in correct dependency order
- ✅ Stops on first failure
- ✅ Shows colored output with pass/fail summary

### Test Execution Order

Tests must run in specific order due to dependencies. See **[TEST_EXECUTION_ORDER.md](./TEST_EXECUTION_ORDER.md)** for:
- Complete dependency graph
- Environment variable flow
- Troubleshooting guide
- CI/CD integration examples

**Quick order:**
1. `auth/` → Sets JWT tokens
2. `vaccines/` → Captures `testVaccineId`
3. `children/` → Captures `testChildId`
4. `appointments/` → Uses `testChildId`
5. `vaccinations/` → Uses `testChildId` + `testVaccineId`
6. `inventory/` → Uses `testVaccineId`
7. `schedules/` → No dependencies
8. `users/` → No dependencies

## Test Categories

### 1. Authentication (`auth/`)

Tests for user authentication and registration.

**Tests:**

- `login-doctor.bru` - Login with DOCTOR role
- `login-nurse.bru` - Login with NURSE role
- `login-parent.bru` - Login with PARENT role
- `register-new-nurse.bru` - Register a new nurse
- `register-new-parent.bru` - Register a new parent
- `invalid-login.bru` - Test invalid credentials

**Default Credentials:**

- Doctor: `admin` / `admin123`
- Nurse: `nurse` / `admin123`
- Parent: `parent` / `admin123`

**Response Structure (AuthResponse):**

```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "tokenType": "Bearer",
  "userId": 1,
  "username": "admin",
  "email": "admin@example.com",
  "role": "DOCTOR",
  "firstName": "Admin",
  "lastName": "User"
}
```

**Key Learning Points:**

- JWT authentication flow
- Token storage in environment variables
- Password validation rules
- Role-based user creation
- Flat response structure (role is at root level, not nested)

### 2. Vaccines (`vaccines/`)

Tests for vaccine catalog management.

**Tests:**

- `get-all-vaccines.bru` - Retrieve all active vaccines
- `get-vaccine-by-id.bru` - Get specific vaccine details
- `get-vaccine-by-name.bru` - Search vaccine by name
- `search-vaccines-by-disease.bru` - Search by disease prevented
- `unauthorized-access.bru` - Test authentication requirement

**Key Learning Points:**

- RESTful GET operations
- Query parameters
- Path parameters
- Authentication headers (Bearer token)

### 3. Children (`children/`)

Tests for child/infant management with CRUD operations.

**Tests:**

- `create-child.bru` - Create new child (Doctor)
- `create-child-by-nurse.bru` - Create child (Nurse)
- `create-child-forbidden-parent.bru` - Test role restriction
- `get-all-children.bru` - List all children
- `get-child-by-id.bru` - Get child details
- `search-children.bru` - Search children by name
- `update-child.bru` - Update child information
- `delete-child.bru` - Soft delete child (Doctor only)
- `get-my-children.bru` - Get children for parent

**Key Learning Points:**

- Complete CRUD workflow
- Role-based access control (RBAC)
- Request body validation
- Date formatting
- Decimal precision (weight, height)

### 4. Users (`users/`)

Tests for user management and medical staff queries.

**Tests:**

- `get-all-users.bru` - Get all users (Doctor only)
- `get-all-doctors.bru` - List active doctors
- `get-all-nurses.bru` - List active nurses
- `get-medical-staff.bru` - Get all medical staff
- `get-users-by-role-forbidden.bru` - Test permission denied

**Key Learning Points:**

- Role-based endpoint access
- Filtering by user role
- 403 Forbidden responses

### 5. Appointments (`appointments/`)

Tests for appointment scheduling and lifecycle.

**Tests:**

- `create-appointment.bru` - Schedule new appointment
- `get-all-appointments.bru` - List appointments
- `get-child-appointments.bru` - Appointments for specific child
- `confirm-appointment.bru` - Confirm appointment (Parent)
- `complete-appointment.bru` - Complete appointment (Nurse)
- `get-upcoming-appointments.bru` - Future appointments
- `get-my-appointments.bru` - Parent's appointments
- `cancel-appointment.bru` - Cancel appointment

**Key Learning Points:**

- State machine workflow (SCHEDULED → CONFIRMED → COMPLETED)
- DateTime formatting (ISO 8601)
- Query parameters for filtering
- Role-specific permissions per action

### 6. Vaccinations (`vaccinations/`)

Tests for vaccination record management.

**Tests:**

- `create-vaccination-record.bru` - Record vaccine administration
- `get-child-vaccination-history.bru` - Complete vaccination history
- `get-vaccination-by-id.bru` - Get specific record
- `get-vaccinations-by-vaccine.bru` - Filter by vaccine type
- `create-vaccination-forbidden-parent.bru` - Test role restriction

**Key Learning Points:**

- Medical record creation
- Batch number tracking
- Expiration date validation
- Next dose scheduling
- Medical staff-only operations

### 7. Schedules (`schedules/`)

Tests for vaccination schedule queries.

**Tests:**

- `get-paraguay-schedule.bru` - Complete PAI schedule
- `get-mandatory-schedules.bru` - Mandatory vaccines by age
- `get-all-schedules.bru` - All vaccination schedules

**Key Learning Points:**

- Paraguay PAI (Programa Ampliado de Inmunizaciones) schedule
- Age-based vaccine recommendations
- Mandatory vs. optional vaccines

### 8. Inventory (`inventory/`)

Tests for vaccine inventory management.

**Tests:**

- `add-inventory.bru` - Add new vaccine batch
- `get-all-inventory.bru` - List all inventory
- `get-expiring-soon.bru` - Vaccines expiring soon
- `get-low-stock.bru` - Low stock alerts
- `update-inventory-quantity.bru` - Update stock quantity
- `forbidden-for-parent.bru` - Test medical staff-only access

**Key Learning Points:**

- Inventory tracking
- FIFO (First In, First Out) logic
- Stock alerts
- Batch management
- Date-based filtering

## Role-Based Access Control (RBAC)

The JavaVacunas system implements three user roles with different permissions:

### DOCTOR

- Full system access
- Can create, update, and delete all records
- Manage users, children, appointments, vaccinations, and inventory
- Access all reports and analytics

### NURSE

- Read/write access to operational data
- Create and manage children, appointments, and vaccinations
- Manage inventory
- Cannot delete records or manage users

### PARENT

- Read-only access to own children's data
- View vaccination history and schedules
- Schedule and confirm appointments for own children
- Cannot access inventory or other users' data

## Environment Variables

The collection uses environment variables to store dynamic values:

**Automatically Set:**

- `doctorToken` - JWT token for DOCTOR role (set by login-doctor.bru)
- `nurseToken` - JWT token for NURSE role (set by login-nurse.bru)
- `parentToken` - JWT token for PARENT role (set by login-parent.bru)
- `testVaccineId` - ID of first vaccine from database (set by get-all-vaccines.bru)
- `testChildId` - ID of created test child (set by create-child.bru)
- `testAppointmentId` - ID of created appointment (set by create-appointment.bru)
- `testVaccinationId` - ID of vaccination record (set by create-vaccination-record.bru)
- `testInventoryId` - ID of inventory item (set by add-inventory.bru)

**Dynamically Generated (by pre-request scripts):**

- `dynamicDocNumber` - Timestamp-based document number (prevents duplicates)
- `dynamicBatchNumber` - Timestamp-based batch number (prevents duplicates)
- `appointmentDateTime` - Future appointment date (30 days from now)

**Manually Configured:**

- `baseUrl` - API base URL (default: `http://localhost:8080/api/v1`)

## Running Tests

### Sequential Execution (Recommended for Learning)

Execute tests in order within each folder:

1. Start with `auth/` folder - Run all authentication tests
2. Move to `vaccines/` - Test vaccine catalog
3. Continue with `children/` - Test child management
4. And so on...

### Individual Test Execution

Click any `.bru` file and press "Send" to execute a single test.

### Folder Execution

Right-click a folder and select "Run Folder" to execute all tests in sequence.

## Test Assertions

Each test includes assertions to verify:

- HTTP status codes (200, 201, 204, 401, 403, 404)
- Response structure and data types
- Required fields presence
- Business logic validation
- Role-based access enforcement

## Educational Use Cases

### For Students

1. **API Testing Fundamentals**
   - HTTP methods (GET, POST, PUT, DELETE)
   - Request headers and body
   - Query and path parameters
   - Response validation

2. **Authentication & Security**
   - JWT token-based authentication
   - Bearer token usage
   - Role-based access control
   - Password complexity requirements

3. **REST API Design**
   - RESTful conventions
   - Resource naming
   - HTTP status codes
   - Error handling

4. **Domain Knowledge**
   - Healthcare data management
   - Vaccination schedules
   - Medical record keeping
   - Inventory management

### For Teachers

1. **Demonstration Scenarios**
   - Complete CRUD operations
   - State machine workflows
   - Role-based permissions
   - Data validation

2. **Testing Exercises**
   - Modify tests to add new assertions
   - Create tests for edge cases
   - Test error scenarios
   - Performance testing

3. **Integration Testing**
   - Multi-step workflows
   - Data dependencies
   - Transaction rollback
   - Idempotency

## Troubleshooting

### Tests Failing with 401 Unauthorized

- Run authentication tests first to generate tokens
- Check token expiration (24 hours)
- Verify environment is selected

### Tests Failing with 403 Forbidden

- Verify you're using the correct role token
- Check role permissions in test description
- Some operations require specific roles (e.g., delete requires DOCTOR)

### Connection Refused

- Verify backend is running
- Check `baseUrl` in environment matches your setup
- Ensure database is accessible

### Validation Errors (400 Bad Request)

- Check request body matches DTO requirements
- Verify date formats (ISO 8601: `YYYY-MM-DD`)
- Ensure required fields are present

## Advanced Usage

### Custom Scenarios

Create custom test scenarios by:

1. Copying existing tests
2. Modifying request parameters
3. Adding new assertions
4. Creating test chains with environment variables

### Automation

Run tests from command line:

```bash
# Install Bruno CLI
npm install -g @usebruno/cli

# Navigate to collection directory
cd api-tests

# Run authentication tests (recommended first!)
bru run auth --env local

# Run specific test category
bru run vaccines --env local

# Run with JSON output
bru run auth --env local --output results.json
```

**Important CLI Note:** Bruno CLI has different behavior than the GUI regarding environment variables and token persistence. **See [BRUNO_CLI_GUIDE.md](./BRUNO_CLI_GUIDE.md) for complete CLI documentation, limitations, and best practices.**

## Additional Documentation

- **API_RESPONSE_REFERENCE.md** - Complete API response structure reference
- **CHANGELOG.md** - Version history and bug fixes
- **TEACHER_GUIDE.md** - Lesson plans and educational resources
- **SUMMARY.md** - Quick reference and statistics

## External Documentation

- Bruno Documentation: https://docs.usebruno.com/
- JavaVacunas API Docs: http://localhost:8080/swagger-ui.html (when running)
- REST API Best Practices: https://restfulapi.net/

## Contributing

To add new tests:

1. Create `.bru` file in appropriate folder
2. Follow naming convention: `action-resource.bru`
3. Add comprehensive assertions
4. Document in this README
5. Test with all three roles if applicable

## License

This test collection is part of the JavaVacunas project and follows the same license.
