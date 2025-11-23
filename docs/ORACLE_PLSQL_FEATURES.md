# Oracle PL/SQL Features

This document describes the Oracle Database PL/SQL features implemented in JavaVacunas, including functions, stored procedures, and packages that enhance the vaccination management system.

## Overview

JavaVacunas leverages Oracle Database 23c XE advanced features to implement complex business logic at the database level. This approach provides:

- Improved performance through reduced network round-trips
- Atomic transactions handled entirely in the database
- Centralized business logic for data integrity
- FIFO inventory management with automatic expiration handling

## PL/SQL Functions

### 1. fn_get_child_age_months

Calculates a child's age in months from their birth date.

**Signature:**
```sql
FUNCTION fn_get_child_age_months(p_child_id IN NUMBER) RETURN NUMBER
```

**Usage in Java:**
```java
Integer ageInMonths = childRepository.getChildAgeInMonths(childId);
```

**Example:**
```sql
SELECT fn_get_child_age_months(123) FROM DUAL;
-- Returns: 24 (child is 24 months old)
```

### 2. fn_get_available_stock

Returns the total available stock for a vaccine, considering only non-expired inventory with AVAILABLE status.

**Signature:**
```sql
FUNCTION fn_get_available_stock(p_vaccine_id IN NUMBER) RETURN NUMBER
```

**Usage in Java:**
```java
Integer availableStock = vaccineInventoryRepository.getAvailableStock(vaccineId);
```

**Example:**
```sql
SELECT fn_get_available_stock(5) FROM DUAL;
-- Returns: 150 (150 doses available)
```

### 3. fn_is_vaccine_overdue

Checks if a vaccine is overdue for a child based on the PAI vaccination schedule.
Returns 'Y' if overdue, 'N' otherwise. Includes a 1-month grace period.

**Signature:**
```sql
FUNCTION fn_is_vaccine_overdue(p_child_id IN NUMBER, p_vaccine_id IN NUMBER) RETURN VARCHAR2
```

**Usage in Java:**
```java
boolean isOverdue = plSqlVaccinationService.isVaccineOverdue(childId, vaccineId);
```

**Example:**
```sql
SELECT fn_is_vaccine_overdue(123, 5) FROM DUAL;
-- Returns: 'Y' (vaccine is overdue)
```

### 4. fn_get_vaccination_coverage

Calculates the vaccination coverage percentage for a given date range.

**Signature:**
```sql
FUNCTION fn_get_vaccination_coverage(p_start_date IN DATE, p_end_date IN DATE) RETURN NUMBER
```

**Usage in Java:**
```java
Double coverage = plSqlVaccinationService.getVaccinationCoverage(startDate, endDate);
```

**Example:**
```sql
SELECT fn_get_vaccination_coverage(DATE '2024-01-01', DATE '2024-12-31') FROM DUAL;
-- Returns: 87.5 (87.5% coverage)
```

## PL/SQL Stored Procedures

### 1. sp_administer_vaccine

Administers a vaccine to a child in a single atomic transaction. This procedure handles:
- Child, vaccine, and user validation
- Inventory lookup using FIFO (earliest expiration first)
- Vaccination record creation
- Inventory quantity update
- Automatic appointment status update

**Signature:**
```sql
PROCEDURE sp_administer_vaccine(
    p_child_id IN NUMBER,
    p_vaccine_id IN NUMBER,
    p_administered_by IN NUMBER,
    p_batch_number IN VARCHAR2,
    p_administration_date IN DATE,
    p_administration_site IN VARCHAR2 DEFAULT NULL,
    p_notes IN VARCHAR2 DEFAULT NULL,
    p_record_id OUT NUMBER
)
```

**Usage in Java:**
```java
Long recordId = plSqlVaccinationService.administerVaccine(
    childId,
    vaccineId,
    userId,
    "BATCH-2024-001",
    LocalDate.now(),
    "Left arm",
    "No adverse reactions"
);
```

**Error Codes:**
- `-20001`: Child not found or inactive
- `-20002`: Vaccine not found or inactive
- `-20003`: User not authorized to administer vaccines
- `-20004`: No available inventory for this vaccine

### 2. sp_deduct_inventory

Deducts inventory for a vaccine using FIFO strategy with validations and audit logging.

