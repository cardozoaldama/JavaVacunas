# Test Execution Order and Dependencies

This document describes the correct execution order for Bruno API tests and the dependencies between test files.

## Overview

The JavaVacunas API test collection now uses **dynamic test data** with no hardcoded entity IDs. Tests capture and reuse IDs through environment variables, making them portable across different environments and database instances.

## Environment Variables

### Authentication Tokens (Set by auth tests)
- `doctorToken` - JWT token for DOCTOR role
- `nurseToken` - JWT token for NURSE role
- `parentToken` - JWT token for PARENT role

### Dynamic Test IDs (Set by create tests)
- `testVaccineId` - First vaccine ID from database (captured by `get-all-vaccines`)
- `testChildId` - Child ID created by `create-child`
- `testAppointmentId` - Appointment ID created by `create-appointment`
- `testVaccinationId` - Vaccination record ID created by `create-vaccination-record`
- `testInventoryId` - Inventory ID created by `add-inventory`

### Dynamic Test Data (Set by pre-request scripts)
- `dynamicDocNumber` - Timestamp-based document number (prevents duplicates)
- `dynamicBatchNumber` - Timestamp-based batch number (prevents duplicates)
- `appointmentDateTime` - Future date for appointments (30 days from now)

## Required Execution Order

### 1. Authentication (MUST run first)
```bash
bru run api-tests/auth --env local
```

**Files (in any order):**
- `login-doctor.bru` → Sets `doctorToken`
- `login-nurse.bru` → Sets `nurseToken`
- `login-parent.bru` → Sets `parentToken`
- `register-new-nurse.bru` (optional)
- `register-new-parent.bru` (optional)

**Why first:** All other tests require valid JWT tokens.

---

### 2. Vaccines (Run after auth)
```bash
bru run api-tests/vaccines --env local
```

**Critical file (MUST run first in this folder):**
- `get-all-vaccines.bru` → Sets `testVaccineId` (first vaccine from database)

**Other files (order not critical):**
- `get-vaccine-by-id.bru` (uses `testVaccineId`)
- `get-vaccine-by-name.bru`
- `search-vaccines-by-disease.bru`
- `get-active-vaccines.bru`

**Why second:** Many tests need `testVaccineId` for vaccinations and inventory.

---

### 3. Children (Run after auth)
```bash
bru run api-tests/children --env local
```

**Recommended order:**
1. `create-child.bru` → Sets `testChildId` (MUST run first)
2. `get-child-by-id.bru` (uses `testChildId`)
3. `update-child.bru` (uses `testChildId`)
4. `get-all-children.bru`
5. `search-children.bru`
6. `create-child-by-nurse.bru` (independent)
7. `create-child-forbidden-parent.bru` (independent)
8. `update-child-forbidden-nurse.bru` (independent)
9. `delete-child.bru` (uses `testChildId`, MUST run last - deletes the test child)

**Why third:** Appointments and vaccinations require a child to exist.

---

### 4. Appointments (Run after auth + children)
```bash
bru run api-tests/appointments --env local
```

**Dependencies:** Requires `testChildId` from children tests

**Recommended order:**
1. `create-appointment.bru` → Sets `testAppointmentId` (uses `testChildId`)
2. `get-child-appointments.bru` (uses `testChildId`)
3. `confirm-appointment.bru` (uses `testAppointmentId` - state transition SCHEDULED→CONFIRMED)
4. `complete-appointment.bru` (uses `testAppointmentId` - state transition CONFIRMED→COMPLETED)
5. Other appointment tests (order not critical)

**State Machine:** SCHEDULED → CONFIRMED → COMPLETED/CANCELLED

**Note:** Cannot cancel after completing. Cannot complete without confirming first.

---

### 5. Vaccinations (Run after auth + children + vaccines)
```bash
bru run api-tests/vaccinations --env local
```

**Dependencies:**
- Requires `testChildId` from children tests
- Requires `testVaccineId` from vaccine tests

**Recommended order:**
1. `create-vaccination-record.bru` → Sets `testVaccinationId` (uses `testChildId`, `testVaccineId`)
2. `get-vaccination-by-id.bru` (uses `testVaccinationId`)
3. `get-child-vaccination-history.bru` (uses `testChildId`)
4. `get-vaccinations-by-vaccine.bru` (uses `testVaccineId`)
5. `create-vaccination-forbidden-parent.bru` (uses `testChildId`, `testVaccineId` - expects 403)

---

### 6. Inventory (Run after auth + vaccines)
```bash
bru run api-tests/inventory --env local
```

**Dependencies:** Requires `testVaccineId` from vaccine tests

**Recommended order:**
1. `add-inventory.bru` → Sets `testInventoryId` (uses `testVaccineId`)
2. `update-inventory-quantity.bru` (uses `testInventoryId`)
3. `get-inventory.bru`
4. `get-low-stock.bru`
5. Other inventory tests

---

### 7. Schedules (Run after auth)
```bash
bru run api-tests/schedules --env local
```

**No dependencies** - can run anytime after auth

**Files (order not critical):**
- `get-paraguay-schedule.bru`
- `get-schedule-by-country.bru`
- Other schedule tests

---

### 8. Users (Run after auth)
```bash
bru run api-tests/users --env local
```

**No dependencies** - can run anytime after auth

**Files (order not critical):**
- `get-all-users.bru`
- `get-user-by-id.bru`
- Other user tests

---

## Complete Test Run (Recommended Order)

```bash
# 1. Start backend
docker compose --env-file .env.docker up -d

# 2. Run tests in order
bru run api-tests/auth --env local
bru run api-tests/vaccines --env local
bru run api-tests/children --env local
bru run api-tests/appointments --env local
bru run api-tests/vaccinations --env local
bru run api-tests/inventory --env local
bru run api-tests/schedules --env local
bru run api-tests/users --env local
```

