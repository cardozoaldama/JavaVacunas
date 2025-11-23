package py.gov.mspbs.javacunas.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.ParameterMode;
import jakarta.persistence.Query;
import jakarta.persistence.StoredProcedureQuery;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import py.gov.mspbs.javacunas.BaseUnitTest;
import py.gov.mspbs.javacunas.exception.BusinessException;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@DisplayName("PlSqlVaccinationService Tests")
class PlSqlVaccinationServiceTest extends BaseUnitTest {

    @Mock
    private EntityManager entityManager;

    @Mock
    private StoredProcedureQuery storedProcedureQuery;

    @Mock
    private Query nativeQuery;

    @InjectMocks
    private PlSqlVaccinationService plSqlVaccinationService;

    private Long childId;
    private Long vaccineId;
    private Long userId;
    private String batchNumber;
    private LocalDate administrationDate;

    @BeforeEach
    void setUp() {
        childId = 1L;
        vaccineId = 2L;
        userId = 3L;
        batchNumber = "BATCH-2024-001";
        administrationDate = LocalDate.of(2024, 11, 23);
    }

    @Nested
    @DisplayName("administerVaccine Tests")
    class AdministerVaccineTests {

        @Test
        @DisplayName("Should administer vaccine successfully and return record ID")
        void shouldAdministerVaccineSuccessfully() {
            // Given
            Long expectedRecordId = 100L;
            String administrationSite = "Left arm";
            String notes = "No adverse reactions";

            when(entityManager.createStoredProcedureQuery("sp_administer_vaccine"))
                    .thenReturn(storedProcedureQuery);
            when(storedProcedureQuery.registerStoredProcedureParameter(anyString(), any(Class.class), any(ParameterMode.class)))
                    .thenReturn(storedProcedureQuery);
            when(storedProcedureQuery.setParameter(anyString(), any()))
                    .thenReturn(storedProcedureQuery);
            when(storedProcedureQuery.execute()).thenReturn(true);
            when(storedProcedureQuery.getOutputParameterValue("p_record_id"))
                    .thenReturn(expectedRecordId);

            // When
            Long recordId = plSqlVaccinationService.administerVaccine(
                    childId,
                    vaccineId,
                    userId,
                    batchNumber,
                    administrationDate,
                    administrationSite,
                    notes
            );

            // Then
            assertThat(recordId).isEqualTo(expectedRecordId);
            verify(entityManager).createStoredProcedureQuery("sp_administer_vaccine");
            verify(storedProcedureQuery, times(8)).registerStoredProcedureParameter(anyString(), any(Class.class), any(ParameterMode.class));
            verify(storedProcedureQuery).setParameter("p_child_id", childId);
            verify(storedProcedureQuery).setParameter("p_vaccine_id", vaccineId);
            verify(storedProcedureQuery).setParameter("p_administered_by", userId);
            verify(storedProcedureQuery).setParameter("p_batch_number", batchNumber);
            verify(storedProcedureQuery).setParameter("p_administration_date", Date.valueOf(administrationDate));
            verify(storedProcedureQuery).setParameter("p_administration_site", administrationSite);
            verify(storedProcedureQuery).setParameter("p_notes", notes);
            verify(storedProcedureQuery).execute();
            verify(storedProcedureQuery).getOutputParameterValue("p_record_id");
        }

        @Test
        @DisplayName("Should throw BusinessException when procedure execution fails")
        void shouldThrowBusinessExceptionWhenProcedureFails() {
            // Given
            when(entityManager.createStoredProcedureQuery("sp_administer_vaccine"))
                    .thenReturn(storedProcedureQuery);
            when(storedProcedureQuery.registerStoredProcedureParameter(anyString(), any(Class.class), any(ParameterMode.class)))
                    .thenReturn(storedProcedureQuery);
            when(storedProcedureQuery.setParameter(anyString(), any()))
                    .thenReturn(storedProcedureQuery);
            when(storedProcedureQuery.execute())
                    .thenThrow(new RuntimeException("Database error"));

            // When & Then
            assertThatThrownBy(() -> plSqlVaccinationService.administerVaccine(
                    childId,
                    vaccineId,
                    userId,
                    batchNumber,
                    administrationDate,
                    "Left arm",
                    "Test"
            ))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("Failed to administer vaccine");
        }

