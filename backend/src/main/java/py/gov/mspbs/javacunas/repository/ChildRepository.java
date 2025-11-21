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
    @Query("SELECT c FROM Child c JOIN child_guardians cg ON c.id = cg.child_id " +
           "WHERE cg.guardian_id = :guardianId AND c.deletedAt IS NULL")
    List<Child> findByGuardianId(@Param("guardianId") Long guardianId);

}
