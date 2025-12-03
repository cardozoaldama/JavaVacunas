package py.gov.mspbs.javacunas.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import py.gov.mspbs.javacunas.entity.VaccineInventory;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for VaccineInventory entity.
 */
@Repository
public interface VaccineInventoryRepository extends JpaRepository<VaccineInventory, Long> {

    /**
     * Find inventory by vaccine id and batch number.
     */
    Optional<VaccineInventory> findByVaccineIdAndBatchNumber(Long vaccineId, String batchNumber);

    /**
     * Find available inventory for a vaccine.
     */
    @Query("SELECT vi FROM VaccineInventory vi " +
           "JOIN FETCH vi.vaccine v " +
           "JOIN FETCH vi.receivedBy u " +
           "WHERE vi.vaccine.id = :vaccineId " +
           "AND vi.status = 'AVAILABLE' " +
           "AND vi.quantity > 0 " +
           "ORDER BY vi.expirationDate")
    List<VaccineInventory> findAvailableByVaccineId(@Param("vaccineId") Long vaccineId);

    /**
     * Find inventory expiring soon.
     */
    @Query("SELECT vi FROM VaccineInventory vi " +
           "JOIN FETCH vi.vaccine v " +
           "JOIN FETCH vi.receivedBy u " +
           "WHERE vi.expirationDate BETWEEN :today AND :endDate " +
           "AND vi.status = 'AVAILABLE' " +
           "AND vi.quantity > 0 " +
           "ORDER BY vi.expirationDate")
    List<VaccineInventory> findExpiringSoon(@Param("today") LocalDate today,
                                            @Param("endDate") LocalDate endDate);

    /**
     * Find low stock inventory.
     */
    @Query("SELECT vi FROM VaccineInventory vi " +
           "JOIN FETCH vi.vaccine v " +
           "JOIN FETCH vi.receivedBy u " +
           "WHERE vi.quantity < :threshold " +
           "AND vi.status = 'AVAILABLE' " +
           "ORDER BY vi.quantity")
    List<VaccineInventory> findLowStock(@Param("threshold") Integer threshold);

    /**
     * Calculate total available quantity for a vaccine.
     */
    @Query("SELECT COALESCE(SUM(vi.quantity), 0) FROM VaccineInventory vi " +
           "WHERE vi.vaccine.id = :vaccineId " +
           "AND vi.status = 'AVAILABLE'")
    Integer calculateTotalAvailableQuantity(@Param("vaccineId") Long vaccineId);

    /**
     * Find inventory by batch number.
     */
    List<VaccineInventory> findByBatchNumber(String batchNumber);

    /**
     * Get available stock using Oracle PL/SQL function.
     * This considers only non-expired, available inventory.
     */
    @Query(value = "SELECT fn_get_available_stock(:vaccineId) FROM DUAL", nativeQuery = true)
    Integer getAvailableStock(@Param("vaccineId") Long vaccineId);

    /**
     * Find all inventory with vaccine and user eagerly loaded.
     */
    @Query("SELECT vi FROM VaccineInventory vi " +
           "JOIN FETCH vi.vaccine v " +
           "JOIN FETCH vi.receivedBy u " +
           "ORDER BY vi.expirationDate")
    List<VaccineInventory> findAllWithRelations();

    /**
     * Find inventory by id with vaccine and user eagerly loaded.
     */
    @Query("SELECT vi FROM VaccineInventory vi " +
           "JOIN FETCH vi.vaccine v " +
           "JOIN FETCH vi.receivedBy u " +
           "WHERE vi.id = :id")
    Optional<VaccineInventory> findByIdWithRelations(@Param("id") Long id);

}
