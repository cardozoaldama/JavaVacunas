package py.gov.mspbs.javacunas.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import py.gov.mspbs.javacunas.BaseIT;
import py.gov.mspbs.javacunas.dto.CreateVaccinationRecordRequest;
import py.gov.mspbs.javacunas.dto.VaccinationRecordDto;
import py.gov.mspbs.javacunas.entity.*;
import py.gov.mspbs.javacunas.exception.BusinessException;
import py.gov.mspbs.javacunas.exception.ResourceNotFoundException;
import py.gov.mspbs.javacunas.repository.*;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Integration tests for VaccinationRecordService.
 * Tests the 3 most critical methods:
 * - createVaccinationRecord
 * - getUpcomingVaccinations
 * - getVaccinationRecordsByBatchNumber
 */
@DisplayName("VaccinationRecordService Integration Tests")
class VaccinationRecordServiceIT extends BaseIT {

    @Autowired
    private VaccinationRecordService vaccinationRecordService;

    @Autowired
    private ChildRepository childRepository;

    @Autowired
    private VaccineRepository vaccineRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private VaccineInventoryRepository inventoryRepository;

    @Autowired
    private VaccinationRecordRepository vaccinationRecordRepository;

    private Child testChild;
    private Vaccine testVaccine;
    private User testUser;
    private VaccineInventory testInventory;

    @BeforeEach
    void setUp() {
        // Create test child
        testChild = Child.builder()
                .firstName("María")
                .lastName("González")
                .documentNumber("7654321")
                .dateOfBirth(LocalDate.now().minusMonths(6))
                .gender('F')
                .deleted(false)
                .build();
        testChild = childRepository.save(testChild);

        // Create test vaccine
        testVaccine = Vaccine.builder()
                .name("BCG")
                .description("Tuberculosis vaccine")
                .manufacturer("Test Manufacturer")
                .diseasePrevented("Tuberculosis")
                .routeOfAdministration("Intradermal")
                .dosage("0.1 ml")
                .storageConditions("2-8°C")
                .isActive('Y')
                .build();
        testVaccine = vaccineRepository.save(testVaccine);

        // Create test user
        testUser = User.builder()
                .username("doctor")
                .email("doctor@hospital.com")
                .firstName("Dr.")
                .lastName("Smith")
                .password("encoded_password")
                .active(true)
                .build();
        testUser = userRepository.save(testUser);

        // Create test inventory
        testInventory = VaccineInventory.builder()
                .vaccine(testVaccine)
                .batchNumber("BATCH001")
                .quantity(100)
                .manufactureDate(LocalDate.now().minusMonths(2))
                .expirationDate(LocalDate.now().plusYears(1))
                .storageLocation("Refrigerator A")
                .status(VaccineInventory.InventoryStatus.AVAILABLE)
                .receivedBy(testUser)
                .build();
        testInventory = inventoryRepository.save(testInventory);
    }

    @Test
    @DisplayName("Should create vaccination record successfully with inventory deduction")
    void createVaccinationRecord_WithValidData_ShouldSucceedAndDeductInventory() {
        // Arrange
        Integer initialQuantity = testInventory.getQuantity();

        CreateVaccinationRecordRequest request = CreateVaccinationRecordRequest.builder()
                .childId(testChild.getId())
                .vaccineId(testVaccine.getId())
                .doseNumber(1)
                .administrationDate(LocalDate.now())
                .batchNumber("BATCH001")
                .expirationDate(LocalDate.now().plusYears(1))
                .administrationSite("Left arm")
                .notes("First dose")
                .nextDoseDate(LocalDate.now().plusMonths(1))
                .build();

        // Act
        VaccinationRecordDto result = vaccinationRecordService.createVaccinationRecord(
                request,
                testUser.getId()
        );

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isNotNull();
        assertThat(result.getChildId()).isEqualTo(testChild.getId());
        assertThat(result.getVaccineId()).isEqualTo(testVaccine.getId());
        assertThat(result.getDoseNumber()).isEqualTo(1);
        assertThat(result.getBatchNumber()).isEqualTo("BATCH001");
        assertThat(result.getAdministrationSite()).isEqualTo("Left arm");
        assertThat(result.getAdministeredById()).isEqualTo(testUser.getId());

        // Verify inventory was decreased
        VaccineInventory updatedInventory = inventoryRepository.findById(testInventory.getId()).orElse(null);
        assertThat(updatedInventory).isNotNull();
        assertThat(updatedInventory.getQuantity()).isEqualTo(initialQuantity - 1);
    }

