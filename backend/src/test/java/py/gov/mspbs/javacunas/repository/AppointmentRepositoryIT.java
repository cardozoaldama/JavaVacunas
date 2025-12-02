package py.gov.mspbs.javacunas.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import py.gov.mspbs.javacunas.AbstractOracleIntegrationTest;
import py.gov.mspbs.javacunas.entity.Appointment;
import py.gov.mspbs.javacunas.entity.Child;
import py.gov.mspbs.javacunas.entity.Guardian;
import py.gov.mspbs.javacunas.entity.User;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * Integration tests for AppointmentRepository using Oracle 23c Free database.
 *
 * These tests verify:
 * - Basic CRUD operations with automatic timestamps
 * - Appointment status enum handling (SCHEDULED, CONFIRMED, COMPLETED, CANCELLED, NO_SHOW)
 * - Custom query methods (findByChildId, findByStatus, findUpcomingAppointments, etc.)
 * - Many-to-One relationships (child, assignedTo, createdBy)
 * - Database constraints (not null, foreign keys)
 * - Complex queries with JOINs through guardians
 * - Date range filtering and ordering
 * - PrePersist and PreUpdate callback functionality
 */
@DisplayName("AppointmentRepository Integration Tests")
class AppointmentRepositoryIT extends AbstractOracleIntegrationTest {

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private ChildRepository childRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GuardianRepository guardianRepository;

    private Child testChild;
    private User doctorUser;
    private User nurseUser;
    private User parentUser;
    private Guardian testGuardian;

    @BeforeEach
    void setUp() {
        // Create doctor user for assignment and creation
        doctorUser = User.builder()
                .username("doctor_test")
                .passwordHash("$2a$10$hashedpassword")
                .firstName("Dr. Carlos")
                .lastName("Rodriguez")
                .email("doctor@test.com")
                .role(User.UserRole.DOCTOR)
                .isActive('Y')
                .build();
        doctorUser = userRepository.save(doctorUser);

        // Create nurse user
        nurseUser = User.builder()
                .username("nurse_test")
                .passwordHash("$2a$10$hashedpassword")
                .firstName("Nurse Maria")
                .lastName("Garcia")
                .email("nurse@test.com")
                .role(User.UserRole.NURSE)
                .isActive('Y')
                .build();
        nurseUser = userRepository.save(nurseUser);

        // Create parent user for guardian
        parentUser = User.builder()
                .username("parent_test")
                .passwordHash("$2a$10$hashedpassword")
                .firstName("John")
                .lastName("Doe")
                .email("parent@test.com")
                .role(User.UserRole.PARENT)
                .isActive('Y')
                .build();
        parentUser = userRepository.save(parentUser);

        // Create guardian
        testGuardian = Guardian.builder()
                .user(parentUser)
                .firstName("John")
                .lastName("Doe")
                .documentNumber("1234567890")
                .phone("+595981234567")
                .email("parent@test.com")
                .address("123 Test Street")
                .relationship("Father")
                .build();
        testGuardian = guardianRepository.save(testGuardian);

        // Create test child
        testChild = Child.builder()
                .firstName("Pedro")
                .lastName("Lopez")
                .documentNumber("9876543210")
                .dateOfBirth(LocalDate.of(2023, 6, 15))
                .gender(Child.Gender.M)
                .build();
        testChild.getGuardians().add(testGuardian);
        testChild = childRepository.save(testChild);
        childRepository.flush();
    }

