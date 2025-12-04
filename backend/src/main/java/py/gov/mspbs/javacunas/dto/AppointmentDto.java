package py.gov.mspbs.javacunas.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import py.gov.mspbs.javacunas.entity.Appointment;

import java.time.LocalDateTime;

/**
 * DTO for Appointment entity.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppointmentDto {

    private Long id;

    // Child information (flattened)
    private Long childId;
    private String childName;

    // Appointment details
    private LocalDateTime appointmentDate;
    private String appointmentType;
    private Appointment.AppointmentStatus status;
    private String scheduledVaccines;

    // Assigned medical staff (flattened)
    private Long assignedToId;
    private String assignedToName;

    // Notes
    private String notes;

    // Creator information (flattened)
    private Long createdById;
    private String createdByName;

    // Audit timestamps
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

}
