package py.gov.mspbs.javacunas.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import py.gov.mspbs.javacunas.dto.UserDto;
import py.gov.mspbs.javacunas.entity.User;
import py.gov.mspbs.javacunas.service.UserService;

import java.util.List;

/**
 * REST controller for user management.
 */
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "User management operations")
@SecurityRequirement(name = "Bearer Authentication")
public class UserController {

    private final UserService userService;

    /**
     * Get user by ID.
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('DOCTOR', 'NURSE')")
    @Operation(summary = "Get user by ID", description = "Retrieve user by ID")
    public ResponseEntity<UserDto> getUserById(@PathVariable Long id) {
        UserDto user = userService.getUserById(id);
        return ResponseEntity.ok(user);
    }

    /**
     * Get all users.
     */
    @GetMapping
    @PreAuthorize("hasRole('DOCTOR')")
    @Operation(summary = "Get all users", description = "Retrieve all users")
    public ResponseEntity<List<UserDto>> getAllUsers() {
        List<UserDto> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    /**
     * Get users by role.
     */
    @GetMapping("/role/{role}")
    @PreAuthorize("hasAnyRole('DOCTOR', 'NURSE')")
    @Operation(summary = "Get users by role", description = "Retrieve users by role")
    public ResponseEntity<List<UserDto>> getUsersByRole(@PathVariable User.UserRole role) {
        List<UserDto> users = userService.getUsersByRole(role);
        return ResponseEntity.ok(users);
    }

    /**
     * Get active medical staff (doctors and nurses).
     */
    @GetMapping("/medical-staff")
    @PreAuthorize("hasAnyRole('DOCTOR', 'NURSE', 'PARENT')")
    @Operation(summary = "Get medical staff", description = "Retrieve all active doctors and nurses")
    public ResponseEntity<List<UserDto>> getMedicalStaff() {
        List<UserDto> staff = userService.getAllActiveMedicalStaff();
        return ResponseEntity.ok(staff);
    }

    /**
     * Get active doctors.
     */
    @GetMapping("/doctors")
    @PreAuthorize("hasAnyRole('DOCTOR', 'NURSE', 'PARENT')")
    @Operation(summary = "Get active doctors", description = "Retrieve all active doctors")
    public ResponseEntity<List<UserDto>> getActiveDoctors() {
        List<UserDto> doctors = userService.getAllActiveDoctors();
        return ResponseEntity.ok(doctors);
    }

    /**
     * Get active nurses.
     */
    @GetMapping("/nurses")
    @PreAuthorize("hasAnyRole('DOCTOR', 'NURSE', 'PARENT')")
    @Operation(summary = "Get active nurses", description = "Retrieve all active nurses")
    public ResponseEntity<List<UserDto>> getActiveNurses() {
        List<UserDto> nurses = userService.getAllActiveNurses();
        return ResponseEntity.ok(nurses);
    }

}
