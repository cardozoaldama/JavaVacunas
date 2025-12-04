package py.gov.mspbs.javacunas.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import py.gov.mspbs.javacunas.entity.VaccinationSchedule;

import java.util.List;

/**
 * Repository interface for VaccinationSchedule entity.
 */
@Repository
public interface VaccinationScheduleRepository extends JpaRepository<VaccinationSchedule, Long> {

    /**
     * Find schedules by country code.
     */
    @Query("SELECT vs FROM VaccinationSchedule vs " +
           "JOIN FETCH vs.vaccine v " +
           "WHERE vs.countryCode = :countryCode " +
           "ORDER BY vs.recommendedAgeMonths, vs.doseNumber")
    List<VaccinationSchedule> findByCountryCode(@Param("countryCode") String countryCode);

    /**
     * Find schedules by vaccine id.
     */
    @Query("SELECT vs FROM VaccinationSchedule vs " +
           "JOIN FETCH vs.vaccine v " +
           "WHERE vs.vaccine.id = :vaccineId " +
           "ORDER BY vs.doseNumber")
    List<VaccinationSchedule> findByVaccineIdOrderByDoseNumber(@Param("vaccineId") Long vaccineId);

    /**
     * Find mandatory schedules for a specific age in months.
     */
    @Query("SELECT vs FROM VaccinationSchedule vs " +
           "JOIN FETCH vs.vaccine v " +
           "WHERE vs.countryCode = :countryCode " +
           "AND vs.recommendedAgeMonths <= :ageMonths " +
           "AND vs.isMandatory = true " +
           "ORDER BY vs.recommendedAgeMonths, vs.doseNumber")
    List<VaccinationSchedule> findMandatorySchedulesUpToAge(@Param("countryCode") String countryCode,
                                                             @Param("ageMonths") Integer ageMonths);

    /**
     * Find all schedules with vaccine eagerly loaded.
     */
    @Query("SELECT vs FROM VaccinationSchedule vs " +
           "JOIN FETCH vs.vaccine v " +
           "ORDER BY vs.recommendedAgeMonths, vs.doseNumber")
    List<VaccinationSchedule> findAllWithVaccine();

}
