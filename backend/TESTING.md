# Testing Documentation - JavaVacunas Backend

## Current Testing Status

### Unit Tests
- **Status**: ✅ ACTIVE
- **Count**: 46 tests
- **Coverage**: All unit tests passing
- **Database**: H2 in-memory database
- **Run**: `mvn test`

### Integration Tests
- **Status**: ⚠️ TEMPORARILY DISABLED
- **Reason**: TestContainers 1.21.3 incompatible with Docker 29.x
- **Files**: Renamed to `*.java.disabled`
- **Re-enable when**: TestContainers adds support for Docker API 1.44+

---

## Problem Analysis

### Root Cause
TestContainers 1.21.3 uses Docker API version 1.32, but Docker 29.x requires minimum API version 1.44.

**Error Message:**
```
Status 400: {"message":"client version 1.32 is too old. Minimum supported API version is 1.44,
please upgrade your client to a newer version"}
```

### Decision
**We chose to maintain modern Docker 29.x and disable integration tests temporarily** rather than downgrade to older Docker versions. This prioritizes using stable, modern tooling.

---

## How to Run Tests

### Unit Tests Only (Current Setup)
```bash
cd backend
mvn clean test
```

### Full Build with Coverage
```bash
mvn clean test jacoco:report
```

### View Coverage Report
```bash
# After running tests with jacoco:report
open target/site/jacoco/index.html
```

---

## Re-enabling Integration Tests

When TestContainers is updated to support Docker API 1.44+:

### 1. Uncomment Dependencies in `pom.xml`
```xml
<!-- Find these lines around line 165-194 -->
<!-- TestContainers dependencies - DISABLED until Docker API compatibility is resolved -->
<!-- Uncomment when TestContainers supports Docker API 1.44+ -->
```

### 2. Enable Failsafe Plugin in `pom.xml`
```xml
<!-- Find around line 267 -->
<configuration>
    <skip>true</skip>  <!-- Change to <skip>false</skip> -->
    <includes>
        <include>**/*IT.java</include>
    </includes>
</configuration>
```

### 3. Rename Test Files
```bash
cd src/test/java/py/gov/mspbs/javacunas
mv BaseIT.java.disabled BaseIT.java
mv config/TestContainersConfiguration.java.disabled config/TestContainersConfiguration.java

cd service
for file in *.disabled; do mv "$file" "${file%.disabled}"; done
```

### 4. Test Locally
```bash
mvn clean verify
```

### 5. Update CI/CD
In `.github/workflows/ci.yml`, change:
```yaml
# From:
- name: Package application
  working-directory: ./backend
  run: mvn package -DskipTests -B

# To:
- name: Run integration tests
  working-directory: ./backend
  run: mvn verify -B
```

---

## Integration Test Architecture

### TestContainers Setup
- **Database**: Oracle XE 21c via `gvenzl/oracle-xe:21-slim-faststart`
- **Profile**: `integration-test` (separate from `test` profile for H2)
- **Configuration**: `TestContainersConfiguration.java`
- **Base Class**: `BaseIT.java` with `@ActiveProfiles("integration-test")`

### Flyway Migrations
- **Main migrations**: `src/main/resources/db/migration/V*.sql`
- **Test-specific**: `src/test/resources/db/test-migration/V99__disable_triggers_for_tests.sql`
- **Schema**: `JAVACUNAS_TEST` (separate from production)

### Test Files (Currently Disabled)
- `AppointmentServiceIT.java.disabled`
- `VaccinationRecordServiceIT.java.disabled`
- `VaccinationScheduleServiceIT.java.disabled`
- `VaccineInventoryServiceIT.java.disabled`

---

## CI/CD Configuration

### Current Pipeline
1. Build: `mvn clean compile -B`
2. Unit Tests: `mvn test -B`
3. Package: `mvn package -DskipTests -B`
4. Coverage: `mvn jacoco:report`
5. SonarCloud Scan
6. Codecov Upload

### When Integration Tests Re-enabled
Replace step 3 with:
```yaml
- name: Run integration tests
  working-directory: ./backend
  run: mvn verify -B
```

---

## Monitoring for Updates

Check these resources regularly for TestContainers Docker API support:

- **TestContainers Releases**: https://github.com/testcontainers/testcontainers-java/releases
- **Docker Java API**: https://github.com/docker-java/docker-java/releases
- **Issue Tracker**: Search for "Docker API 1.44" in TestContainers issues

---

## Contact

If you have questions about testing setup, contact the development team or check:
- Project README
- CI/CD pipeline logs
- SonarCloud reports: https://sonarcloud.io
