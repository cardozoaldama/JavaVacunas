package py.gov.mspbs.javacunas.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import py.gov.mspbs.javacunas.BaseUnitTest;
import py.gov.mspbs.javacunas.entity.User.UserRole;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for Appointment entity.
 * Focus on @Builder.Default behavior, lifecycle callbacks, and enum values.
 */
@DisplayName("Appointment Entity Tests")
class AppointmentTest extends BaseUnitTest {

    @Nested
    @DisplayName("Builder Default Tests")
    class BuilderDefaultTests {

        @Test
        @DisplayName("Should have SCHEDULED status by default when using builder without setting status")
        void shouldHaveScheduledStatusByDefaultWhenUsingBuilderWithoutSettingStatus() {
            // Given
            Child child = Child.builder().id(1L).build();
            User createdBy = User.builder().id(1L).build();
            LocalDateTime appointmentDate = LocalDateTime.now().plusDays(7);

            // When
            Appointment appointment = Appointment.builder()
                    .child(child)
                    .appointmentDate(appointmentDate)
                    .appointmentType("ROUTINE_VACCINATION")
                    .createdBy(createdBy)
                    .build();

            // Then
            assertThat(appointment.getStatus()).isEqualTo(Appointment.AppointmentStatus.SCHEDULED);
        }

        @Test
        @DisplayName("Should use provided status when explicitly set in builder")
        void shouldUseProvidedStatusWhenExplicitlySetInBuilder() {
            // Given
            Child child = Child.builder().id(1L).build();
            User createdBy = User.builder().id(1L).build();
            LocalDateTime appointmentDate = LocalDateTime.now().plusDays(7);

            // When
            Appointment appointment = Appointment.builder()
                    .child(child)
                    .appointmentDate(appointmentDate)
                    .appointmentType("ROUTINE_VACCINATION")
                    .status(Appointment.AppointmentStatus.CONFIRMED)
                    .createdBy(createdBy)
                    .build();

            // Then
            assertThat(appointment.getStatus()).isEqualTo(Appointment.AppointmentStatus.CONFIRMED);
        }

        @Test
        @DisplayName("Should allow setting status to null explicitly")
        void shouldAllowSettingStatusToNullExplicitly() {
            // Given
            Child child = Child.builder().id(1L).build();
            User createdBy = User.builder().id(1L).build();
            LocalDateTime appointmentDate = LocalDateTime.now().plusDays(7);

            // When
            Appointment appointment = Appointment.builder()
                    .child(child)
                    .appointmentDate(appointmentDate)
                    .appointmentType("ROUTINE_VACCINATION")
                    .status(null)
                    .createdBy(createdBy)
                    .build();

            // Then
            assertThat(appointment.getStatus()).isNull();
        }
    }

    @Nested
    @DisplayName("PrePersist Lifecycle Tests")
    class PrePersistLifecycleTests {

        @Test
        @DisplayName("Should set createdAt and updatedAt on @PrePersist")
        void shouldSetCreatedAtAndUpdatedAtOnPrePersist() {
            // Given
            Child child = Child.builder().id(1L).build();
            User createdBy = User.builder().id(1L).build();
            LocalDateTime appointmentDate = LocalDateTime.now().plusDays(7);

            Appointment appointment = Appointment.builder()
                    .child(child)
                    .appointmentDate(appointmentDate)
                    .appointmentType("ROUTINE_VACCINATION")
                    .createdBy(createdBy)
                    .build();

            LocalDateTime beforeCreate = LocalDateTime.now().minusSeconds(1);

            // When
            appointment.onCreate();

            // Then
            LocalDateTime afterCreate = LocalDateTime.now().plusSeconds(1);

            assertThat(appointment.getCreatedAt()).isNotNull();
            assertThat(appointment.getUpdatedAt()).isNotNull();
            assertThat(appointment.getCreatedAt()).isAfterOrEqualTo(beforeCreate);
            assertThat(appointment.getCreatedAt()).isBeforeOrEqualTo(afterCreate);
            assertThat(appointment.getUpdatedAt()).isAfterOrEqualTo(beforeCreate);
            assertThat(appointment.getUpdatedAt()).isBeforeOrEqualTo(afterCreate);
        }