**Signature:**
```sql
PROCEDURE sp_deduct_inventory(
    p_vaccine_id IN NUMBER,
    p_quantity IN NUMBER,
    p_batch_number IN VARCHAR2 DEFAULT NULL,
    p_reason IN VARCHAR2 DEFAULT 'USAGE',
    p_deducted_by IN NUMBER
)
```

**Usage in Java:**
```java
plSqlVaccinationService.deductInventory(
    vaccineId,
    10,
    null, // Any batch (FIFO)
    "WASTAGE",
    userId
);
```

**Error Codes:**
- `-20005`: Quantity must be positive
- `-20006`: Invalid user
- `-20007`: Insufficient stock
- `-20008`: Failed to deduct full quantity

## PL/SQL Package: pkg_vaccination_management

A package that groups related vaccination management functions for the PAI schedule.

### Functions in Package

#### 1. get_pending_vaccines

Returns a pipelined table of pending vaccines for a child based on age and PAI schedule.

**Signature:**
```sql
FUNCTION get_pending_vaccines(p_child_id IN NUMBER) RETURN t_vaccine_list PIPELINED
```

**Returns:** Table with columns:
- `vaccine_id`: Vaccine identifier
- `vaccine_name`: Vaccine name
- `scheduled_age_months`: Age when vaccine should be administered
- `is_overdue`: 'Y' if overdue, 'N' otherwise

**Example:**
```sql
SELECT * FROM TABLE(pkg_vaccination_management.get_pending_vaccines(123));
```

#### 2. calculate_next_appointment

Calculates the next appointment date for a child based on pending vaccines.

**Signature:**
```sql
FUNCTION calculate_next_appointment(p_child_id IN NUMBER) RETURN DATE
```

**Usage in Java:**
```java
LocalDate nextAppointment = childRepository.calculateNextAppointment(childId);
```

**Example:**
```sql
SELECT pkg_vaccination_management.calculate_next_appointment(123) FROM DUAL;
-- Returns: 15-JAN-2025
```

#### 3. validate_vaccine_application

Validates if a vaccine can be administered to a child. Checks:
- Child and vaccine are active
- Child is old enough for the vaccine
- Vaccine hasn't exceeded maximum doses
- Stock is available

**Signature:**
```sql
FUNCTION validate_vaccine_application(p_child_id IN NUMBER, p_vaccine_id IN NUMBER) RETURN VARCHAR2
```

**Usage in Java:**
```java
String validation = plSqlVaccinationService.validateVaccineApplication(childId, vaccineId);
if (validation.startsWith("ERROR")) {
    throw new BusinessException(validation);
}
```

**Returns:**
- `'OK'`: Vaccine can be administered
- `'ERROR: <message>'`: Validation failed with reason

#### 4. get_vaccination_completion

Returns the vaccination completion percentage for a child based on age and PAI schedule.

**Signature:**
```sql
FUNCTION get_vaccination_completion(p_child_id IN NUMBER) RETURN NUMBER
```

**Usage in Java:**
```java
Double completionPct = childRepository.getVaccinationCompletion(childId);
```

**Example:**
```sql
SELECT pkg_vaccination_management.get_vaccination_completion(123) FROM DUAL;
-- Returns: 75.00 (75% complete)
```

#### 5. get_overdue_vaccines_count

Returns the count of overdue vaccines for a child.

**Signature:**
```sql
FUNCTION get_overdue_vaccines_count(p_child_id IN NUMBER) RETURN NUMBER
```

**Usage in Java:**
```java
Integer overdueCount = childRepository.getOverdueVaccinesCount(childId);
```

**Example:**
```sql
SELECT pkg_vaccination_management.get_overdue_vaccines_count(123) FROM DUAL;
-- Returns: 2 (2 vaccines overdue)
```

## Integration with Spring Boot

### Repository Methods

PL/SQL functions are integrated into Spring Data JPA repositories using `@Query` with `nativeQuery = true`:

```java
@Query(value = "SELECT fn_get_child_age_months(:childId) FROM DUAL", nativeQuery = true)
Integer getChildAgeInMonths(@Param("childId") Long childId);
```

### Service Layer

The `PlSqlVaccinationService` provides methods that call stored procedures using `EntityManager`:

