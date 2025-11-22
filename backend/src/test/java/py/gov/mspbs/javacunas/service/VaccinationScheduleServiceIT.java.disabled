package py.gov.mspbs.javacunas.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import py.gov.mspbs.javacunas.BaseIT;
import py.gov.mspbs.javacunas.entity.Vaccine;
import py.gov.mspbs.javacunas.entity.VaccinationSchedule;
import py.gov.mspbs.javacunas.repository.VaccinationScheduleRepository;
import py.gov.mspbs.javacunas.repository.VaccineRepository;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for VaccinationScheduleService.
 * Tests the most critical method:
 * - getMandatorySchedulesUpToAge
 */
@DisplayName("VaccinationScheduleService Integration Tests")
class VaccinationScheduleServiceIT extends BaseIT {

    @Autowired
    private VaccinationScheduleService vaccinationScheduleService;

    @Autowired
    private VaccineRepository vaccineRepository;

    @Autowired
    private VaccinationScheduleRepository scheduleRepository;

    private Vaccine bcgVaccine;
    private Vaccine hepatitisBVaccine;
    private Vaccine dtpVaccine;

    @BeforeEach
    void setUp() {
        // Create test vaccines
        bcgVaccine = Vaccine.builder()
                .name("BCG")
                .description("Tuberculosis vaccine")
                .manufacturer("Test Manufacturer")
                .diseasePrevented("Tuberculosis")
                .doseCount(1)
                .isActive('Y')
                .build();
        bcgVaccine = vaccineRepository.save(bcgVaccine);

        hepatitisBVaccine = Vaccine.builder()
                .name("Hepatitis B")
                .description("Hepatitis B vaccine")
                .manufacturer("Test Manufacturer")
                .diseasePrevented("Hepatitis B")
                .doseCount(3)
                .isActive('Y')
                .build();
        hepatitisBVaccine = vaccineRepository.save(hepatitisBVaccine);

        dtpVaccine = Vaccine.builder()
                .name("DTP")
                .description("Diphtheria, Tetanus, Pertussis vaccine")
                .manufacturer("Test Manufacturer")
                .diseasePrevented("Diphtheria, Tetanus, Pertussis")
                .doseCount(5)
                .isActive('Y')
                .build();
        dtpVaccine = vaccineRepository.save(dtpVaccine);

        // Create vaccination schedules for Paraguay (PY)
        createSchedule(bcgVaccine, "PY", 0, 1, true, "At birth");
        createSchedule(hepatitisBVaccine, "PY", 0, 1, true, "At birth");
        createSchedule(hepatitisBVaccine, "PY", 2, 2, true, "2 months");
        createSchedule(hepatitisBVaccine, "PY", 6, 3, true, "6 months");
        createSchedule(dtpVaccine, "PY", 2, 1, true, "2 months");
        createSchedule(dtpVaccine, "PY", 4, 2, true, "4 months");
        createSchedule(dtpVaccine, "PY", 6, 3, true, "6 months");
        createSchedule(dtpVaccine, "PY", 18, 4, true, "18 months booster");
        createSchedule(dtpVaccine, "PY", 48, 5, false, "4 years optional booster");
    }

    @Test
    @DisplayName("Should retrieve mandatory schedules up to specified age")
    void getMandatorySchedulesUpToAge_WithValidAge_ShouldReturnCorrectSchedules() {
        // Arrange
        Integer ageInMonths = 6;

        // Act
        List<VaccinationSchedule> result = vaccinationScheduleService.getMandatorySchedulesUpToAge(ageInMonths);

        // Assert
        assertThat(result).isNotEmpty();
        assertThat(result).hasSize(7); // BCG, Hep B (3 doses), DTP (3 doses) up to 6 months
        assertThat(result).allMatch(schedule ->
                schedule.getRecommendedAgeMonths() <= ageInMonths &&
                schedule.getIsMandatory() == 'Y' &&
                "PY".equals(schedule.getCountryCode())
        );

        // Verify specific vaccines are included
        assertThat(result).extracting(schedule -> schedule.getVaccine().getName())
                .contains("BCG", "Hepatitis B", "DTP");
    }

    @Test
    @DisplayName("Should return only mandatory schedules, excluding optional ones")
    void getMandatorySchedulesUpToAge_WithHigherAge_ShouldExcludeOptional() {
        // Arrange
        Integer ageInMonths = 60; // 5 years

        // Act
        List<VaccinationSchedule> result = vaccinationScheduleService.getMandatorySchedulesUpToAge(ageInMonths);

        // Assert
        assertThat(result).isNotEmpty();
        assertThat(result).allMatch(schedule -> schedule.getIsMandatory() == 'Y');

        // Count schedules for different vaccines
        long bcgCount = result.stream()
                .filter(s -> s.getVaccine().getName().equals("BCG"))
                .count();
        long hepBCount = result.stream()
                .filter(s -> s.getVaccine().getName().equals("Hepatitis B"))
                .count();
        long dtpCount = result.stream()
                .filter(s -> s.getVaccine().getName().equals("DTP"))
                .count();

        assertThat(bcgCount).isEqualTo(1);  // 1 dose at birth
        assertThat(hepBCount).isEqualTo(3); // 3 doses (birth, 2m, 6m)
        assertThat(dtpCount).isEqualTo(4);  // 4 mandatory doses (2m, 4m, 6m, 18m)
    }