        @Test
        @DisplayName("Should handle null administration site and notes")
        void shouldHandleNullOptionalParameters() {
            // Given
            Long expectedRecordId = 101L;

            when(entityManager.createStoredProcedureQuery("sp_administer_vaccine"))
                    .thenReturn(storedProcedureQuery);
            when(storedProcedureQuery.registerStoredProcedureParameter(anyString(), any(Class.class), any(ParameterMode.class)))
                    .thenReturn(storedProcedureQuery);
            when(storedProcedureQuery.setParameter(anyString(), any()))
                    .thenReturn(storedProcedureQuery);
            when(storedProcedureQuery.execute()).thenReturn(true);
            when(storedProcedureQuery.getOutputParameterValue("p_record_id"))
                    .thenReturn(expectedRecordId);

            // When
            Long recordId = plSqlVaccinationService.administerVaccine(
                    childId,
                    vaccineId,
                    userId,
                    batchNumber,
                    administrationDate,
                    null,
                    null
            );

            // Then
            assertThat(recordId).isEqualTo(expectedRecordId);
            verify(storedProcedureQuery).setParameter("p_administration_site", null);
            verify(storedProcedureQuery).setParameter("p_notes", null);
        }
    }

    @Nested
    @DisplayName("deductInventory Tests")
    class DeductInventoryTests {

        @Test
        @DisplayName("Should deduct inventory successfully")
        void shouldDeductInventorySuccessfully() {
            // Given
            Integer quantity = 10;
            String reason = "WASTAGE";

            when(entityManager.createStoredProcedureQuery("sp_deduct_inventory"))
                    .thenReturn(storedProcedureQuery);
            when(storedProcedureQuery.registerStoredProcedureParameter(anyString(), any(Class.class), any(ParameterMode.class)))
                    .thenReturn(storedProcedureQuery);
            when(storedProcedureQuery.setParameter(anyString(), any()))
                    .thenReturn(storedProcedureQuery);
            when(storedProcedureQuery.execute()).thenReturn(true);

            // When
            plSqlVaccinationService.deductInventory(
                    vaccineId,
                    quantity,
                    batchNumber,
                    reason,
                    userId
            );

            // Then
            verify(entityManager).createStoredProcedureQuery("sp_deduct_inventory");
            verify(storedProcedureQuery).setParameter("p_vaccine_id", vaccineId);
            verify(storedProcedureQuery).setParameter("p_quantity", quantity);
            verify(storedProcedureQuery).setParameter("p_batch_number", batchNumber);
            verify(storedProcedureQuery).setParameter("p_reason", reason);
            verify(storedProcedureQuery).setParameter("p_deducted_by", userId);
            verify(storedProcedureQuery).execute();
        }

        @Test
        @DisplayName("Should throw BusinessException when deduction fails")
        void shouldThrowBusinessExceptionWhenDeductionFails() {
            // Given
            when(entityManager.createStoredProcedureQuery("sp_deduct_inventory"))
                    .thenReturn(storedProcedureQuery);
            when(storedProcedureQuery.registerStoredProcedureParameter(anyString(), any(Class.class), any(ParameterMode.class)))
                    .thenReturn(storedProcedureQuery);
            when(storedProcedureQuery.setParameter(anyString(), any()))
                    .thenReturn(storedProcedureQuery);
            when(storedProcedureQuery.execute())
                    .thenThrow(new RuntimeException("Insufficient stock"));

            // When & Then
            assertThatThrownBy(() -> plSqlVaccinationService.deductInventory(
                    vaccineId,
                    5,
                    batchNumber,
                    "USAGE",
                    userId
            ))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("Failed to deduct inventory");
        }
    }

