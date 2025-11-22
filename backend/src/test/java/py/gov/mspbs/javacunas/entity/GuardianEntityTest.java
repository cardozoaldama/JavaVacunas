package py.gov.mspbs.javacunas.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Guardian Entity Tests")
class GuardianEntityTest {

    @Test
    @DisplayName("Should initialize children collection when created with builder")
    void shouldInitializeChildrenCollectionWithBuilder() {
        // When
        Guardian guardian = Guardian.builder()
                .firstName("Parent")
                .lastName("Test")
                .documentNumber("987654321")
                .phone("123456789")
                .email("test@example.com")
                .relationship("Mother")
                .build();

        // Then
        assertThat(guardian.getChildren()).isNotNull();
        assertThat(guardian.getChildren()).isEmpty();
    }

    @Test
    @DisplayName("Should initialize children collection when created with no-args constructor")
    void shouldInitializeChildrenCollectionWithNoArgsConstructor() {
        // When
        Guardian guardian = new Guardian();

        // Then
        assertThat(guardian.getChildren()).isNotNull();
        assertThat(guardian.getChildren()).isEmpty();
    }

    @Test
    @DisplayName("Should allow adding children to collection")
    void shouldAllowAddingChildrenToCollection() {
        // Given
        Guardian guardian = Guardian.builder()
                .firstName("Parent")
                .lastName("Test")
                .documentNumber("987654321")
                .phone("123456789")
                .email("test@example.com")
                .build();

        Child child = Child.builder()
                .firstName("Test")
                .lastName("Child")
                .documentNumber("123456789")
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
}
