package py.gov.mspbs.javacunas.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import py.gov.mspbs.javacunas.BaseUnitTest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for Child entity.
 * Tests Lombok annotations, lifecycle callbacks, soft delete functionality,
 * and bidirectional relationship handling.
 */
@DisplayName("Child Entity Tests")
class ChildTest extends BaseUnitTest {

    @Nested
    @DisplayName("Lombok EqualsAndHashCode Tests")
    class EqualsAndHashCodeTests {

        @Test
        @DisplayName("Should exclude guardians from equals comparison")
        void shouldExcludeGuardiansFromEqualsComparison() {
            // Given
            Child child1 = Child.builder()
                    .id(1L)
                    .firstName("Juan")
                    .lastName("Pérez")
                    .documentNumber("1234567")
                    .dateOfBirth(LocalDate.of(2023, 1, 15))
                    .gender(Child.Gender.M)
                    .build();

            Child child2 = Child.builder()
                    .id(1L)
                    .firstName("Juan")
                    .lastName("Pérez")
                    .documentNumber("1234567")
                    .dateOfBirth(LocalDate.of(2023, 1, 15))
                    .gender(Child.Gender.M)
                    .build();

            Guardian guardian = Guardian.builder()
                    .id(1L)
                    .firstName("Maria")
                    .lastName("Pérez")
                    .documentNumber("7654321")
                    .phone("0981234567")
                    .relationship("Mother")
                    .build();

            // When
            child1.getGuardians().add(guardian);
            // child2 has no guardians

            // Then - Children should still be equal despite different guardian sets
            assertThat(child1).isEqualTo(child2);
        }

        @Test
        @DisplayName("Should exclude guardians from hashCode calculation")
        void shouldExcludeGuardiansFromHashCode() {
            // Given
            Child child1 = Child.builder()
                    .id(1L)
                    .firstName("Juan")
                    .lastName("Pérez")
                    .documentNumber("1234567")
                    .dateOfBirth(LocalDate.of(2023, 1, 15))
                    .gender(Child.Gender.M)
                    .build();

            Child child2 = Child.builder()
                    .id(1L)
                    .firstName("Juan")
                    .lastName("Pérez")
                    .documentNumber("1234567")
                    .dateOfBirth(LocalDate.of(2023, 1, 15))
                    .gender(Child.Gender.M)
                    .build();

            Guardian guardian = Guardian.builder()
                    .id(1L)
                    .firstName("Maria")
                    .lastName("Pérez")
                    .documentNumber("7654321")
                    .phone("0981234567")
                    .relationship("Mother")
                    .build();

            // When
            child1.getGuardians().add(guardian);
            // child2 has no guardians

            // Then - HashCodes should be equal despite different guardian sets
            assertThat(child1.hashCode()).isEqualTo(child2.hashCode());
        }

        @Test
        @DisplayName("Should not cause StackOverflow when accessing hashCode with bidirectional relationship")
        void shouldNotCauseStackOverflowForHashCodeWithBidirectionalRelationship() {
            // Given
            Child child = Child.builder()
                    .id(1L)
                    .firstName("Juan")
                    .lastName("Pérez")
                    .documentNumber("1234567")
                    .dateOfBirth(LocalDate.of(2023, 1, 15))
                    .gender(Child.Gender.M)
                    .build();

            Guardian guardian = Guardian.builder()
                    .id(1L)
                    .firstName("Maria")
                    .lastName("Pérez")
                    .documentNumber("7654321")
                    .phone("0981234567")
                    .relationship("Mother")
                    .build();

            // When - Set up bidirectional relationship
            child.getGuardians().add(guardian);
            guardian.getChildren().add(child);

            // Then - Should not throw StackOverflowError
            assertThat(child.hashCode()).isNotNull();
            assertThat(guardian.hashCode()).isNotNull();
        }

        @Test
        @DisplayName("Should not cause StackOverflow when accessing equals with bidirectional relationship")
        void shouldNotCauseStackOverflowForEqualsWithBidirectionalRelationship() {
            // Given
            Child child1 = Child.builder()
                    .id(1L)
                    .firstName("Juan")
                    .lastName("Pérez")
                    .documentNumber("1234567")
                    .dateOfBirth(LocalDate.of(2023, 1, 15))
                    .gender(Child.Gender.M)
                    .build();

            Child child2 = Child.builder()
                    .id(1L)
                    .firstName("Juan")
                    .lastName("Pérez")
                    .documentNumber("1234567")
                    .dateOfBirth(LocalDate.of(2023, 1, 15))
                    .gender(Child.Gender.M)
                    .build();

            Guardian guardian = Guardian.builder()
                    .id(1L)
                    .firstName("Maria")
                    .lastName("Pérez")
                    .documentNumber("7654321")
                    .phone("0981234567")
                    .relationship("Mother")
                    .build();

            // When - Set up bidirectional relationship
            child1.getGuardians().add(guardian);
            guardian.getChildren().add(child1);

            // Then - Should not throw StackOverflowError
            assertThat(child1.equals(child2)).isTrue();
        }
    }

