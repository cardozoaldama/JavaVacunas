# API Response Reference

Quick reference for JavaVacunas API response structures.

## Authentication Endpoints

### POST /api/v1/auth/login
### POST /api/v1/auth/register

**Response: AuthResponse**
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

**Important:** Fields are at root level, NOT nested under a `user` object.

## Vaccine Endpoints

### GET /api/v1/vaccines

**Response: Array of VaccineDto**
```json
[
  {
    "id": 1,
    "name": "BCG",
    "description": "Bacillus Calmette-Guerin vaccine",
    "diseasePrevented": "Tuberculosis",
    "manufacturer": "...",
    "ageRangeMonths": "0-1",
    "isActive": "Y"
  }
]
```

### GET /api/v1/vaccines/{id}

**Response: VaccineDto**
```json
{
  "id": 1,
  "name": "BCG",
  "description": "Bacillus Calmette-Guerin vaccine",
  "diseasePrevented": "Tuberculosis",
  "manufacturer": "...",
  "ageRangeMonths": "0-1",
  "isActive": "Y"
}
```

## Child Endpoints

### GET /api/v1/children
### GET /api/v1/children/{id}

**Response: ChildDto or Array of ChildDto**
```json
{
  "id": 1,
  "firstName": "Juan",
  "lastName": "Perez",
  "documentNumber": "1234567",
  "dateOfBirth": "2024-01-15",
  "gender": "M",
  "bloodType": "O+",
  "birthWeight": 3.5,
  "birthHeight": 50.5,
  "createdAt": "2024-12-01T10:00:00",
  "updatedAt": "2024-12-01T10:00:00"
}
```

**Note:** Valid gender values are: `"M"` (Male), `"F"` (Female), or `"O"` (Other)

## User Endpoints

### GET /api/v1/users
### GET /api/v1/users/{id}

**Response: UserDto or Array of UserDto**
```json
{
  "id": 1,
  "username": "admin",
  "firstName": "Admin",
  "lastName": "User",
  "email": "admin@example.com",
  "role": "DOCTOR",
  "licenseNumber": "DOC-001",
  "isActive": "Y",
  "lastLogin": "2024-12-01T09:00:00",
  "createdAt": "2024-11-01T08:00:00",
  "updatedAt": "2024-12-01T09:00:00"
}
```

**Note:** Password is NEVER included in responses.

## Appointment Endpoints

### GET /api/v1/appointments
### GET /api/v1/appointments/{id}

**Response: Appointment Entity (not DTO)**
```json
{
  "id": 1,
  "child": { /* Child entity */ },
  "appointmentDate": "2025-12-10T09:00:00",
  "appointmentType": "SCHEDULED",
  "scheduledVaccines": "BCG,Hepatitis B",
  "status": "SCHEDULED",
  "assignedTo": { /* User entity */ },
  "createdBy": { /* User entity */ },
  "notes": "Primera visita",
  "createdAt": "2024-12-01T10:00:00",
  "updatedAt": "2024-12-01T10:00:00"
}
```

**Status Values:**
- `SCHEDULED`
- `CONFIRMED`
- `COMPLETED`
- `CANCELLED`
- `NO_SHOW`

## Vaccination Record Endpoints

### GET /api/v1/vaccinations
### GET /api/v1/vaccinations/{id}

**Response: VaccinationRecordDto or Array**
```json
{
  "id": 1,
  "childId": 1,
  "vaccineId": 1,
  "vaccineName": "BCG",
  "doseNumber": 1,
  "administrationDate": "2024-12-01",
  "batchNumber": "BCG-2024-001",
  "expirationDate": "2025-12-01",
  "administrationSite": "Brazo izquierdo",
  "administeredById": 2,
  "administeredByName": "Nurse Maria",
  "notes": "Vacuna administrada sin complicaciones",
  "nextDoseDate": "2025-02-01",
  "createdAt": "2024-12-01T10:30:00"
}
```

## Schedule Endpoints

### GET /api/v1/schedules
### GET /api/v1/schedules/paraguay

**Response: VaccinationSchedule Entity (not DTO)**
```json
{
  "id": 1,
  "vaccine": { /* Vaccine entity */ },
  "ageInMonths": 0,
  "doseNumber": 1,
  "isMandatory": true,
  "countryCode": "PY",
  "description": "Vacuna BCG al nacer",
  "notes": "Dosis única"
}
```

## Inventory Endpoints

### GET /api/v1/inventory
### GET /api/v1/inventory/{id}

**Response: VaccineInventory Entity (not DTO)**
```json
{
  "id": 1,
  "vaccine": { /* Vaccine entity */ },
  "batchNumber": "BCG-2024-BATCH-001",
  "quantity": 500,
  "manufactureDate": "2024-01-15",
  "expirationDate": "2025-12-31",
  "storageLocation": "Refrigerador A1",
  "status": "AVAILABLE",
  "addedBy": { /* User entity */ },
  "createdAt": "2024-12-01T08:00:00",
  "updatedAt": "2024-12-01T08:00:00"
}
```

**Status Values:**
- `AVAILABLE`
- `DEPLETED`
- `EXPIRED`
- `QUARANTINED`

## Error Responses

### 400 Bad Request (Validation Error)

```json
{
  "timestamp": "2024-12-01T10:00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "errors": {
    "firstName": "First name is required",
    "dateOfBirth": "Date of birth must be in the past"
  }
}
```

### 401 Unauthorized (No Token or Invalid Token)

```json
{
  "timestamp": "2024-12-01T10:00:00",
  "status": 401,
  "error": "Unauthorized",
  "message": "Full authentication is required to access this resource"
}
```

### 403 Forbidden (Insufficient Permissions)

```json
{
  "timestamp": "2024-12-01T10:00:00",
  "status": 403,
  "error": "Forbidden",
  "message": "Access Denied"
}
```

### 404 Not Found

```json
{
  "timestamp": "2024-12-01T10:00:00",
  "status": 404,
  "error": "Not Found",
  "message": "Child not found with id: 999"
}
```

## Common Patterns

### Date Formats

- **Date Only**: `YYYY-MM-DD` (e.g., `"2024-12-01"`)
- **DateTime**: `YYYY-MM-DDTHH:mm:ss` (e.g., `"2024-12-01T10:00:00"`)

### Boolean Flags

Some entities use Character flags:
- `isActive`: `"Y"` or `"N"`
- `isMandatory`: `true` or `false` (actual boolean)

### Entity Relationships

Some responses include full nested entities (Appointment, Inventory), while others use DTOs with IDs only (VaccinationRecordDto).

**Example - Full Entity:**
```json
{
  "id": 1,
  "child": {
    "id": 1,
    "firstName": "Juan",
    ...
  }
}
```

**Example - DTO with IDs:**
```json
{
  "id": 1,
  "childId": 1,
  "vaccineName": "BCG"
}
```

## Testing Tips

1. **Check Response Structure First**: Use `console.log(res.getBody())` in Bruno to inspect actual response
2. **Access Nested Properties Safely**: Use optional chaining when available
3. **Validate Data Types**: Check if fields are strings, numbers, or booleans
4. **Handle Arrays**: Verify length before accessing array elements
5. **Error Cases**: Test both success and error responses

## Quick Debug Script

Add this to any Bruno test to inspect the response:

```javascript
// In Bruno test section
test("Debug response structure", function() {
  const data = res.getBody();
  console.log("Response:", JSON.stringify(data, null, 2));
});
```

## Updates

- **2024-12-02**: Initial version - Fixed AuthResponse structure documentation
