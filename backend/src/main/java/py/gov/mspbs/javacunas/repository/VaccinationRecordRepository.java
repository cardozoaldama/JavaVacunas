package py.gov.mspbs.javacunas.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import py.gov.mspbs.javacunas.entity.VaccinationRecord;

import java.time.LocalDate;
import java.util.List;

/**
 * Repository interface for VaccinationRecord entity.
 */
@Repository
public interface VaccinationRecordRepository extends JpaRepository<VaccinationRecord, Long> {

    /**
     * Find all vaccination records for a child.
     */
    @Query("SELECT vr FROM VaccinationRecord vr " +
           "JOIN FETCH vr.vaccine v " +
           "WHERE vr.child.id = :childId " +
           "ORDER BY vr.administrationDate DESC")
    List<VaccinationRecord> findByChildId(@Param("childId") Long childId);

    /**
     * Find vaccination records by vaccine id.
     */
    @Query("SELECT vr FROM VaccinationRecord vr " +
           "WHERE vr.vaccine.id = :vaccineId " +
           "ORDER BY vr.administrationDate DESC")
    List<VaccinationRecord> findByVaccineId(@Param("vaccineId") Long vaccineId);

    /**
     * Find records by batch number.
     */
    List<VaccinationRecord> findByBatchNumber(String batchNumber);

    /**
     * Find records with upcoming next dose dates.
     */
    @Query("SELECT vr FROM VaccinationRecord vr " +
           "WHERE vr.nextDoseDate BETWEEN :startDate AND :endDate " +
           "ORDER BY vr.nextDoseDate")
    List<VaccinationRecord> findByNextDoseDateBetween(@Param("startDate") LocalDate startDate,
                                                       @Param("endDate") LocalDate endDate);

    /**
     * Find records by child and vaccine.
     */
    @Query("SELECT vr FROM VaccinationRecord vr " +
           "WHERE vr.child.id = :childId AND vr.vaccine.id = :vaccineId " +
           "ORDER BY vr.doseNumber")
    List<VaccinationRecord> findByChildIdAndVaccineId(@Param("childId") Long childId,
                                                       @Param("vaccineId") Long vaccineId);

    /**
     * Count administered doses for a child and vaccine.
     */
    @Query("SELECT COUNT(vr) FROM VaccinationRecord vr " +
           "WHERE vr.child.id = :childId AND vr.vaccine.id = :vaccineId")
    Long countByChildIdAndVaccineId(@Param("childId") Long childId,
                                    @Param("vaccineId") Long vaccineId);

}
