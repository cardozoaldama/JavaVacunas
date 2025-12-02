package py.gov.mspbs.javacunas.repository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import py.gov.mspbs.javacunas.AbstractOracleIntegrationTest;
import py.gov.mspbs.javacunas.entity.Vaccine;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

/**
 * Integration tests for VaccineRepository using Oracle 23c Free database.
 *
 * These tests verify:
 * - Basic CRUD operations
 * - Custom query methods
 * - Database constraints (unique, not null)
 * - Oracle-specific behavior
 * - JPA lifecycle callbacks (@PrePersist, @PreUpdate)
 */
@DisplayName("VaccineRepository Integration Tests")
class VaccineRepositoryIT extends AbstractOracleIntegrationTest {

    @Autowired
    private VaccineRepository vaccineRepository;

    @Test
    @DisplayName("Should save and retrieve vaccine with all required fields")
    void testSaveAndRetrieveVaccine() {
        // Given
        Vaccine vaccine = Vaccine.builder()
                .name("BCG")
                .description("Bacillus Calmette-Guerin vaccine")
                .manufacturer("Serum Institute")
                .diseasePrevented("Tuberculosis")
                .doseCount(1)
                .minimumAgeMonths(0)
                .storageTemperatureMin(new BigDecimal("2.0"))
                .storageTemperatureMax(new BigDecimal("8.0"))
                .isActive('Y')
                .build();

        // When
        Vaccine savedVaccine = vaccineRepository.save(vaccine);
        vaccineRepository.flush();

        // Then
        assertThat(savedVaccine.getId()).isNotNull();
        assertThat(savedVaccine.getCreatedAt()).isNotNull();
        assertThat(savedVaccine.getUpdatedAt()).isNotNull();
        assertThat(savedVaccine.getName()).isEqualTo("BCG");
        assertThat(savedVaccine.getDiseasePrevented()).isEqualTo("Tuberculosis");

        // Verify database retrieval
        Optional<Vaccine> retrievedVaccine = vaccineRepository.findById(savedVaccine.getId());
        assertThat(retrievedVaccine).isPresent();
        assertThat(retrievedVaccine.get().getName()).isEqualTo("BCG");
    }

    @Test
    @DisplayName("Should enforce unique vaccine name constraint")
    void testUniqueVaccineNameConstraint() {
        // Given
        Vaccine vaccine1 = Vaccine.builder()
                .name("Polio")
                .diseasePrevented("Poliomyelitis")
                .doseCount(4)
                .build();

        Vaccine vaccine2 = Vaccine.builder()
                .name("Polio")
                .diseasePrevented("Poliomyelitis")
                .doseCount(4)
                .build();

        // When
        vaccineRepository.save(vaccine1);
        vaccineRepository.flush();

        // Then - attempting to save duplicate name should fail
        assertThatThrownBy(() -> {
            vaccineRepository.save(vaccine2);
            vaccineRepository.flush();
        }).isInstanceOf(DataIntegrityViolationException.class)
          .hasMessageContaining("unique constraint");
    }

