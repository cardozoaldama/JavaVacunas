# Bruno CLI Testing Guide

Complete guide for running JavaVacunas API tests with Bruno CLI.

## Prerequisites

1. **Install Bruno CLI**
   ```bash
   npm install -g @usebruno/cli
   ```

2. **Verify Installation**
   ```bash
   bru --version
   # Should show: 2.15.0 or later
   ```

3. **Start Backend**
   ```bash
   # From project root
   docker compose --env-file .env.docker up -d

   # Verify backend is running
   curl http://localhost:8080/actuator/health
   # Should return: {"status":"UP"}
   ```

## Running Tests

### Run All Tests

```bash
cd api-tests
bru run --env local
```

**Note:** When running all tests together, you may see some failures because environment variables (tokens) don't persist between test categories in CLI mode.

### Run Specific Test Category

```bash
cd api-tests

# Authentication tests (run these first!)
bru run auth --env local

# Vaccine tests
bru run vaccines --env local

# Children tests
bru run children --env local

# User tests
bru run users --env local

# Appointment tests
bru run appointments --env local

# Vaccination record tests
bru run vaccinations --env local

# Schedule tests
bru run schedules --env local

# Inventory tests
bru run inventory --env local
```

### Recommended Test Execution Order

For best results, run tests in this order:

```bash
cd api-tests

# 1. Run auth tests first to verify login works
bru run auth --env local

# 2. Run vaccine tests (read-only, no auth issues)
bru run vaccines --env local

# 3. Run schedule tests (read-only)
bru run schedules --env local

# 4. Run other test categories as needed
bru run children --env local
bru run users --env local
```

## Understanding Test Results

### Successful Test Output

```
auth/login-doctor (200 ) - 143 ms
Tests
   ✓ should return 200 status
   ✓ should return token
   ✓ should return user with DOCTOR role

📊 Execution Summary
┌───────────────┬──────────────┐
│ Status        │    ✓ PASS    │
├───────────────┼──────────────┤
│ Requests      │ 6 (6 Passed) │
├───────────────┼──────────────┤
│ Tests         │    17/17     │
└───────────────┴──────────────┘
```

### Failed Test Output

```
children/create-child (403 ) - 47 ms
Tests
   ✕ should return 201 status
      expected 403 to equal 201
```

**Common failure reasons:**
- **403 Forbidden**: Missing or invalid JWT token
- **409 Conflict**: Resource already exists (e.g., duplicate username)
- **404 Not Found**: Resource doesn't exist or was deleted

## CLI Limitations vs GUI

### Bruno GUI
- ✓ Tokens persist across all tests
- ✓ Environment variables shared between folders
- ✓ Visual test results
- ✓ Easy debugging
- ✓ Interactive test execution

### Bruno CLI
- ✗ Environment variables don't persist between runs
- ✗ Each test category runs independently
- ✓ Automated testing
- ✓ CI/CD integration
- ✓ Scriptable execution

## Workarounds for CLI Token Issues

### Option 1: Manual Token Setup

1. Run auth tests to get tokens:
   ```bash
   bru run auth --env local
   ```

2. Copy the token from a successful login

3. Set environment variable manually:
   ```bash
   export DOCTOR_TOKEN="your-token-here"
   ```

4. Update environment file to use the token

### Option 2: Run in GUI for Development

For day-to-day testing and development:
1. Open Bruno GUI
2. Load the `api-tests` collection
3. Select "local" environment
4. Run tests interactively

The GUI maintains token state across all tests.

### Option 3: Create Integration Test Script

Create a bash script that runs tests sequentially with token capture:

```bash
#!/bin/bash
# run-all-tests.sh

cd api-tests

echo "Running auth tests..."
bru run auth --env local

echo "Running vaccine tests..."
bru run vaccines --env local

# Add more test categories...

echo "All tests completed!"
```

## Test Categories Status

### ✓ Working Perfectly in CLI

