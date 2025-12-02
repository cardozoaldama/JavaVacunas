# Changelog

All notable changes to the JavaVacunas API test collection.

## [1.0.2] - 2025-12-02

### Fixed

- **Registration tests**: Added dynamic usernames with timestamps
  - `register-new-nurse.bru` - Uses `nurse.maria.{timestamp}` format
  - `register-new-parent.bru` - Uses `parent.carlos.{timestamp}` format
  - Tests now pass on repeated runs without 409 Conflict errors
  - Dynamic email addresses with timestamps

- **Unauthorized access test**: Fixed status code expectations
  - `vaccines/unauthorized-access.bru` - Now accepts both 401 and 403
  - Spring Security may return either status for missing tokens

### Added

- **BRUNO_CLI_GUIDE.md** - Complete guide for Bruno CLI usage
  - CLI limitations vs GUI
  - Test execution order recommendations
  - CI/CD integration examples
  - Troubleshooting guide

## [1.0.1] - 2025-12-02

### Fixed

- **Authentication tests**: Corrected response structure expectations
  - Changed `data.user.role` to `data.role` in all auth tests
  - Added username validation to login tests
  - Fixed register tests to match flat AuthResponse structure

### Context

The API returns a flat `AuthResponse` object with fields at root level:

```json
{
  "token": "...",
  "tokenType": "Bearer",
  "userId": 1,
  "username": "admin",
  "role": "DOCTOR",
  "firstName": "...",
  "lastName": "...",
  "email": "..."
}
```

Not a nested structure with a `user` object.

### Updated Files

- `auth/login-doctor.bru`
- `auth/login-nurse.bru`
- `auth/login-parent.bru`
- `auth/register-new-nurse.bru`
- `auth/register-new-parent.bru`
- `README.md` - Added AuthResponse structure documentation

## [1.0.0] - 2025-12-02

### Added

- Initial release with 49 API tests
- Complete coverage of all JavaVacunas endpoints
- 8 test categories (auth, vaccines, children, users, appointments, vaccinations, schedules, inventory)
- Comprehensive documentation (README, TEACHER_GUIDE, SUMMARY)
- Role-based access control testing
- Educational resources for teachers and students
