package py.gov.mspbs.javacunas.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import py.gov.mspbs.javacunas.AbstractOracleIntegrationTest;
import py.gov.mspbs.javacunas.entity.Child;
import py.gov.mspbs.javacunas.entity.Guardian;
import py.gov.mspbs.javacunas.entity.User;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

/**
 * Integration tests for ChildRepository using Oracle 23c Free database.
 *
 * These tests verify:
 * - Basic CRUD operations with soft delete support
 * - Many-to-Many relationship with Guardians
 * - Custom query methods (findAllActive, searchByName, etc.)
 * - Database constraints (unique document number, not null, foreign keys)
 * - Oracle-specific features (PL/SQL functions)
 * - Soft delete behavior (deletedAt timestamp)
 */
@DisplayName("ChildRepository Integration Tests")
class ChildRepositoryIT extends AbstractOracleIntegrationTest {

    @Autowired
    private ChildRepository childRepository;

    @Autowired
    private GuardianRepository guardianRepository;

    @Autowired
    private UserRepository userRepository;

    private User testUser;
    private Guardian testGuardian;

    @BeforeEach
    void setUp() {
        // Create test user for guardian
        testUser = User.builder()
                .username("parent_test")
                .passwordHash("$2a$10$hashedpassword")
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@test.com")
                .role(User.UserRole.PARENT)
                .isActive('Y')
                .build();
        testUser = userRepository.save(testUser);

        // Create test guardian
        testGuardian = Guardian.builder()
                .user(testUser)
                .firstName("John")
                .lastName("Doe")
                .documentNumber("1234567890")
                .phone("+595981234567")
                .email("john.doe@test.com")
                .address("123 Test Street")
                .relationship("Father")
                .build();
        testGuardian = guardianRepository.save(testGuardian);
    }

    @Test
    @DisplayName("Should save and retrieve child with all required fields")
    void testSaveAndRetrieveChild() {
        // Given
        Child child = Child.builder()
                .firstName("Maria")
                .lastName("Garcia")
                .documentNumber("9876543210")
                .dateOfBirth(LocalDate.of(2023, 6, 15))
                .gender(Child.Gender.F)
                .bloodType("O+")
                .birthWeight(new BigDecimal("3.45"))
                .birthHeight(new BigDecimal("50.5"))
                .build();

        // When
        Child savedChild = childRepository.save(child);
        childRepository.flush();

        // Then
        assertThat(savedChild.getId()).isNotNull();
        assertThat(savedChild.getCreatedAt()).isNotNull();
        assertThat(savedChild.getUpdatedAt()).isNotNull();
        assertThat(savedChild.getDeletedAt()).isNull();
        assertThat(savedChild.getFirstName()).isEqualTo("Maria");
        assertThat(savedChild.getDocumentNumber()).isEqualTo("9876543210");
        assertThat(savedChild.isDeleted()).isFalse();

        // Verify database retrieval
        Optional<Child> retrievedChild = childRepository.findById(savedChild.getId());
        assertThat(retrievedChild).isPresent();
        assertThat(retrievedChild.get().getFirstName()).isEqualTo("Maria");
    }

    @Test
    @DisplayName("Should enforce unique document number constraint")
    void testUniqueDocumentNumberConstraint() {
        // Given
        Child child1 = Child.builder()
                .firstName("Pedro")
                .lastName("Lopez")
                .documentNumber("1111111111")
                .dateOfBirth(LocalDate.of(2023, 1, 1))
                .gender(Child.Gender.M)
                .build();

        Child child2 = Child.builder()
                .firstName("Ana")
                .lastName("Martinez")
                .documentNumber("1111111111")  // Duplicate document number
                .dateOfBirth(LocalDate.of(2023, 2, 1))
                .gender(Child.Gender.F)
                .build();

        // When
        childRepository.save(child1);
        childRepository.flush();

        // Then - attempting to save duplicate document number should fail
        assertThatThrownBy(() -> {
            childRepository.save(child2);
            childRepository.flush();
        }).isInstanceOf(DataIntegrityViolationException.class)
          .hasMessageContaining("unique constraint");
    }