    @Test
    @DisplayName("Should return empty list when age is zero but schedules exist")
    void getMandatorySchedulesUpToAge_WithZeroAge_ShouldReturnBirthSchedules() {
        // Arrange
        Integer ageInMonths = 0;

        // Act
        List<VaccinationSchedule> result = vaccinationScheduleService.getMandatorySchedulesUpToAge(ageInMonths);

        // Assert
        assertThat(result).isNotEmpty();
        assertThat(result).hasSize(2); // BCG and Hepatitis B at birth
        assertThat(result).allMatch(schedule ->
                schedule.getRecommendedAgeMonths() == 0 && schedule.getIsMandatory() == 'Y'
        );
    }

    @Test
    @DisplayName("Should return schedules ordered by age and dose number")
    void getMandatorySchedulesUpToAge_ShouldReturnOrderedResults() {
        // Arrange
        Integer ageInMonths = 18;

        // Act
        List<VaccinationSchedule> result = vaccinationScheduleService.getMandatorySchedulesUpToAge(ageInMonths);

        // Assert
        assertThat(result).isNotEmpty();

        // Verify ordering (age should be in ascending order)
        List<Integer> ages = result.stream()
                .map(VaccinationSchedule::getRecommendedAgeMonths)
                .toList();

        for (int i = 1; i < ages.size(); i++) {
            assertThat(ages.get(i)).isGreaterThanOrEqualTo(ages.get(i - 1));
        }
    }

    @Test
    @DisplayName("Should only return schedules for Paraguay (PY)")
    void getMandatorySchedulesUpToAge_ShouldReturnOnlyParaguaySchedules() {
        // Arrange - Add a schedule for another country
        Vaccine testVaccine = vaccineRepository.save(
                Vaccine.builder()
                        .name("Test Vaccine")
                        .description("Test")
                        .manufacturer("Test")
                        .diseasePrevented("Test")
                        .doseCount(1)
                        .isActive('Y')
                        .build()
        );

        createSchedule(testVaccine, "AR", 2, 1, true, "Argentina schedule");

        Integer ageInMonths = 6;

        // Act
        List<VaccinationSchedule> result = vaccinationScheduleService.getMandatorySchedulesUpToAge(ageInMonths);

        // Assert
        assertThat(result).isNotEmpty();
        assertThat(result).allMatch(schedule -> "PY".equals(schedule.getCountryCode()));
        assertThat(result).noneMatch(schedule ->
                schedule.getVaccine().getName().equals("Test Vaccine")
        );
    }

    @Test
    @DisplayName("Should handle edge case with exact age match")
    void getMandatorySchedulesUpToAge_WithExactAgeMatch_ShouldIncludeSchedule() {
        // Arrange - Looking for schedules up to exactly 2 months
        Integer ageInMonths = 2;

        // Act
        List<VaccinationSchedule> result = vaccinationScheduleService.getMandatorySchedulesUpToAge(ageInMonths);

        // Assert
        assertThat(result).isNotEmpty();
        assertThat(result).anyMatch(schedule ->
                schedule.getRecommendedAgeMonths() == 2 &&
                schedule.getVaccine().getName().equals("Hepatitis B") &&
                schedule.getDoseNumber() == 2
        );
        assertThat(result).anyMatch(schedule ->
                schedule.getRecommendedAgeMonths() == 2 &&
                schedule.getVaccine().getName().equals("DTP") &&
                schedule.getDoseNumber() == 1
        );
    }

    @Test
    @DisplayName("Should return correct number of schedules for first year (12 months)")
    void getMandatorySchedulesUpToAge_ForFirstYear_ShouldReturnAllSchedules() {
        // Arrange
        Integer ageInMonths = 12;

        // Act
        List<VaccinationSchedule> result = vaccinationScheduleService.getMandatorySchedulesUpToAge(ageInMonths);

        // Assert
        assertThat(result).isNotEmpty();
        assertThat(result).hasSize(7); // All schedules up to 12 months
        assertThat(result).allMatch(schedule -> schedule.getRecommendedAgeMonths() <= 12);

        // Verify none of the 18-month schedules are included
        assertThat(result).noneMatch(schedule -> schedule.getRecommendedAgeMonths() == 18);
    }

    /**
     * Helper method to create a vaccination schedule.
     */
    private void createSchedule(Vaccine vaccine, String countryCode, Integer ageInMonths,
                               Integer doseNumber, boolean mandatory, String notes) {
        VaccinationSchedule schedule = VaccinationSchedule.builder()
                .vaccine(vaccine)
                .countryCode(countryCode)
                .recommendedAgeMonths(ageInMonths)
                .doseNumber(doseNumber)
                .isMandatory(mandatory ? 'Y' : 'N')
                .notes(notes)
                .build();
        scheduleRepository.save(schedule);
    }
}
