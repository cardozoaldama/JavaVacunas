package py.gov.mspbs.javacunas.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import py.gov.mspbs.javacunas.dto.AppointmentDto;
import py.gov.mspbs.javacunas.entity.Appointment;
import py.gov.mspbs.javacunas.entity.Child;
import py.gov.mspbs.javacunas.entity.User;
import py.gov.mspbs.javacunas.exception.BusinessException;
import py.gov.mspbs.javacunas.exception.ResourceNotFoundException;
import py.gov.mspbs.javacunas.repository.AppointmentRepository;
import py.gov.mspbs.javacunas.repository.ChildRepository;
import py.gov.mspbs.javacunas.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for managing appointments.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final ChildRepository childRepository;
    private final UserRepository userRepository;

    /**
     * Convert Appointment entity to AppointmentDto.
     */
    private AppointmentDto toDto(Appointment appointment) {
        if (appointment == null) {
            return null;
        }

        return AppointmentDto.builder()
                .id(appointment.getId())
                .childId(appointment.getChild() != null ? appointment.getChild().getId() : null)
                .childName(appointment.getChild() != null ?
                    appointment.getChild().getFirstName() + " " + appointment.getChild().getLastName() : null)
                .appointmentDate(appointment.getAppointmentDate())
                .appointmentType(appointment.getAppointmentType())
                .status(appointment.getStatus())
                .scheduledVaccines(appointment.getScheduledVaccines())
                .assignedToId(appointment.getAssignedTo() != null ? appointment.getAssignedTo().getId() : null)
                .assignedToName(appointment.getAssignedTo() != null ?
                    appointment.getAssignedTo().getFirstName() + " " + appointment.getAssignedTo().getLastName() : null)
                .notes(appointment.getNotes())
                .createdById(appointment.getCreatedBy() != null ? appointment.getCreatedBy().getId() : null)
                .createdByName(appointment.getCreatedBy() != null ?
                    appointment.getCreatedBy().getFirstName() + " " + appointment.getCreatedBy().getLastName() : null)
                .createdAt(appointment.getCreatedAt())
                .updatedAt(appointment.getUpdatedAt())
                .build();
    }

    /**
     * Create a new appointment.
     */
    @Transactional
    public AppointmentDto createAppointment(Long childId, LocalDateTime appointmentDate,
                                        String appointmentType, String scheduledVaccines,
                                        Long assignedToId, String notes, Long createdById) {
        log.info("Creating appointment for child {} on {}", childId, appointmentDate);

        // Validate child exists and is active
        Child child = childRepository.findById(childId)
                .orElseThrow(() -> new ResourceNotFoundException("Child", "id", childId));

        if (child.isDeleted()) {
            throw new BusinessException("Cannot create appointment for deleted child");
        }

        // Validate creator exists
        User creator = userRepository.findById(createdById)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", createdById));

        // Validate assigned user if provided
        User assignedUser = null;
        if (assignedToId != null) {
            assignedUser = userRepository.findById(assignedToId)
                    .orElseThrow(() -> new ResourceNotFoundException("User", "id", assignedToId));
        }

        // Validate appointment date is in the future
        if (appointmentDate.isBefore(LocalDateTime.now())) {
            throw new BusinessException("Appointment date must be in the future");
        }

        Appointment appointment = Appointment.builder()
                .child(child)
                .appointmentDate(appointmentDate)
                .appointmentType(appointmentType)
                .status(Appointment.AppointmentStatus.SCHEDULED)
                .scheduledVaccines(scheduledVaccines)
                .assignedTo(assignedUser)
                .notes(notes)
                .createdBy(creator)
                .build();

        Appointment saved = appointmentRepository.save(appointment);
        log.info("Appointment created successfully with ID: {}", saved.getId());

        return toDto(saved);
    }

    /**
     * Get appointment by ID.
     */
    @Transactional(readOnly = true)
    public AppointmentDto getAppointmentById(Long id) {
        log.debug("Retrieving appointment with ID: {}", id);
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment", "id", id));
        return toDto(appointment);
    }

    /**
     * Get appointments by child ID.
     */
    @Transactional(readOnly = true)
    public List<AppointmentDto> getAppointmentsByChildId(Long childId) {
        log.debug("Retrieving appointments for child: {}", childId);

        // Verify child exists
        Child child = childRepository.findById(childId)
                .orElseThrow(() -> new ResourceNotFoundException("Child", "id", childId));

        if (child.isDeleted()) {
            throw new ResourceNotFoundException("Child", "id", childId);
        }

        return appointmentRepository.findByChildId(childId).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Get all appointments.
     */
    @Transactional(readOnly = true)
    public List<AppointmentDto> getAllAppointments() {
        log.debug("Retrieving all appointments");
        return appointmentRepository.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Get appointments for a user's children (for PARENT role).
     */
    @Transactional(readOnly = true)
    public List<AppointmentDto> getAppointmentsByUserId(Long userId) {
        log.debug("Retrieving appointments for user: {}", userId);
        return appointmentRepository.findByUserIdThroughChildren(userId).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Get appointments by status.
     */
    @Transactional(readOnly = true)
    public List<AppointmentDto> getAppointmentsByStatus(Appointment.AppointmentStatus status) {
        log.debug("Retrieving appointments with status: {}", status);
        return appointmentRepository.findByStatusOrderByAppointmentDate(status).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Get upcoming appointments.
     */
    @Transactional(readOnly = true)
    public List<AppointmentDto> getUpcomingAppointments() {
        log.debug("Retrieving upcoming appointments");
        return appointmentRepository.findUpcomingAppointments(LocalDateTime.now()).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Get appointments assigned to a user.
     */
    @Transactional(readOnly = true)
    public List<AppointmentDto> getAppointmentsAssignedToUser(Long userId) {
        log.debug("Retrieving appointments assigned to user: {}", userId);
        return appointmentRepository.findByAssignedToId(userId).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Get appointments created by a user.
     */
    @Transactional(readOnly = true)
    public List<AppointmentDto> getAppointmentsCreatedByUser(Long userId) {
        log.debug("Retrieving appointments created by user: {}", userId);
        return appointmentRepository.findByCreatedById(userId).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Update appointment status.
     */
    @Transactional
    public AppointmentDto updateAppointmentStatus(Long id, Appointment.AppointmentStatus status) {
        log.info("Updating appointment {} status to {}", id, status);

        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment", "id", id));

        appointment.setStatus(status);

        Appointment updated = appointmentRepository.save(appointment);
        log.info("Appointment status updated successfully");

        return toDto(updated);
    }

    /**
     * Update appointment.
     */
    @Transactional
    public AppointmentDto updateAppointment(Long id, LocalDateTime appointmentDate,
                                        String appointmentType, String scheduledVaccines,
                                        Long assignedToId, String notes) {
        log.info("Updating appointment with ID: {}", id);

        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment", "id", id));

        // Validate assigned user if provided
        if (assignedToId != null) {
            User assignedUser = userRepository.findById(assignedToId)
                    .orElseThrow(() -> new ResourceNotFoundException("User", "id", assignedToId));
            appointment.setAssignedTo(assignedUser);
        }

        // Validate appointment date if being changed
        if (appointmentDate != null && appointmentDate.isBefore(LocalDateTime.now())) {
            throw new BusinessException("Appointment date must be in the future");
        }

        if (appointmentDate != null) appointment.setAppointmentDate(appointmentDate);
        if (appointmentType != null) appointment.setAppointmentType(appointmentType);
        if (scheduledVaccines != null) appointment.setScheduledVaccines(scheduledVaccines);
        if (notes != null) appointment.setNotes(notes);

        Appointment updated = appointmentRepository.save(appointment);
        log.info("Appointment updated successfully");

        return toDto(updated);
    }

    /**
     * Cancel appointment.
     */
    @Transactional
    public AppointmentDto cancelAppointment(Long id) {
        log.info("Cancelling appointment with ID: {}", id);
        return updateAppointmentStatus(id, Appointment.AppointmentStatus.CANCELLED);
    }

    /**
     * Confirm appointment.
     */
    @Transactional
    public AppointmentDto confirmAppointment(Long id) {
        log.info("Confirming appointment with ID: {}", id);
        return updateAppointmentStatus(id, Appointment.AppointmentStatus.CONFIRMED);
    }

    /**
     * Complete appointment.
     */
    @Transactional
    public AppointmentDto completeAppointment(Long id) {
        log.info("Completing appointment with ID: {}", id);
        return updateAppointmentStatus(id, Appointment.AppointmentStatus.COMPLETED);
    }

}
