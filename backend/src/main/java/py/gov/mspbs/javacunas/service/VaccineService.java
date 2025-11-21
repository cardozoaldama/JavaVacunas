package py.gov.mspbs.javacunas.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import py.gov.mspbs.javacunas.dto.VaccineDto;
import py.gov.mspbs.javacunas.entity.Vaccine;
import py.gov.mspbs.javacunas.exception.ResourceNotFoundException;
import py.gov.mspbs.javacunas.repository.VaccineRepository;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for managing vaccines.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class VaccineService {

    private final VaccineRepository vaccineRepository;

    /**
     * Get vaccine by ID.
     */
    @Transactional(readOnly = true)
    public VaccineDto getVaccineById(Long id) {
        log.debug("Retrieving vaccine with ID: {}", id);

        Vaccine vaccine = vaccineRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vaccine", "id", id));

        return mapToDto(vaccine);
    }

    /**
     * Get vaccine by name.
     */
    @Transactional(readOnly = true)
    public VaccineDto getVaccineByName(String name) {
        log.debug("Retrieving vaccine with name: {}", name);

        Vaccine vaccine = vaccineRepository.findByName(name)
                .orElseThrow(() -> new ResourceNotFoundException("Vaccine", "name", name));

        return mapToDto(vaccine);
    }

    /**
     * Get all active vaccines.
     */
    @Transactional(readOnly = true)
    public List<VaccineDto> getAllActiveVaccines() {
        log.debug("Retrieving all active vaccines");

        return vaccineRepository.findAllActive().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    /**
     * Get all vaccines.
     */
    @Transactional(readOnly = true)
    public List<VaccineDto> getAllVaccines() {
        log.debug("Retrieving all vaccines");

        return vaccineRepository.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    /**
     * Search vaccines by disease.
     */
    @Transactional(readOnly = true)
    public List<VaccineDto> searchByDisease(String disease) {
        log.debug("Searching vaccines by disease: {}", disease);

        return vaccineRepository.findByDiseasePrevented(disease).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    /**
     * Map Vaccine entity to DTO.
     */
    private VaccineDto mapToDto(Vaccine vaccine) {
        return VaccineDto.builder()
                .id(vaccine.getId())
                .name(vaccine.getName())
                .description(vaccine.getDescription())
                .manufacturer(vaccine.getManufacturer())
                .diseasePrevented(vaccine.getDiseasePrevented())
                .doseCount(vaccine.getDoseCount())
                .minimumAgeMonths(vaccine.getMinimumAgeMonths())
                .storageTemperatureMin(vaccine.getStorageTemperatureMin())
                .storageTemperatureMax(vaccine.getStorageTemperatureMax())
                .isActive(vaccine.getIsActive() == 'Y')
                .createdAt(vaccine.getCreatedAt())
                .updatedAt(vaccine.getUpdatedAt())
                .build();
    }

}