    @Test
    @DisplayName("Should save and retrieve appointment with all required fields")
    void testSaveAndRetrieveAppointment() {
        // Given
        LocalDateTime appointmentDate = LocalDateTime.now().plusDays(7);
        Appointment appointment = Appointment.builder()
                .child(testChild)
                .appointmentDate(appointmentDate)
                .appointmentType("Vaccination")
                .status(Appointment.AppointmentStatus.SCHEDULED)
                .scheduledVaccines("BCG, Hepatitis B")
                .assignedTo(doctorUser)
                .notes("First vaccination appointment")
                .createdBy(nurseUser)
                .build();

        // When
        Appointment savedAppointment = appointmentRepository.save(appointment);
        appointmentRepository.flush();

        // Then
        assertThat(savedAppointment.getId()).isNotNull();
        assertThat(savedAppointment.getCreatedAt()).isNotNull();
        assertThat(savedAppointment.getUpdatedAt()).isNotNull();
        assertThat(savedAppointment.getChild().getId()).isEqualTo(testChild.getId());
        assertThat(savedAppointment.getAppointmentType()).isEqualTo("Vaccination");
        assertThat(savedAppointment.getStatus()).isEqualTo(Appointment.AppointmentStatus.SCHEDULED);
        assertThat(savedAppointment.getScheduledVaccines()).isEqualTo("BCG, Hepatitis B");
        assertThat(savedAppointment.getAssignedTo().getId()).isEqualTo(doctorUser.getId());
        assertThat(savedAppointment.getNotes()).isEqualTo("First vaccination appointment");
        assertThat(savedAppointment.getCreatedBy().getId()).isEqualTo(nurseUser.getId());

        // Verify database retrieval
        Appointment retrievedAppointment = appointmentRepository.findById(savedAppointment.getId()).orElseThrow();
        assertThat(retrievedAppointment.getAppointmentType()).isEqualTo("Vaccination");
        assertThat(retrievedAppointment.getCreatedAt()).isEqualTo(savedAppointment.getCreatedAt());
    }

