package py.gov.mspbs.javacunas.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import py.gov.mspbs.javacunas.dto.VaccinationScheduleDto;
import py.gov.mspbs.javacunas.dto.VaccineDto;
import py.gov.mspbs.javacunas.entity.VaccinationSchedule;
import py.gov.mspbs.javacunas.entity.Vaccine;
import py.gov.mspbs.javacunas.repository.VaccinationScheduleRepository;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for managing vaccination schedules (PAI).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class VaccinationScheduleService {

    private final VaccinationScheduleRepository scheduleRepository;

    private static final String PARAGUAY_CODE = "PY";

    /**
     * Get Paraguay's vaccination schedule.
     */
    @Transactional(readOnly = true)
    public List<VaccinationScheduleDto> getParaguaySchedule() {
        log.debug("Retrieving Paraguay vaccination schedule");
        return scheduleRepository.findByCountryCode(PARAGUAY_CODE).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    /**
     * Get schedule by vaccine ID.
     */
    @Transactional(readOnly = true)
    public List<VaccinationScheduleDto> getScheduleByVaccineId(Long vaccineId) {
        log.debug("Retrieving schedule for vaccine ID: {}", vaccineId);
        return scheduleRepository.findByVaccineIdOrderByDoseNumber(vaccineId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    /**
     * Get mandatory schedules up to a specific age in months.
     */
    @Transactional(readOnly = true)
    public List<VaccinationScheduleDto> getMandatorySchedulesUpToAge(Integer ageInMonths) {
        log.debug("Retrieving mandatory schedules up to age: {} months", ageInMonths);
        return scheduleRepository.findMandatorySchedulesUpToAge(PARAGUAY_CODE, ageInMonths).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    /**
     * Get all schedules for all countries.
     */
    @Transactional(readOnly = true)
    public List<VaccinationScheduleDto> getAllSchedules() {
        log.debug("Retrieving all vaccination schedules");
        return scheduleRepository.findAllWithVaccine().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    /**
     * Map VaccinationSchedule entity to DTO.
     */
    private VaccinationScheduleDto mapToDto(VaccinationSchedule schedule) {
        return VaccinationScheduleDto.builder()
                .id(schedule.getId())
                .vaccine(mapVaccineToDto(schedule.getVaccine()))
                .countryCode(schedule.getCountryCode())
                .doseNumber(schedule.getDoseNumber())
                .recommendedAgeMonths(schedule.getRecommendedAgeMonths())
                .ageRangeStartMonths(schedule.getAgeRangeStartMonths())
                .ageRangeEndMonths(schedule.getAgeRangeEndMonths())
                .isMandatory(schedule.getIsMandatory())
                .notes(schedule.getNotes())
                .createdAt(schedule.getCreatedAt())
                .build();
    }

    /**
     * Map Vaccine entity to DTO.
     */
    private VaccineDto mapVaccineToDto(Vaccine vaccine) {
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