        @Test
        @DisplayName("Should set createdAt and updatedAt to same or very close timestamp on @PrePersist")
        void shouldSetCreatedAtAndUpdatedAtToSameOrVeryCloseTimestampOnPrePersist() {
            // Given
            Appointment appointment = Appointment.builder()
                    .appointmentType("ROUTINE_VACCINATION")
                    .build();

            // When
            appointment.onCreate();

            // Then
            assertThat(appointment.getCreatedAt()).isNotNull();
            assertThat(appointment.getUpdatedAt()).isNotNull();
            // Timestamps should be equal or within milliseconds of each other
            long diffInNanos = Math.abs(
                appointment.getCreatedAt().toLocalTime().toNanoOfDay() -
                appointment.getUpdatedAt().toLocalTime().toNanoOfDay()
            );
            assertThat(diffInNanos).isLessThan(1_000_000); // Less than 1 millisecond
        }
    }

    @Nested
    @DisplayName("PreUpdate Lifecycle Tests")
    class PreUpdateLifecycleTests {

        @Test
        @DisplayName("Should update updatedAt on @PreUpdate")
        void shouldUpdateUpdatedAtOnPreUpdate() throws InterruptedException {
            // Given
            Appointment appointment = Appointment.builder()
                    .appointmentType("ROUTINE_VACCINATION")
                    .build();

            appointment.onCreate();
            LocalDateTime originalUpdatedAt = appointment.getUpdatedAt();

            // Ensure time passes
            Thread.sleep(10);

            // When
            appointment.onUpdate();

            // Then
            assertThat(appointment.getUpdatedAt()).isNotNull();
            assertThat(appointment.getUpdatedAt()).isAfter(originalUpdatedAt);
        }

        @Test
        @DisplayName("Should preserve createdAt on @PreUpdate")
        void shouldPreserveCreatedAtOnPreUpdate() throws InterruptedException {
            // Given
            Appointment appointment = Appointment.builder()
                    .appointmentType("ROUTINE_VACCINATION")
                    .build();

            appointment.onCreate();
            LocalDateTime originalCreatedAt = appointment.getCreatedAt();

            // Ensure time passes
            Thread.sleep(10);

            // When
            appointment.onUpdate();

            // Then
            assertThat(appointment.getCreatedAt()).isEqualTo(originalCreatedAt);
        }

        @Test
        @DisplayName("Should update updatedAt multiple times on @PreUpdate")
        void shouldUpdateUpdatedAtMultipleTimesOnPreUpdate() throws InterruptedException {
            // Given
            Appointment appointment = Appointment.builder()
                    .appointmentType("ROUTINE_VACCINATION")
                    .build();

            appointment.onCreate();
            LocalDateTime firstUpdatedAt = appointment.getUpdatedAt();
            LocalDateTime createdAt = appointment.getCreatedAt();

            Thread.sleep(10);
            appointment.onUpdate();
            LocalDateTime secondUpdatedAt = appointment.getUpdatedAt();

            Thread.sleep(10);

            // When
            appointment.onUpdate();
            LocalDateTime thirdUpdatedAt = appointment.getUpdatedAt();

            // Then
            assertThat(secondUpdatedAt).isAfterOrEqualTo(firstUpdatedAt);
            assertThat(thirdUpdatedAt).isAfterOrEqualTo(secondUpdatedAt);
            assertThat(appointment.getCreatedAt()).isEqualTo(createdAt);
        }
    }

    @Nested
    @DisplayName("AppointmentStatus Enum Tests")
    class AppointmentStatusEnumTests {

