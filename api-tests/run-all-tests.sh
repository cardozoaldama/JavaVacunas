#!/bin/bash

# JavaVacunas API Test Runner
# This script runs all Bruno API tests in the correct execution order
# Ensures proper test dependencies and environment variable propagation

set -e  # Exit on first error

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Configuration
ENV="${1:-local}"  # Use first argument or default to "local"
BRUNO_CLI="bru"

# Print header
echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}JavaVacunas API Test Suite${NC}"
echo -e "${BLUE}========================================${NC}"
echo ""
echo -e "Environment: ${YELLOW}${ENV}${NC}"
echo -e "Timestamp: $(date '+%Y-%m-%d %H:%M:%S')"
echo ""

# Check if Bruno CLI is installed
if ! command -v $BRUNO_CLI &> /dev/null; then
    echo -e "${RED}ERROR: Bruno CLI (bru) is not installed${NC}"
    echo ""
    echo "Please install Bruno CLI:"
    echo "  npm install -g @usebruno/cli"
    echo ""
    echo "Or download Bruno Desktop from:"
    echo "  https://www.usebruno.com/"
    exit 1
fi

# Check if backend is running
echo -e "${BLUE}Checking if backend is running...${NC}"
BACKEND_URL="http://localhost:8080/api/v1/auth/health"
if ! curl -s -f -o /dev/null "$BACKEND_URL" 2>/dev/null; then
    echo -e "${YELLOW}WARNING: Backend may not be running at localhost:8080${NC}"
    echo -e "${YELLOW}Tests will likely fail. Start the backend first:${NC}"
    echo -e "  ${YELLOW}docker compose --env-file .env.docker up -d${NC}"
    echo ""
    read -p "Continue anyway? (y/N): " -n 1 -r
    echo
    if [[ ! $REPLY =~ ^[Yy]$ ]]; then
        exit 1
    fi
else
    echo -e "${GREEN}✓ Backend is running${NC}"
fi

echo ""
echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}Starting Test Execution${NC}"
echo -e "${BLUE}========================================${NC}"
echo ""

# Test counter
TOTAL_TESTS=0
PASSED_TESTS=0
FAILED_TESTS=0

# Function to run a test folder
run_test_folder() {
    local folder=$1
    local description=$2

    echo -e "${BLUE}[$(($TOTAL_TESTS + 1))/8] Running: ${description}${NC}"
    echo -e "  Folder: api-tests/${folder}"

    if $BRUNO_CLI run "api-tests/${folder}" --env "$ENV"; then
        echo -e "${GREEN}✓ PASSED: ${description}${NC}"
        PASSED_TESTS=$((PASSED_TESTS + 1))
    else
        echo -e "${RED}✗ FAILED: ${description}${NC}"
        FAILED_TESTS=$((FAILED_TESTS + 1))
        echo ""
        echo -e "${RED}Test execution stopped due to failure${NC}"
        echo -e "${RED}Fix the failing tests before continuing${NC}"
        exit 1
    fi

    TOTAL_TESTS=$((TOTAL_TESTS + 1))
    echo ""
}

# Run tests in dependency order
echo -e "${YELLOW}Note: Tests are executed in dependency order${NC}"
echo -e "${YELLOW}See TEST_EXECUTION_ORDER.md for details${NC}"
echo ""

# 1. Authentication (MUST run first)
run_test_folder "auth" "Authentication Tests"

# 2. Vaccines (captures testVaccineId)
run_test_folder "vaccines" "Vaccine Tests"

# 3. Children (captures testChildId)
run_test_folder "children" "Child Management Tests"

# 4. Appointments (depends on testChildId)
run_test_folder "appointments" "Appointment Tests"

# 5. Vaccinations (depends on testChildId + testVaccineId)
run_test_folder "vaccinations" "Vaccination Record Tests"

# 6. Inventory (depends on testVaccineId)
run_test_folder "inventory" "Inventory Tests"

# 7. Schedules (no dependencies)
run_test_folder "schedules" "Vaccination Schedule Tests"

# 8. Users (no dependencies)
run_test_folder "users" "User Management Tests"

# Print summary
echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}Test Execution Summary${NC}"
echo -e "${BLUE}========================================${NC}"
echo ""
echo -e "Total Test Folders: ${TOTAL_TESTS}"
echo -e "${GREEN}Passed: ${PASSED_TESTS}${NC}"
echo -e "${RED}Failed: ${FAILED_TESTS}${NC}"
echo ""

if [ $FAILED_TESTS -eq 0 ]; then
    echo -e "${GREEN}========================================${NC}"
    echo -e "${GREEN}ALL TESTS PASSED! 🎉${NC}"
    echo -e "${GREEN}========================================${NC}"
    exit 0
else
    echo -e "${RED}========================================${NC}"
    echo -e "${RED}SOME TESTS FAILED ❌${NC}"
    echo -e "${RED}========================================${NC}"
    exit 1
fi
