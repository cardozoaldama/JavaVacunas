package py.gov.mspbs.javacunas.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
     * Create a new appointment.
     */
    @Transactional
    public Appointment createAppointment(Long childId, LocalDateTime appointmentDate,
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

        return saved;
    }

    /**
     * Get appointment by ID.
     */
    @Transactional(readOnly = true)
    public Appointment getAppointmentById(Long id) {
        log.debug("Retrieving appointment with ID: {}", id);
        return appointmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment", "id", id));
    }

    /**
     * Get appointments by child ID.
     */
    @Transactional(readOnly = true)
    public List<Appointment> getAppointmentsByChildId(Long childId) {
        log.debug("Retrieving appointments for child: {}", childId);

        // Verify child exists
        Child child = childRepository.findById(childId)
                .orElseThrow(() -> new ResourceNotFoundException("Child", "id", childId));

        if (child.isDeleted()) {
            throw new ResourceNotFoundException("Child", "id", childId);
        }

        return appointmentRepository.findByChildId(childId);
    }

    /**
     * Get all appointments.
     */
    @Transactional(readOnly = true)
    public List<Appointment> getAllAppointments() {
        log.debug("Retrieving all appointments");
        return appointmentRepository.findAll();
    }

    /**
     * Get appointments for a user's children (for PARENT role).
     */
    @Transactional(readOnly = true)
    public List<Appointment> getAppointmentsByUserId(Long userId) {
        log.debug("Retrieving appointments for user: {}", userId);
        return appointmentRepository.findByUserIdThroughChildren(userId);
    }

    /**
     * Get appointments by status.
     */
    @Transactional(readOnly = true)
    public List<Appointment> getAppointmentsByStatus(Appointment.AppointmentStatus status) {
        log.debug("Retrieving appointments with status: {}", status);
        return appointmentRepository.findByStatusOrderByAppointmentDate(status);
    }

    /**
     * Get upcoming appointments.
     */
    @Transactional(readOnly = true)
    public List<Appointment> getUpcomingAppointments() {
        log.debug("Retrieving upcoming appointments");
        return appointmentRepository.findUpcomingAppointments(LocalDateTime.now());
    }

    /**
     * Get appointments assigned to a user.
     */
    @Transactional(readOnly = true)
    public List<Appointment> getAppointmentsAssignedToUser(Long userId) {
        log.debug("Retrieving appointments assigned to user: {}", userId);
        return appointmentRepository.findByAssignedToId(userId);
    }

    /**
     * Get appointments created by a user.
     */
    @Transactional(readOnly = true)
    public List<Appointment> getAppointmentsCreatedByUser(Long userId) {
        log.debug("Retrieving appointments created by user: {}", userId);
        return appointmentRepository.findByCreatedById(userId);
    }

    /**
     * Update appointment status.
     */
    @Transactional
    public Appointment updateAppointmentStatus(Long id, Appointment.AppointmentStatus status) {
        log.info("Updating appointment {} status to {}", id, status);

        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment", "id", id));

        appointment.setStatus(status);

        Appointment updated = appointmentRepository.save(appointment);
        log.info("Appointment status updated successfully");

        return updated;
    }

    /**
     * Update appointment.
     */
    @Transactional
    public Appointment updateAppointment(Long id, LocalDateTime appointmentDate,
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

        return updated;
    }

    /**
     * Cancel appointment.
     */
    @Transactional
    public Appointment cancelAppointment(Long id) {
        log.info("Cancelling appointment with ID: {}", id);
        return updateAppointmentStatus(id, Appointment.AppointmentStatus.CANCELLED);
    }

    /**
     * Confirm appointment.
     */
    @Transactional
    public Appointment confirmAppointment(Long id) {
        log.info("Confirming appointment with ID: {}", id);
        return updateAppointmentStatus(id, Appointment.AppointmentStatus.CONFIRMED);
    }

    /**
     * Complete appointment.
     */
    @Transactional
    public Appointment completeAppointment(Long id) {
        log.info("Completing appointment with ID: {}", id);
        return updateAppointmentStatus(id, Appointment.AppointmentStatus.COMPLETED);
    }

}