    @Nested
    @DisplayName("Lombok ToString Tests")
    class ToStringTests {

        @Test
        @DisplayName("Should exclude guardians from toString representation")
        void shouldExcludeGuardiansFromToString() {
            // Given
            Child child = Child.builder()
                    .id(1L)
                    .firstName("Juan")
                    .lastName("Pérez")
                    .documentNumber("1234567")
                    .dateOfBirth(LocalDate.of(2023, 1, 15))
                    .gender(Child.Gender.M)
                    .bloodType("O+")
                    .birthWeight(new BigDecimal("3.45"))
                    .birthHeight(new BigDecimal("50.5"))
                    .build();

            Guardian guardian = Guardian.builder()
                    .id(1L)
                    .firstName("Maria")
                    .lastName("Pérez")
                    .documentNumber("7654321")
                    .phone("0981234567")
                    .relationship("Mother")
                    .build();

            child.getGuardians().add(guardian);

            // When
            String childString = child.toString();

            // Then - Should contain child properties but not guardians
            assertThat(childString).contains("Juan");
            assertThat(childString).contains("Pérez");
            assertThat(childString).contains("1234567");
            assertThat(childString).contains("O+");
            assertThat(childString).doesNotContain("guardians");
        }

        @Test
        @DisplayName("Should not cause StackOverflow when accessing toString with bidirectional relationship")
        void shouldNotCauseStackOverflowForToStringWithBidirectionalRelationship() {
            // Given
            Child child = Child.builder()
                    .id(1L)
                    .firstName("Juan")
                    .lastName("Pérez")
                    .documentNumber("1234567")
                    .dateOfBirth(LocalDate.of(2023, 1, 15))
                    .gender(Child.Gender.M)
                    .build();

            Guardian guardian = Guardian.builder()
                    .id(1L)
                    .firstName("Maria")
                    .lastName("Pérez")
                    .documentNumber("7654321")
                    .phone("0981234567")
                    .relationship("Mother")
                    .build();

            // When - Set up bidirectional relationship
            child.getGuardians().add(guardian);
            guardian.getChildren().add(child);

            // Then - Should not throw StackOverflowError
            assertThat(child.toString()).isNotNull().isNotEmpty();
            assertThat(guardian.toString()).isNotNull().isNotEmpty();
        }
    }

    @Nested
    @DisplayName("Lifecycle Callback Tests")
    class LifecycleCallbackTests {

        @Test
        @DisplayName("Should set createdAt and updatedAt on onCreate")
        void shouldSetCreatedAtAndUpdatedAtOnCreate() {
            // Given
            Child child = Child.builder()
                    .firstName("Juan")
                    .lastName("Pérez")
                    .documentNumber("1234567")
                    .dateOfBirth(LocalDate.of(2023, 1, 15))
                    .gender(Child.Gender.M)
                    .build();

            // When
            child.onCreate();

            // Then
            assertThat(child.getCreatedAt()).isNotNull();
            assertThat(child.getUpdatedAt()).isNotNull();

            // Verify timestamps are very close (within 10 milliseconds) since onCreate sets both
            // Note: They may differ slightly due to two separate LocalDateTime.now() calls in onCreate
            long millisBetween = java.time.Duration.between(child.getCreatedAt(), child.getUpdatedAt()).toMillis();
            assertThat(Math.abs(millisBetween)).isLessThanOrEqualTo(10L);

            // Verify timestamps are recent (within the last second)
            LocalDateTime now = LocalDateTime.now();
            assertThat(child.getCreatedAt()).isAfter(now.minusSeconds(1));
            assertThat(child.getCreatedAt()).isBefore(now.plusSeconds(1));
        }

