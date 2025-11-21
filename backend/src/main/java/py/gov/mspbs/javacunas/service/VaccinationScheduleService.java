package py.gov.mspbs.javacunas.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import py.gov.mspbs.javacunas.entity.VaccinationSchedule;
import py.gov.mspbs.javacunas.repository.VaccinationScheduleRepository;

import java.util.List;

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
    public List<VaccinationSchedule> getParaguaySchedule() {
        log.debug("Retrieving Paraguay vaccination schedule");
        return scheduleRepository.findByCountryCode(PARAGUAY_CODE);
    }

    /**
     * Get schedule by vaccine ID.
     */
    @Transactional(readOnly = true)
    public List<VaccinationSchedule> getScheduleByVaccineId(Long vaccineId) {
        log.debug("Retrieving schedule for vaccine ID: {}", vaccineId);
        return scheduleRepository.findByVaccineIdOrderByDoseNumber(vaccineId);
    }

    /**
     * Get mandatory schedules up to a specific age in months.
     */
    @Transactional(readOnly = true)
    public List<VaccinationSchedule> getMandatorySchedulesUpToAge(Integer ageInMonths) {
        log.debug("Retrieving mandatory schedules up to age: {} months", ageInMonths);
        return scheduleRepository.findMandatorySchedulesUpToAge(PARAGUAY_CODE, ageInMonths);
    }

    /**
     * Get all schedules for all countries.
     */
    @Transactional(readOnly = true)
    public List<VaccinationSchedule> getAllSchedules() {
        log.debug("Retrieving all vaccination schedules");
        return scheduleRepository.findAll();
    }

}