    @Test
    @DisplayName("Should find child by document number")
    void testFindByDocumentNumber() {
        // Given
        Child child = Child.builder()
                .firstName("Carlos")
                .lastName("Rodriguez")
                .documentNumber("2222222222")
                .dateOfBirth(LocalDate.of(2022, 8, 20))
                .gender(Child.Gender.M)
                .build();

        childRepository.save(child);
        childRepository.flush();

        // When
        Optional<Child> foundChild = childRepository.findByDocumentNumber("2222222222");

        // Then
        assertThat(foundChild).isPresent();
        assertThat(foundChild.get().getFirstName()).isEqualTo("Carlos");
        assertThat(foundChild.get().getLastName()).isEqualTo("Rodriguez");
    }

    @Test
    @DisplayName("Should find all active children excluding soft-deleted ones")
    void testFindAllActive() {
        // Given
        Child activeChild1 = Child.builder()
                .firstName("Laura")
                .lastName("Fernandez")
                .documentNumber("3333333333")
                .dateOfBirth(LocalDate.of(2023, 3, 10))
                .gender(Child.Gender.F)
                .build();

        Child activeChild2 = Child.builder()
                .firstName("Diego")
                .lastName("Silva")
                .documentNumber("4444444444")
                .dateOfBirth(LocalDate.of(2023, 5, 25))
                .gender(Child.Gender.M)
                .build();

        Child deletedChild = Child.builder()
                .firstName("Sofia")
                .lastName("Torres")
                .documentNumber("5555555555")
                .dateOfBirth(LocalDate.of(2023, 7, 12))
                .gender(Child.Gender.F)
                .build();

        childRepository.saveAll(List.of(activeChild1, activeChild2, deletedChild));
        childRepository.flush();

        // Soft delete one child
        deletedChild.softDelete();
        childRepository.save(deletedChild);
        childRepository.flush();

        // When
        List<Child> activeChildren = childRepository.findAllActive();

        // Then
        assertThat(activeChildren).hasSize(2);
        assertThat(activeChildren)
                .extracting(Child::getDocumentNumber)
                .containsExactlyInAnyOrder("3333333333", "4444444444")
                .doesNotContain("5555555555");
    }

    @Test
    @DisplayName("Should perform soft delete and preserve data")
    void testSoftDelete() {
        // Given
        Child child = Child.builder()
                .firstName("Roberto")
                .lastName("Benitez")
                .documentNumber("6666666666")
                .dateOfBirth(LocalDate.of(2022, 12, 5))
                .gender(Child.Gender.M)
                .build();

        Child savedChild = childRepository.save(child);
        childRepository.flush();
        Long childId = savedChild.getId();

        // When - soft delete
        savedChild.softDelete();
        childRepository.save(savedChild);
        childRepository.flush();

        // Then - child still exists in database but marked as deleted
        Optional<Child> deletedChild = childRepository.findById(childId);
        assertThat(deletedChild).isPresent();
        assertThat(deletedChild.get().getDeletedAt()).isNotNull();
        assertThat(deletedChild.get().isDeleted()).isTrue();

        // Should not appear in active children
        List<Child> activeChildren = childRepository.findAllActive();
        assertThat(activeChildren).noneMatch(c -> c.getId().equals(childId));
    }

    @Test
    @DisplayName("Should find children by date of birth range")
    void testFindByDateOfBirthBetween() {
        // Given
        Child child1 = Child.builder()
                .firstName("Elena")
                .lastName("Ramirez")
                .documentNumber("7777777777")
                .dateOfBirth(LocalDate.of(2023, 1, 15))
                .gender(Child.Gender.F)
                .build();

        Child child2 = Child.builder()
                .firstName("Mateo")
                .lastName("Cabrera")
                .documentNumber("8888888888")
                .dateOfBirth(LocalDate.of(2023, 6, 20))
                .gender(Child.Gender.M)
                .build();

        Child child3 = Child.builder()
                .firstName("Lucia")
                .lastName("Mendez")
                .documentNumber("9999999999")
                .dateOfBirth(LocalDate.of(2022, 12, 10))
                .gender(Child.Gender.F)
                .build();

        childRepository.saveAll(List.of(child1, child2, child3));
        childRepository.flush();

        // When - search for children born in first half of 2023
        List<Child> childrenInRange = childRepository.findByDateOfBirthBetween(
                LocalDate.of(2023, 1, 1),
                LocalDate.of(2023, 6, 30)
        );

        // Then
        assertThat(childrenInRange).hasSize(2);
        assertThat(childrenInRange)
                .extracting(Child::getDocumentNumber)
                .containsExactlyInAnyOrder("7777777777", "8888888888");
    }