    @Test
    @DisplayName("Should throw exception when creating record with deleted child")
    void createVaccinationRecord_WithDeletedChild_ShouldThrowException() {
        // Arrange
        testChild.setDeleted(true);
        childRepository.save(testChild);

        CreateVaccinationRecordRequest request = CreateVaccinationRecordRequest.builder()
                .childId(testChild.getId())
                .vaccineId(testVaccine.getId())
                .doseNumber(1)
                .administrationDate(LocalDate.now())
                .batchNumber("BATCH001")
                .expirationDate(LocalDate.now().plusYears(1))
                .administrationSite("Left arm")
                .build();

        // Act & Assert
        assertThatThrownBy(() -> vaccinationRecordService.createVaccinationRecord(
                request,
                testUser.getId()
        ))
        .isInstanceOf(BusinessException.class)
        .hasMessageContaining("Cannot create vaccination record for deleted child");
    }

    @Test
    @DisplayName("Should throw exception when creating record with inactive vaccine")
    void createVaccinationRecord_WithInactiveVaccine_ShouldThrowException() {
        // Arrange
        testVaccine.setIsActive('N');
        vaccineRepository.save(testVaccine);

        CreateVaccinationRecordRequest request = CreateVaccinationRecordRequest.builder()
                .childId(testChild.getId())
                .vaccineId(testVaccine.getId())
                .doseNumber(1)
                .administrationDate(LocalDate.now())
                .batchNumber("BATCH001")
                .expirationDate(LocalDate.now().plusYears(1))
                .administrationSite("Left arm")
                .build();

        // Act & Assert
        assertThatThrownBy(() -> vaccinationRecordService.createVaccinationRecord(
                request,
                testUser.getId()
        ))
        .isInstanceOf(BusinessException.class)
        .hasMessageContaining("Cannot administer inactive vaccine");
    }

    @Test
    @DisplayName("Should throw exception when expiration date is before administration date")
    void createVaccinationRecord_WithInvalidExpirationDate_ShouldThrowException() {
        // Arrange
        CreateVaccinationRecordRequest request = CreateVaccinationRecordRequest.builder()
                .childId(testChild.getId())
                .vaccineId(testVaccine.getId())
                .doseNumber(1)
                .administrationDate(LocalDate.now())
                .batchNumber("BATCH001")
                .expirationDate(LocalDate.now().minusDays(1))
                .administrationSite("Left arm")
                .build();

        // Act & Assert
        assertThatThrownBy(() -> vaccinationRecordService.createVaccinationRecord(
                request,
                testUser.getId()
        ))
        .isInstanceOf(BusinessException.class)
        .hasMessageContaining("Expiration date must be after administration date");
    }

    @Test
    @DisplayName("Should throw exception when inventory has insufficient quantity")
    void createVaccinationRecord_WithInsufficientInventory_ShouldThrowException() {
        // Arrange
        testInventory.setQuantity(0);
        inventoryRepository.save(testInventory);

        CreateVaccinationRecordRequest request = CreateVaccinationRecordRequest.builder()
                .childId(testChild.getId())
                .vaccineId(testVaccine.getId())
                .doseNumber(1)
                .administrationDate(LocalDate.now())
                .batchNumber("BATCH001")
                .expirationDate(LocalDate.now().plusYears(1))
                .administrationSite("Left arm")
                .build();

        // Act & Assert
        assertThatThrownBy(() -> vaccinationRecordService.createVaccinationRecord(
                request,
                testUser.getId()
        ))
        .isInstanceOf(BusinessException.class)
        .hasMessageContaining("not available or has insufficient quantity");
    }

    @Test
    @DisplayName("Should retrieve upcoming vaccinations within specified days")
    void getUpcomingVaccinations_WithMultipleRecords_ShouldReturnOnlyUpcoming() {
        // Arrange - Create vaccination records with different next dose dates
        createTestVaccinationRecord(LocalDate.now().plusDays(5));
        createTestVaccinationRecord(LocalDate.now().plusDays(15));
        createTestVaccinationRecord(LocalDate.now().plusDays(25));
        createTestVaccinationRecord(LocalDate.now().plusDays(35));

        // Act - Get upcoming vaccinations for next 30 days
        List<VaccinationRecordDto> result = vaccinationRecordService.getUpcomingVaccinations(30);

        // Assert
        assertThat(result).isNotEmpty();
        assertThat(result).hasSize(3); // Only 3 within 30 days
        assertThat(result).allMatch(record -> {
            LocalDate nextDose = record.getNextDoseDate();
            return nextDose != null &&
                   !nextDose.isBefore(LocalDate.now()) &&
                   !nextDose.isAfter(LocalDate.now().plusDays(30));
        });
    }

