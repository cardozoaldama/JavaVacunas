package py.gov.mspbs.javacunas.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import py.gov.mspbs.javacunas.BaseIT;
import py.gov.mspbs.javacunas.entity.Appointment;
import py.gov.mspbs.javacunas.entity.Child;
import py.gov.mspbs.javacunas.entity.User;
import py.gov.mspbs.javacunas.exception.BusinessException;
import py.gov.mspbs.javacunas.exception.ResourceNotFoundException;
import py.gov.mspbs.javacunas.repository.AppointmentRepository;
import py.gov.mspbs.javacunas.repository.ChildRepository;
import py.gov.mspbs.javacunas.repository.UserRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Integration tests for AppointmentService.
 * Tests the 3 most critical methods:
 * - createAppointment
 * - getUpcomingAppointments
 * - updateAppointmentStatus
 */
@DisplayName("AppointmentService Integration Tests")
class AppointmentServiceIT extends BaseIT {

    @Autowired
    private AppointmentService appointmentService;

    @Autowired
    private ChildRepository childRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AppointmentRepository appointmentRepository;

    private Child testChild;
    private User testUser;

    @BeforeEach
    void setUp() {
        // Create test child
        testChild = Child.builder()
                .firstName("Juan")
                .lastName("Pérez")
                .documentNumber("1234567")
                .dateOfBirth(LocalDate.now().minusYears(2))
                .gender(Child.Gender.M)
                .build();
        testChild = childRepository.save(testChild);

        // Create test user
        testUser = User.builder()
                .username("testuser")
                .email("test@test.com")
                .firstName("Test")
                .lastName("User")
                .passwordHash("encoded_password")
                .role(User.UserRole.DOCTOR)
                .isActive('Y')
                .build();
        testUser = userRepository.save(testUser);
    }

    @Test
    @DisplayName("Should create appointment successfully with valid data")
    void createAppointment_WithValidData_ShouldSucceed() {
        // Arrange
        LocalDateTime appointmentDate = LocalDateTime.now().plusDays(7);
        String appointmentType = "VACCINATION";
        String scheduledVaccines = "BCG, Hepatitis B";
        String notes = "First vaccination appointment";

        // Act
        Appointment result = appointmentService.createAppointment(
                testChild.getId(),
                appointmentDate,
                appointmentType,
                scheduledVaccines,
                testUser.getId(),
                notes,
                testUser.getId()
        );

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isNotNull();
        assertThat(result.getChild().getId()).isEqualTo(testChild.getId());
        assertThat(result.getAppointmentDate()).isEqualTo(appointmentDate);
        assertThat(result.getAppointmentType()).isEqualTo(appointmentType);
        assertThat(result.getScheduledVaccines()).isEqualTo(scheduledVaccines);
        assertThat(result.getStatus()).isEqualTo(Appointment.AppointmentStatus.SCHEDULED);
        assertThat(result.getAssignedTo().getId()).isEqualTo(testUser.getId());
        assertThat(result.getCreatedBy().getId()).isEqualTo(testUser.getId());
        assertThat(result.getNotes()).isEqualTo(notes);
        assertThat(result.getCreatedAt()).isNotNull();

        // Verify it's persisted in database
        Appointment savedAppointment = appointmentRepository.findById(result.getId()).orElse(null);
        assertThat(savedAppointment).isNotNull();
        assertThat(savedAppointment.getChild().getId()).isEqualTo(testChild.getId());
    }

    @Test
    @DisplayName("Should throw exception when creating appointment with deleted child")
    void createAppointment_WithDeletedChild_ShouldThrowException() {
        // Arrange
        testChild.softDelete();
        childRepository.save(testChild);

        LocalDateTime appointmentDate = LocalDateTime.now().plusDays(7);

        // Act & Assert
        assertThatThrownBy(() -> appointmentService.createAppointment(
                testChild.getId(),
                appointmentDate,
                "VACCINATION",
                "BCG",
                testUser.getId(),
                "notes",
                testUser.getId()
        ))
        .isInstanceOf(BusinessException.class)
        .hasMessageContaining("Cannot create appointment for deleted child");
    }

    @Test
    @DisplayName("Should throw exception when creating appointment with past date")
    void createAppointment_WithPastDate_ShouldThrowException() {
        // Arrange
        LocalDateTime pastDate = LocalDateTime.now().minusDays(1);

        // Act & Assert
        assertThatThrownBy(() -> appointmentService.createAppointment(
                testChild.getId(),
                pastDate,
                "VACCINATION",
                "BCG",
                testUser.getId(),
                "notes",
                testUser.getId()
        ))
        .isInstanceOf(BusinessException.class)
        .hasMessageContaining("Appointment date must be in the future");
    }

    @Test
    @DisplayName("Should throw exception when creating appointment with non-existent child")
    void createAppointment_WithNonExistentChild_ShouldThrowException() {
        // Arrange
        Long nonExistentChildId = 99999L;
        LocalDateTime futureDate = LocalDateTime.now().plusDays(7);

        // Act & Assert
        assertThatThrownBy(() -> appointmentService.createAppointment(
                nonExistentChildId,
                futureDate,
                "VACCINATION",
                "BCG",
                testUser.getId(),
                "notes",
                testUser.getId()
        ))
        .isInstanceOf(ResourceNotFoundException.class)
        .hasMessageContaining("Child");
    }

