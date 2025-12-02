# Teacher Guide - JavaVacunas API Testing

Quick reference guide for educators using the JavaVacunas API test collection.

## Setup for Classroom (5 minutes)

### 1. Start the Application

```bash
# Navigate to project
cd JavaVacunas

# Start with Docker (recommended for classroom)
docker compose --env-file .env.docker up -d

# Wait 30-60 seconds for Oracle DB to be ready
# Verify: http://localhost:8080/actuator/health
```

### 2. Open Bruno

```bash
# Install Bruno (one-time)
# Download from: https://www.usebruno.com/

# Open the collection
File > Open Collection > Select 'api-tests' folder
```

### 3. First Demo

1. Select "local" environment
2. Run `auth/login-doctor.bru`
3. Show the token being saved automatically
4. Run `vaccines/get-all-vaccines.bru`
5. Show how the token is used

## Suggested Lesson Plans

### Lesson 1: API Basics (45 min)

**Objectives:**

- Understand HTTP methods
- Learn request/response structure
- Practice using Bruno

**Activities:**

1. Run `auth/login-doctor.bru`
   - Explain POST method
   - Show request body (JSON)
   - Examine response structure
   - Discuss JWT tokens

2. Run `vaccines/get-all-vaccines.bru`
   - Explain GET method
   - Show Bearer token authentication
   - Examine array response

3. Run `vaccines/unauthorized-access.bru`
   - Demonstrate 401 error
   - Explain authentication requirement

**Exercises:**

- Students modify login credentials and observe errors
- Students add new assertions to vaccine tests
- Students create a test for getting vaccine by ID

### Lesson 2: CRUD Operations (90 min)

**Objectives:**

- Complete Create-Read-Update-Delete workflow
- Understand RESTful conventions
- Practice data validation

**Activities:**

1. **Create** - `children/create-child.bru`
   - Show POST with complex body
   - Demonstrate validation rules
   - Capture ID in environment variable

2. **Read** - `children/get-child-by-id.bru`
   - Use captured ID
   - Show GET with path parameter

3. **Update** - `children/update-child.bru`
   - Demonstrate PUT method
   - Show partial vs. full update

4. **Delete** - `children/delete-child.bru`
   - Explain soft delete concept
   - Show 204 No Content response

**Exercises:**

- Students create their own child record
- Students update and verify changes
- Students attempt operations with wrong roles

### Lesson 3: Role-Based Access Control (60 min)

**Objectives:**

- Understand RBAC concept
- Test permission enforcement
- Practice security testing

**Activities:**

1. Login with all three roles
   - Doctor: Full access
   - Nurse: Read/write operational data
   - Parent: Read-only own children

2. Test same endpoint with different roles
   - `children/create-child.bru` (Doctor) - Success
   - `children/create-child-by-nurse.bru` (Nurse) - Success
   - `children/create-child-forbidden-parent.bru` (Parent) - Forbidden

3. Compare responses
   - 201 Created vs. 403 Forbidden
   - Error message structure

**Exercises:**

- Students create permission matrix
- Students test inventory endpoints with parent token
- Students identify which operations each role can perform

### Lesson 4: Complex Workflows (90 min)

**Objectives:**

- Chain multiple API calls
- Manage state transitions
- Work with related entities

**Activities:**

1. Complete Vaccination Workflow

   ```
   auth/login-nurse.bru
   → children/create-child.bru
   → appointments/create-appointment.bru
   → appointments/confirm-appointment.bru (switch to parent)
   → vaccinations/create-vaccination-record.bru (back to nurse)
   → vaccinations/get-child-vaccination-history.bru
   ```

2. Inventory Management Workflow
   ```
   auth/login-doctor.bru
   → inventory/add-inventory.bru
   → inventory/get-all-inventory.bru
   → inventory/update-inventory-quantity.bru
   → inventory/get-low-stock.bru
   ```

**Exercises:**

- Students create complete appointment lifecycle
- Students track vaccine from inventory to administration
- Students design their own workflow scenarios

### Lesson 5: Testing & Validation (60 min)

**Objectives:**

- Write test assertions
- Validate business rules
- Test error scenarios

**Activities:**

1. Examine existing assertions

   ```javascript
   test("should return 200 status", function () {
     expect(res.getStatus()).to.equal(200);
   });
   ```

2. Add custom assertions
   - Response time checks
   - Data type validation
   - Business rule verification

3. Test edge cases
   - Invalid dates
   - Missing required fields
   - Duplicate records

**Exercises:**

- Students add 3 new assertions to a test
- Students create a test for invalid data
- Students test boundary conditions

## Demo Scenarios

### Scenario 1: New Patient Registration (10 min)

**Story:** A new baby visits the clinic for the first vaccination.

1. Doctor logs in
2. Creates child record (Juan Perez, born 2024-12-01)
3. Checks Paraguay vaccination schedule
4. Creates appointment for BCG vaccine
5. Nurse logs in
6. Records BCG vaccination
7. Checks child's vaccination history
8. Parent logs in
9. Views their child's records

**Learning Points:**

- Multi-role collaboration
- Data flow between entities
- Real-world healthcare workflow

