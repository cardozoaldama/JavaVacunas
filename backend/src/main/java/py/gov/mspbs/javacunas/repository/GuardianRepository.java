package py.gov.mspbs.javacunas.repository;

import org.springframework.data.jpa.repository.JpaRepository;
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

}