## Run All Tests at Once

```bash
# Run entire collection (requires proper execution order)
bru run api-tests --env local
```

**Note:** Bruno CLI respects the `seq` field in test files, so tests should run in the correct order within each folder.

## GUI Mode (Bruno Desktop)

In Bruno Desktop GUI, tests work reliably because:
- Environment variables persist across folders (unlike CLI mode)
- Token state is maintained throughout the session
- You can see test results immediately with syntax highlighting
- Pre-request and post-response scripts execute properly

**Recommended workflow:**

1. **Open collection:** File → Open Collection → Select `api-tests/`

2. **Select environment:** Choose `local` or `docker`

3. **Run tests in dependency order:**
   - **FIRST:** Run entire `auth/` folder (or manually run the 3 login tests)
     - This sets `doctorToken`, `nurseToken`, `parentToken`
   - **SECOND:** Run `vaccines/get-all-vaccines.bru`
     - This captures `testVaccineId` from your database
   - **THIRD:** Run `children/create-child.bru`
     - This creates a test child and captures `testChildId`
   - **THEN:** Run other tests/folders as needed

4. **For complete workflow testing:**
   - Run folders in this order: auth → vaccines → children → appointments → vaccinations → inventory → schedules → users
   - Or use "Run Folder" on each folder sequentially

**GUI Advantages over CLI:**
- ✅ Environment variables persist between folders
- ✅ Visual feedback with formatted responses
- ✅ Easy to re-run individual failed tests
- ✅ Can inspect request/response details
- ✅ No token persistence issues

## Troubleshooting

### Problem: Tests fail with 401 Unauthorized
**Solution:** Run auth tests first to set JWT tokens
```bash
bru run api-tests/auth --env local
```

### Problem: Tests fail with 404 Not Found (child/vaccine not found)
**Solution:** Run dependency tests first
```bash
# For child-related tests:
bru run api-tests/children/create-child.bru --env local

# For vaccine-related tests:
bru run api-tests/vaccines/get-all-vaccines.bru --env local
```

### Problem: Tests fail with 409 Conflict (duplicate document number)
**Solution:** This should no longer happen - `create-child` now uses dynamic timestamps. If it persists, check that:
- The test file has `script:pre-request` block
- The document number uses `{{dynamicDocNumber}}`

### Problem: Appointment date is in the past
**Solution:** This should no longer happen - `create-appointment` now uses dynamic future dates. The test calculates a date 30 days from now.

### Problem: Environment variables not persisting between folders (CLI mode)
**Solution:** This is a known Bruno CLI limitation. Workarounds:
- Run folders individually in order (recommended)
- Use Bruno GUI for integrated testing
- Set tokens manually in environment files

## Changes from Previous Version

### What was fixed:

1. **Hardcoded Child IDs removed** (8 files)
   - Now use `{{testChildId}}` from `create-child.bru`

2. **Hardcoded Vaccine IDs removed** (3 files)
   - Now use `{{testVaccineId}}` from `get-all-vaccines.bru`

3. **Document Number made dynamic**
   - Uses timestamp to prevent duplicates
   - Pattern: Last 7 digits of `Date.now()`

4. **Appointment Date made dynamic**
   - Calculates future date (30 days from now)
   - Prevents test failures when hardcoded date passes

5. **Batch Number made dynamic**
   - Uses timestamp to prevent duplicates
   - Pattern: "BATCH-" + last 8 digits of `Date.now()`

### Benefits:

- **Portable:** Tests work on any machine/database
- **No seed data required:** Tests create their own test data
- **No conflicts:** Dynamic values prevent duplicate errors
- **Repeatable:** Can run tests multiple times without cleanup
- **Isolated:** Each test run creates fresh test data

## Test Data Lifecycle

```
1. Login (auth tests) → Get JWT tokens
2. Get vaccines (vaccine tests) → Capture testVaccineId
3. Create child (children tests) → Capture testChildId (dynamic doc number)
4. Create appointment (appointment tests) → Capture testAppointmentId (uses testChildId)
5. Create vaccination (vaccination tests) → Capture testVaccinationId (uses testChildId + testVaccineId)
6. Create inventory (inventory tests) → Capture testInventoryId (uses testVaccineId, dynamic batch)
7. Read/Update operations use captured IDs
8. Delete operations clean up test data (optional)
```

## Best Practices

1. **Always run auth tests first** in a new test session
2. **Run get-all-vaccines early** to capture testVaccineId
3. **Run create-child before appointment/vaccination tests** to capture testChildId
4. **Follow state machine transitions** for appointments (SCHEDULED→CONFIRMED→COMPLETED)
5. **Don't run delete tests** until you're done with dependent tests
6. **Use Bruno GUI** for interactive testing and debugging
7. **Use Bruno CLI** for automated testing in CI/CD pipelines

## CI/CD Integration

For automated testing in CI/CD:

```bash
#!/bin/bash
set -e

# Start services
docker compose --env-file .env.docker up -d

# Wait for backend to be ready
sleep 10

# Run tests in order (exit on first failure)
bru run api-tests/auth --env local || exit 1
bru run api-tests/vaccines --env local || exit 1
bru run api-tests/children --env local || exit 1
bru run api-tests/appointments --env local || exit 1
bru run api-tests/vaccinations --env local || exit 1
bru run api-tests/inventory --env local || exit 1
bru run api-tests/schedules --env local || exit 1
bru run api-tests/users --env local || exit 1

echo "All tests passed!"
```

Save as `api-tests/run-all-tests.sh` and make executable:
```bash
chmod +x api-tests/run-all-tests.sh
```
