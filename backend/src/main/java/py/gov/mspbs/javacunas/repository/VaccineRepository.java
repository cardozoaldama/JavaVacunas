package py.gov.mspbs.javacunas.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import py.gov.mspbs.javacunas.entity.Vaccine;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Vaccine entity.
 */
@Repository
public interface VaccineRepository extends JpaRepository<Vaccine, Long> {

    /**
     * Find vaccine by name.
     */
    Optional<Vaccine> findByName(String name);

    /**
     * Find all active vaccines.
     */
    @Query("SELECT v FROM Vaccine v WHERE v.isActive = 'Y' ORDER BY v.name")
    List<Vaccine> findAllActive();

    /**
     * Find vaccines by disease prevented (case insensitive search).
     */
    @Query("SELECT v FROM Vaccine v WHERE LOWER(v.diseasePrevented) LIKE LOWER(CONCAT('%', :disease, '%')) " +
           "AND v.isActive = 'Y'")
    List<Vaccine> findByDiseasePrevented(String disease);

}