    @Test
    @DisplayName("Should enforce not null constraint on required fields")
    void testNotNullConstraints() {
        // Given - vaccine without required 'name' field
        Vaccine vaccine = Vaccine.builder()
                .diseasePrevented("Some Disease")
                .doseCount(2)
                .build();

        // When/Then - should fail due to null name
        assertThatThrownBy(() -> {
            vaccineRepository.save(vaccine);
            vaccineRepository.flush();
        }).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("Should find vaccine by exact name")
    void testFindByName() {
        // Given
        Vaccine hepatitisB = Vaccine.builder()
                .name("Hepatitis B")
                .diseasePrevented("Hepatitis B")
                .manufacturer("Merck")
                .doseCount(3)
                .build();

        vaccineRepository.save(hepatitisB);
        vaccineRepository.flush();

        // When
        Optional<Vaccine> foundVaccine = vaccineRepository.findByName("Hepatitis B");

        // Then
        assertThat(foundVaccine).isPresent();
        assertThat(foundVaccine.get().getName()).isEqualTo("Hepatitis B");
        assertThat(foundVaccine.get().getManufacturer()).isEqualTo("Merck");
    }

    @Test
    @DisplayName("Should find all active vaccines ordered by name")
    void testFindAllActive() {
        // Given
        Vaccine vaccine1 = Vaccine.builder()
                .name("MMR")
                .diseasePrevented("Measles, Mumps, Rubella")
                .doseCount(2)
                .isActive('Y')
                .build();

        Vaccine vaccine2 = Vaccine.builder()
                .name("DTP")
                .diseasePrevented("Diphtheria, Tetanus, Pertussis")
                .doseCount(5)
                .isActive('Y')
                .build();

        Vaccine vaccine3 = Vaccine.builder()
                .name("HPV")
                .diseasePrevented("Human Papillomavirus")
                .doseCount(2)
                .isActive('N')  // Inactive
                .build();

        vaccineRepository.saveAll(List.of(vaccine1, vaccine2, vaccine3));
        vaccineRepository.flush();

        // When
        List<Vaccine> activeVaccines = vaccineRepository.findAllActive();

        // Then
        assertThat(activeVaccines).hasSize(2);
        assertThat(activeVaccines)
                .extracting(Vaccine::getName)
                .containsExactly("DTP", "MMR");  // Ordered alphabetically
        assertThat(activeVaccines)
                .allMatch(v -> v.getIsActive() == 'Y');
    }

    @Test
    @DisplayName("Should find vaccines by disease prevented with case-insensitive partial match")
    void testFindByDiseasePrevented() {
        // Given
        Vaccine vaccine1 = Vaccine.builder()
                .name("Rotavirus")
                .diseasePrevented("Rotavirus Gastroenteritis")
                .doseCount(3)
                .isActive('Y')
                .build();

        Vaccine vaccine2 = Vaccine.builder()
                .name("Cholera")
                .diseasePrevented("Cholera")
                .doseCount(2)
                .isActive('Y')
                .build();

        vaccineRepository.saveAll(List.of(vaccine1, vaccine2));
        vaccineRepository.flush();

        // When - case insensitive partial search
        List<Vaccine> virusVaccines = vaccineRepository.findByDiseasePrevented("virus");

        // Then
        assertThat(virusVaccines).hasSize(1);
        assertThat(virusVaccines.get(0).getName()).isEqualTo("Rotavirus");
    }

    @Test
    @DisplayName("Should update vaccine and modify updatedAt timestamp")
    void testUpdateVaccine() throws InterruptedException {
        // Given
        Vaccine vaccine = Vaccine.builder()
                .name("Influenza")
                .diseasePrevented("Influenza")
                .manufacturer("Original Manufacturer")
                .doseCount(1)
                .build();

        Vaccine savedVaccine = vaccineRepository.save(vaccine);
        vaccineRepository.flush();

        var originalCreatedAt = savedVaccine.getCreatedAt();
        var originalUpdatedAt = savedVaccine.getUpdatedAt();

        // Small delay to ensure updatedAt will be different
        Thread.sleep(10);

        // When
        savedVaccine.setManufacturer("Updated Manufacturer");
        savedVaccine.setDoseCount(2);
        Vaccine updatedVaccine = vaccineRepository.save(savedVaccine);
        vaccineRepository.flush();

        // Then
        assertThat(updatedVaccine.getManufacturer()).isEqualTo("Updated Manufacturer");
        assertThat(updatedVaccine.getDoseCount()).isEqualTo(2);
        assertThat(updatedVaccine.getCreatedAt()).isEqualTo(originalCreatedAt);
        assertThat(updatedVaccine.getUpdatedAt()).isAfter(originalUpdatedAt);
    }

    @Test
    @DisplayName("Should delete vaccine from database")
    void testDeleteVaccine() {
        // Given
        Vaccine vaccine = Vaccine.builder()
                .name("Yellow Fever")
                .diseasePrevented("Yellow Fever")
                .doseCount(1)
                .build();

        Vaccine savedVaccine = vaccineRepository.save(vaccine);
        vaccineRepository.flush();
        Long vaccineId = savedVaccine.getId();

        // When
        vaccineRepository.delete(savedVaccine);
        vaccineRepository.flush();

        // Then
        Optional<Vaccine> deletedVaccine = vaccineRepository.findById(vaccineId);
        assertThat(deletedVaccine).isEmpty();
    }

    @Test
    @DisplayName("Should persist storage temperature with correct precision")
    void testStorageTemperaturePrecision() {
        // Given
        Vaccine vaccine = Vaccine.builder()
                .name("Varicella")
                .diseasePrevented("Chickenpox")
                .doseCount(2)
                .storageTemperatureMin(new BigDecimal("-15.5"))
                .storageTemperatureMax(new BigDecimal("-10.0"))
                .build();

        // When
        Vaccine savedVaccine = vaccineRepository.save(vaccine);
        vaccineRepository.flush();

        // Then
        Vaccine retrievedVaccine = vaccineRepository.findById(savedVaccine.getId()).orElseThrow();
        assertThat(retrievedVaccine.getStorageTemperatureMin())
                .isEqualByComparingTo(new BigDecimal("-15.5"));
        assertThat(retrievedVaccine.getStorageTemperatureMax())
                .isEqualByComparingTo(new BigDecimal("-10.0"));
    }

    @Test
    @DisplayName("Should count vaccines correctly")
    void testCountVaccines() {
        // Given
        List<Vaccine> vaccines = List.of(
                Vaccine.builder().name("Vaccine1").diseasePrevented("Disease1").doseCount(1).build(),
                Vaccine.builder().name("Vaccine2").diseasePrevented("Disease2").doseCount(2).build(),
                Vaccine.builder().name("Vaccine3").diseasePrevented("Disease3").doseCount(3).build()
        );

        vaccineRepository.saveAll(vaccines);
        vaccineRepository.flush();

        // When
        long count = vaccineRepository.count();

        // Then
        assertThat(count).isEqualTo(3);
    }

    @Test
    @DisplayName("Should verify Oracle database connection and schema")
    void testOracleDatabaseConnection() {
        // Given/When
        String oracleVersion = getOracleVersion();
        boolean vaccinesTableExists = tableExists("vaccines");

        // Then
        assertThat(oracleVersion).containsIgnoringCase("Oracle");
        assertThat(vaccinesTableExists).isTrue();
    }
}