    @Nested
    @DisplayName("validateVaccineApplication Tests")
    class ValidateVaccineApplicationTests {

        @Test
        @DisplayName("Should return OK when vaccine can be administered")
        void shouldReturnOkWhenVaccineCanBeAdministered() {
            // Given
            String expectedResult = "OK";

            when(entityManager.createNativeQuery(anyString()))
                    .thenReturn(nativeQuery);
            when(nativeQuery.setParameter("childId", childId))
                    .thenReturn(nativeQuery);
            when(nativeQuery.setParameter("vaccineId", vaccineId))
                    .thenReturn(nativeQuery);
            when(nativeQuery.getSingleResult()).thenReturn(expectedResult);

            // When
            String result = plSqlVaccinationService.validateVaccineApplication(childId, vaccineId);

            // Then
            assertThat(result).isEqualTo("OK");
            verify(entityManager).createNativeQuery(contains("validate_vaccine_application"));
            verify(nativeQuery).setParameter("childId", childId);
            verify(nativeQuery).setParameter("vaccineId", vaccineId);
        }

        @Test
        @DisplayName("Should return error message when validation fails")
        void shouldReturnErrorMessageWhenValidationFails() {
            // Given
            String expectedError = "ERROR: Child too young for this vaccine";

            when(entityManager.createNativeQuery(anyString()))
                    .thenReturn(nativeQuery);
            when(nativeQuery.setParameter("childId", childId))
                    .thenReturn(nativeQuery);
            when(nativeQuery.setParameter("vaccineId", vaccineId))
                    .thenReturn(nativeQuery);
            when(nativeQuery.getSingleResult()).thenReturn(expectedError);

            // When
            String result = plSqlVaccinationService.validateVaccineApplication(childId, vaccineId);

            // Then
            assertThat(result).startsWith("ERROR:");
            assertThat(result).contains("too young");
        }

        @Test
        @DisplayName("Should return error when exception occurs")
        void shouldReturnErrorWhenExceptionOccurs() {
            // Given
            when(entityManager.createNativeQuery(anyString()))
                    .thenThrow(new RuntimeException("Database connection error"));

            // When
            String result = plSqlVaccinationService.validateVaccineApplication(childId, vaccineId);

            // Then
            assertThat(result).startsWith("ERROR:");
        }
    }

    @Nested
    @DisplayName("isVaccineOverdue Tests")
    class IsVaccineOverdueTests {

        @Test
        @DisplayName("Should return true when vaccine is overdue")
        void shouldReturnTrueWhenVaccineIsOverdue() {
            // Given
            when(entityManager.createNativeQuery(anyString()))
                    .thenReturn(nativeQuery);
            when(nativeQuery.setParameter("childId", childId))
                    .thenReturn(nativeQuery);
            when(nativeQuery.setParameter("vaccineId", vaccineId))
                    .thenReturn(nativeQuery);
            when(nativeQuery.getSingleResult()).thenReturn("Y");

            // When
            boolean isOverdue = plSqlVaccinationService.isVaccineOverdue(childId, vaccineId);

            // Then
            assertThat(isOverdue).isTrue();
        }

        @Test
        @DisplayName("Should return false when vaccine is not overdue")
        void shouldReturnFalseWhenVaccineIsNotOverdue() {
            // Given
            when(entityManager.createNativeQuery(anyString()))
                    .thenReturn(nativeQuery);
            when(nativeQuery.setParameter("childId", childId))
                    .thenReturn(nativeQuery);
            when(nativeQuery.setParameter("vaccineId", vaccineId))
                    .thenReturn(nativeQuery);
            when(nativeQuery.getSingleResult()).thenReturn("N");

            // When
            boolean isOverdue = plSqlVaccinationService.isVaccineOverdue(childId, vaccineId);

            // Then
            assertThat(isOverdue).isFalse();
        }