        @Test
        @DisplayName("Should update updatedAt on onUpdate")
        void shouldUpdateUpdatedAtOnUpdate() throws InterruptedException {
            // Given
            Child child = Child.builder()
                    .firstName("Juan")
                    .lastName("Pérez")
                    .documentNumber("1234567")
                    .dateOfBirth(LocalDate.of(2023, 1, 15))
                    .gender(Child.Gender.M)
                    .build();

            child.onCreate();
            LocalDateTime originalCreatedAt = child.getCreatedAt();
            LocalDateTime originalUpdatedAt = child.getUpdatedAt();

            // Small delay to ensure time difference
            Thread.sleep(10);

            // When
            child.onUpdate();

            // Then
            assertThat(child.getCreatedAt()).isEqualTo(originalCreatedAt);
            assertThat(child.getUpdatedAt()).isNotNull();
            assertThat(child.getUpdatedAt()).isAfter(originalUpdatedAt);
        }

        @Test
        @DisplayName("Should not modify createdAt on onUpdate")
        void shouldNotModifyCreatedAtOnUpdate() throws InterruptedException {
            // Given
            Child child = Child.builder()
                    .firstName("Juan")
                    .lastName("Pérez")
                    .documentNumber("1234567")
                    .dateOfBirth(LocalDate.of(2023, 1, 15))
                    .gender(Child.Gender.M)
                    .build();

            child.onCreate();
            LocalDateTime originalCreatedAt = child.getCreatedAt();

            Thread.sleep(10);

            // When
            child.onUpdate();

            // Then
            assertThat(child.getCreatedAt()).isEqualTo(originalCreatedAt);
        }
    }

    @Nested
    @DisplayName("Soft Delete Tests")
    class SoftDeleteTests {

        @Test
        @DisplayName("Should set deletedAt when softDelete is called")
        void shouldSetDeletedAtWhenSoftDeleteIsCalled() {
            // Given
            Child child = Child.builder()
                    .id(1L)
                    .firstName("Juan")
                    .lastName("Pérez")
                    .documentNumber("1234567")
                    .dateOfBirth(LocalDate.of(2023, 1, 15))
                    .gender(Child.Gender.M)
                    .build();

            assertThat(child.getDeletedAt()).isNull();

            LocalDateTime beforeDelete = LocalDateTime.now().minusSeconds(1);

            // When
            child.softDelete();

            // Then
            LocalDateTime afterDelete = LocalDateTime.now().plusSeconds(1);

            assertThat(child.getDeletedAt()).isNotNull();
            assertThat(child.getDeletedAt()).isAfter(beforeDelete);
            assertThat(child.getDeletedAt()).isBefore(afterDelete);
        }

        @Test
        @DisplayName("Should return true for isDeleted when deletedAt is set")
        void shouldReturnTrueForIsDeletedWhenDeletedAtIsSet() {
            // Given
            Child child = Child.builder()
                    .id(1L)
                    .firstName("Juan")
                    .lastName("Pérez")
                    .documentNumber("1234567")
                    .dateOfBirth(LocalDate.of(2023, 1, 15))
                    .gender(Child.Gender.M)
                    .build();

            // When
            child.softDelete();

            // Then
            assertThat(child.isDeleted()).isTrue();
            assertThat(child.getDeletedAt()).isNotNull();
        }

        @Test
        @DisplayName("Should return false for isDeleted when deletedAt is null")
        void shouldReturnFalseForIsDeletedWhenDeletedAtIsNull() {
            // Given
            Child child = Child.builder()
                    .id(1L)
                    .firstName("Juan")
                    .lastName("Pérez")
                    .documentNumber("1234567")
                    .dateOfBirth(LocalDate.of(2023, 1, 15))
                    .gender(Child.Gender.M)
                    .build();

            // When & Then
            assertThat(child.isDeleted()).isFalse();
            assertThat(child.getDeletedAt()).isNull();
        }

        @Test
        @DisplayName("Should allow multiple calls to softDelete")
        void shouldAllowMultipleCallsToSoftDelete() throws InterruptedException {
            // Given
            Child child = Child.builder()
                    .id(1L)
                    .firstName("Juan")
                    .lastName("Pérez")
                    .documentNumber("1234567")
                    .dateOfBirth(LocalDate.of(2023, 1, 15))
                    .gender(Child.Gender.M)
                    .build();

            // When
            child.softDelete();
            LocalDateTime firstDeletedAt = child.getDeletedAt();

            Thread.sleep(10);

            child.softDelete();
            LocalDateTime secondDeletedAt = child.getDeletedAt();

            // Then
            assertThat(child.isDeleted()).isTrue();
            assertThat(secondDeletedAt).isAfter(firstDeletedAt);
        }
    }

    @Nested
    @DisplayName("Builder and Default Values Tests")
    class BuilderAndDefaultValuesTests {

