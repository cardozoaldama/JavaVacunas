package py.gov.mspbs.javacunas.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import py.gov.mspbs.javacunas.BaseUnitTest;
import py.gov.mspbs.javacunas.dto.VaccineDto;
import py.gov.mspbs.javacunas.entity.Vaccine;
import py.gov.mspbs.javacunas.exception.ResourceNotFoundException;
import py.gov.mspbs.javacunas.repository.VaccineRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("VaccineService Tests")
class VaccineServiceTest extends BaseUnitTest {

    @Mock
    private VaccineRepository vaccineRepository;

    @Inject Mocks
    private VaccineService vaccineService;

    private Vaccine testVaccine;

    @BeforeEach
    void setUp() {
        testVaccine = Vaccine.builder()
                .id(1L)
                .name("BCG")
                .description("Bacillus Calmette-Guérin vaccine")
                .manufacturer("Test Manufacturer")
                .diseasePrevented("Tuberculosis")
                .doseCount(1)
                .minimumAgeMonths(0)
                .storageTemperatureMin(new BigDecimal("2.0"))
                .storageTemperatureMax(new BigDecimal("8.0"))
                .isActive('Y')
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("Should get vaccine by ID successfully")
    void shouldGetVaccineById() {
        when(vaccineRepository.findById(1L)).thenReturn(Optional.of(testVaccine));

        VaccineDto result = vaccineService.getVaccineById(1L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("BCG");
        assertThat(result.getDiseasePrevented()).isEqualTo("Tuberculosis");
        assertThat(result.isActive()).isTrue();
    }

    @Test
    @DisplayName("Should throw exception when vaccine not found by ID")
    void shouldThrowExceptionWhenVaccineNotFoundById() {
        when(vaccineRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> vaccineService.getVaccineById(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Vaccine")
                .hasMessageContaining("999");
    }

    @Test
    @DisplayName("Should get vaccine by name successfully")
    void shouldGetVaccineByName() {
        when(vaccineRepository.findByName("BCG")).thenReturn(Optional.of(testVaccine));

        VaccineDto result = vaccineService.getVaccineByName("BCG");

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("BCG");
    }

    @Test
    @DisplayName("Should throw exception when vaccine not found by name")
    void shouldThrowExceptionWhenVaccineNotFoundByName() {
        when(vaccineRepository.findByName("NonExistent")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> vaccineService.getVaccineByName("NonExistent"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("Should get all active vaccines")
    void shouldGetAllActiveVaccines() {
        Vaccine vaccine2 = Vaccine.builder()
                .id(2L)
                .name("Hepatitis B")
                .diseasePrevented("Hepatitis B")
                .isActive('Y')
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(vaccineRepository.findAllActive()).thenReturn(Arrays.asList(testVaccine, vaccine2));

        List<VaccineDto> result = vaccineService.getAllActiveVaccines();

        assertThat(result).hasSize(2);
        assertThat(result).allMatch(VaccineDto::isActive);
    }

    @Test
    @DisplayName("Should search vaccines by disease")
    void shouldSearchVaccinesByDisease() {
        when(vaccineRepository.findByDiseasePrevented("Tuberculosis")).thenReturn(List.of(testVaccine));

        List<VaccineDto> result = vaccineService.searchByDisease("Tuberculosis");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getDiseasePrevented()).isEqualTo("Tuberculosis");
    }

    @Test
    @DisplayName("Should map vaccine to DTO correctly")
    void shouldMapVaccineToDtoCorrectly() {
        when(vaccineRepository.findById(1L)).thenReturn(Optional.of(testVaccine));

        VaccineDto result = vaccineService.getVaccineById(1L);

        assertThat(result.getId()).isEqualTo(testVaccine.getId());
        assertThat(result.getName()).isEqualTo(testVaccine.getName());
        assertThat(result.getDescription()).isEqualTo(testVaccine.getDescription());
        assertThat(result.getManufacturer()).isEqualTo(testVaccine.getManufacturer());
        assertThat(result.getDiseasePrevented()).isEqualTo(testVaccine.getDiseasePrevented());
        assertThat(result.getDoseCount()).isEqualTo(testVaccine.getDoseCount());
        assertThat(result.getMinimumAgeMonths()).isEqualTo(testVaccine.getMinimumAgeMonths());
        assertThat(result.getStorageTemperatureMin()).isEqualByComparingTo(testVaccine.getStorageTemperatureMin());
        assertThat(result.getStorageTemperatureMax()).isEqualByComparingTo(testVaccine.getStorageTemperatureMax());
        assertThat(result.isActive()).isTrue();
    }
}
