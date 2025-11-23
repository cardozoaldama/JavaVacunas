package py.gov.mspbs.javacunas.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import py.gov.mspbs.javacunas.entity.Appointment;
import py.gov.mspbs.javacunas.security.UserPrincipal;
import py.gov.mspbs.javacunas.service.AppointmentService;

import java.time.LocalDateTime;
import java.util.List;

/**
 * REST controller for appointment management.
 */
@RestController
@RequestMapping("/api/v1/appointments")
@RequiredArgsConstructor
@Tag(name = "Appointments", description = "Appointment management operations")
@SecurityRequirement(name = "Bearer Authentication")
public class AppointmentController {

    private final AppointmentService appointmentService;

    /**
     * Create a new appointment.
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('DOCTOR', 'NURSE', 'PARENT')")
    @Operation(summary = "Create appointment", description = "Create a new vaccination appointment")
    public ResponseEntity<Appointment> createAppointment(
            @RequestParam Long childId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime appointmentDate,
            @RequestParam String appointmentType,
            @RequestParam(required = false) String scheduledVaccines,
            @RequestParam(required = false) Long assignedToId,
            @RequestParam(required = false) String notes,
            Authentication authentication) {

        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        Appointment appointment = appointmentService.createAppointment(
                childId, appointmentDate, appointmentType, scheduledVaccines,
                assignedToId, notes, userPrincipal.getId());

        return ResponseEntity.status(HttpStatus.CREATED).body(appointment);
    }

    /**
     * Get appointment by ID.
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('DOCTOR', 'NURSE', 'PARENT')")
    @Operation(summary = "Get appointment", description = "Retrieve appointment by ID")
    public ResponseEntity<Appointment> getAppointmentById(@PathVariable Long id) {
        Appointment appointment = appointmentService.getAppointmentById(id);
        return ResponseEntity.ok(appointment);
    }

    /**
     * Get appointments by child ID.
     */
    @GetMapping("/child/{childId}")
    @PreAuthorize("hasAnyRole('DOCTOR', 'NURSE', 'PARENT')")
    @Operation(summary = "Get child appointments", description = "Retrieve all appointments for a child")
    public ResponseEntity<List<Appointment>> getAppointmentsByChildId(@PathVariable Long childId) {
        List<Appointment> appointments = appointmentService.getAppointmentsByChildId(childId);
        return ResponseEntity.ok(appointments);
    }

    /**
     * Get upcoming appointments.
     */
    @GetMapping("/upcoming")
    @PreAuthorize("hasAnyRole('DOCTOR', 'NURSE')")
    @Operation(summary = "Get upcoming appointments", description = "Retrieve all upcoming appointments")
    public ResponseEntity<List<Appointment>> getUpcomingAppointments() {
        List<Appointment> appointments = appointmentService.getUpcomingAppointments();
        return ResponseEntity.ok(appointments);
    }

    /**
     * Get appointments by status.
     */
    @GetMapping("/status/{status}")
    @PreAuthorize("hasAnyRole('DOCTOR', 'NURSE')")
    @Operation(summary = "Get appointments by status", description = "Retrieve appointments by status")
    public ResponseEntity<List<Appointment>> getAppointmentsByStatus(
            @PathVariable Appointment.AppointmentStatus status) {
        List<Appointment> appointments = appointmentService.getAppointmentsByStatus(status);
        return ResponseEntity.ok(appointments);
    }

    /**
     * Update appointment status.
     */
    @PutMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('DOCTOR', 'NURSE')")
    @Operation(summary = "Update appointment status", description = "Update the status of an appointment")
    public ResponseEntity<Appointment> updateAppointmentStatus(
            @PathVariable Long id,
            @RequestParam Appointment.AppointmentStatus status) {
        Appointment appointment = appointmentService.updateAppointmentStatus(id, status);
        return ResponseEntity.ok(appointment);
    }

    /**
     * Confirm appointment.
     */
    @PutMapping("/{id}/confirm")
    @PreAuthorize("hasAnyRole('DOCTOR', 'NURSE', 'PARENT')")
    @Operation(summary = "Confirm appointment", description = "Confirm an appointment")
    public ResponseEntity<Appointment> confirmAppointment(@PathVariable Long id) {
        Appointment appointment = appointmentService.confirmAppointment(id);
        return ResponseEntity.ok(appointment);
    }

    /**
     * Complete appointment.
     */
    @PutMapping("/{id}/complete")
    @PreAuthorize("hasAnyRole('DOCTOR', 'NURSE')")
    @Operation(summary = "Complete appointment", description = "Mark appointment as completed")
    public ResponseEntity<Appointment> completeAppointment(@PathVariable Long id) {
        Appointment appointment = appointmentService.completeAppointment(id);
        return ResponseEntity.ok(appointment);
    }

    /**
     * Cancel appointment.
     */
    @PutMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('DOCTOR', 'NURSE', 'PARENT')")
    @Operation(summary = "Cancel appointment", description = "Cancel an appointment")
    public ResponseEntity<Appointment> cancelAppointment(@PathVariable Long id) {
        Appointment appointment = appointmentService.cancelAppointment(id);
        return ResponseEntity.ok(appointment);
    }

}