        @Test
        @DisplayName("Should initialize guardians collection with empty HashSet by default")
        void shouldInitializeGuardiansCollectionWithEmptyHashSetByDefault() {
            // Given & When
            Child child = Child.builder()
                    .id(1L)
                    .firstName("Juan")
                    .lastName("Pérez")
                    .documentNumber("1234567")
                    .dateOfBirth(LocalDate.of(2023, 1, 15))
                    .gender(Child.Gender.M)
                    .build();

            // Then
            assertThat(child.getGuardians()).isNotNull();
            assertThat(child.getGuardians()).isEmpty();
            assertThat(child.getGuardians()).isInstanceOf(HashSet.class);
        }

        @Test
        @DisplayName("Should allow adding guardians without StackOverflow")
        void shouldAllowAddingGuardiansWithoutStackOverflow() {
            // Given
            Child child = Child.builder()
                    .id(1L)
                    .firstName("Juan")
                    .lastName("Pérez")
                    .documentNumber("1234567")
                    .dateOfBirth(LocalDate.of(2023, 1, 15))
                    .gender(Child.Gender.M)
                    .build();

            Guardian guardian1 = Guardian.builder()
                    .id(1L)
                    .firstName("Maria")
                    .lastName("Pérez")
                    .documentNumber("7654321")
                    .phone("0981234567")
                    .relationship("Mother")
                    .build();

            Guardian guardian2 = Guardian.builder()
                    .id(2L)
                    .firstName("José")
                    .lastName("Pérez")
                    .documentNumber("7654322")
                    .phone("0981234568")
                    .relationship("Father")
                    .build();

            // When
            child.getGuardians().add(guardian1);
            child.getGuardians().add(guardian2);

            // Then
            assertThat(child.getGuardians()).hasSize(2);
            assertThat(child.getGuardians()).contains(guardian1, guardian2);
        }

        @Test
        @DisplayName("Should allow building with custom guardians set")
        void shouldAllowBuildingWithCustomGuardiansSet() {
            // Given
            Guardian guardian = Guardian.builder()
                    .id(1L)
                    .firstName("Maria")
                    .lastName("Pérez")
                    .documentNumber("7654321")
                    .phone("0981234567")
                    .relationship("Mother")
                    .build();

            Set<Guardian> guardians = new HashSet<>();
            guardians.add(guardian);

            // When
            Child child = Child.builder()
                    .id(1L)
                    .firstName("Juan")
                    .lastName("Pérez")
                    .documentNumber("1234567")
                    .dateOfBirth(LocalDate.of(2023, 1, 15))
                    .gender(Child.Gender.M)
                    .guardians(guardians)
                    .build();

            // Then
            assertThat(child.getGuardians()).hasSize(1);
            assertThat(child.getGuardians()).contains(guardian);
        }

        @Test
        @DisplayName("Should build child with all properties")
        void shouldBuildChildWithAllProperties() {
            // Given
            LocalDate dateOfBirth = LocalDate.of(2023, 1, 15);
            BigDecimal birthWeight = new BigDecimal("3.45");
            BigDecimal birthHeight = new BigDecimal("50.5");

            // When
            Child child = Child.builder()
                    .id(1L)
                    .firstName("Juan")
                    .lastName("Pérez")
                    .documentNumber("1234567")
                    .dateOfBirth(dateOfBirth)
                    .gender(Child.Gender.M)
                    .bloodType("O+")
                    .birthWeight(birthWeight)
                    .birthHeight(birthHeight)
                    .build();

            // Then
            assertThat(child.getId()).isEqualTo(1L);
            assertThat(child.getFirstName()).isEqualTo("Juan");
            assertThat(child.getLastName()).isEqualTo("Pérez");
            assertThat(child.getDocumentNumber()).isEqualTo("1234567");
            assertThat(child.getDateOfBirth()).isEqualTo(dateOfBirth);
            assertThat(child.getGender()).isEqualTo(Child.Gender.M);
            assertThat(child.getBloodType()).isEqualTo("O+");
            assertThat(child.getBirthWeight()).isEqualByComparingTo(birthWeight);
            assertThat(child.getBirthHeight()).isEqualByComparingTo(birthHeight);
            assertThat(child.getGuardians()).isNotNull().isEmpty();
        }
    }

    @Nested
    @DisplayName("Gender Enum Tests")
    class GenderEnumTests {