    @Test
    @DisplayName("Should enforce not null constraints on required fields")
    void testNotNullConstraints() {
        // Given - appointment without appointmentDate
        Appointment appointment = Appointment.builder()
                .child(testChild)
                .appointmentType("Vaccination")
                .createdBy(doctorUser)
                .build();

        // When & Then
        assertThatThrownBy(() -> {
            appointmentRepository.save(appointment);
            appointmentRepository.flush();
        }).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("Should find appointments by child id")
    void testFindByChildId() {
        // Given - 3 appointments for the same child in different dates
        Appointment appointment1 = Appointment.builder()
                .child(testChild)
                .appointmentDate(LocalDateTime.now().plusDays(1))
                .appointmentType("Vaccination")
                .createdBy(doctorUser)
                .build();

        Appointment appointment2 = Appointment.builder()
                .child(testChild)
                .appointmentDate(LocalDateTime.now().plusDays(5))
                .appointmentType("Checkup")
                .createdBy(doctorUser)
                .build();

        Appointment appointment3 = Appointment.builder()
                .child(testChild)
                .appointmentDate(LocalDateTime.now().plusDays(10))
                .appointmentType("Follow-up")
                .createdBy(doctorUser)
                .build();

        appointmentRepository.saveAll(List.of(appointment1, appointment2, appointment3));
        appointmentRepository.flush();

        // When
        List<Appointment> childAppointments = appointmentRepository.findByChildId(testChild.getId());

        // Then - should be ordered by appointmentDate DESC
        assertThat(childAppointments).hasSize(3);
        assertThat(childAppointments.get(0).getAppointmentType()).isEqualTo("Follow-up");
        assertThat(childAppointments.get(1).getAppointmentType()).isEqualTo("Checkup");
        assertThat(childAppointments.get(2).getAppointmentType()).isEqualTo("Vaccination");
    }

    @Test
    @DisplayName("Should find appointments by status ordered by appointment date")
    void testFindByStatusOrderByAppointmentDate() {
        // Given
        Appointment scheduledAppointment1 = Appointment.builder()
                .child(testChild)
                .appointmentDate(LocalDateTime.now().plusDays(10))
                .appointmentType("Follow-up")
                .status(Appointment.AppointmentStatus.SCHEDULED)
                .createdBy(doctorUser)
                .build();

        Appointment scheduledAppointment2 = Appointment.builder()
                .child(testChild)
                .appointmentDate(LocalDateTime.now().plusDays(3))
                .appointmentType("Vaccination")
                .status(Appointment.AppointmentStatus.SCHEDULED)
                .createdBy(doctorUser)
                .build();

        Appointment completedAppointment = Appointment.builder()
                .child(testChild)
                .appointmentDate(LocalDateTime.now().plusDays(5))
                .appointmentType("Checkup")
                .status(Appointment.AppointmentStatus.COMPLETED)
                .createdBy(doctorUser)
                .build();

        appointmentRepository.saveAll(List.of(scheduledAppointment1, scheduledAppointment2, completedAppointment));
        appointmentRepository.flush();

        // When
        List<Appointment> scheduledAppointments = appointmentRepository.findByStatusOrderByAppointmentDate(
                Appointment.AppointmentStatus.SCHEDULED
        );

        // Then - should return only SCHEDULED appointments ordered by date ASC
        assertThat(scheduledAppointments).hasSize(2);
        assertThat(scheduledAppointments.get(0).getAppointmentType()).isEqualTo("Vaccination");
        assertThat(scheduledAppointments.get(1).getAppointmentType()).isEqualTo("Follow-up");
        assertThat(scheduledAppointments).allMatch(a -> a.getStatus() == Appointment.AppointmentStatus.SCHEDULED);
    }

    @Test
    @DisplayName("Should handle all appointment status enum values")
    void testAllAppointmentStatusValues() {
        // Given - create appointments with all status values
        Appointment scheduled = Appointment.builder()
                .child(testChild)
                .appointmentDate(LocalDateTime.now().plusDays(1))
                .appointmentType("Vaccination")
                .status(Appointment.AppointmentStatus.SCHEDULED)
                .createdBy(doctorUser)
                .build();

        Appointment confirmed = Appointment.builder()
                .child(testChild)
                .appointmentDate(LocalDateTime.now().plusDays(2))
                .appointmentType("Vaccination")
                .status(Appointment.AppointmentStatus.CONFIRMED)
                .createdBy(doctorUser)
                .build();

        Appointment completed = Appointment.builder()
                .child(testChild)
                .appointmentDate(LocalDateTime.now().minusDays(1))
                .appointmentType("Vaccination")
                .status(Appointment.AppointmentStatus.COMPLETED)
                .createdBy(doctorUser)
                .build();

        Appointment cancelled = Appointment.builder()
                .child(testChild)
                .appointmentDate(LocalDateTime.now().plusDays(3))
                .appointmentType("Vaccination")
                .status(Appointment.AppointmentStatus.CANCELLED)
                .createdBy(doctorUser)
                .build();

        Appointment noShow = Appointment.builder()
                .child(testChild)
                .appointmentDate(LocalDateTime.now().minusDays(2))
                .appointmentType("Vaccination")
                .status(Appointment.AppointmentStatus.NO_SHOW)
                .createdBy(doctorUser)
                .build();

        appointmentRepository.saveAll(List.of(scheduled, confirmed, completed, cancelled, noShow));
        appointmentRepository.flush();

        // When
        List<Appointment> allAppointments = appointmentRepository.findAll();

        // Then
        assertThat(allAppointments).hasSize(5);
        assertThat(allAppointments)
                .extracting(Appointment::getStatus)
                .containsExactlyInAnyOrder(
                        Appointment.AppointmentStatus.SCHEDULED,
                        Appointment.AppointmentStatus.CONFIRMED,
                        Appointment.AppointmentStatus.COMPLETED,
                        Appointment.AppointmentStatus.CANCELLED,
                        Appointment.AppointmentStatus.NO_SHOW
                );
    }

    @Test
    @DisplayName("Should find appointments by date range")
    void testFindByAppointmentDateBetween() {
        // Given - 4 appointments with different dates
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startRange = now.plusDays(5);
        LocalDateTime endRange = now.plusDays(15);

        Appointment beforeRange = Appointment.builder()
                .child(testChild)
                .appointmentDate(now.plusDays(2))
                .appointmentType("Vaccination")
                .createdBy(doctorUser)
                .build();

        Appointment inRange1 = Appointment.builder()
                .child(testChild)
                .appointmentDate(now.plusDays(7))
                .appointmentType("Checkup")
                .createdBy(doctorUser)
                .build();

        Appointment inRange2 = Appointment.builder()
                .child(testChild)
                .appointmentDate(now.plusDays(12))
                .appointmentType("Follow-up")
                .createdBy(doctorUser)
                .build();

        Appointment afterRange = Appointment.builder()
                .child(testChild)
                .appointmentDate(now.plusDays(20))
                .appointmentType("Vaccination")
                .createdBy(doctorUser)
                .build();

        appointmentRepository.saveAll(List.of(beforeRange, inRange1, inRange2, afterRange));
        appointmentRepository.flush();

        // When
        List<Appointment> appointmentsInRange = appointmentRepository.findByAppointmentDateBetween(
                startRange, endRange
        );

        // Then - should return only 2 appointments in range, ordered by date
        assertThat(appointmentsInRange).hasSize(2);
        assertThat(appointmentsInRange.get(0).getAppointmentType()).isEqualTo("Checkup");
        assertThat(appointmentsInRange.get(1).getAppointmentType()).isEqualTo("Follow-up");
    }

    @Test
    @DisplayName("Should find appointments by assigned to user id")
    void testFindByAssignedToId() {
        // Given - 2 appointments assigned to doctor, 1 to nurse
        Appointment doctorAppointment1 = Appointment.builder()
                .child(testChild)
                .appointmentDate(LocalDateTime.now().plusDays(3))
                .appointmentType("Vaccination")
                .assignedTo(doctorUser)
                .createdBy(nurseUser)
                .build();

        Appointment doctorAppointment2 = Appointment.builder()
                .child(testChild)
                .appointmentDate(LocalDateTime.now().plusDays(7))
                .appointmentType("Checkup")
                .assignedTo(doctorUser)
                .createdBy(nurseUser)
                .build();

        Appointment nurseAppointment = Appointment.builder()
                .child(testChild)
                .appointmentDate(LocalDateTime.now().plusDays(5))
                .appointmentType("Follow-up")
                .assignedTo(nurseUser)
                .createdBy(doctorUser)
                .build();

        appointmentRepository.saveAll(List.of(doctorAppointment1, doctorAppointment2, nurseAppointment));
        appointmentRepository.flush();

        // When
        List<Appointment> doctorAssignedAppointments = appointmentRepository.findByAssignedToId(doctorUser.getId());

        // Then - should return only doctor's appointments ordered by date ASC
        assertThat(doctorAssignedAppointments).hasSize(2);
        assertThat(doctorAssignedAppointments.get(0).getAppointmentType()).isEqualTo("Vaccination");
        assertThat(doctorAssignedAppointments.get(1).getAppointmentType()).isEqualTo("Checkup");
        assertThat(doctorAssignedAppointments).allMatch(a -> a.getAssignedTo().getId().equals(doctorUser.getId()));
    }

    @Test
    @DisplayName("Should find upcoming appointments with scheduled or confirmed status")
    void testFindUpcomingAppointments() {
        // Given - appointments with different statuses and dates (past and future)
        LocalDateTime now = LocalDateTime.now();

        Appointment pastScheduled = Appointment.builder()
                .child(testChild)
                .appointmentDate(now.minusDays(5))
                .appointmentType("Vaccination")
                .status(Appointment.AppointmentStatus.SCHEDULED)
                .createdBy(doctorUser)
                .build();

        Appointment futureScheduled = Appointment.builder()
                .child(testChild)
                .appointmentDate(now.plusDays(3))
                .appointmentType("Checkup")
                .status(Appointment.AppointmentStatus.SCHEDULED)
                .createdBy(doctorUser)
                .build();

        Appointment futureConfirmed = Appointment.builder()
                .child(testChild)
                .appointmentDate(now.plusDays(7))
                .appointmentType("Follow-up")
                .status(Appointment.AppointmentStatus.CONFIRMED)
                .createdBy(doctorUser)
                .build();

        Appointment futureCompleted = Appointment.builder()
                .child(testChild)
                .appointmentDate(now.plusDays(10))
                .appointmentType("Vaccination")
                .status(Appointment.AppointmentStatus.COMPLETED)
                .createdBy(doctorUser)
                .build();

        Appointment futureCancelled = Appointment.builder()
                .child(testChild)
                .appointmentDate(now.plusDays(12))
                .appointmentType("Checkup")
                .status(Appointment.AppointmentStatus.CANCELLED)
                .createdBy(doctorUser)
                .build();

        appointmentRepository.saveAll(List.of(
                pastScheduled, futureScheduled, futureConfirmed, futureCompleted, futureCancelled
        ));
        appointmentRepository.flush();

        // When
        List<Appointment> upcomingAppointments = appointmentRepository.findUpcomingAppointments(now);

        // Then - should return only future appointments with SCHEDULED or CONFIRMED status
        assertThat(upcomingAppointments).hasSize(2);
        assertThat(upcomingAppointments.get(0).getAppointmentType()).isEqualTo("Checkup");
        assertThat(upcomingAppointments.get(1).getAppointmentType()).isEqualTo("Follow-up");
        assertThat(upcomingAppointments).allMatch(a ->
                a.getAppointmentDate().isAfter(now) || a.getAppointmentDate().isEqual(now)
        );
        assertThat(upcomingAppointments).allMatch(a ->
                a.getStatus() == Appointment.AppointmentStatus.SCHEDULED ||
                a.getStatus() == Appointment.AppointmentStatus.CONFIRMED
        );
    }

    @Test
    @DisplayName("Should find appointments by creator user id")
    void testFindByCreatedById() {
        // Given - appointments created by different users
        Appointment doctorCreated1 = Appointment.builder()
                .child(testChild)
                .appointmentDate(LocalDateTime.now().plusDays(5))
                .appointmentType("Checkup")
                .createdBy(doctorUser)
                .build();

        Appointment doctorCreated2 = Appointment.builder()
                .child(testChild)
                .appointmentDate(LocalDateTime.now().plusDays(2))
                .appointmentType("Vaccination")
                .createdBy(doctorUser)
                .build();

        Appointment nurseCreated = Appointment.builder()
                .child(testChild)
                .appointmentDate(LocalDateTime.now().plusDays(3))
                .appointmentType("Follow-up")
                .createdBy(nurseUser)
                .build();

        appointmentRepository.saveAll(List.of(doctorCreated1, doctorCreated2, nurseCreated));
        appointmentRepository.flush();

        // When
        List<Appointment> doctorCreatedAppointments = appointmentRepository.findByCreatedById(doctorUser.getId());

        // Then - should return only doctor-created appointments ordered by date DESC
        assertThat(doctorCreatedAppointments).hasSize(2);
        assertThat(doctorCreatedAppointments.get(0).getAppointmentType()).isEqualTo("Checkup");
        assertThat(doctorCreatedAppointments.get(1).getAppointmentType()).isEqualTo("Vaccination");
        assertThat(doctorCreatedAppointments).allMatch(a -> a.getCreatedBy().getId().equals(doctorUser.getId()));
    }

    @Test
    @DisplayName("Should find appointments by user id through children via guardians")
    void testFindByUserIdThroughChildren() {
        // Given - child associated with parent through guardian
        Appointment appointment1 = Appointment.builder()
                .child(testChild)
                .appointmentDate(LocalDateTime.now().plusDays(3))
                .appointmentType("Vaccination")
                .createdBy(doctorUser)
                .build();

        Appointment appointment2 = Appointment.builder()
                .child(testChild)
                .appointmentDate(LocalDateTime.now().plusDays(7))
                .appointmentType("Checkup")
                .createdBy(doctorUser)
                .build();

        // Create another child without guardian
        Child childWithoutGuardian = Child.builder()
                .firstName("Ana")
                .lastName("Martinez")
                .documentNumber("1111111111")
                .dateOfBirth(LocalDate.of(2023, 3, 10))
                .gender(Child.Gender.F)
                .build();
        childWithoutGuardian = childRepository.save(childWithoutGuardian);

        Appointment otherChildAppointment = Appointment.builder()
                .child(childWithoutGuardian)
                .appointmentDate(LocalDateTime.now().plusDays(5))
                .appointmentType("Follow-up")
                .createdBy(doctorUser)
                .build();

        appointmentRepository.saveAll(List.of(appointment1, appointment2, otherChildAppointment));
        appointmentRepository.flush();

        // When - find appointments through parent user's children
        List<Appointment> parentAppointments = appointmentRepository.findByUserIdThroughChildren(parentUser.getId());

        // Then - should return only appointments for children with this parent as guardian
        assertThat(parentAppointments).hasSize(2);
        assertThat(parentAppointments.get(0).getAppointmentType()).isEqualTo("Checkup");
        assertThat(parentAppointments.get(1).getAppointmentType()).isEqualTo("Vaccination");
        assertThat(parentAppointments).allMatch(a -> a.getChild().getId().equals(testChild.getId()));
    }

    @Test
    @DisplayName("Should verify appointment-child relationship")
    void testAppointmentChildRelationship() {
        // Given
        Appointment appointment = Appointment.builder()
                .child(testChild)
                .appointmentDate(LocalDateTime.now().plusDays(5))
                .appointmentType("Vaccination")
                .createdBy(doctorUser)
                .build();

        // When
        Appointment savedAppointment = appointmentRepository.save(appointment);
        appointmentRepository.flush();

        // Then - child is required (not null)
        assertThat(savedAppointment.getChild()).isNotNull();
        assertThat(savedAppointment.getChild().getId()).isEqualTo(testChild.getId());

        // Verify lazy loading works
        Appointment retrievedAppointment = appointmentRepository.findById(savedAppointment.getId()).orElseThrow();
        assertThat(retrievedAppointment.getChild().getFirstName()).isEqualTo("Pedro");
    }

    @Test
    @DisplayName("Should verify appointment-user assigned to optional relationship")
    void testAppointmentAssignedToOptionalRelationship() {
        // Given - appointment without assignedTo (nullable)
        Appointment appointment = Appointment.builder()
                .child(testChild)
                .appointmentDate(LocalDateTime.now().plusDays(5))
                .appointmentType("Vaccination")
                .createdBy(doctorUser)
                .build();

        // When
        Appointment savedAppointment = appointmentRepository.save(appointment);
        appointmentRepository.flush();

        // Then - assignedTo is optional (can be null)
        assertThat(savedAppointment.getAssignedTo()).isNull();

        // Verify it can be set
        savedAppointment.setAssignedTo(nurseUser);
        Appointment updatedAppointment = appointmentRepository.save(savedAppointment);
        appointmentRepository.flush();

        assertThat(updatedAppointment.getAssignedTo()).isNotNull();
        assertThat(updatedAppointment.getAssignedTo().getId()).isEqualTo(nurseUser.getId());
    }

    @Test
    @DisplayName("Should verify appointment-user created by relationship")
    void testAppointmentCreatedByRelationship() {
        // Given - appointment without createdBy (required)
        Appointment appointment = Appointment.builder()
                .child(testChild)
                .appointmentDate(LocalDateTime.now().plusDays(5))
                .appointmentType("Vaccination")
                .build();

        // When & Then - should fail due to not null constraint
        assertThatThrownBy(() -> {
            appointmentRepository.save(appointment);
            appointmentRepository.flush();
        }).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("Should verify appointment date ordering")
    void testAppointmentDateOrdering() {
        // Given - 3 appointments saved in random order
        LocalDateTime now = LocalDateTime.now();

        Appointment middle = Appointment.builder()
                .child(testChild)
                .appointmentDate(now.plusDays(5))
                .appointmentType("Middle")
                .createdBy(doctorUser)
                .build();

        Appointment earliest = Appointment.builder()
                .child(testChild)
                .appointmentDate(now.plusDays(2))
                .appointmentType("Earliest")
                .createdBy(doctorUser)
                .build();

        Appointment latest = Appointment.builder()
                .child(testChild)
                .appointmentDate(now.plusDays(10))
                .appointmentType("Latest")
                .createdBy(doctorUser)
                .build();

        appointmentRepository.saveAll(List.of(middle, earliest, latest));
        appointmentRepository.flush();

        // When - retrieve with different ordering
        List<Appointment> orderedAsc = appointmentRepository.findByStatusOrderByAppointmentDate(
                Appointment.AppointmentStatus.SCHEDULED
        );
        List<Appointment> orderedDesc = appointmentRepository.findByChildId(testChild.getId());

        // Then - verify correct ordering
        assertThat(orderedAsc.get(0).getAppointmentType()).isEqualTo("Earliest");
        assertThat(orderedAsc.get(1).getAppointmentType()).isEqualTo("Middle");
        assertThat(orderedAsc.get(2).getAppointmentType()).isEqualTo("Latest");

        assertThat(orderedDesc.get(0).getAppointmentType()).isEqualTo("Latest");
        assertThat(orderedDesc.get(1).getAppointmentType()).isEqualTo("Middle");
        assertThat(orderedDesc.get(2).getAppointmentType()).isEqualTo("Earliest");
    }

    @Test
    @DisplayName("Should update appointment and modify updatedAt timestamp")
    void testUpdateAppointmentModifiesUpdatedAt() throws InterruptedException {
        // Given
        Appointment appointment = Appointment.builder()
                .child(testChild)
                .appointmentDate(LocalDateTime.now().plusDays(5))
                .appointmentType("Vaccination")
                .status(Appointment.AppointmentStatus.SCHEDULED)
                .createdBy(doctorUser)
                .build();

        Appointment savedAppointment = appointmentRepository.save(appointment);
        appointmentRepository.flush();

        LocalDateTime originalUpdatedAt = savedAppointment.getUpdatedAt();
        LocalDateTime originalCreatedAt = savedAppointment.getCreatedAt();

        // Wait to ensure timestamp difference
        Thread.sleep(10);

        // When - update appointment status
        savedAppointment.setStatus(Appointment.AppointmentStatus.CONFIRMED);
        savedAppointment.setNotes("Confirmed by phone");
        Appointment updatedAppointment = appointmentRepository.save(savedAppointment);
        appointmentRepository.flush();

        // Then - updatedAt should be modified, createdAt should remain unchanged
        assertThat(updatedAppointment.getUpdatedAt()).isAfter(originalUpdatedAt);
        assertThat(updatedAppointment.getCreatedAt()).isEqualTo(originalCreatedAt);
        assertThat(updatedAppointment.getStatus()).isEqualTo(Appointment.AppointmentStatus.CONFIRMED);
        assertThat(updatedAppointment.getNotes()).isEqualTo("Confirmed by phone");
    }

    @Test
    @DisplayName("Should verify Oracle database connection and schema")
    void testOracleDatabaseConnection() {
        // Given/When
        String oracleVersion = getOracleVersion();
        boolean appointmentsTableExists = tableExists("appointments");

        // Then
        assertThat(oracleVersion).containsIgnoringCase("Oracle");
        assertThat(appointmentsTableExists).isTrue();
    }
}