- **auth/** - All 6 tests pass (17/17 assertions)
  - Login for all roles
  - Registration with dynamic usernames
  - Invalid credentials testing

### ⚠️ Requires Token from Auth

These tests need valid JWT tokens from auth tests:

- **children/** - CRUD operations require DOCTOR/NURSE token
- **users/** - User management requires DOCTOR token
- **appointments/** - Appointment management requires tokens
- **vaccinations/** - Vaccination records require NURSE token
- **inventory/** - Inventory management requires DOCTOR/NURSE token

### ✓ Read-Only Tests (Less Token-Sensitive)

- **vaccines/** - Most tests work with any valid token
- **schedules/** - Public schedule information

## Output Formats

### Default (Console)

```bash
bru run auth --env local
```

Shows colored output with test results.

### JSON Output

```bash
bru run auth --env local --output results.json
```

Generates JSON file with detailed results.

### Quiet Mode

```bash
bru run auth --env local --reporter simple
```

Minimal output, useful for CI/CD.

## CI/CD Integration

### GitHub Actions Example

```yaml
name: API Tests

on: [push, pull_request]

jobs:
  test:
    runs-on: ubuntu-latest

    steps:
      - uses: actions/checkout@v3

      - name: Start services
        run: docker compose --env-file .env.docker up -d

      - name: Wait for backend
        run: |
          timeout 60 bash -c 'until curl -f http://localhost:8080/actuator/health; do sleep 2; done'

      - name: Install Bruno CLI
        run: npm install -g @usebruno/cli

      - name: Run auth tests
        run: cd api-tests && bru run auth --env local

      - name: Run vaccine tests
        run: cd api-tests && bru run vaccines --env local
```

## Troubleshooting

### Tests Fail with "Connection Refused"

**Problem:** Backend not running
```bash
curl http://localhost:8080/actuator/health
```

**Solution:**
```bash
docker compose --env-file .env.docker up -d
# Wait 30-60 seconds for Oracle DB to start
```

### Tests Fail with 403 Forbidden

**Problem:** Missing or expired JWT token

**Solution:** Run auth tests first:
```bash
bru run auth --env local
```

### Tests Fail with 409 Conflict

**Problem:** Resource already exists (e.g., user with same username)

**Solution:**
- Registration tests now use dynamic usernames with timestamps
- Tests can be run multiple times without conflicts

### Tests Fail with "Cannot find collection"

**Problem:** Not in correct directory

**Solution:**
```bash
cd /home/fcardozo/Projects/JavaVacunas/api-tests
bru run --env local
```

## Best Practices

### For Development
1. Use Bruno GUI for interactive testing
2. Run auth tests first
3. Test individual endpoints as you develop

### For CI/CD
1. Run tests in specific order (auth first)
2. Use JSON output for result processing
3. Set appropriate timeouts
4. Clean database between runs

### For Teachers/Students
1. Start with auth tests to understand authentication
2. Progress through categories sequentially
3. Use GUI for learning and visualization
4. Use CLI for automation practice

## Known Issues

### Issue 1: Token Persistence in CLI
**Status:** Known limitation
**Impact:** Tests requiring auth may fail when run as full collection
**Workaround:** Run test categories individually or use GUI

### Issue 2: 403 vs 401 for Unauthorized Access
**Status:** Spring Security behavior
**Impact:** Some "no token" tests expect 401 but get 403
**Solution:** Tests now accept both status codes

### Issue 3: Database State Between Runs
**Status:** By design
**Impact:** Some tests may fail if database is in unexpected state
**Solution:** Reset database or use fresh Docker containers

## Security Note

The security advisory you mentioned (GHSA-246j-fv2m-6jhx) has been fixed in Bruno CLI 2.15.0+. Always use the latest version:

```bash
npm update -g @usebruno/cli
bru --version
```

## Support

For issues with:
- **Bruno CLI**: https://github.com/usebruno/bruno/issues
- **JavaVacunas API**: Check main project README
- **Test Collection**: See api-tests/README.md

## Quick Reference

```bash
# Install
npm install -g @usebruno/cli

# Check version
bru --version

# Run all tests
cd api-tests && bru run --env local

# Run specific folder
cd api-tests && bru run auth --env local

# Output to JSON
cd api-tests && bru run auth --env local --output results.json

# Simple reporter (less verbose)
cd api-tests && bru run auth --env local --reporter simple
```

---

**Last Updated:** 2024-12-02
**Bruno CLI Version:** 2.15.0
**Collection Version:** 1.0.1
