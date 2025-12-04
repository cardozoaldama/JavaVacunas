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

# Determine the correct base path for tests
if [ -d "api-tests" ]; then
    # Running from project root
    TEST_BASE="api-tests"
elif [ -d "auth" ] && [ -d "children" ]; then
    # Running from api-tests directory
    TEST_BASE="."
else
    echo -e "${RED}ERROR: Cannot find test directories${NC}"
    echo "Please run this script from the project root or api-tests directory"
    exit 1
fi

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
BACKEND_URL="http://localhost:8080/actuator/health"
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
    echo -e "${GREEN}Backend is running${NC}"
fi

echo ""
echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}Starting Test Execution${NC}"
echo -e "${BLUE}========================================${NC}"
echo ""

# Run tests in dependency order
echo -e "${YELLOW}Note: Tests must be executed in dependency order${NC}"
echo -e "${YELLOW}See TEST_EXECUTION_ORDER.md for details${NC}"
echo -e "${YELLOW}Running all tests in a single command to preserve environment variables${NC}"
echo ""

# Run all tests at once by specifying all folders in dependency order
# Bruno CLI will preserve environment variables when all folders are in one command
echo -e "${BLUE}Running all API tests...${NC}"
echo ""

if $BRUNO_CLI run "${TEST_BASE}/auth" "${TEST_BASE}/vaccines" "${TEST_BASE}/children" "${TEST_BASE}/appointments" "${TEST_BASE}/vaccinations" "${TEST_BASE}/inventory" "${TEST_BASE}/schedules" "${TEST_BASE}/users" --env "$ENV"; then
    echo ""
    echo -e "${GREEN}========================================${NC}"
    echo -e "${GREEN}ALL TESTS PASSED!${NC}"
    echo -e "${GREEN}========================================${NC}"
    exit 0
else
    echo ""
    echo -e "${RED}========================================${NC}"
    echo -e "${RED}SOME TESTS FAILED${NC}"
    echo -e "${RED}========================================${NC}"
    echo ""
    echo -e "${YELLOW}Note: If tests are failing due to missing dependencies (like testChildId),${NC}"
    echo -e "${YELLOW}make sure you run tests in the correct order as documented in${NC}"
    echo -e "${YELLOW}TEST_EXECUTION_ORDER.md. For folder-by-folder execution with proper${NC}"
    echo -e "${YELLOW}state management, use the Bruno GUI instead of this CLI script.${NC}"
    exit 1
fi
