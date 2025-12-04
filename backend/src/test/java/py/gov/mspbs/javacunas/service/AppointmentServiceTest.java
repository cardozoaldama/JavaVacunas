package py.gov.mspbs.javacunas.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import py.gov.mspbs.javacunas.BaseUnitTest;
import py.gov.mspbs.javacunas.dto.AppointmentDto;
import py.gov.mspbs.javacunas.entity.Appointment;
import py.gov.mspbs.javacunas.entity.Child;
import py.gov.mspbs.javacunas.entity.User;
import py.gov.mspbs.javacunas.repository.AppointmentRepository;
import py.gov.mspbs.javacunas.repository.ChildRepository;
import py.gov.mspbs.javacunas.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@DisplayName("AppointmentService Tests")
class AppointmentServiceTest extends BaseUnitTest {

    @Mock
    private AppointmentRepository appointmentRepository;

    @Mock
    private ChildRepository childRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AppointmentService appointmentService;

    private Appointment appointment1;
    private Appointment appointment2;
    private Child child;
    private User user;

    @BeforeEach
    void setUp() {
        child = Child.builder()
                .id(1L)
                .firstName("Juan")
                .lastName("Pérez")
                .build();

        user = User.builder()
                .id(1L)
                .username("parent1")
                .role(User.UserRole.PARENT)
                .build();

        appointment1 = Appointment.builder()
                .id(1L)
                .child(child)
                .appointmentDate(LocalDateTime.now().plusDays(7))
                .appointmentType("Vacunación")
                .status(Appointment.AppointmentStatus.SCHEDULED)
                .createdBy(user)
                .build();

        appointment2 = Appointment.builder()
                .id(2L)
                .child(child)
                .appointmentDate(LocalDateTime.now().plusDays(14))
                .appointmentType("Control")
                .status(Appointment.AppointmentStatus.CONFIRMED)
                .createdBy(user)
                .build();
    }

    @Nested
    @DisplayName("Get Appointments By User ID Tests")
    class GetAppointmentsByUserIdTests {

        @Test
        @DisplayName("Should get appointments for user's children")
        void shouldGetAppointmentsByUserId() {
            // Given
            when(appointmentRepository.findByUserIdThroughChildren(1L))
                    .thenReturn(Arrays.asList(appointment1, appointment2));

            // When
            List<AppointmentDto> result = appointmentService.getAppointmentsByUserId(1L);

            // Then
            assertThat(result).hasSize(2);
            assertThat(result.get(0).getId()).isEqualTo(1L);
            assertThat(result.get(1).getId()).isEqualTo(2L);
            verify(appointmentRepository).findByUserIdThroughChildren(1L);
        }

        @Test
        @DisplayName("Should return empty list when user has no appointments")
        void shouldReturnEmptyListWhenUserHasNoAppointments() {
            // Given
            when(appointmentRepository.findByUserIdThroughChildren(999L))
                    .thenReturn(List.of());

            // When
            List<AppointmentDto> result = appointmentService.getAppointmentsByUserId(999L);

            // Then
            assertThat(result).isEmpty();
            verify(appointmentRepository).findByUserIdThroughChildren(999L);
        }
    }

    @Nested
    @DisplayName("Get All Appointments Tests")
    class GetAllAppointmentsTests {

        @Test
        @DisplayName("Should get all appointments")
        void shouldGetAllAppointments() {
            // Given
            when(appointmentRepository.findAll())
                    .thenReturn(Arrays.asList(appointment1, appointment2));

            // When
            List<AppointmentDto> result = appointmentService.getAllAppointments();

            // Then
            assertThat(result).hasSize(2);
            verify(appointmentRepository).findAll();
        }
    }
}
