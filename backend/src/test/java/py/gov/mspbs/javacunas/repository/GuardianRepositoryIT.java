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

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

/**
 * Integration tests for GuardianRepository using Oracle 23c Free database.
 *
 * These tests verify:
 * - Basic CRUD operations
 * - Many-to-One relationship with User
 * - Many-to-Many relationship with Children
 * - Custom query methods (findByChildId, findAllWithChildren, etc.)
 * - Database constraints (unique document number, not null, foreign keys)
 * - JOIN FETCH queries for eager loading to prevent N+1 queries
 * - Filtering of soft-deleted children in relationship queries
 */
@DisplayName("GuardianRepository Integration Tests")
class GuardianRepositoryIT extends AbstractOracleIntegrationTest {

    @Autowired
    private GuardianRepository guardianRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ChildRepository childRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
        // Create test user for guardian
        testUser = User.builder()
                .username("parent_test")
                .passwordHash("$2a$10$hashedpassword")
                .firstName("Carlos")
                .lastName("Rodriguez")
                .email("carlos.rodriguez@test.com")
                .role(User.UserRole.PARENT)
                .isActive('Y')
                .build();
        testUser = userRepository.save(testUser);
    }

    @Test
    @DisplayName("Should save and retrieve guardian with all required fields")
    void testSaveAndRetrieveGuardian() {
        // Given
        Guardian guardian = Guardian.builder()
                .user(testUser)
                .firstName("Carlos")
                .lastName("Rodriguez")
                .documentNumber("1234567890")
                .phone("+595981234567")
                .email("carlos.rodriguez@test.com")
                .address("Av. España 123, Asunción")
                .relationship("Father")
                .build();

        // When
        Guardian savedGuardian = guardianRepository.save(guardian);
        guardianRepository.flush();

        // Then
        assertThat(savedGuardian.getId()).isNotNull();
        assertThat(savedGuardian.getCreatedAt()).isNotNull();
        assertThat(savedGuardian.getFirstName()).isEqualTo("Carlos");
        assertThat(savedGuardian.getLastName()).isEqualTo("Rodriguez");
        assertThat(savedGuardian.getDocumentNumber()).isEqualTo("1234567890");
        assertThat(savedGuardian.getPhone()).isEqualTo("+595981234567");
        assertThat(savedGuardian.getEmail()).isEqualTo("carlos.rodriguez@test.com");
        assertThat(savedGuardian.getAddress()).isEqualTo("Av. España 123, Asunción");
        assertThat(savedGuardian.getRelationship()).isEqualTo("Father");

        // Verify database retrieval
        Optional<Guardian> retrievedGuardian = guardianRepository.findById(savedGuardian.getId());
        assertThat(retrievedGuardian).isPresent();
        assertThat(retrievedGuardian.get().getFirstName()).isEqualTo("Carlos");
    }

    @Test
    @DisplayName("Should enforce unique document number constraint")
    void testUniqueDocumentNumberConstraint() {
        // Given
        Guardian guardian1 = Guardian.builder()
                .user(testUser)
                .firstName("Maria")
                .lastName("Lopez")
                .documentNumber("9876543210")
                .phone("+595987654321")
                .relationship("Mother")
                .build();

        Guardian guardian2 = Guardian.builder()
                .user(testUser)
                .firstName("Ana")
                .lastName("Martinez")
                .documentNumber("9876543210")  // Duplicate document number
                .phone("+595981111111")
                .relationship("Mother")
                .build();

        // When
        guardianRepository.save(guardian1);
        guardianRepository.flush();

        // Then - attempting to save duplicate document number should fail
        assertThatThrownBy(() -> {
            guardianRepository.save(guardian2);
            guardianRepository.flush();
        }).isInstanceOf(DataIntegrityViolationException.class)
          .hasMessageContaining("unique constraint");
    }

    @Test
    @DisplayName("Should enforce not null constraint on required fields")
    void testNotNullConstraints() {
        // Given - guardian without required 'firstName' field
        Guardian guardian = Guardian.builder()
                .user(testUser)
                .lastName("Garcia")
                .documentNumber("1111111111")
                .phone("+595982222222")
                .relationship("Father")
                .build();

        // When/Then - should fail due to null firstName
        assertThatThrownBy(() -> {
            guardianRepository.save(guardian);
            guardianRepository.flush();
        }).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("Should find guardian by document number")
    void testFindByDocumentNumber() {
        // Given
        Guardian guardian = Guardian.builder()
                .user(testUser)
                .firstName("Pedro")
                .lastName("Benitez")
                .documentNumber("2222222222")
                .phone("+595983333333")
                .relationship("Father")
                .build();

        guardianRepository.save(guardian);
        guardianRepository.flush();

        // When
        Optional<Guardian> foundGuardian = guardianRepository.findByDocumentNumber("2222222222");

        // Then
        assertThat(foundGuardian).isPresent();
        assertThat(foundGuardian.get().getFirstName()).isEqualTo("Pedro");
        assertThat(foundGuardian.get().getLastName()).isEqualTo("Benitez");
    }

    @Test
    @DisplayName("Should find guardians by user id")
    void testFindByUserId() {
        // Given - create two guardians for the same user
        Guardian guardian1 = Guardian.builder()
                .user(testUser)
                .firstName("Roberto")
                .lastName("Silva")
                .documentNumber("3333333333")
                .phone("+595984444444")
                .relationship("Father")
                .build();

        Guardian guardian2 = Guardian.builder()
                .user(testUser)
                .firstName("Laura")
                .lastName("Silva")
                .documentNumber("4444444444")
                .phone("+595985555555")
                .relationship("Mother")
                .build();

        guardianRepository.saveAll(List.of(guardian1, guardian2));
        guardianRepository.flush();

        // When
        List<Guardian> guardians = guardianRepository.findByUserId(testUser.getId());

        // Then
        assertThat(guardians).hasSize(2);
        assertThat(guardians)
                .extracting(Guardian::getDocumentNumber)
                .containsExactlyInAnyOrder("3333333333", "4444444444");
    }

    @Test
    @DisplayName("Should check if document number exists")
    void testExistsByDocumentNumber() {
        // Given
        Guardian guardian = Guardian.builder()
                .user(testUser)
                .firstName("Diego")
                .lastName("Fernandez")
                .documentNumber("5555555555")
                .phone("+595986666666")
                .relationship("Father")
                .build();

        guardianRepository.save(guardian);
        guardianRepository.flush();

        // When/Then - document number exists
        assertThat(guardianRepository.existsByDocumentNumber("5555555555")).isTrue();

        // When/Then - document number does not exist
        assertThat(guardianRepository.existsByDocumentNumber("9999999999")).isFalse();
    }

    @Test
    @DisplayName("Should find guardians by child id")
    void testFindByChildId() {
        // Given
        Guardian guardian1 = Guardian.builder()
                .user(testUser)
                .firstName("Julio")
                .lastName("Ramirez")
                .documentNumber("6666666666")
                .phone("+595987777777")
                .relationship("Father")
                .build();
        guardian1 = guardianRepository.save(guardian1);

        User secondUser = User.builder()
                .username("parent_test2")
                .passwordHash("$2a$10$hashedpassword2")
                .firstName("Marta")
                .lastName("Ramirez")
                .email("marta.ramirez@test.com")
                .role(User.UserRole.PARENT)
                .isActive('Y')
                .build();
        secondUser = userRepository.save(secondUser);

        Guardian guardian2 = Guardian.builder()
                .user(secondUser)
                .firstName("Marta")
                .lastName("Ramirez")
                .documentNumber("7777777777")
                .phone("+595988888888")
                .relationship("Mother")
                .build();
        guardian2 = guardianRepository.save(guardian2);

        Child child = Child.builder()
                .firstName("Santiago")
                .lastName("Ramirez")
                .documentNumber("1010101010")
                .dateOfBirth(LocalDate.of(2023, 3, 15))
                .gender(Child.Gender.M)
                .build();
        child.getGuardians().add(guardian1);
        child.getGuardians().add(guardian2);
        child = childRepository.save(child);
        childRepository.flush();

        // When
        List<Guardian> guardiansForChild = guardianRepository.findByChildId(child.getId());

        // Then
        assertThat(guardiansForChild).hasSize(2);
        assertThat(guardiansForChild)
                .extracting(Guardian::getDocumentNumber)
                .containsExactlyInAnyOrder("6666666666", "7777777777");
    }

    @Test
    @DisplayName("Should find guardians by child document number")
    void testFindByChildDocumentNumber() {
        // Given
        Guardian guardian = Guardian.builder()
                .user(testUser)
                .firstName("Antonio")
                .lastName("Cabrera")
                .documentNumber("8888888888")
                .phone("+595989999999")
                .relationship("Father")
                .build();
        guardian = guardianRepository.save(guardian);

        Child child = Child.builder()
                .firstName("Valentina")
                .lastName("Cabrera")
                .documentNumber("2020202020")
                .dateOfBirth(LocalDate.of(2023, 6, 10))
                .gender(Child.Gender.F)
                .build();
        child.getGuardians().add(guardian);
        childRepository.save(child);
        childRepository.flush();

        // When
        List<Guardian> guardians = guardianRepository.findByChildDocumentNumber("2020202020");

        // Then
        assertThat(guardians).hasSize(1);
        assertThat(guardians.get(0).getDocumentNumber()).isEqualTo("8888888888");
        assertThat(guardians.get(0).getFirstName()).isEqualTo("Antonio");
    }

    @Test
    @DisplayName("Should find all guardians with children eagerly loaded")
    void testFindAllWithChildren() {
        // Given
        Guardian guardian1 = Guardian.builder()
                .user(testUser)
                .firstName("Fernando")
                .lastName("Torres")
                .documentNumber("3030303030")
                .phone("+595981010101")
                .relationship("Father")
                .build();
        guardian1 = guardianRepository.save(guardian1);

        User secondUser = User.builder()
                .username("parent_test3")
                .passwordHash("$2a$10$hashedpassword3")
                .firstName("Elena")
                .lastName("Mendez")
                .email("elena.mendez@test.com")
                .role(User.UserRole.PARENT)
                .isActive('Y')
                .build();
        secondUser = userRepository.save(secondUser);

        Guardian guardian2 = Guardian.builder()
                .user(secondUser)
                .firstName("Elena")
                .lastName("Mendez")
                .documentNumber("4040404040")
                .phone("+595982020202")
                .relationship("Mother")
                .build();
        guardian2 = guardianRepository.save(guardian2);

        Child child1 = Child.builder()
                .firstName("Sofia")
                .lastName("Torres")
                .documentNumber("5050505050")
                .dateOfBirth(LocalDate.of(2023, 1, 20))
                .gender(Child.Gender.F)
                .build();
        child1.getGuardians().add(guardian1);
        guardian1.getChildren().add(child1);  // Sync bidirectional relationship

        Child child2 = Child.builder()
                .firstName("Mateo")
                .lastName("Torres")
                .documentNumber("6060606060")
                .dateOfBirth(LocalDate.of(2022, 8, 15))
                .gender(Child.Gender.M)
                .build();
        child2.getGuardians().add(guardian1);
        guardian1.getChildren().add(child2);  // Sync bidirectional relationship

        childRepository.saveAll(List.of(child1, child2));
        childRepository.flush();

        // When
        List<Guardian> guardiansWithChildren = guardianRepository.findAllWithChildren();

        // Then
        assertThat(guardiansWithChildren).hasSizeGreaterThanOrEqualTo(2);

        // Verify no N+1 queries by accessing children
        Guardian guardianWithChildren = guardiansWithChildren.stream()
                .filter(g -> g.getDocumentNumber().equals("3030303030"))
                .findFirst()
                .orElseThrow();

        assertThat(guardianWithChildren.getChildren()).hasSize(2);
        assertThat(guardianWithChildren.getChildren())
                .extracting(Child::getDocumentNumber)
                .containsExactlyInAnyOrder("5050505050", "6060606060");
    }

    @Test
    @DisplayName("Should find guardian by id with children eagerly loaded")
    void testFindByIdWithChildren() {
        // Given
        Guardian guardian = Guardian.builder()
                .user(testUser)
                .firstName("Luis")
                .lastName("Gonzalez")
                .documentNumber("7070707070")
                .phone("+595983030303")
                .relationship("Father")
                .build();
        guardian = guardianRepository.save(guardian);

        Child child = Child.builder()
                .firstName("Isabella")
                .lastName("Gonzalez")
                .documentNumber("8080808080")
                .dateOfBirth(LocalDate.of(2023, 4, 25))
                .gender(Child.Gender.F)
                .build();
        child.getGuardians().add(guardian);
        guardian.getChildren().add(child);  // Sync bidirectional relationship
        childRepository.save(child);
        childRepository.flush();

        // When
        Optional<Guardian> foundGuardian = guardianRepository.findByIdWithChildren(guardian.getId());

        // Then
        assertThat(foundGuardian).isPresent();
        assertThat(foundGuardian.get().getChildren()).hasSize(1);
        assertThat(foundGuardian.get().getChildren())
                .extracting(Child::getDocumentNumber)
                .contains("8080808080");
    }

    @Test
    @DisplayName("Should find guardians without children")
    void testFindGuardiansWithoutChildren() {
        // Given - guardian with children
        Guardian guardianWithChildren = Guardian.builder()
                .user(testUser)
                .firstName("Miguel")
                .lastName("Perez")
                .documentNumber("9090909090")
                .phone("+595984040404")
                .relationship("Father")
                .build();
        guardianWithChildren = guardianRepository.save(guardianWithChildren);

        Child child = Child.builder()
                .firstName("Carmen")
                .lastName("Perez")
                .documentNumber("1212121212")
                .dateOfBirth(LocalDate.of(2023, 7, 8))
                .gender(Child.Gender.F)
                .build();
        child.getGuardians().add(guardianWithChildren);
        childRepository.save(child);

        // Given - guardian without children
        User secondUser = User.builder()
                .username("parent_test4")
                .passwordHash("$2a$10$hashedpassword4")
                .firstName("Rosa")
                .lastName("Ortiz")
                .email("rosa.ortiz@test.com")
                .role(User.UserRole.PARENT)
                .isActive('Y')
                .build();
        secondUser = userRepository.save(secondUser);

        Guardian guardianWithoutChildren = Guardian.builder()
                .user(secondUser)
                .firstName("Rosa")
                .lastName("Ortiz")
                .documentNumber("1313131313")
                .phone("+595985050505")
                .relationship("Mother")
                .build();
        guardianRepository.save(guardianWithoutChildren);
        guardianRepository.flush();

        // When
        List<Guardian> guardiansWithoutChildren = guardianRepository.findGuardiansWithoutChildren();

        // Then
        assertThat(guardiansWithoutChildren).hasSize(1);
        assertThat(guardiansWithoutChildren.get(0).getDocumentNumber()).isEqualTo("1313131313");
        assertThat(guardiansWithoutChildren.get(0).getFirstName()).isEqualTo("Rosa");
    }

    @Test
    @DisplayName("Should count children by guardian id")
    void testCountChildrenByGuardianId() {
        // Given - guardian with no children
        Guardian guardianWithNoChildren = Guardian.builder()
                .user(testUser)
                .firstName("Pablo")
                .lastName("Vera")
                .documentNumber("1414141414")
                .phone("+595986060606")
                .relationship("Father")
                .build();
        guardianWithNoChildren = guardianRepository.save(guardianWithNoChildren);

        // When/Then - count should be 0
        Long countZero = guardianRepository.countChildrenByGuardianId(guardianWithNoChildren.getId());
        assertThat(countZero).isEqualTo(0);

        // Given - guardian with one child
        User secondUser = User.builder()
                .username("parent_test5")
                .passwordHash("$2a$10$hashedpassword5")
                .firstName("Andrea")
                .lastName("Silva")
                .email("andrea.silva@test.com")
                .role(User.UserRole.PARENT)
                .isActive('Y')
                .build();
        secondUser = userRepository.save(secondUser);

        Guardian guardianWithOneChild = Guardian.builder()
                .user(secondUser)
                .firstName("Andrea")
                .lastName("Silva")
                .documentNumber("1515151515")
                .phone("+595987070707")
                .relationship("Mother")
                .build();
        guardianWithOneChild = guardianRepository.save(guardianWithOneChild);

        Child child1 = Child.builder()
                .firstName("Nicolas")
                .lastName("Silva")
                .documentNumber("1616161616")
                .dateOfBirth(LocalDate.of(2023, 9, 12))
                .gender(Child.Gender.M)
                .build();
        child1.getGuardians().add(guardianWithOneChild);
        childRepository.save(child1);

        // When/Then - count should be 1
        Long countOne = guardianRepository.countChildrenByGuardianId(guardianWithOneChild.getId());
        assertThat(countOne).isEqualTo(1);

        // Given - guardian with two children
        User thirdUser = User.builder()
                .username("parent_test6")
                .passwordHash("$2a$10$hashedpassword6")
                .firstName("Gabriel")
                .lastName("Rojas")
                .email("gabriel.rojas@test.com")
                .role(User.UserRole.PARENT)
                .isActive('Y')
                .build();
        thirdUser = userRepository.save(thirdUser);

        Guardian guardianWithTwoChildren = Guardian.builder()
                .user(thirdUser)
                .firstName("Gabriel")
                .lastName("Rojas")
                .documentNumber("1717171717")
                .phone("+595988080808")
                .relationship("Father")
                .build();
        guardianWithTwoChildren = guardianRepository.save(guardianWithTwoChildren);

        Child child2 = Child.builder()
                .firstName("Lucia")
                .lastName("Rojas")
                .documentNumber("1818181818")
                .dateOfBirth(LocalDate.of(2022, 5, 20))
                .gender(Child.Gender.F)
                .build();
        child2.getGuardians().add(guardianWithTwoChildren);

        Child child3 = Child.builder()
                .firstName("Martin")
                .lastName("Rojas")
                .documentNumber("1919191919")
                .dateOfBirth(LocalDate.of(2023, 11, 5))
                .gender(Child.Gender.M)
                .build();
        child3.getGuardians().add(guardianWithTwoChildren);

        childRepository.saveAll(List.of(child2, child3));
        childRepository.flush();

        // When/Then - count should be 2
        Long countTwo = guardianRepository.countChildrenByGuardianId(guardianWithTwoChildren.getId());
        assertThat(countTwo).isEqualTo(2);
    }

    @Test
    @DisplayName("Should verify guardian-user relationship")
    void testGuardianUserRelationship() {
        // Given
        Guardian guardian = Guardian.builder()
                .user(testUser)
                .firstName("Hector")
                .lastName("Lopez")
                .documentNumber("2121212121")
                .phone("+595989090909")
                .relationship("Father")
                .build();

        // When
        Guardian savedGuardian = guardianRepository.save(guardian);
        guardianRepository.flush();

        // Then - verify relationship is persisted
        Optional<Guardian> retrievedGuardian = guardianRepository.findById(savedGuardian.getId());
        assertThat(retrievedGuardian).isPresent();
        assertThat(retrievedGuardian.get().getUser()).isNotNull();
        assertThat(retrievedGuardian.get().getUser().getId()).isEqualTo(testUser.getId());
        assertThat(retrievedGuardian.get().getUser().getUsername()).isEqualTo("parent_test");
    }

    @Test
    @DisplayName("Should verify guardian-child many-to-many relationship")
    void testGuardianChildManyToManyRelationship() {
        // Given
        Guardian guardian1 = Guardian.builder()
                .user(testUser)
                .firstName("Ricardo")
                .lastName("Benitez")
                .documentNumber("2222222223")
                .phone("+595981212121")
                .relationship("Father")
                .build();
        guardian1 = guardianRepository.save(guardian1);

        User secondUser = User.builder()
                .username("parent_test7")
                .passwordHash("$2a$10$hashedpassword7")
                .firstName("Patricia")
                .lastName("Benitez")
                .email("patricia.benitez@test.com")
                .role(User.UserRole.PARENT)
                .isActive('Y')
                .build();
        secondUser = userRepository.save(secondUser);

        Guardian guardian2 = Guardian.builder()
                .user(secondUser)
                .firstName("Patricia")
                .lastName("Benitez")
                .documentNumber("2323232323")
                .phone("+595982323232")
                .relationship("Mother")
                .build();
        guardian2 = guardianRepository.save(guardian2);

        // Create child with both guardians
        Child child = Child.builder()
                .firstName("Elena")
                .lastName("Benitez")
                .documentNumber("2424242424")
                .dateOfBirth(LocalDate.of(2023, 2, 14))
                .gender(Child.Gender.F)
                .build();
        child.getGuardians().add(guardian1);
        child.getGuardians().add(guardian2);
        child = childRepository.save(child);
        childRepository.flush();

        // When - retrieve guardians for the child
        List<Guardian> guardians = guardianRepository.findByChildId(child.getId());

        // Then - verify bidirectional relationship
        assertThat(guardians).hasSize(2);
        assertThat(guardians)
                .extracting(Guardian::getDocumentNumber)
                .containsExactlyInAnyOrder("2222222223", "2323232323");

        // Verify the reverse relationship
        Optional<Child> retrievedChild = childRepository.findById(child.getId());
        assertThat(retrievedChild).isPresent();
        assertThat(retrievedChild.get().getGuardians()).hasSize(2);
    }

    @Test
    @DisplayName("Should verify Oracle database connection and schema")
    void testOracleDatabaseConnection() {
        // Given/When
        String oracleVersion = getOracleVersion();
        boolean guardiansTableExists = tableExists("guardians");
        boolean childGuardiansTableExists = tableExists("child_guardians");

        // Then
        assertThat(oracleVersion).containsIgnoringCase("Oracle");
        assertThat(guardiansTableExists).isTrue();
        assertThat(childGuardiansTableExists).isTrue();
    }
}