        @Test
        @DisplayName("Should handle SCHEDULED status")
        void shouldHandleScheduledStatus() {
            // Given
            Appointment appointment = Appointment.builder()
                    .appointmentType("ROUTINE_VACCINATION")
                    .status(Appointment.AppointmentStatus.SCHEDULED)
                    .build();

            // When
            Appointment.AppointmentStatus status = appointment.getStatus();

            // Then
            assertThat(status).isEqualTo(Appointment.AppointmentStatus.SCHEDULED);
            assertThat(status.name()).isEqualTo("SCHEDULED");
        }

        @Test
        @DisplayName("Should handle CONFIRMED status")
        void shouldHandleConfirmedStatus() {
            // Given
            Appointment appointment = Appointment.builder()
                    .appointmentType("ROUTINE_VACCINATION")
                    .status(Appointment.AppointmentStatus.CONFIRMED)
                    .build();

            // When
            Appointment.AppointmentStatus status = appointment.getStatus();

            // Then
            assertThat(status).isEqualTo(Appointment.AppointmentStatus.CONFIRMED);
            assertThat(status.name()).isEqualTo("CONFIRMED");
        }

        @Test
        @DisplayName("Should handle COMPLETED status")
        void shouldHandleCompletedStatus() {
            // Given
            Appointment appointment = Appointment.builder()
                    .appointmentType("ROUTINE_VACCINATION")
                    .status(Appointment.AppointmentStatus.COMPLETED)
                    .build();

            // When
            Appointment.AppointmentStatus status = appointment.getStatus();

            // Then
            assertThat(status).isEqualTo(Appointment.AppointmentStatus.COMPLETED);
            assertThat(status.name()).isEqualTo("COMPLETED");
        }

        @Test
        @DisplayName("Should handle CANCELLED status")
        void shouldHandleCancelledStatus() {
            // Given
            Appointment appointment = Appointment.builder()
                    .appointmentType("ROUTINE_VACCINATION")
                    .status(Appointment.AppointmentStatus.CANCELLED)
                    .build();

            // When
            Appointment.AppointmentStatus status = appointment.getStatus();

            // Then
            assertThat(status).isEqualTo(Appointment.AppointmentStatus.CANCELLED);
            assertThat(status.name()).isEqualTo("CANCELLED");
        }

        @Test
        @DisplayName("Should handle NO_SHOW status")
        void shouldHandleNoShowStatus() {
            // Given
            Appointment appointment = Appointment.builder()
                    .appointmentType("ROUTINE_VACCINATION")
                    .status(Appointment.AppointmentStatus.NO_SHOW)
                    .build();

            // When
            Appointment.AppointmentStatus status = appointment.getStatus();

            // Then
            assertThat(status).isEqualTo(Appointment.AppointmentStatus.NO_SHOW);
            assertThat(status.name()).isEqualTo("NO_SHOW");
        }

        @Test
        @DisplayName("Should have exactly 5 enum values")
        void shouldHaveExactly5EnumValues() {
            // Given & When
            Appointment.AppointmentStatus[] statuses = Appointment.AppointmentStatus.values();

            // Then
            assertThat(statuses).hasSize(5);
            assertThat(statuses).containsExactlyInAnyOrder(
                    Appointment.AppointmentStatus.SCHEDULED,
                    Appointment.AppointmentStatus.CONFIRMED,
                    Appointment.AppointmentStatus.COMPLETED,
                    Appointment.AppointmentStatus.CANCELLED,
                    Appointment.AppointmentStatus.NO_SHOW
            );
        }

        @Test
        @DisplayName("Should convert enum from string value")
        void shouldConvertEnumFromStringValue() {
            // Given
            String statusString = "CONFIRMED";

            // When
            Appointment.AppointmentStatus status = Appointment.AppointmentStatus.valueOf(statusString);

            // Then
            assertThat(status).isEqualTo(Appointment.AppointmentStatus.CONFIRMED);
        }
    }

    @Nested
    @DisplayName("Builder Tests with Required Fields")
    class BuilderWithRequiredFieldsTests {

