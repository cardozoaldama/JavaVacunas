package py.gov.mspbs.javacunas.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Child Entity Tests")
class ChildEntityTest {

    @Test
    @DisplayName("Should initialize guardians collection when created with builder")
    void shouldInitializeGuardiansCollectionWithBuilder() {
        // When
        Child child = Child.builder()
                .firstName("Test")
                .lastName("Child")
                .documentNumber("123456789")
                .dateOfBirth(LocalDate.of(2023, 1, 1))
                .gender(Child.Gender.M)
                .birthWeight(new BigDecimal("3.5"))
                .birthHeight(new BigDecimal("50.0"))
                .build();

        // Then
        assertThat(child.getGuardians()).isNotNull();
        assertThat(child.getGuardians()).isEmpty();
    }

    @Test
    @DisplayName("Should initialize guardians collection when created with no-args constructor")
    void shouldInitializeGuardiansCollectionWithNoArgsConstructor() {
        // When
        Child child = new Child();

        // Then
        assertThat(child.getGuardians()).isNotNull();
        assertThat(child.getGuardians()).isEmpty();
    }

    @Test
    @DisplayName("Should allow adding guardians to collection")
    void shouldAllowAddingGuardiansToCollection() {
        // Given
        Child child = Child.builder()
                .firstName("Test")
                .lastName("Child")
                .documentNumber("123456789")
                .dateOfBirth(LocalDate.of(2023, 1, 1))
                .gender(Child.Gender.M)
                .build();

        Guardian guardian = Guardian.builder()
                .firstName("Parent")
                .lastName("Test")
                .documentNumber("987654321")
                .phone("123456789")
                .email("test@example.com")
                .build();

        // When
        child.getGuardians().add(guardian);

        // Then
        assertThat(child.getGuardians()).hasSize(1);
        assertThat(child.getGuardians()).contains(guardian);
    }
}