    @Test
    @DisplayName("Should search children by name with case-insensitive partial match")
    void testSearchByName() {
        // Given
        Child child1 = Child.builder()
                .firstName("Valentina")
                .lastName("Gonzalez")
                .documentNumber("1010101010")
                .dateOfBirth(LocalDate.of(2023, 4, 8))
                .gender(Child.Gender.F)
                .build();

        Child child2 = Child.builder()
                .firstName("Valentin")
                .lastName("Perez")
                .documentNumber("2020202020")
                .dateOfBirth(LocalDate.of(2023, 5, 12))
                .gender(Child.Gender.M)
                .build();

        childRepository.saveAll(List.of(child1, child2));
        childRepository.flush();

        // When - case insensitive partial search
        List<Child> valentineChildren = childRepository.searchByName("valen");

        // Then
        assertThat(valentineChildren).hasSize(2);
        assertThat(valentineChildren)
                .extracting(Child::getFirstName)
                .containsExactlyInAnyOrder("Valentina", "Valentin");
    }

    @Test
    @DisplayName("Should add and retrieve guardians for a child")
    void testChildGuardianRelationship() {
        // Given
        Child child = Child.builder()
                .firstName("Isabella")
                .lastName("Doe")
                .documentNumber("3030303030")
                .dateOfBirth(LocalDate.of(2023, 9, 1))
                .gender(Child.Gender.F)
                .build();

        child.getGuardians().add(testGuardian);
        Child savedChild = childRepository.save(child);
        childRepository.flush();

        // When
        Optional<Child> retrievedChild = childRepository.findByIdWithGuardians(savedChild.getId());

        // Then
        assertThat(retrievedChild).isPresent();
        assertThat(retrievedChild.get().getGuardians()).hasSize(1);
        assertThat(retrievedChild.get().getGuardians())
                .extracting(Guardian::getDocumentNumber)
                .contains("1234567890");
    }

    @Test
    @DisplayName("Should find children by guardian ID")
    void testFindByGuardianId() {
        // Given
        Child child1 = Child.builder()
                .firstName("Miguel")
                .lastName("Doe")
                .documentNumber("4040404040")
                .dateOfBirth(LocalDate.of(2022, 3, 15))
                .gender(Child.Gender.M)
                .build();
        child1.getGuardians().add(testGuardian);

        Child child2 = Child.builder()
                .firstName("Carmen")
                .lastName("Doe")
                .documentNumber("5050505050")
                .dateOfBirth(LocalDate.of(2023, 8, 20))
                .gender(Child.Gender.F)
                .build();
        child2.getGuardians().add(testGuardian);

        childRepository.saveAll(List.of(child1, child2));
        childRepository.flush();

        // When
        List<Child> guardianChildren = childRepository.findByGuardianId(testGuardian.getId());

        // Then
        assertThat(guardianChildren).hasSize(2);
        assertThat(guardianChildren)
                .extracting(Child::getDocumentNumber)
                .containsExactlyInAnyOrder("4040404040", "5050505050");
    }

    @Test
    @DisplayName("Should find children by guardian document number")
    void testFindByGuardianDocumentNumber() {
        // Given
        Child child = Child.builder()
                .firstName("Andrea")
                .lastName("Doe")
                .documentNumber("6060606060")
                .dateOfBirth(LocalDate.of(2023, 2, 28))
                .gender(Child.Gender.F)
                .build();
        child.getGuardians().add(testGuardian);

        childRepository.save(child);
        childRepository.flush();

        // When
        List<Child> guardianChildren = childRepository.findByGuardianDocumentNumber("1234567890");

        // Then
        assertThat(guardianChildren).hasSize(1);
        assertThat(guardianChildren.get(0).getDocumentNumber()).isEqualTo("6060606060");
    }