        @Test
        @DisplayName("Should allow building appointment with all required fields")
        void shouldAllowBuildingAppointmentWithAllRequiredFields() {
            // Given
            Child child = Child.builder()
                    .id(1L)
                    .firstName("Juan")
                    .lastName("Pérez")
                    .dateOfBirth(LocalDate.of(2023, 6, 15))
                    .build();

            User createdBy = User.builder()
                    .id(1L)
                    .username("doctor1")
                    .role(UserRole.DOCTOR)
                    .build();

            LocalDateTime appointmentDate = LocalDateTime.of(2025, 12, 15, 10, 0);

            // When
            Appointment appointment = Appointment.builder()
                    .child(child)
                    .appointmentDate(appointmentDate)
                    .appointmentType("ROUTINE_VACCINATION")
                    .createdBy(createdBy)
                    .build();

            // Then
            assertThat(appointment.getChild()).isEqualTo(child);
            assertThat(appointment.getAppointmentDate()).isEqualTo(appointmentDate);
            assertThat(appointment.getAppointmentType()).isEqualTo("ROUTINE_VACCINATION");
            assertThat(appointment.getCreatedBy()).isEqualTo(createdBy);
            assertThat(appointment.getStatus()).isEqualTo(Appointment.AppointmentStatus.SCHEDULED);
        }

        @Test
        @DisplayName("Should allow building appointment without optional fields")
        void shouldAllowBuildingAppointmentWithoutOptionalFields() {
            // Given & When
            Appointment appointment = Appointment.builder()
                    .appointmentType("ROUTINE_VACCINATION")
                    .build();

            // Then
            assertThat(appointment.getChild()).isNull();
            assertThat(appointment.getAppointmentDate()).isNull();
            assertThat(appointment.getScheduledVaccines()).isNull();
            assertThat(appointment.getAssignedTo()).isNull();
            assertThat(appointment.getNotes()).isNull();
            assertThat(appointment.getCreatedBy()).isNull();
            assertThat(appointment.getStatus()).isEqualTo(Appointment.AppointmentStatus.SCHEDULED);
        }
    }

    @Nested
    @DisplayName("Builder Tests with Optional Fields")
    class BuilderWithOptionalFieldsTests {

        @Test
        @DisplayName("Should allow building appointment with notes")
        void shouldAllowBuildingAppointmentWithNotes() {
            // Given
            String notes = "Patient has fever, reschedule if temperature above 38C";

            // When
            Appointment appointment = Appointment.builder()
                    .appointmentType("ROUTINE_VACCINATION")
                    .notes(notes)
                    .build();

            // Then
            assertThat(appointment.getNotes()).isEqualTo(notes);
        }

        @Test
        @DisplayName("Should allow building appointment with scheduled vaccines")
        void shouldAllowBuildingAppointmentWithScheduledVaccines() {
            // Given
            String scheduledVaccines = "BCG, Hepatitis B";

            // When
            Appointment appointment = Appointment.builder()
                    .appointmentType("ROUTINE_VACCINATION")
                    .scheduledVaccines(scheduledVaccines)
                    .build();

            // Then
            assertThat(appointment.getScheduledVaccines()).isEqualTo(scheduledVaccines);
        }

        @Test
        @DisplayName("Should allow building appointment with assigned user")
        void shouldAllowBuildingAppointmentWithAssignedUser() {
            // Given
            User assignedTo = User.builder()
                    .id(2L)
                    .username("nurse1")
                    .role(UserRole.NURSE)
                    .build();

            // When
            Appointment appointment = Appointment.builder()
                    .appointmentType("ROUTINE_VACCINATION")
                    .assignedTo(assignedTo)
                    .build();

            // Then
            assertThat(appointment.getAssignedTo()).isEqualTo(assignedTo);
            assertThat(appointment.getAssignedTo().getUsername()).isEqualTo("nurse1");
            assertThat(appointment.getAssignedTo().getRole()).isEqualTo(UserRole.NURSE);
        }