```java
@PersistenceContext
private EntityManager entityManager;

@Transactional
public Long administerVaccine(...) {
    StoredProcedureQuery query = entityManager
        .createStoredProcedureQuery("sp_administer_vaccine")
        .registerStoredProcedureParameter("p_child_id", Long.class, ParameterMode.IN)
        // ... more parameters
        .setParameter("p_child_id", childId);

    query.execute();
    return (Long) query.getOutputParameterValue("p_record_id");
}
```

## Existing Triggers

JavaVacunas already includes several triggers (from V8 migration):

### Auto-Update Triggers
- `trg_users_updated_at`
- `trg_children_updated_at`
- `trg_vaccines_updated_at`
- `trg_vaccination_records_updated_at`
- `trg_appointments_updated_at`
- `trg_vaccine_inventory_updated_at`

### Business Logic Triggers
- `trg_audit_vaccination_records`: Automatic audit logging
- `trg_appointment_reminder`: Creates appointment reminders
- `trg_inventory_expire_check`: Auto-expires inventory
- `trg_inventory_alerts`: Alerts for low stock and expiring vaccines

## Migration Files

The PL/SQL features are managed through Flyway migrations:

- `V12__create_plsql_functions.sql`: Creates the 4 utility functions
- `V13__create_plsql_procedures.sql`: Creates the 2 stored procedures
- `V14__create_vaccination_package.sql`: Creates the vaccination management package

## Benefits

1. **Performance**: Reduced network round-trips between application and database
2. **Data Integrity**: Complex transactions handled atomically in database
3. **Reusability**: PL/SQL code can be used by multiple applications
4. **FIFO Inventory**: Automatic first-in-first-out inventory management
5. **Business Logic**: Vaccination schedule logic centralized in database

## Testing Considerations

When testing code that uses PL/SQL features:

1. **Unit Tests**: Use H2 database (existing approach)
   - PL/SQL features won't work in H2
   - Mock repository methods that call PL/SQL functions

2. **Integration Tests**: Require Oracle database
   - Use TestContainers with Oracle XE
   - Currently disabled due to Docker API compatibility

3. **Manual Testing**: Use Oracle database
   - Start Oracle with `docker-compose up oracle-db`
   - Run application with `docker` profile

## Example Usage Scenarios

### Scenario 1: Administer Vaccine with Full Validation

```java
@Service
public class VaccinationController {
    private final PlSqlVaccinationService plSqlService;

    public void administerVaccine(AdministerVaccineRequest request) {
        // Validate using PL/SQL package
        String validation = plSqlService.validateVaccineApplication(
            request.getChildId(),
            request.getVaccineId()
        );

        if (validation.startsWith("ERROR")) {
            throw new BusinessException(validation);
        }

        // Administer using stored procedure
        Long recordId = plSqlService.administerVaccine(
            request.getChildId(),
            request.getVaccineId(),
            getCurrentUserId(),
            request.getBatchNumber(),
            request.getAdministrationDate(),
            request.getAdministrationSite(),
            request.getNotes()
        );
    }
}
```

### Scenario 2: Display Child Vaccination Progress

```java
@Service
public class ChildDashboardService {
    private final ChildRepository childRepository;

    public ChildDashboardDto getChildDashboard(Long childId) {
        return ChildDashboardDto.builder()
            .ageInMonths(childRepository.getChildAgeInMonths(childId))
            .completionPercentage(childRepository.getVaccinationCompletion(childId))
            .overdueVaccinesCount(childRepository.getOverdueVaccinesCount(childId))
            .nextAppointment(childRepository.calculateNextAppointment(childId))
            .build();
    }
}
```

### Scenario 3: Generate Coverage Report

```java
@Service
public class ReportService {
    private final PlSqlVaccinationService plSqlService;

    public CoverageReportDto generateCoverageReport(LocalDate start, LocalDate end) {
        Double coverage = plSqlService.getVaccinationCoverage(start, end);
        return new CoverageReportDto(start, end, coverage);
    }
}
```

## References

- Oracle PL/SQL Documentation: https://docs.oracle.com/en/database/oracle/oracle-database/23/
- Spring Data JPA Native Queries: https://docs.spring.io/spring-data/jpa/reference/jpa/query-methods.html
- Stored Procedures in JPA: https://docs.spring.io/spring-data/jpa/reference/jpa/stored-procedures.html
