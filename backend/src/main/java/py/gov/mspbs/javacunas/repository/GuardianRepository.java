package py.gov.mspbs.javacunas.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import py.gov.mspbs.javacunas.entity.Guardian;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Guardian entity.
 */
@Repository
public interface GuardianRepository extends JpaRepository<Guardian, Long> {

    /**
     * Find guardian by document number.
     */
    Optional<Guardian> findByDocumentNumber(String documentNumber);

    /**
     * Find guardians by user id.
     */
    List<Guardian> findByUserId(Long userId);

    /**
     * Check if document number exists.
     */
    boolean existsByDocumentNumber(String documentNumber);

    /**
     * Find all guardians for a specific child.
     */
    @Query("SELECT g FROM Guardian g JOIN g.children c WHERE c.id = :childId AND c.deletedAt IS NULL")
    List<Guardian> findByChildId(@Param("childId") Long childId);

    /**
     * Find guardians by child document number.
     */
    @Query("SELECT g FROM Guardian g JOIN g.children c WHERE c.documentNumber = :documentNumber AND c.deletedAt IS NULL")
    List<Guardian> findByChildDocumentNumber(@Param("documentNumber") String documentNumber);

    /**
     * Find all guardians with their children eagerly loaded.
     * Useful for avoiding N+1 queries when you need both guardian and children data.
     */
    @Query("SELECT DISTINCT g FROM Guardian g LEFT JOIN FETCH g.children c WHERE c.deletedAt IS NULL OR c IS NULL")
    List<Guardian> findAllWithChildren();

    /**
     * Find a specific guardian with children eagerly loaded.
     */
    @Query("SELECT g FROM Guardian g LEFT JOIN FETCH g.children WHERE g.id = :guardianId")
    Optional<Guardian> findByIdWithChildren(@Param("guardianId") Long guardianId);

    /**
     * Find guardians who have no children assigned.
     */
    @Query("SELECT g FROM Guardian g WHERE g.children IS EMPTY")
    List<Guardian> findGuardiansWithoutChildren();

    /**
     * Count children for a specific guardian.
     */
    @Query("SELECT COUNT(c) FROM Guardian g JOIN g.children c WHERE g.id = :guardianId AND c.deletedAt IS NULL")
    Long countChildrenByGuardianId(@Param("guardianId") Long guardianId);

}
