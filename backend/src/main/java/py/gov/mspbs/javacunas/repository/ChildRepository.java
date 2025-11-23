package py.gov.mspbs.javacunas.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import py.gov.mspbs.javacunas.entity.Child;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Child entity.
 */
@Repository
public interface ChildRepository extends JpaRepository<Child, Long> {

    /**
     * Find child by document number.
     */
    Optional<Child> findByDocumentNumber(String documentNumber);

    /**
     * Find all children that are not deleted.
     */
    @Query("SELECT c FROM Child c WHERE c.deletedAt IS NULL")
    List<Child> findAllActive();

    /**
     * Find children by date of birth range.
     */
    @Query("SELECT c FROM Child c WHERE c.dateOfBirth BETWEEN :startDate AND :endDate AND c.deletedAt IS NULL")
    List<Child> findByDateOfBirthBetween(@Param("startDate") LocalDate startDate,
                                         @Param("endDate") LocalDate endDate);

    /**
     * Search children by name (case insensitive).
     */
    @Query("SELECT c FROM Child c WHERE (LOWER(c.firstName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
           "OR LOWER(c.lastName) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) AND c.deletedAt IS NULL")
    List<Child> searchByName(@Param("searchTerm") String searchTerm);

    /**
     * Find children by guardian id.
     */
    @Query("SELECT c FROM Child c JOIN c.guardians g WHERE g.id = :guardianId AND c.deletedAt IS NULL")
    List<Child> findByGuardianId(@Param("guardianId") Long guardianId);

    /**
     * Find children by guardian document number.
     */
    @Query("SELECT c FROM Child c JOIN c.guardians g WHERE g.documentNumber = :documentNumber AND c.deletedAt IS NULL")
    List<Child> findByGuardianDocumentNumber(@Param("documentNumber") String documentNumber);

    /**
     * Find children by user id (through guardians).
     */
    @Query("SELECT c FROM Child c JOIN c.guardians g WHERE g.user.id = :userId AND c.deletedAt IS NULL")
    List<Child> findByUserId(@Param("userId") Long userId);

    /**
     * Find active children with their guardians eagerly loaded.
     * Useful for avoiding N+1 queries when you need both child and guardian data.
     */
    @Query("SELECT DISTINCT c FROM Child c LEFT JOIN FETCH c.guardians WHERE c.deletedAt IS NULL")
    List<Child> findAllActiveWithGuardians();

    /**
     * Find a specific child with guardians eagerly loaded.
     */
    @Query("SELECT c FROM Child c LEFT JOIN FETCH c.guardians WHERE c.id = :childId AND c.deletedAt IS NULL")
    Optional<Child> findByIdWithGuardians(@Param("childId") Long childId);

    /**
     * Find children who have no guardians assigned.
     */
    @Query("SELECT c FROM Child c WHERE c.guardians IS EMPTY AND c.deletedAt IS NULL")
    List<Child> findChildrenWithoutGuardians();

    /**
     * Count guardians for a specific child.
     */
    @Query("SELECT COUNT(g) FROM Child c JOIN c.guardians g WHERE c.id = :childId AND c.deletedAt IS NULL")
    Long countGuardiansByChildId(@Param("childId") Long childId);

    /**
     * Get child age in months using Oracle PL/SQL function.
     */
    @Query(value = "SELECT fn_get_child_age_months(:childId) FROM DUAL", nativeQuery = true)
    Integer getChildAgeInMonths(@Param("childId") Long childId);

    /**
     * Get vaccination completion percentage using Oracle PL/SQL package.
     */
    @Query(value = "SELECT pkg_vaccination_management.get_vaccination_completion(:childId) FROM DUAL", nativeQuery = true)
    Double getVaccinationCompletion(@Param("childId") Long childId);

    /**
     * Get overdue vaccines count using Oracle PL/SQL package.
     */
    @Query(value = "SELECT pkg_vaccination_management.get_overdue_vaccines_count(:childId) FROM DUAL", nativeQuery = true)
    Integer getOverdueVaccinesCount(@Param("childId") Long childId);

    /**
     * Calculate next appointment date using Oracle PL/SQL package.
     */
    @Query(value = "SELECT pkg_vaccination_management.calculate_next_appointment(:childId) FROM DUAL", nativeQuery = true)
    LocalDate calculateNextAppointment(@Param("childId") Long childId);

}
