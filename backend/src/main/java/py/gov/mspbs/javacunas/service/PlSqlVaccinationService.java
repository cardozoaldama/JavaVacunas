package py.gov.mspbs.javacunas.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.ParameterMode;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.StoredProcedureQuery;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import py.gov.mspbs.javacunas.exception.BusinessException;

import java.sql.Date;
import java.time.LocalDate;

/**
 * Service for vaccination operations using Oracle PL/SQL stored procedures and functions.
 * This service demonstrates integration with Oracle database features.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PlSqlVaccinationService {

    @PersistenceContext
    private EntityManager entityManager;

    /**
     * Administer a vaccine using Oracle stored procedure.
     * This method calls sp_administer_vaccine which handles the complete transaction:
     * - Validates child, vaccine, and user
     * - Finds available inventory (FIFO)
     * - Creates vaccination record
     * - Updates inventory
     * - Marks appointment as completed
     */
    @Transactional
    public Long administerVaccine(
            Long childId,
            Long vaccineId,
            Long administeredBy,
            String batchNumber,
            LocalDate administrationDate,
            String administrationSite,
            String notes
    ) {
        log.info("Administering vaccine {} to child {} using PL/SQL procedure", vaccineId, childId);

        try {
            StoredProcedureQuery query = entityManager
                    .createStoredProcedureQuery("sp_administer_vaccine")
                    .registerStoredProcedureParameter("p_child_id", Long.class, ParameterMode.IN)
                    .registerStoredProcedureParameter("p_vaccine_id", Long.class, ParameterMode.IN)
                    .registerStoredProcedureParameter("p_administered_by", Long.class, ParameterMode.IN)
                    .registerStoredProcedureParameter("p_batch_number", String.class, ParameterMode.IN)
                    .registerStoredProcedureParameter("p_administration_date", Date.class, ParameterMode.IN)
                    .registerStoredProcedureParameter("p_administration_site", String.class, ParameterMode.IN)
                    .registerStoredProcedureParameter("p_notes", String.class, ParameterMode.IN)
                    .registerStoredProcedureParameter("p_record_id", Long.class, ParameterMode.OUT)
                    .setParameter("p_child_id", childId)
                    .setParameter("p_vaccine_id", vaccineId)
                    .setParameter("p_administered_by", administeredBy)
                    .setParameter("p_batch_number", batchNumber)
                    .setParameter("p_administration_date", Date.valueOf(administrationDate))
                    .setParameter("p_administration_site", administrationSite)
                    .setParameter("p_notes", notes);

            query.execute();
            Long recordId = (Long) query.getOutputParameterValue("p_record_id");

            log.info("Successfully administered vaccine. Record ID: {}", recordId);
            return recordId;

        } catch (Exception e) {
            log.error("Error administering vaccine using PL/SQL: {}", e.getMessage());
            throw new BusinessException("Failed to administer vaccine: " + e.getMessage());
        }
    }

    /**
     * Deduct inventory using Oracle stored procedure.
     * This method calls sp_deduct_inventory which handles:
     * - Validations
     * - FIFO inventory deduction
     * - Audit logging
     */
    @Transactional
    public void deductInventory(
            Long vaccineId,
            Integer quantity,
            String batchNumber,
            String reason,
            Long deductedBy
    ) {
        log.info("Deducting {} units of vaccine {} using PL/SQL procedure", quantity, vaccineId);

        try {
            StoredProcedureQuery query = entityManager
                    .createStoredProcedureQuery("sp_deduct_inventory")
                    .registerStoredProcedureParameter("p_vaccine_id", Long.class, ParameterMode.IN)
                    .registerStoredProcedureParameter("p_quantity", Integer.class, ParameterMode.IN)
                    .registerStoredProcedureParameter("p_batch_number", String.class, ParameterMode.IN)
                    .registerStoredProcedureParameter("p_reason", String.class, ParameterMode.IN)
                    .registerStoredProcedureParameter("p_deducted_by", Long.class, ParameterMode.IN)
                    .setParameter("p_vaccine_id", vaccineId)
                    .setParameter("p_quantity", quantity)
                    .setParameter("p_batch_number", batchNumber)
                    .setParameter("p_reason", reason)
                    .setParameter("p_deducted_by", deductedBy);

            query.execute();

            log.info("Successfully deducted {} units from inventory", quantity);

        } catch (Exception e) {
            log.error("Error deducting inventory using PL/SQL: {}", e.getMessage());
            throw new BusinessException("Failed to deduct inventory: " + e.getMessage());
        }
    }

    /**
     * Validate if a vaccine can be administered to a child using Oracle package.
     */
    public String validateVaccineApplication(Long childId, Long vaccineId) {
        log.debug("Validating vaccine {} application for child {} using PL/SQL", vaccineId, childId);

        try {
            String result = (String) entityManager
                    .createNativeQuery("SELECT pkg_vaccination_management.validate_vaccine_application(:childId, :vaccineId) FROM DUAL")
                    .setParameter("childId", childId)
                    .setParameter("vaccineId", vaccineId)
                    .getSingleResult();

            log.debug("Validation result: {}", result);
            return result;

        } catch (Exception e) {
            log.error("Error validating vaccine application: {}", e.getMessage());
            return "ERROR: " + e.getMessage();
        }
    }

    /**
     * Check if a vaccine is overdue for a child using Oracle function.
     */
    public boolean isVaccineOverdue(Long childId, Long vaccineId) {
        log.debug("Checking if vaccine {} is overdue for child {}", vaccineId, childId);

        try {
            String result = (String) entityManager
                    .createNativeQuery("SELECT fn_is_vaccine_overdue(:childId, :vaccineId) FROM DUAL")
                    .setParameter("childId", childId)
                    .setParameter("vaccineId", vaccineId)
                    .getSingleResult();

            return "Y".equals(result);

        } catch (Exception e) {
            log.error("Error checking if vaccine is overdue: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Get vaccination coverage percentage for a date range using Oracle function.
     */
    public Double getVaccinationCoverage(LocalDate startDate, LocalDate endDate) {
        log.debug("Getting vaccination coverage from {} to {}", startDate, endDate);

        try {
            Object result = entityManager
                    .createNativeQuery("SELECT fn_get_vaccination_coverage(:startDate, :endDate) FROM DUAL")
                    .setParameter("startDate", Date.valueOf(startDate))
                    .setParameter("endDate", Date.valueOf(endDate))
                    .getSingleResult();

            return result != null ? ((Number) result).doubleValue() : 0.0;

        } catch (Exception e) {
            log.error("Error getting vaccination coverage: {}", e.getMessage());
            return 0.0;
        }
    }
}