    @Test
    @DisplayName("Should retrieve upcoming appointments correctly")
    void getUpcomingAppointments_WithMultipleAppointments_ShouldReturnOnlyUpcoming() {
        // Arrange - Create appointments at different times
        // Past appointment
        Appointment pastAppointment = createTestAppointment(LocalDateTime.now().minusDays(1));
        pastAppointment.setStatus(Appointment.AppointmentStatus.COMPLETED);
        appointmentRepository.save(pastAppointment);

        // Future appointments
        Appointment upcoming1 = createTestAppointment(LocalDateTime.now().plusDays(1));
        Appointment upcoming2 = createTestAppointment(LocalDateTime.now().plusDays(7));
        Appointment upcoming3 = createTestAppointment(LocalDateTime.now().plusDays(14));

        appointmentRepository.save(upcoming1);
        appointmentRepository.save(upcoming2);
        appointmentRepository.save(upcoming3);

        // Act
        List<Appointment> result = appointmentService.getUpcomingAppointments();

        // Assert
        assertThat(result).isNotEmpty();
        assertThat(result).hasSize(3);
        assertThat(result).extracting(Appointment::getAppointmentDate)
                .allMatch(date -> date.isAfter(LocalDateTime.now()));
        assertThat(result).extracting(Appointment::getStatus)
                .containsOnly(Appointment.AppointmentStatus.SCHEDULED);
    }

    @Test
    @DisplayName("Should return empty list when no upcoming appointments exist")
    void getUpcomingAppointments_WithNoUpcomingAppointments_ShouldReturnEmptyList() {
        // Arrange - Only create past appointments
        Appointment pastAppointment = createTestAppointment(LocalDateTime.now().minusDays(5));
        pastAppointment.setStatus(Appointment.AppointmentStatus.COMPLETED);
        appointmentRepository.save(pastAppointment);

        // Act
        List<Appointment> result = appointmentService.getUpcomingAppointments();

        // Assert
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Should update appointment status successfully")
    void updateAppointmentStatus_WithValidData_ShouldSucceed() {
        // Arrange
        Appointment appointment = createTestAppointment(LocalDateTime.now().plusDays(7));
        Appointment saved = appointmentRepository.save(appointment);

        assertThat(saved.getStatus()).isEqualTo(Appointment.AppointmentStatus.SCHEDULED);

        // Act
        Appointment result = appointmentService.updateAppointmentStatus(
                saved.getId(),
                Appointment.AppointmentStatus.CONFIRMED
        );

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(saved.getId());
        assertThat(result.getStatus()).isEqualTo(Appointment.AppointmentStatus.CONFIRMED);

        // Verify it's persisted in database
        Appointment updatedAppointment = appointmentRepository.findById(saved.getId()).orElse(null);
        assertThat(updatedAppointment).isNotNull();
        assertThat(updatedAppointment.getStatus()).isEqualTo(Appointment.AppointmentStatus.CONFIRMED);
    }

    @Test
    @DisplayName("Should throw exception when updating non-existent appointment status")
    void updateAppointmentStatus_WithNonExistentAppointment_ShouldThrowException() {
        // Arrange
        Long nonExistentId = 99999L;

        // Act & Assert
        assertThatThrownBy(() -> appointmentService.updateAppointmentStatus(
                nonExistentId,
                Appointment.AppointmentStatus.CONFIRMED
        ))
        .isInstanceOf(ResourceNotFoundException.class)
        .hasMessageContaining("Appointment");
    }

    @Test
    @DisplayName("Should update appointment status through all lifecycle states")
    void updateAppointmentStatus_ThroughLifecycle_ShouldSucceed() {
        // Arrange
        Appointment appointment = createTestAppointment(LocalDateTime.now().plusDays(7));
        Appointment saved = appointmentRepository.save(appointment);

        // Act & Assert - SCHEDULED -> CONFIRMED
        Appointment confirmed = appointmentService.updateAppointmentStatus(
                saved.getId(),
                Appointment.AppointmentStatus.CONFIRMED
        );
        assertThat(confirmed.getStatus()).isEqualTo(Appointment.AppointmentStatus.CONFIRMED);

        // Act & Assert - CONFIRMED -> COMPLETED
        Appointment completed = appointmentService.updateAppointmentStatus(
                saved.getId(),
                Appointment.AppointmentStatus.COMPLETED
        );
        assertThat(completed.getStatus()).isEqualTo(Appointment.AppointmentStatus.COMPLETED);

        // Verify final state in database
        Appointment finalState = appointmentRepository.findById(saved.getId()).orElse(null);
        assertThat(finalState).isNotNull();
        assertThat(finalState.getStatus()).isEqualTo(Appointment.AppointmentStatus.COMPLETED);
    }

    /**
     * Helper method to create a test appointment.
     */
    private Appointment createTestAppointment(LocalDateTime appointmentDate) {
        return Appointment.builder()
                .child(testChild)
                .appointmentDate(appointmentDate)
                .appointmentType("VACCINATION")
                .status(Appointment.AppointmentStatus.SCHEDULED)
                .scheduledVaccines("Test vaccines")
                .assignedTo(testUser)
                .createdBy(testUser)
                .notes("Test notes")
                .build();
    }
}