        @Test
        @DisplayName("Should support all gender values")
        void shouldSupportAllGenderValues() {
            // Given & When
            Child maleChild = Child.builder()
                    .firstName("Juan")
                    .lastName("Pérez")
                    .documentNumber("1234567")
                    .dateOfBirth(LocalDate.of(2023, 1, 15))
                    .gender(Child.Gender.M)
                    .build();

            Child femaleChild = Child.builder()
                    .firstName("María")
                    .lastName("Pérez")
                    .documentNumber("1234568")
                    .dateOfBirth(LocalDate.of(2023, 1, 15))
                    .gender(Child.Gender.F)
                    .build();

            Child otherChild = Child.builder()
                    .firstName("Alex")
                    .lastName("Pérez")
                    .documentNumber("1234569")
                    .dateOfBirth(LocalDate.of(2023, 1, 15))
                    .gender(Child.Gender.O)
                    .build();

            // Then
            assertThat(maleChild.getGender()).isEqualTo(Child.Gender.M);
            assertThat(femaleChild.getGender()).isEqualTo(Child.Gender.F);
            assertThat(otherChild.getGender()).isEqualTo(Child.Gender.O);
        }

        @Test
        @DisplayName("Should have exactly three gender enum values")
        void shouldHaveExactlyThreeGenderEnumValues() {
            // Given & When
            Child.Gender[] genders = Child.Gender.values();

            // Then
            assertThat(genders).hasSize(3);
            assertThat(genders).containsExactlyInAnyOrder(
                    Child.Gender.M,
                    Child.Gender.F,
                    Child.Gender.O
            );
        }
    }

    @Nested
    @DisplayName("Bidirectional Relationship Tests")
    class BidirectionalRelationshipTests {

        @Test
        @DisplayName("Should maintain bidirectional relationship with guardians")
        void shouldMaintainBidirectionalRelationshipWithGuardians() {
            // Given
            Child child = Child.builder()
                    .id(1L)
                    .firstName("Juan")
                    .lastName("Pérez")
                    .documentNumber("1234567")
                    .dateOfBirth(LocalDate.of(2023, 1, 15))
                    .gender(Child.Gender.M)
                    .build();

            Guardian guardian = Guardian.builder()
                    .id(1L)
                    .firstName("Maria")
                    .lastName("Pérez")
                    .documentNumber("7654321")
                    .phone("0981234567")
                    .relationship("Mother")
                    .build();

            // When
            child.getGuardians().add(guardian);
            guardian.getChildren().add(child);

            // Then
            assertThat(child.getGuardians()).contains(guardian);
            assertThat(guardian.getChildren()).contains(child);
        }

        @Test
        @DisplayName("Should handle multiple guardians for one child")
        void shouldHandleMultipleGuardiansForOneChild() {
            // Given
            Child child = Child.builder()
                    .id(1L)
                    .firstName("Juan")
                    .lastName("Pérez")
                    .documentNumber("1234567")
                    .dateOfBirth(LocalDate.of(2023, 1, 15))
                    .gender(Child.Gender.M)
                    .build();

            Guardian mother = Guardian.builder()
                    .id(1L)
                    .firstName("Maria")
                    .lastName("Pérez")
                    .documentNumber("7654321")
                    .phone("0981234567")
                    .relationship("Mother")
                    .build();

            Guardian father = Guardian.builder()
                    .id(2L)
                    .firstName("José")
                    .lastName("Pérez")
                    .documentNumber("7654322")
                    .phone("0981234568")
                    .relationship("Father")
                    .build();

            // When
            child.getGuardians().add(mother);
            child.getGuardians().add(father);
            mother.getChildren().add(child);
            father.getChildren().add(child);

            // Then
            assertThat(child.getGuardians()).hasSize(2);
            assertThat(child.getGuardians()).contains(mother, father);
            assertThat(mother.getChildren()).contains(child);
            assertThat(father.getChildren()).contains(child);
        }

        @Test
        @DisplayName("Should handle removing guardian from child")
        void shouldHandleRemovingGuardianFromChild() {
            // Given
            Child child = Child.builder()
                    .id(1L)
                    .firstName("Juan")
                    .lastName("Pérez")
                    .documentNumber("1234567")
                    .dateOfBirth(LocalDate.of(2023, 1, 15))
                    .gender(Child.Gender.M)
                    .build();

            Guardian guardian = Guardian.builder()
                    .id(1L)
                    .firstName("Maria")
                    .lastName("Pérez")
                    .documentNumber("7654321")
                    .phone("0981234567")
                    .relationship("Mother")
                    .build();

            child.getGuardians().add(guardian);
            guardian.getChildren().add(child);

            // When
            child.getGuardians().remove(guardian);

            // Then
            assertThat(child.getGuardians()).isEmpty();
            assertThat(guardian.getChildren()).contains(child);
        }
    }
}