### Scenario 2: Inventory Alert (5 min)

**Story:** Vaccine inventory is running low.

1. Doctor logs in
2. Checks low stock inventory
3. Checks expiring soon inventory
4. Adds new vaccine batch
5. Verifies total available quantity

**Learning Points:**

- Inventory management
- Proactive monitoring
- Stock replenishment

### Scenario 3: Appointment Lifecycle (8 min)

**Story:** Parent schedules and attends vaccination appointment.

1. Parent logs in
2. Views children
3. Creates appointment
4. Confirms appointment
5. Nurse logs in
6. Views assigned appointments
7. Completes appointment
8. Records vaccination

**Learning Points:**

- State transitions
- Role-specific actions
- Appointment workflow

## Common Student Questions

### Q: Why do we need to login first?

**A:** Most endpoints require authentication. The login response includes a JWT token that proves who you are and what you're allowed to do. Without it, you'll get 401 Unauthorized errors.

### Q: What's the difference between 401 and 403?

**A:**

- **401 Unauthorized**: No token or invalid token (not logged in)
- **403 Forbidden**: Valid token but insufficient permissions (logged in as wrong role)

### Q: Why do IDs keep changing?

**A:** Each test creates new data in the database. IDs are auto-generated and sequential. That's why we use environment variables to capture and reuse IDs.

### Q: Can I delete the test data?

**A:** Yes! Use soft deletes (children/delete-child.bru) or restart the Docker containers for a clean database.

### Q: How do I see the actual API?

**A:** Visit http://localhost:8080/swagger-ui.html when the backend is running to see interactive API documentation.

## Troubleshooting

### Problem: "Connection refused"

**Solution:**

```bash
# Check if backend is running
docker compose ps

# If not running, start it
docker compose --env-file .env.docker up -d

# Wait 60 seconds, then verify
curl http://localhost:8080/actuator/health
```

### Problem: "Token expired"

**Solution:**

```bash
# Tokens expire after 24 hours
# Just re-run the login tests
```

### Problem: "Validation error on dates"

**Solution:**

```javascript
// Use ISO 8601 format: YYYY-MM-DD
"dateOfBirth": "2024-12-01"  // Correct
"dateOfBirth": "01/12/2024"  // Wrong
```

### Problem: "Child not found"

**Solution:**

```javascript
// Make sure to run create-child.bru first
// Or use an existing child ID from get-all-children.bru
```

## Assessment Ideas

### Quiz Questions

1. What HTTP method is used to create a new resource?
2. What status code indicates successful creation?
3. Explain the difference between PUT and POST.
4. What does RBAC stand for?
5. Name three roles in JavaVacunas and their permissions.

### Practical Exercises

1. Create a test for registering a new doctor
2. Add assertions to verify password strength requirements
3. Create a workflow that schedules 3 vaccinations
4. Test what happens when inventory runs out
5. Create a test suite for error scenarios

### Projects

1. **API Test Report**: Students create a complete test report for one module
2. **Custom Scenario**: Design and implement a new vaccination workflow
3. **Security Audit**: Test all endpoints with different role combinations
4. **Documentation**: Write API documentation for a specific endpoint
5. **Automation**: Create a shell script that runs tests in sequence

## Additional Resources

### Bruno Documentation

- Official Docs: https://docs.usebruno.com/
- Scripting Guide: https://docs.usebruno.com/scripting/introduction
- Testing Guide: https://docs.usebruno.com/testing/introduction

### API Design

- REST API Tutorial: https://restfulapi.net/
- HTTP Status Codes: https://httpstatuses.com/
- JWT Introduction: https://jwt.io/introduction

### Healthcare IT

- Paraguay PAI Program: Information about vaccination schedules
- FHIR Standard: https://www.hl7.org/fhir/
- Healthcare Data Standards

## Tips for Success

1. **Start Simple**: Begin with GET requests before POST/PUT/DELETE
2. **Use Environment Variables**: Teach students to reuse values
3. **Show Errors**: Failed tests are great learning opportunities
4. **Real-World Context**: Connect to actual healthcare processes
5. **Hands-On**: Students learn best by doing, not watching
6. **Incremental**: Build complexity gradually
7. **Documentation**: Encourage students to document their tests

## Sample Grading Rubric

### API Test Assignment (100 points)

- **Test Coverage (30 points)**
  - All required endpoints tested
  - Both success and error scenarios
  - Edge cases included

- **Assertions (25 points)**
  - Status code validation
  - Response structure validation
  - Business logic validation

- **Documentation (20 points)**
  - Clear test names
  - Descriptive comments
  - README with instructions

- **Best Practices (15 points)**
  - Environment variables used
  - Proper authentication
  - Clean test organization

- **Creativity (10 points)**
  - Custom scenarios
  - Advanced assertions
  - Workflow automation

## Contact & Support

For questions or issues:

- Check the main README.md
- Review Swagger docs at http://localhost:8080/swagger-ui.html
- Check Bruno documentation
- Review the project's CLAUDE.md for technical details

---

**Remember:** The goal is to help students understand API testing, REST principles, and real-world application workflows. Use the JavaVacunas system as a practical, healthcare-focused learning tool.