    @Test
    @DisplayName("Should return empty list when no upcoming vaccinations exist")
    void getUpcomingVaccinations_WithNoUpcomingRecords_ShouldReturnEmptyList() {
        // Arrange - Create records with no next dose dates
        VaccinationRecord record = createTestVaccinationRecord(null);
        vaccinationRecordRepository.save(record);

        // Act
        List<VaccinationRecordDto> result = vaccinationRecordService.getUpcomingVaccinations(30);

        // Assert
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Should retrieve vaccination records by batch number")
    void getVaccinationRecordsByBatchNumber_WithMultipleRecords_ShouldReturnMatching() {
        // Arrange - Create inventory with different batches
        VaccineInventory inventory2 = VaccineInventory.builder()
                .vaccine(testVaccine)
                .batchNumber("BATCH002")
                .quantity(50)
                .manufactureDate(LocalDate.now().minusMonths(1))
                .expirationDate(LocalDate.now().plusYears(1))
                .storageLocation("Refrigerator B")
                .status(VaccineInventory.InventoryStatus.AVAILABLE)
                .receivedBy(testUser)
                .build();
        inventoryRepository.save(inventory2);

        // Create records with different batch numbers
        createTestVaccinationRecordWithBatch("BATCH001");
        createTestVaccinationRecordWithBatch("BATCH001");
        createTestVaccinationRecordWithBatch("BATCH002");

        // Act
        List<VaccinationRecordDto> result = vaccinationRecordService.getVaccinationRecordsByBatchNumber("BATCH001");

        // Assert
        assertThat(result).isNotEmpty();
        assertThat(result).hasSize(2);
        assertThat(result).allMatch(record -> record.getBatchNumber().equals("BATCH001"));
    }

    @Test
    @DisplayName("Should return empty list when batch number has no records")
    void getVaccinationRecordsByBatchNumber_WithNonExistentBatch_ShouldReturnEmptyList() {
        // Arrange - Create some records
        createTestVaccinationRecordWithBatch("BATCH001");

        // Act
        List<VaccinationRecordDto> result = vaccinationRecordService.getVaccinationRecordsByBatchNumber("BATCH999");

        // Assert
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Should handle case-sensitive batch number search")
    void getVaccinationRecordsByBatchNumber_CaseSensitive_ShouldMatchExactly() {
        // Arrange
        createTestVaccinationRecordWithBatch("BATCH001");

        // Act
        List<VaccinationRecordDto> upperCaseResult = vaccinationRecordService.getVaccinationRecordsByBatchNumber("BATCH001");
        List<VaccinationRecordDto> lowerCaseResult = vaccinationRecordService.getVaccinationRecordsByBatchNumber("batch001");

        // Assert
        assertThat(upperCaseResult).hasSize(1);
        assertThat(lowerCaseResult).isEmpty(); // Case-sensitive
    }

    /**
     * Helper method to create a test vaccination record with a specific next dose date.
     */
    private VaccinationRecord createTestVaccinationRecord(LocalDate nextDoseDate) {
        VaccinationRecord record = VaccinationRecord.builder()
                .child(testChild)
                .vaccine(testVaccine)
                .doseNumber(1)
                .administrationDate(LocalDate.now())
                .batchNumber("BATCH001")
                .expirationDate(LocalDate.now().plusYears(1))
                .administeredBy(testUser)
                .administrationSite("Left arm")
                .notes("Test vaccination")
                .nextDoseDate(nextDoseDate)
                .build();
        return vaccinationRecordRepository.save(record);
    }

    /**
     * Helper method to create a test vaccination record with a specific batch number.
     */
    private VaccinationRecord createTestVaccinationRecordWithBatch(String batchNumber) {
        VaccinationRecord record = VaccinationRecord.builder()
                .child(testChild)
                .vaccine(testVaccine)
                .doseNumber(1)
                .administrationDate(LocalDate.now())
                .batchNumber(batchNumber)
                .expirationDate(LocalDate.now().plusYears(1))
                .administeredBy(testUser)
                .administrationSite("Left arm")
                .notes("Test vaccination")
                .build();
        return vaccinationRecordRepository.save(record);
    }
}