        @Test
        @DisplayName("Should allow building appointment with all optional fields")
        void shouldAllowBuildingAppointmentWithAllOptionalFields() {
            // Given
            Child child = Child.builder().id(1L).build();
            User createdBy = User.builder().id(1L).build();
            User assignedTo = User.builder().id(2L).build();
            LocalDateTime appointmentDate = LocalDateTime.now().plusDays(7);
            String scheduledVaccines = "BCG, Hepatitis B, Rotavirus";
            String notes = "First vaccination appointment for newborn";

            // When
            Appointment appointment = Appointment.builder()
                    .child(child)
                    .appointmentDate(appointmentDate)
                    .appointmentType("ROUTINE_VACCINATION")
                    .status(Appointment.AppointmentStatus.CONFIRMED)
                    .scheduledVaccines(scheduledVaccines)
                    .assignedTo(assignedTo)
                    .notes(notes)
                    .createdBy(createdBy)
                    .build();

            // Then
            assertThat(appointment.getChild()).isEqualTo(child);
            assertThat(appointment.getAppointmentDate()).isEqualTo(appointmentDate);
            assertThat(appointment.getAppointmentType()).isEqualTo("ROUTINE_VACCINATION");
            assertThat(appointment.getStatus()).isEqualTo(Appointment.AppointmentStatus.CONFIRMED);
            assertThat(appointment.getScheduledVaccines()).isEqualTo(scheduledVaccines);
            assertThat(appointment.getAssignedTo()).isEqualTo(assignedTo);
            assertThat(appointment.getNotes()).isEqualTo(notes);
            assertThat(appointment.getCreatedBy()).isEqualTo(createdBy);
        }
    }

    @Nested
    @DisplayName("Entity Relationship Tests")
    class EntityRelationshipTests {

        @Test
        @DisplayName("Should maintain relationship with Child entity")
        void shouldMaintainRelationshipWithChildEntity() {
            // Given
            Child child = Child.builder()
                    .id(1L)
                    .firstName("María")
                    .lastName("González")
                    .dateOfBirth(LocalDate.of(2024, 3, 20))
                    .build();

            // When
            Appointment appointment = Appointment.builder()
                    .child(child)
                    .appointmentType("ROUTINE_VACCINATION")
                    .build();

            // Then
            assertThat(appointment.getChild()).isNotNull();
            assertThat(appointment.getChild().getId()).isEqualTo(1L);
            assertThat(appointment.getChild().getFirstName()).isEqualTo("María");
        }

        @Test
        @DisplayName("Should maintain relationship with User entity as createdBy")
        void shouldMaintainRelationshipWithUserEntityAsCreatedBy() {
            // Given
            User doctor = User.builder()
                    .id(1L)
                    .username("doctor1")
                    .email("doctor1@mspbs.gov.py")
                    .role(UserRole.DOCTOR)
                    .build();

            // When
            Appointment appointment = Appointment.builder()
                    .appointmentType("ROUTINE_VACCINATION")
                    .createdBy(doctor)
                    .build();

            // Then
            assertThat(appointment.getCreatedBy()).isNotNull();
            assertThat(appointment.getCreatedBy().getId()).isEqualTo(1L);
            assertThat(appointment.getCreatedBy().getUsername()).isEqualTo("doctor1");
            assertThat(appointment.getCreatedBy().getRole()).isEqualTo(UserRole.DOCTOR);
        }

        @Test
        @DisplayName("Should maintain relationship with User entity as assignedTo")
        void shouldMaintainRelationshipWithUserEntityAsAssignedTo() {
            // Given
            User nurse = User.builder()
                    .id(2L)
                    .username("nurse1")
                    .email("nurse1@mspbs.gov.py")
                    .role(UserRole.NURSE)
                    .build();

            // When
            Appointment appointment = Appointment.builder()
                    .appointmentType("ROUTINE_VACCINATION")
                    .assignedTo(nurse)
                    .build();

            // Then
            assertThat(appointment.getAssignedTo()).isNotNull();
            assertThat(appointment.getAssignedTo().getId()).isEqualTo(2L);
            assertThat(appointment.getAssignedTo().getUsername()).isEqualTo("nurse1");
            assertThat(appointment.getAssignedTo().getRole()).isEqualTo(UserRole.NURSE);
        }