        @Test
        @DisplayName("Should return false when exception occurs")
        void shouldReturnFalseWhenExceptionOccurs() {
            // Given
            when(entityManager.createNativeQuery(anyString()))
                    .thenThrow(new RuntimeException("Query error"));

            // When
            boolean isOverdue = plSqlVaccinationService.isVaccineOverdue(childId, vaccineId);

            // Then
            assertThat(isOverdue).isFalse();
        }
    }

    @Nested
    @DisplayName("getVaccinationCoverage Tests")
    class GetVaccinationCoverageTests {

        @Test
        @DisplayName("Should return coverage percentage")
        void shouldReturnCoveragePercentage() {
            // Given
            LocalDate startDate = LocalDate.of(2024, 1, 1);
            LocalDate endDate = LocalDate.of(2024, 12, 31);
            BigDecimal expectedCoverage = BigDecimal.valueOf(87.50);

            when(entityManager.createNativeQuery(anyString()))
                    .thenReturn(nativeQuery);
            when(nativeQuery.setParameter("startDate", Date.valueOf(startDate)))
                    .thenReturn(nativeQuery);
            when(nativeQuery.setParameter("endDate", Date.valueOf(endDate)))
                    .thenReturn(nativeQuery);
            when(nativeQuery.getSingleResult()).thenReturn(expectedCoverage);

            // When
            Double coverage = plSqlVaccinationService.getVaccinationCoverage(startDate, endDate);

            // Then
            assertThat(coverage).isEqualTo(87.50);
            verify(nativeQuery).setParameter("startDate", Date.valueOf(startDate));
            verify(nativeQuery).setParameter("endDate", Date.valueOf(endDate));
        }

        @Test
        @DisplayName("Should return zero when result is null")
        void shouldReturnZeroWhenResultIsNull() {
            // Given
            LocalDate startDate = LocalDate.of(2024, 1, 1);
            LocalDate endDate = LocalDate.of(2024, 12, 31);

            when(entityManager.createNativeQuery(anyString()))
                    .thenReturn(nativeQuery);
            when(nativeQuery.setParameter(anyString(), any()))
                    .thenReturn(nativeQuery);
            when(nativeQuery.getSingleResult()).thenReturn(null);

            // When
            Double coverage = plSqlVaccinationService.getVaccinationCoverage(startDate, endDate);

            // Then
            assertThat(coverage).isEqualTo(0.0);
        }

        @Test
        @DisplayName("Should return zero when exception occurs")
        void shouldReturnZeroWhenExceptionOccurs() {
            // Given
            LocalDate startDate = LocalDate.of(2024, 1, 1);
            LocalDate endDate = LocalDate.of(2024, 12, 31);

            when(entityManager.createNativeQuery(anyString()))
                    .thenThrow(new RuntimeException("Query execution error"));

            // When
            Double coverage = plSqlVaccinationService.getVaccinationCoverage(startDate, endDate);

            // Then
            assertThat(coverage).isEqualTo(0.0);
        }

        @Test
        @DisplayName("Should handle Integer return type from database")
        void shouldHandleIntegerReturnType() {
            // Given
            LocalDate startDate = LocalDate.of(2024, 1, 1);
            LocalDate endDate = LocalDate.of(2024, 12, 31);
            Integer coverageInt = 75;

            when(entityManager.createNativeQuery(anyString()))
                    .thenReturn(nativeQuery);
            when(nativeQuery.setParameter(anyString(), any()))
                    .thenReturn(nativeQuery);
            when(nativeQuery.getSingleResult()).thenReturn(coverageInt);

            // When
            Double coverage = plSqlVaccinationService.getVaccinationCoverage(startDate, endDate);

            // Then
            assertThat(coverage).isEqualTo(75.0);
        }
    }
}
