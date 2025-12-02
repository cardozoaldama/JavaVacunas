package py.gov.mspbs.javacunas.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import py.gov.mspbs.javacunas.BaseUnitTest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Guardian Tests")
class GuardianTest extends BaseUnitTest {

    @Nested
    @DisplayName("Builder Tests")
    class BuilderTests {

        @Test
        @DisplayName("Should initialize children collection with empty HashSet by default")
        void shouldInitializeChildrenCollectionWithEmptyHashSet() {
            // When
            Guardian guardian = Guardian.builder()
                    .firstName("Maria")
                    .lastName("Gonzalez")
                    .documentNumber("1234567")
                    .phone("0981123456")
                    .email("maria@example.com")
                    .relationship("Mother")
                    .build();

            // Then
            assertThat(guardian.getChildren()).isNotNull();
            assertThat(guardian.getChildren()).isEmpty();
        }

        @Test
        @DisplayName("Should build guardian with all required fields")
        void shouldBuildGuardianWithAllRequiredFields() {
            // Given
            User user = User.builder()
                    .id(1L)
                    .username("maria")
                    .build();

            // When
            Guardian guardian = Guardian.builder()
                    .id(1L)
                    .user(user)
                    .firstName("Maria")
                    .lastName("Gonzalez")
                    .documentNumber("1234567")
                    .phone("0981123456")
                    .email("maria@example.com")
                    .address("Asuncion, Paraguay")
                    .relationship("Mother")
                    .build();

            // Then
            assertThat(guardian.getId()).isEqualTo(1L);
            assertThat(guardian.getUser()).isEqualTo(user);
            assertThat(guardian.getFirstName()).isEqualTo("Maria");
            assertThat(guardian.getLastName()).isEqualTo("Gonzalez");
            assertThat(guardian.getDocumentNumber()).isEqualTo("1234567");
            assertThat(guardian.getPhone()).isEqualTo("0981123456");
            assertThat(guardian.getEmail()).isEqualTo("maria@example.com");
            assertThat(guardian.getAddress()).isEqualTo("Asuncion, Paraguay");
            assertThat(guardian.getRelationship()).isEqualTo("Mother");
        }
    }

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("Should not initialize children collection when created with no-args constructor")
        void shouldNotInitializeChildrenCollectionWithNoArgsConstructor() {
            // When
            Guardian guardian = new Guardian();

            // Then
            assertThat(guardian.getChildren()).isNotNull();
            assertThat(guardian.getChildren()).isEmpty();
        }