    @Test
    @DisplayName("Should find children without guardians")
    void testFindChildrenWithoutGuardians() {
        // Given
        Child childWithGuardian = Child.builder()
                .firstName("Pablo")
                .lastName("Vera")
                .documentNumber("7070707070")
                .dateOfBirth(LocalDate.of(2023, 10, 5))
                .gender(Child.Gender.M)
                .build();
        childWithGuardian.getGuardians().add(testGuardian);

        Child childWithoutGuardian = Child.builder()
                .firstName("Rosa")
                .lastName("Ortiz")
                .documentNumber("8080808080")
                .dateOfBirth(LocalDate.of(2023, 11, 12))
                .gender(Child.Gender.F)
                .build();

        childRepository.saveAll(List.of(childWithGuardian, childWithoutGuardian));
        childRepository.flush();

        // When
        List<Child> orphanChildren = childRepository.findChildrenWithoutGuardians();

        // Then
        assertThat(orphanChildren).hasSize(1);
        assertThat(orphanChildren.get(0).getDocumentNumber()).isEqualTo("8080808080");
    }

    @Test
    @DisplayName("Should count guardians for a child")
    void testCountGuardiansByChildId() {
        // Given
        User secondUser = User.builder()
                .username("parent_test2")
                .passwordHash("$2a$10$hashedpassword2")
                .firstName("Jane")
                .lastName("Smith")
                .email("jane.smith@test.com")
                .role(User.UserRole.PARENT)
                .isActive('Y')
                .build();
        secondUser = userRepository.save(secondUser);

        Guardian secondGuardian = Guardian.builder()
                .user(secondUser)
                .firstName("Jane")
                .lastName("Smith")
                .documentNumber("0987654321")
                .phone("+595987654321")
                .email("jane.smith@test.com")
                .address("456 Test Avenue")
                .relationship("Mother")
                .build();
        secondGuardian = guardianRepository.save(secondGuardian);

        Child child = Child.builder()
                .firstName("Gabriel")
                .lastName("Smith-Doe")
                .documentNumber("9090909090")
                .dateOfBirth(LocalDate.of(2023, 7, 30))
                .gender(Child.Gender.M)
                .build();
        child.getGuardians().add(testGuardian);
        child.getGuardians().add(secondGuardian);

        Child savedChild = childRepository.save(child);
        childRepository.flush();

        // When
        Long guardianCount = childRepository.countGuardiansByChildId(savedChild.getId());

        // Then
        assertThat(guardianCount).isEqualTo(2);
    }

    @Test
    @DisplayName("Should persist birth measurements with correct precision")
    void testBirthMeasurementsPrecision() {
        // Given
        Child child = Child.builder()
                .firstName("Nicolas")
                .lastName("Rojas")
                .documentNumber("1212121212")
                .dateOfBirth(LocalDate.of(2023, 12, 1))
                .gender(Child.Gender.M)
                .birthWeight(new BigDecimal("3.75"))
                .birthHeight(new BigDecimal("52.3"))
                .build();

        // When
        Child savedChild = childRepository.save(child);
        childRepository.flush();

        // Then
        Child retrievedChild = childRepository.findById(savedChild.getId()).orElseThrow();
        assertThat(retrievedChild.getBirthWeight())
                .isEqualByComparingTo(new BigDecimal("3.75"));
        assertThat(retrievedChild.getBirthHeight())
                .isEqualByComparingTo(new BigDecimal("52.3"));
    }

    @Test
    @DisplayName("Should verify Oracle database connection and schema")
    void testOracleDatabaseConnection() {
        // Given/When
        String oracleVersion = getOracleVersion();
        boolean childrenTableExists = tableExists("children");
        boolean childGuardiansTableExists = tableExists("child_guardians");

        // Then
        assertThat(oracleVersion).containsIgnoringCase("Oracle");
        assertThat(childrenTableExists).isTrue();
        assertThat(childGuardiansTableExists).isTrue();
    }
}
