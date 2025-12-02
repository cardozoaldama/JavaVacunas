package py.gov.mspbs.javacunas.repository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import py.gov.mspbs.javacunas.AbstractOracleIntegrationTest;
import py.gov.mspbs.javacunas.entity.User;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

/**
 * Integration tests for UserRepository using Oracle 23c Free database.
 *
 * These tests verify:
 * - Basic CRUD operations
 * - Unique constraints (username, email)
 * - Custom query methods (findByUsername, findByEmail, existsBy*, findByRole)
 * - User role filtering
 * - Active/inactive user filtering
 * - Lifecycle callbacks (@PrePersist, @PreUpdate)
 */
@DisplayName("UserRepository Integration Tests")
class UserRepositoryIT extends AbstractOracleIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Nested
    @DisplayName("CRUD Operations")
    class CrudOperations {

        @Test
        @DisplayName("Should save and retrieve user with all required fields")
        void shouldSaveAndRetrieveUserWithAllRequiredFields() {
            // Given
            User user = User.builder()
                    .username("dr_smith")
                    .passwordHash("$2a$10$hashedpassword")
                    .firstName("John")
                    .lastName("Smith")
                    .email("john.smith@test.com")
                    .role(User.UserRole.DOCTOR)
                    .licenseNumber("MED-12345")
                    .isActive('Y')
                    .build();

            // When
            User savedUser = userRepository.save(user);
            userRepository.flush();

            // Then
            assertThat(savedUser.getId()).isNotNull();
            assertThat(savedUser.getCreatedAt()).isNotNull();
            assertThat(savedUser.getUpdatedAt()).isNotNull();
            assertThat(savedUser.getUsername()).isEqualTo("dr_smith");
            assertThat(savedUser.getFirstName()).isEqualTo("John");
            assertThat(savedUser.getLastName()).isEqualTo("Smith");
            assertThat(savedUser.getEmail()).isEqualTo("john.smith@test.com");
            assertThat(savedUser.getRole()).isEqualTo(User.UserRole.DOCTOR);
            assertThat(savedUser.getLicenseNumber()).isEqualTo("MED-12345");
            assertThat(savedUser.getIsActive()).isEqualTo('Y');

            // Verify database retrieval
            Optional<User> retrievedUser = userRepository.findById(savedUser.getId());
            assertThat(retrievedUser).isPresent();
            assertThat(retrievedUser.get().getUsername()).isEqualTo("dr_smith");
        }

        @Test
        @DisplayName("Should enforce unique username constraint")
        void shouldEnforceUniqueUsernameConstraint() {
            // Given
            User user1 = User.builder()
                    .username("duplicate_user")
                    .passwordHash("$2a$10$hashedpassword1")
                    .firstName("Alice")
                    .lastName("Johnson")
                    .email("alice.johnson@test.com")
                    .role(User.UserRole.NURSE)
                    .isActive('Y')
                    .build();

            User user2 = User.builder()
                    .username("duplicate_user")  // Duplicate username
                    .passwordHash("$2a$10$hashedpassword2")
                    .firstName("Bob")
                    .lastName("Williams")
                    .email("bob.williams@test.com")
                    .role(User.UserRole.NURSE)
                    .isActive('Y')
                    .build();

            // When
            userRepository.save(user1);
            userRepository.flush();

            // Then - attempting to save duplicate username should fail
            assertThatThrownBy(() -> {
                userRepository.save(user2);
                userRepository.flush();
            }).isInstanceOf(DataIntegrityViolationException.class)
              .hasMessageContaining("unique constraint");
        }

        @Test
        @DisplayName("Should enforce unique email constraint")
        void shouldEnforceUniqueEmailConstraint() {
            // Given
            User user1 = User.builder()
                    .username("user_email1")
                    .passwordHash("$2a$10$hashedpassword1")
                    .firstName("Emma")
                    .lastName("Davis")
                    .email("duplicate@test.com")
                    .role(User.UserRole.PARENT)
                    .isActive('Y')
                    .build();

            User user2 = User.builder()
                    .username("user_email2")
                    .passwordHash("$2a$10$hashedpassword2")
                    .firstName("Oliver")
                    .lastName("Miller")
                    .email("duplicate@test.com")  // Duplicate email
                    .role(User.UserRole.PARENT)
                    .isActive('Y')
                    .build();

            // When
            userRepository.save(user1);
            userRepository.flush();

            // Then - attempting to save duplicate email should fail
            assertThatThrownBy(() -> {
                userRepository.save(user2);
                userRepository.flush();
            }).isInstanceOf(DataIntegrityViolationException.class)
              .hasMessageContaining("unique constraint");
        }

        @Test
        @DisplayName("Should update user and modify updatedAt timestamp")
        void shouldUpdateUserAndModifyUpdatedAtTimestamp() throws InterruptedException {
            // Given
            User user = User.builder()
                    .username("update_test")
                    .passwordHash("$2a$10$hashedpassword")
                    .firstName("Michael")
                    .lastName("Brown")
                    .email("michael.brown@test.com")
                    .role(User.UserRole.DOCTOR)
                    .isActive('Y')
                    .build();

            User savedUser = userRepository.save(user);
            userRepository.flush();

            var originalCreatedAt = savedUser.getCreatedAt();
            var originalUpdatedAt = savedUser.getUpdatedAt();

            // Small delay to ensure updatedAt will be different
            Thread.sleep(10);

            // When
            savedUser.setFirstName("Michael Jr.");
            savedUser.setLicenseNumber("MED-67890");
            User updatedUser = userRepository.save(savedUser);
            userRepository.flush();

            // Then
            assertThat(updatedUser.getFirstName()).isEqualTo("Michael Jr.");
            assertThat(updatedUser.getLicenseNumber()).isEqualTo("MED-67890");
            assertThat(updatedUser.getCreatedAt()).isEqualTo(originalCreatedAt);
            assertThat(updatedUser.getUpdatedAt()).isAfter(originalUpdatedAt);
        }

        @Test
        @DisplayName("Should delete user from database")
        void shouldDeleteUserFromDatabase() {
            // Given
            User user = User.builder()
                    .username("delete_test")
                    .passwordHash("$2a$10$hashedpassword")
                    .firstName("Sarah")
                    .lastName("Garcia")
                    .email("sarah.garcia@test.com")
                    .role(User.UserRole.NURSE)
                    .isActive('Y')
                    .build();

            User savedUser = userRepository.save(user);
            userRepository.flush();
            Long userId = savedUser.getId();

            // When
            userRepository.delete(savedUser);
            userRepository.flush();

            // Then
            Optional<User> deletedUser = userRepository.findById(userId);
            assertThat(deletedUser).isEmpty();
        }
    }

    @Nested
    @DisplayName("Custom Query Methods")
    class CustomQueries {

        @Test
        @DisplayName("Should find user by username")
        void shouldFindUserByUsername() {
            // Given
            User user = User.builder()
                    .username("search_user")
                    .passwordHash("$2a$10$hashedpassword")
                    .firstName("David")
                    .lastName("Martinez")
                    .email("david.martinez@test.com")
                    .role(User.UserRole.DOCTOR)
                    .isActive('Y')
                    .build();

            userRepository.save(user);
            userRepository.flush();

            // When
            Optional<User> foundUser = userRepository.findByUsername("search_user");

            // Then
            assertThat(foundUser).isPresent();
            assertThat(foundUser.get().getUsername()).isEqualTo("search_user");
            assertThat(foundUser.get().getFirstName()).isEqualTo("David");
            assertThat(foundUser.get().getLastName()).isEqualTo("Martinez");
        }

        @Test
        @DisplayName("Should find user by email")
        void shouldFindUserByEmail() {
            // Given
            User user = User.builder()
                    .username("email_search")
                    .passwordHash("$2a$10$hashedpassword")
                    .firstName("Maria")
                    .lastName("Rodriguez")
                    .email("maria.rodriguez@test.com")
                    .role(User.UserRole.NURSE)
                    .isActive('Y')
                    .build();

            userRepository.save(user);
            userRepository.flush();

            // When
            Optional<User> foundUser = userRepository.findByEmail("maria.rodriguez@test.com");

            // Then
            assertThat(foundUser).isPresent();
            assertThat(foundUser.get().getEmail()).isEqualTo("maria.rodriguez@test.com");
            assertThat(foundUser.get().getUsername()).isEqualTo("email_search");
        }

        @Test
        @DisplayName("Should check if username exists")
        void shouldCheckIfUsernameExists() {
            // Given
            User user = User.builder()
                    .username("exists_test")
                    .passwordHash("$2a$10$hashedpassword")
                    .firstName("James")
                    .lastName("Wilson")
                    .email("james.wilson@test.com")
                    .role(User.UserRole.PARENT)
                    .isActive('Y')
                    .build();

            userRepository.save(user);
            userRepository.flush();

            // When/Then
            assertThat(userRepository.existsByUsername("exists_test")).isTrue();
            assertThat(userRepository.existsByUsername("nonexistent_user")).isFalse();
        }

        @Test
        @DisplayName("Should check if email exists")
        void shouldCheckIfEmailExists() {
            // Given
            User user = User.builder()
                    .username("email_exists_test")
                    .passwordHash("$2a$10$hashedpassword")
                    .firstName("Patricia")
                    .lastName("Anderson")
                    .email("patricia.anderson@test.com")
                    .role(User.UserRole.PARENT)
                    .isActive('Y')
                    .build();

            userRepository.save(user);
            userRepository.flush();

            // When/Then
            assertThat(userRepository.existsByEmail("patricia.anderson@test.com")).isTrue();
            assertThat(userRepository.existsByEmail("nonexistent@test.com")).isFalse();
        }

        @Test
        @DisplayName("Should find users by role")
        void shouldFindUsersByRole() {
            // Given
            User doctor = User.builder()
                    .username("doctor_role")
                    .passwordHash("$2a$10$hashedpassword")
                    .firstName("Dr. Richard")
                    .lastName("Thomas")
                    .email("richard.thomas@test.com")
                    .role(User.UserRole.DOCTOR)
                    .isActive('Y')
                    .build();

            User nurse = User.builder()
                    .username("nurse_role")
                    .passwordHash("$2a$10$hashedpassword")
                    .firstName("Linda")
                    .lastName("Taylor")
                    .email("linda.taylor@test.com")
                    .role(User.UserRole.NURSE)
                    .isActive('Y')
                    .build();

            User parent = User.builder()
                    .username("parent_role")
                    .passwordHash("$2a$10$hashedpassword")
                    .firstName("Robert")
                    .lastName("Moore")
                    .email("robert.moore@test.com")
                    .role(User.UserRole.PARENT)
                    .isActive('Y')
                    .build();

            userRepository.saveAll(List.of(doctor, nurse, parent));
            userRepository.flush();

            // When
            List<User> nurses = userRepository.findByRole(User.UserRole.NURSE);
            List<User> doctors = userRepository.findByRole(User.UserRole.DOCTOR);

            // Then
            assertThat(nurses).hasSize(1);
            assertThat(nurses.get(0).getUsername()).isEqualTo("nurse_role");
            assertThat(nurses.get(0).getRole()).isEqualTo(User.UserRole.NURSE);

            assertThat(doctors).hasSize(1);
            assertThat(doctors.get(0).getUsername()).isEqualTo("doctor_role");
        }

        @Test
        @DisplayName("Should find active users by role")
        void shouldFindActiveUsersByRole() {
            // Given
            User activeDoctor = User.builder()
                    .username("active_doctor")
                    .passwordHash("$2a$10$hashedpassword")
                    .firstName("Dr. Charles")
                    .lastName("Jackson")
                    .email("charles.jackson@test.com")
                    .role(User.UserRole.DOCTOR)
                    .isActive('Y')
                    .build();

            User inactiveDoctor = User.builder()
                    .username("inactive_doctor")
                    .passwordHash("$2a$10$hashedpassword")
                    .firstName("Dr. William")
                    .lastName("White")
                    .email("william.white@test.com")
                    .role(User.UserRole.DOCTOR)
                    .isActive('N')
                    .build();

            userRepository.saveAll(List.of(activeDoctor, inactiveDoctor));
            userRepository.flush();

            // When
            List<User> activeDoctors = userRepository.findByRoleAndIsActive(User.UserRole.DOCTOR, 'Y');

            // Then
            assertThat(activeDoctors).hasSize(1);
            assertThat(activeDoctors.get(0).getUsername()).isEqualTo("active_doctor");
            assertThat(activeDoctors.get(0).getIsActive()).isEqualTo('Y');
        }
    }

    @Nested
    @DisplayName("Database Schema Validation")
    class SchemaValidation {

        @Test
        @DisplayName("Should verify Oracle database connection and schema")
        void shouldVerifyOracleDatabaseConnectionAndSchema() {
            // Given/When
            String oracleVersion = getOracleVersion();
            boolean usersTableExists = tableExists("users");

            // Then
            assertThat(oracleVersion).containsIgnoringCase("Oracle");
            assertThat(usersTableExists).isTrue();
        }
    }
}