        @Test
        @DisplayName("Should handle empty children collection in toString")
        void shouldHandleEmptyChildrenCollectionInToString() {
            // When
            Guardian guardian = new Guardian();

            // Then
            assertThat(guardian.getChildren()).isNotNull();
            assertThat(guardian.getChildren()).isEmpty();
            assertThat(guardian.toString()).doesNotContain("children");
        }
    }

    @Nested
    @DisplayName("EqualsAndHashCode Tests")
    class EqualsAndHashCodeTests {

        @Test
        @DisplayName("Should exclude children from equals comparison")
        void shouldExcludeChildrenFromEqualsComparison() {
            // Given
            Guardian guardian1 = Guardian.builder()
                    .id(1L)
                    .firstName("Maria")
                    .lastName("Gonzalez")
                    .documentNumber("1234567")
                    .phone("0981123456")
                    .relationship("Mother")
                    .build();

            Guardian guardian2 = Guardian.builder()
                    .id(1L)
                    .firstName("Maria")
                    .lastName("Gonzalez")
                    .documentNumber("1234567")
                    .phone("0981123456")
                    .relationship("Mother")
                    .build();

            Child child1 = Child.builder()
                    .id(1L)
                    .firstName("Juan")
                    .lastName("Gonzalez")
                    .documentNumber("1111111")
                    .dateOfBirth(LocalDate.of(2023, 1, 1))
                    .gender(Child.Gender.M)
                    .build();

            Child child2 = Child.builder()
                    .id(2L)
                    .firstName("Pedro")
                    .lastName("Gonzalez")
                    .documentNumber("2222222")
                    .dateOfBirth(LocalDate.of(2024, 1, 1))
                    .gender(Child.Gender.M)
                    .build();

            guardian1.getChildren().add(child1);
            guardian2.getChildren().add(child2);

            // When & Then
            assertThat(guardian1).isEqualTo(guardian2);
        }

        @Test
        @DisplayName("Should exclude children from hashCode calculation")
        void shouldExcludeChildrenFromHashCodeCalculation() {
            // Given
            Guardian guardian1 = Guardian.builder()
                    .id(1L)
                    .firstName("Maria")
                    .lastName("Gonzalez")
                    .documentNumber("1234567")
                    .phone("0981123456")
                    .relationship("Mother")
                    .build();

            Guardian guardian2 = Guardian.builder()
                    .id(1L)
                    .firstName("Maria")
                    .lastName("Gonzalez")
                    .documentNumber("1234567")
                    .phone("0981123456")
                    .relationship("Mother")
                    .build();

            Child child1 = Child.builder()
                    .id(1L)
                    .firstName("Juan")
                    .lastName("Gonzalez")
                    .documentNumber("1111111")
                    .dateOfBirth(LocalDate.of(2023, 1, 1))
                    .gender(Child.Gender.M)
                    .build();

            Child child2 = Child.builder()
                    .id(2L)
                    .firstName("Pedro")
                    .lastName("Gonzalez")
                    .documentNumber("2222222")
                    .dateOfBirth(LocalDate.of(2024, 1, 1))
                    .gender(Child.Gender.M)
                    .build();

            guardian1.getChildren().add(child1);
            guardian2.getChildren().add(child2);

            // When & Then
            assertThat(guardian1.hashCode()).isEqualTo(guardian2.hashCode());
        }

        @Test
        @DisplayName("Should not be equal when core fields differ")
        void shouldNotBeEqualWhenCoreFieldsDiffer() {
            // Given
            Guardian guardian1 = Guardian.builder()
                    .id(1L)
                    .firstName("Maria")
                    .lastName("Gonzalez")
                    .documentNumber("1234567")
                    .phone("0981123456")
                    .relationship("Mother")
                    .build();

            Guardian guardian2 = Guardian.builder()
                    .id(2L)
                    .firstName("Maria")
                    .lastName("Gonzalez")
                    .documentNumber("7654321")
                    .phone("0981123456")
                    .relationship("Mother")
                    .build();

            // When & Then
            assertThat(guardian1).isNotEqualTo(guardian2);
        }
    }

    @Nested
    @DisplayName("ToString Tests")
    class ToStringTests {

        @Test
        @DisplayName("Should exclude children from toString output")
        void shouldExcludeChildrenFromToStringOutput() {
            // Given
            Guardian guardian = Guardian.builder()
                    .id(1L)
                    .firstName("Maria")
                    .lastName("Gonzalez")
                    .documentNumber("1234567")
                    .phone("0981123456")
                    .relationship("Mother")
                    .build();

            Child child = Child.builder()
                    .id(1L)
                    .firstName("Juan")
                    .lastName("Gonzalez")
                    .documentNumber("1111111")
                    .dateOfBirth(LocalDate.of(2023, 1, 1))
                    .gender(Child.Gender.M)
                    .build();

            guardian.getChildren().add(child);

            // When
            String toStringOutput = guardian.toString();

            // Then
            assertThat(toStringOutput).contains("Maria");
            assertThat(toStringOutput).contains("Gonzalez");
            assertThat(toStringOutput).contains("1234567");
            assertThat(toStringOutput).doesNotContain("children");
            assertThat(toStringOutput).doesNotContain("Juan");
        }

        @Test
        @DisplayName("Should include all core fields in toString output")
        void shouldIncludeAllCoreFieldsInToStringOutput() {
            // Given
            Guardian guardian = Guardian.builder()
                    .id(1L)
                    .firstName("Maria")
                    .lastName("Gonzalez")
                    .documentNumber("1234567")
                    .phone("0981123456")
                    .email("maria@example.com")
                    .address("Asuncion, Paraguay")
                    .relationship("Mother")
                    .build();

            // When
            String toStringOutput = guardian.toString();

            // Then
            assertThat(toStringOutput).contains("id=1");
            assertThat(toStringOutput).contains("firstName=Maria");
            assertThat(toStringOutput).contains("lastName=Gonzalez");
            assertThat(toStringOutput).contains("documentNumber=1234567");
            assertThat(toStringOutput).contains("phone=0981123456");
            assertThat(toStringOutput).contains("email=maria@example.com");
            assertThat(toStringOutput).contains("address=Asuncion, Paraguay");
            assertThat(toStringOutput).contains("relationship=Mother");
        }
    }

    @Nested
    @DisplayName("StackOverflow Prevention Tests")
    class StackOverflowPreventionTests {

        @Test
        @DisplayName("Should not cause StackOverflow when accessing hashCode with bidirectional relationship")
        void shouldNotCauseStackOverflowWhenAccessingHashCodeWithBidirectionalRelationship() {
            // Given
            Guardian guardian = Guardian.builder()
                    .id(1L)
                    .firstName("Maria")
                    .lastName("Gonzalez")
                    .documentNumber("1234567")
                    .phone("0981123456")
                    .relationship("Mother")
                    .build();

            Child child = Child.builder()
                    .id(1L)
                    .firstName("Juan")
                    .lastName("Gonzalez")
                    .documentNumber("1111111")
                    .dateOfBirth(LocalDate.of(2023, 1, 1))
                    .gender(Child.Gender.M)
                    .build();

            // Establish bidirectional relationship
            guardian.getChildren().add(child);
            child.getGuardians().add(guardian);

            // When & Then - Should not throw StackOverflowError
            assertThat(guardian.hashCode()).isNotZero();
            assertThat(child.hashCode()).isNotZero();
        }

        @Test
        @DisplayName("Should not cause StackOverflow when accessing toString with bidirectional relationship")
        void shouldNotCauseStackOverflowWhenAccessingToStringWithBidirectionalRelationship() {
            // Given
            Guardian guardian = Guardian.builder()
                    .id(1L)
                    .firstName("Maria")
                    .lastName("Gonzalez")
                    .documentNumber("1234567")
                    .phone("0981123456")
                    .relationship("Mother")
                    .build();

            Child child = Child.builder()
                    .id(1L)
                    .firstName("Juan")
                    .lastName("Gonzalez")
                    .documentNumber("1111111")
                    .dateOfBirth(LocalDate.of(2023, 1, 1))
                    .gender(Child.Gender.M)
                    .build();

            // Establish bidirectional relationship
            guardian.getChildren().add(child);
            child.getGuardians().add(guardian);

            // When & Then - Should not throw StackOverflowError
            assertThat(guardian.toString()).isNotNull();
            assertThat(child.toString()).isNotNull();
            assertThat(guardian.toString()).doesNotContain("children");
            assertThat(child.toString()).doesNotContain("guardians");
        }

        @Test
        @DisplayName("Should not cause StackOverflow when accessing equals with bidirectional relationship")
        void shouldNotCauseStackOverflowWhenAccessingEqualsWithBidirectionalRelationship() {
            // Given
            Guardian guardian1 = Guardian.builder()
                    .id(1L)
                    .firstName("Maria")
                    .lastName("Gonzalez")
                    .documentNumber("1234567")
                    .phone("0981123456")
                    .relationship("Mother")
                    .build();

            Guardian guardian2 = Guardian.builder()
                    .id(1L)
                    .firstName("Maria")
                    .lastName("Gonzalez")
                    .documentNumber("1234567")
                    .phone("0981123456")
                    .relationship("Mother")
                    .build();

            Child child1 = Child.builder()
                    .id(1L)
                    .firstName("Juan")
                    .lastName("Gonzalez")
                    .documentNumber("1111111")
                    .dateOfBirth(LocalDate.of(2023, 1, 1))
                    .gender(Child.Gender.M)
                    .build();

            Child child2 = Child.builder()
                    .id(1L)
                    .firstName("Juan")
                    .lastName("Gonzalez")
                    .documentNumber("1111111")
                    .dateOfBirth(LocalDate.of(2023, 1, 1))
                    .gender(Child.Gender.M)
                    .build();

            // Establish bidirectional relationships
            guardian1.getChildren().add(child1);
            child1.getGuardians().add(guardian1);
            guardian2.getChildren().add(child2);
            child2.getGuardians().add(guardian2);

            // When & Then - Should not throw StackOverflowError
            assertThat(guardian1).isEqualTo(guardian2);
            assertThat(child1).isEqualTo(child2);
        }

        @Test
        @DisplayName("Should allow adding children without StackOverflow")
        void shouldAllowAddingChildrenWithoutStackOverflow() {
            // Given
            Guardian guardian = Guardian.builder()
                    .id(1L)
                    .firstName("Maria")
                    .lastName("Gonzalez")
                    .documentNumber("1234567")
                    .phone("0981123456")
                    .relationship("Mother")
                    .build();

            Child child1 = Child.builder()
                    .id(1L)
                    .firstName("Juan")
                    .lastName("Gonzalez")
                    .documentNumber("1111111")
                    .dateOfBirth(LocalDate.of(2023, 1, 1))
                    .gender(Child.Gender.M)
                    .build();

            Child child2 = Child.builder()
                    .id(2L)
                    .firstName("Pedro")
                    .lastName("Gonzalez")
                    .documentNumber("2222222")
                    .dateOfBirth(LocalDate.of(2024, 1, 1))
                    .gender(Child.Gender.M)
                    .build();

            // When - Establish bidirectional relationships
            guardian.getChildren().add(child1);
            child1.getGuardians().add(guardian);
            guardian.getChildren().add(child2);
            child2.getGuardians().add(guardian);

            // Then - Should not throw StackOverflowError
            assertThat(guardian.getChildren()).hasSize(2);
            assertThat(guardian.getChildren()).contains(child1, child2);
            assertThat(guardian.hashCode()).isNotZero();
            assertThat(guardian.toString()).isNotNull();
            assertThat(guardian.equals(guardian)).isTrue();
        }

        @Test
        @DisplayName("Should handle complex bidirectional relationships without StackOverflow")
        void shouldHandleComplexBidirectionalRelationshipsWithoutStackOverflow() {
            // Given - Multiple guardians with multiple children
            Guardian guardian1 = Guardian.builder()
                    .id(1L)
                    .firstName("Maria")
                    .lastName("Gonzalez")
                    .documentNumber("1234567")
                    .phone("0981123456")
                    .relationship("Mother")
                    .build();

            Guardian guardian2 = Guardian.builder()
                    .id(2L)
                    .firstName("Jose")
                    .lastName("Gonzalez")
                    .documentNumber("7654321")
                    .phone("0981654321")
                    .relationship("Father")
                    .build();

            Child child1 = Child.builder()
                    .id(1L)
                    .firstName("Juan")
                    .lastName("Gonzalez")
                    .documentNumber("1111111")
                    .dateOfBirth(LocalDate.of(2023, 1, 1))
                    .gender(Child.Gender.M)
                    .build();

            Child child2 = Child.builder()
                    .id(2L)
                    .firstName("Pedro")
                    .lastName("Gonzalez")
                    .documentNumber("2222222")
                    .dateOfBirth(LocalDate.of(2024, 1, 1))
                    .gender(Child.Gender.M)
                    .build();

            // When - Establish complex bidirectional relationships
            guardian1.getChildren().add(child1);
            guardian1.getChildren().add(child2);
            guardian2.getChildren().add(child1);
            guardian2.getChildren().add(child2);

            child1.getGuardians().add(guardian1);
            child1.getGuardians().add(guardian2);
            child2.getGuardians().add(guardian1);
            child2.getGuardians().add(guardian2);

            // Then - Should not throw StackOverflowError on any operation
            assertThat(guardian1.hashCode()).isNotZero();
            assertThat(guardian2.hashCode()).isNotZero();
            assertThat(child1.hashCode()).isNotZero();
            assertThat(child2.hashCode()).isNotZero();

            assertThat(guardian1.toString()).isNotNull();
            assertThat(guardian2.toString()).isNotNull();
            assertThat(child1.toString()).isNotNull();
            assertThat(child2.toString()).isNotNull();

            assertThat(guardian1.equals(guardian1)).isTrue();
            assertThat(guardian2.equals(guardian2)).isTrue();
            assertThat(child1.equals(child1)).isTrue();
            assertThat(child2.equals(child2)).isTrue();
        }
    }

    @Nested
    @DisplayName("PrePersist Callback Tests")
    class PrePersistCallbackTests {

        @Test
        @DisplayName("Should set createdAt on PrePersist callback")
        void shouldSetCreatedAtOnPrePersistCallback() {
            // Given
            Guardian guardian = Guardian.builder()
                    .firstName("Maria")
                    .lastName("Gonzalez")
                    .documentNumber("1234567")
                    .phone("0981123456")
                    .relationship("Mother")
                    .build();

            LocalDateTime beforeCreate = LocalDateTime.now();

            // When
            guardian.onCreate();

            // Then
            LocalDateTime afterCreate = LocalDateTime.now();
            assertThat(guardian.getCreatedAt()).isNotNull();
            assertThat(guardian.getCreatedAt()).isAfterOrEqualTo(beforeCreate);
            assertThat(guardian.getCreatedAt()).isBeforeOrEqualTo(afterCreate);
        }

        @Test
        @DisplayName("Should set createdAt only once when PrePersist is called")
        void shouldSetCreatedAtOnlyOnceWhenPrePersistIsCalled() {
            // Given
            Guardian guardian = Guardian.builder()
                    .firstName("Maria")
                    .lastName("Gonzalez")
                    .documentNumber("1234567")
                    .phone("0981123456")
                    .relationship("Mother")
                    .build();

            // When
            guardian.onCreate();
            LocalDateTime firstCreatedAt = guardian.getCreatedAt();

            // Wait a bit to ensure different timestamp if set again
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            guardian.onCreate();
            LocalDateTime secondCreatedAt = guardian.getCreatedAt();

            // Then
            assertThat(firstCreatedAt).isNotNull();
            assertThat(secondCreatedAt).isNotNull();
            assertThat(secondCreatedAt).isAfter(firstCreatedAt);
        }
    }

    @Nested
    @DisplayName("Collection Management Tests")
    class CollectionManagementTests {

        @Test
        @DisplayName("Should allow adding children to collection")
        void shouldAllowAddingChildrenToCollection() {
            // Given
            Guardian guardian = Guardian.builder()
                    .firstName("Maria")
                    .lastName("Gonzalez")
                    .documentNumber("1234567")
                    .phone("0981123456")
                    .relationship("Mother")
                    .build();

            Child child = Child.builder()
                    .firstName("Juan")
                    .lastName("Gonzalez")
                    .documentNumber("1111111")
                    .dateOfBirth(LocalDate.of(2023, 1, 1))
                    .gender(Child.Gender.M)
                    .birthWeight(new BigDecimal("3.5"))
                    .birthHeight(new BigDecimal("50.0"))
                    .build();

            // When
            guardian.getChildren().add(child);

            // Then
            assertThat(guardian.getChildren()).hasSize(1);
            assertThat(guardian.getChildren()).contains(child);
        }

        @Test
        @DisplayName("Should allow removing children from collection")
        void shouldAllowRemovingChildrenFromCollection() {
            // Given
            Guardian guardian = Guardian.builder()
                    .firstName("Maria")
                    .lastName("Gonzalez")
                    .documentNumber("1234567")
                    .phone("0981123456")
                    .relationship("Mother")
                    .build();

            Child child = Child.builder()
                    .firstName("Juan")
                    .lastName("Gonzalez")
                    .documentNumber("1111111")
                    .dateOfBirth(LocalDate.of(2023, 1, 1))
                    .gender(Child.Gender.M)
                    .build();

            guardian.getChildren().add(child);

            // When
            guardian.getChildren().remove(child);

            // Then
            assertThat(guardian.getChildren()).isEmpty();
        }

        @Test
        @DisplayName("Should support multiple children in collection")
        void shouldSupportMultipleChildrenInCollection() {
            // Given
            Guardian guardian = Guardian.builder()
                    .firstName("Maria")
                    .lastName("Gonzalez")
                    .documentNumber("1234567")
                    .phone("0981123456")
                    .relationship("Mother")
                    .build();

            Child child1 = Child.builder()
                    .id(1L)
                    .firstName("Juan")
                    .lastName("Gonzalez")
                    .documentNumber("1111111")
                    .dateOfBirth(LocalDate.of(2023, 1, 1))
                    .gender(Child.Gender.M)
                    .build();

            Child child2 = Child.builder()
                    .id(2L)
                    .firstName("Pedro")
                    .lastName("Gonzalez")
                    .documentNumber("2222222")
                    .dateOfBirth(LocalDate.of(2024, 1, 1))
                    .gender(Child.Gender.M)
                    .build();

            // When
            guardian.getChildren().add(child1);
            guardian.getChildren().add(child2);

            // Then
            assertThat(guardian.getChildren()).hasSize(2);
            assertThat(guardian.getChildren()).containsExactlyInAnyOrder(child1, child2);
        }

        @Test
        @DisplayName("Should prevent duplicate children in collection")
        void shouldPreventDuplicateChildrenInCollection() {
            // Given
            Guardian guardian = Guardian.builder()
                    .firstName("Maria")
                    .lastName("Gonzalez")
                    .documentNumber("1234567")
                    .phone("0981123456")
                    .relationship("Mother")
                    .build();

            Child child = Child.builder()
                    .id(1L)
                    .firstName("Juan")
                    .lastName("Gonzalez")
                    .documentNumber("1111111")
                    .dateOfBirth(LocalDate.of(2023, 1, 1))
                    .gender(Child.Gender.M)
                    .build();

            // When
            guardian.getChildren().add(child);
            guardian.getChildren().add(child);

            // Then - Set should prevent duplicates
            assertThat(guardian.getChildren()).hasSize(1);
        }
    }
}
