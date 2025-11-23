package py.gov.mspbs.javacunas.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import py.gov.mspbs.javacunas.BaseUnitTest;
import py.gov.mspbs.javacunas.dto.UserDto;
import py.gov.mspbs.javacunas.entity.User;
import py.gov.mspbs.javacunas.exception.ResourceNotFoundException;
import py.gov.mspbs.javacunas.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("UserService Tests")
class UserServiceTest extends BaseUnitTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private User doctor;
    private User nurse;
    private User parent;

    @BeforeEach
    void setUp() {
        doctor = User.builder()
                .id(1L)
                .username("doctor1")
                .firstName("Dr. Juan")
                .lastName("Médico")
                .email("doctor@test.com")
                .role(User.UserRole.DOCTOR)
                .licenseNumber("DOC-001")
                .isActive('Y')
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        nurse = User.builder()
                .id(2L)
                .username("nurse1")
                .firstName("María")
                .lastName("Enfermera")
                .email("nurse@test.com")
                .role(User.UserRole.NURSE)
                .licenseNumber("NUR-001")
                .isActive('Y')
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        parent = User.builder()
                .id(3L)
                .username("parent1")
                .firstName("Pedro")
                .lastName("Padre")
                .email("parent@test.com")
                .role(User.UserRole.PARENT)
                .isActive('Y')
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Nested
    @DisplayName("Get User By ID Tests")
    class GetUserByIdTests {

        @Test
        @DisplayName("Should get user by ID successfully")
        void getUserByIdSuccess() {
            // Given
            when(userRepository.findById(1L)).thenReturn(Optional.of(doctor));

            // When
            UserDto result = userService.getUserById(1L);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1L);
            assertThat(result.getUsername()).isEqualTo("doctor1");
            assertThat(result.getRole()).isEqualTo(User.UserRole.DOCTOR);
            verify(userRepository).findById(1L);
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when user not found")
        void getUserByIdNotFound() {
            // Given
            when(userRepository.findById(999L)).thenReturn(Optional.empty());

            // When/Then
            assertThatThrownBy(() -> userService.getUserById(999L))
                    .isInstanceOf(ResourceNotFoundException.class);
            verify(userRepository).findById(999L);
        }
    }

    @Nested
    @DisplayName("Get All Users Tests")
    class GetAllUsersTests {

        @Test
        @DisplayName("Should get all users successfully")
        void getAllUsersSuccess() {
            // Given
            when(userRepository.findAll()).thenReturn(Arrays.asList(doctor, nurse, parent));

            // When
            List<UserDto> result = userService.getAllUsers();

            // Then
            assertThat(result).hasSize(3);
            assertThat(result).extracting(UserDto::getRole)
                    .containsExactlyInAnyOrder(User.UserRole.DOCTOR, User.UserRole.NURSE, User.UserRole.PARENT);
            verify(userRepository).findAll();
        }

        @Test
        @DisplayName("Should return empty list when no users exist")
        void getAllUsersEmpty() {
            // Given
            when(userRepository.findAll()).thenReturn(Arrays.asList());

            // When
            List<UserDto> result = userService.getAllUsers();

            // Then
            assertThat(result).isEmpty();
            verify(userRepository).findAll();
        }
    }

    @Nested
    @DisplayName("Get Users By Role Tests")
    class GetUsersByRoleTests {

        @Test
        @DisplayName("Should get doctors successfully")
        void getDoctorsSuccess() {
            // Given
            when(userRepository.findByRole(User.UserRole.DOCTOR)).thenReturn(Arrays.asList(doctor));

            // When
            List<UserDto> result = userService.getUsersByRole(User.UserRole.DOCTOR);

            // Then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getRole()).isEqualTo(User.UserRole.DOCTOR);
            verify(userRepository).findByRole(User.UserRole.DOCTOR);
        }

        @Test
        @DisplayName("Should get nurses successfully")
        void getNursesSuccess() {
            // Given
            when(userRepository.findByRole(User.UserRole.NURSE)).thenReturn(Arrays.asList(nurse));

            // When
            List<UserDto> result = userService.getUsersByRole(User.UserRole.NURSE);

            // Then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getRole()).isEqualTo(User.UserRole.NURSE);
            verify(userRepository).findByRole(User.UserRole.NURSE);
        }
    }

    @Nested
    @DisplayName("Get Active Users By Role Tests")
    class GetActiveUsersByRoleTests {

        @Test
        @DisplayName("Should get active doctors successfully")
        void getActiveDoctorsSuccess() {
            // Given
            when(userRepository.findByRoleAndIsActive(User.UserRole.DOCTOR, 'Y'))
                    .thenReturn(Arrays.asList(doctor));

            // When
            List<UserDto> result = userService.getActiveUsersByRole(User.UserRole.DOCTOR);

            // Then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getIsActive()).isEqualTo('Y');
            verify(userRepository).findByRoleAndIsActive(User.UserRole.DOCTOR, 'Y');
        }
    }

    @Nested
    @DisplayName("Get Active Doctors Tests")
    class GetActiveDoctorsTests {

        @Test
        @DisplayName("Should get all active doctors")
        void getAllActiveDoctors() {
            // Given
            when(userRepository.findByRoleAndIsActive(User.UserRole.DOCTOR, 'Y'))
                    .thenReturn(Arrays.asList(doctor));

            // When
            List<UserDto> result = userService.getAllActiveDoctors();

            // Then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getRole()).isEqualTo(User.UserRole.DOCTOR);
            verify(userRepository).findByRoleAndIsActive(User.UserRole.DOCTOR, 'Y');
        }
    }

    @Nested
    @DisplayName("Get Active Nurses Tests")
    class GetActiveNursesTests {

        @Test
        @DisplayName("Should get all active nurses")
        void getAllActiveNurses() {
            // Given
            when(userRepository.findByRoleAndIsActive(User.UserRole.NURSE, 'Y'))
                    .thenReturn(Arrays.asList(nurse));

            // When
            List<UserDto> result = userService.getAllActiveNurses();

            // Then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getRole()).isEqualTo(User.UserRole.NURSE);
            verify(userRepository).findByRoleAndIsActive(User.UserRole.NURSE, 'Y');
        }
    }

    @Nested
    @DisplayName("Get All Active Medical Staff Tests")
    class GetAllActiveMedicalStaffTests {

        @Test
        @DisplayName("Should get all active doctors and nurses")
        void getAllActiveMedicalStaff() {
            // Given
            when(userRepository.findByRoleAndIsActive(User.UserRole.DOCTOR, 'Y'))
                    .thenReturn(Arrays.asList(doctor));
            when(userRepository.findByRoleAndIsActive(User.UserRole.NURSE, 'Y'))
                    .thenReturn(Arrays.asList(nurse));

            // When
            List<UserDto> result = userService.getAllActiveMedicalStaff();

            // Then
            assertThat(result).hasSize(2);
            assertThat(result).extracting(UserDto::getRole)
                    .containsExactlyInAnyOrder(User.UserRole.DOCTOR, User.UserRole.NURSE);
            verify(userRepository).findByRoleAndIsActive(User.UserRole.DOCTOR, 'Y');
            verify(userRepository).findByRoleAndIsActive(User.UserRole.NURSE, 'Y');
        }

        @Test
        @DisplayName("Should return empty list when no medical staff exists")
        void getAllActiveMedicalStaffEmpty() {
            // Given
            when(userRepository.findByRoleAndIsActive(User.UserRole.DOCTOR, 'Y'))
                    .thenReturn(Arrays.asList());
            when(userRepository.findByRoleAndIsActive(User.UserRole.NURSE, 'Y'))
                    .thenReturn(Arrays.asList());

            // When
            List<UserDto> result = userService.getAllActiveMedicalStaff();

            // Then
            assertThat(result).isEmpty();
        }
    }
}