        @Test
        @DisplayName("Should allow different users for createdBy and assignedTo")
        void shouldAllowDifferentUsersForCreatedByAndAssignedTo() {
            // Given
            User doctor = User.builder()
                    .id(1L)
                    .username("doctor1")
                    .role(UserRole.DOCTOR)
                    .build();

            User nurse = User.builder()
                    .id(2L)
                    .username("nurse1")
                    .role(UserRole.NURSE)
                    .build();

            // When
            Appointment appointment = Appointment.builder()
                    .appointmentType("ROUTINE_VACCINATION")
                    .createdBy(doctor)
                    .assignedTo(nurse)
                    .build();

            // Then
            assertThat(appointment.getCreatedBy()).isNotEqualTo(appointment.getAssignedTo());
            assertThat(appointment.getCreatedBy().getUsername()).isEqualTo("doctor1");
            assertThat(appointment.getAssignedTo().getUsername()).isEqualTo("nurse1");
        }
    }

    @Nested
    @DisplayName("Field Value Tests")
    class FieldValueTests {

        @Test
        @DisplayName("Should store appointment type correctly")
        void shouldStoreAppointmentTypeCorrectly() {
            // Given
            String appointmentType = "FOLLOW_UP_VACCINATION";

            // When
            Appointment appointment = Appointment.builder()
                    .appointmentType(appointmentType)
                    .build();

            // Then
            assertThat(appointment.getAppointmentType()).isEqualTo("FOLLOW_UP_VACCINATION");
        }

        @Test
        @DisplayName("Should store appointment date correctly")
        void shouldStoreAppointmentDateCorrectly() {
            // Given
            LocalDateTime appointmentDate = LocalDateTime.of(2025, 12, 15, 14, 30);

            // When
            Appointment appointment = Appointment.builder()
                    .appointmentType("ROUTINE_VACCINATION")
                    .appointmentDate(appointmentDate)
                    .build();

            // Then
            assertThat(appointment.getAppointmentDate()).isEqualTo(appointmentDate);
            assertThat(appointment.getAppointmentDate().getYear()).isEqualTo(2025);
            assertThat(appointment.getAppointmentDate().getMonthValue()).isEqualTo(12);
            assertThat(appointment.getAppointmentDate().getDayOfMonth()).isEqualTo(15);
            assertThat(appointment.getAppointmentDate().getHour()).isEqualTo(14);
            assertThat(appointment.getAppointmentDate().getMinute()).isEqualTo(30);
        }

        @Test
        @DisplayName("Should store notes with maximum length")
        void shouldStoreNotesWithMaximumLength() {
            // Given
            String longNotes = "A".repeat(500);

            // When
            Appointment appointment = Appointment.builder()
                    .appointmentType("ROUTINE_VACCINATION")
                    .notes(longNotes)
                    .build();

            // Then
            assertThat(appointment.getNotes()).hasSize(500);
            assertThat(appointment.getNotes()).isEqualTo(longNotes);
        }

        @Test
        @DisplayName("Should store scheduled vaccines with multiple entries")
        void shouldStoreScheduledVaccinesWithMultipleEntries() {
            // Given
            String scheduledVaccines = "BCG, Hepatitis B, Pentavalent, IPV, Rotavirus, Pneumococcal";

            // When
            Appointment appointment = Appointment.builder()
                    .appointmentType("ROUTINE_VACCINATION")
                    .scheduledVaccines(scheduledVaccines)
                    .build();

            // Then
            assertThat(appointment.getScheduledVaccines()).isEqualTo(scheduledVaccines);
            assertThat(appointment.getScheduledVaccines()).contains("BCG");
            assertThat(appointment.getScheduledVaccines()).contains("Hepatitis B");
            assertThat(appointment.getScheduledVaccines()).contains("Pneumococcal");
        }
    }
}
