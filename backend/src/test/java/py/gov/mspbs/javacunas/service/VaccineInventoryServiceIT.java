package py.gov.mspbs.javacunas.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import py.gov.mspbs.javacunas.BaseIT;
import py.gov.mspbs.javacunas.entity.User;
import py.gov.mspbs.javacunas.entity.Vaccine;
import py.gov.mspbs.javacunas.entity.VaccineInventory;
import py.gov.mspbs.javacunas.exception.BusinessException;
import py.gov.mspbs.javacunas.exception.DuplicateResourceException;
import py.gov.mspbs.javacunas.exception.ResourceNotFoundException;
import py.gov.mspbs.javacunas.repository.UserRepository;
import py.gov.mspbs.javacunas.repository.VaccineInventoryRepository;
import py.gov.mspbs.javacunas.repository.VaccineRepository;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Integration tests for VaccineInventoryService.
 * Tests the 4 most critical methods:
 * - addInventory
 * - updateQuantity
 * - getExpiringSoonInventory
 * - getLowStockInventory
 */
@DisplayName("VaccineInventoryService Integration Tests")
class VaccineInventoryServiceIT extends BaseIT {

    @Autowired
    private VaccineInventoryService vaccineInventoryService;

    @Autowired
    private VaccineRepository vaccineRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private VaccineInventoryRepository inventoryRepository;

    private Vaccine testVaccine;
    private User testUser;

    @BeforeEach
    void setUp() {
        // Create test vaccine
        testVaccine = Vaccine.builder()
                .name("Hepatitis B")
                .description("Hepatitis B vaccine")
                .manufacturer("Pharma Corp")
                .diseasePrevented("Hepatitis B")
                .routeOfAdministration("Intramuscular")
                .dosage("0.5 ml")
                .storageConditions("2-8°C")
                .isActive('Y')
                .build();
        testVaccine = vaccineRepository.save(testVaccine);

        // Create test user
        testUser = User.builder()
                .username("warehouse")
                .email("warehouse@hospital.com")
                .firstName("Warehouse")
                .lastName("Manager")
                .password("encoded_password")
                .active(true)
                .build();
        testUser = userRepository.save(testUser);
    }

    @Test
    @DisplayName("Should add inventory successfully with valid data")
    void addInventory_WithValidData_ShouldSucceed() {
        // Arrange
        String batchNumber = "HEP-B-2024-001";
        Integer quantity = 100;
        LocalDate manufactureDate = LocalDate.now().minusMonths(1);
        LocalDate expirationDate = LocalDate.now().plusYears(2);
        String storageLocation = "Freezer A-1";

        // Act
        VaccineInventory result = vaccineInventoryService.addInventory(
                testVaccine.getId(),
                batchNumber,
                quantity,
                manufactureDate,
                expirationDate,
                storageLocation,
                testUser.getId()
        );

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isNotNull();
        assertThat(result.getVaccine().getId()).isEqualTo(testVaccine.getId());
        assertThat(result.getBatchNumber()).isEqualTo(batchNumber);
        assertThat(result.getQuantity()).isEqualTo(quantity);
        assertThat(result.getManufactureDate()).isEqualTo(manufactureDate);
        assertThat(result.getExpirationDate()).isEqualTo(expirationDate);
        assertThat(result.getStorageLocation()).isEqualTo(storageLocation);
        assertThat(result.getStatus()).isEqualTo(VaccineInventory.InventoryStatus.AVAILABLE);
        assertThat(result.getReceivedBy().getId()).isEqualTo(testUser.getId());

        // Verify it's persisted in database
        VaccineInventory savedInventory = inventoryRepository.findById(result.getId()).orElse(null);
        assertThat(savedInventory).isNotNull();
        assertThat(savedInventory.getBatchNumber()).isEqualTo(batchNumber);
    }

    @Test
    @DisplayName("Should throw exception when adding duplicate batch number")
    void addInventory_WithDuplicateBatchNumber_ShouldThrowException() {
        // Arrange
        String batchNumber = "HEP-B-DUP-001";
        vaccineInventoryService.addInventory(
                testVaccine.getId(),
                batchNumber,
                100,
                LocalDate.now().minusMonths(1),
                LocalDate.now().plusYears(2),
                "Storage A",
                testUser.getId()
        );

        // Act & Assert
        assertThatThrownBy(() -> vaccineInventoryService.addInventory(
                testVaccine.getId(),
                batchNumber,
                50,
                LocalDate.now().minusMonths(1),
                LocalDate.now().plusYears(2),
                "Storage B",
                testUser.getId()
        ))
        .isInstanceOf(DuplicateResourceException.class)
        .hasMessageContaining("batchNumber");
    }

    @Test
    @DisplayName("Should throw exception when expiration date is before manufacture date")
    void addInventory_WithInvalidDates_ShouldThrowException() {
        // Arrange
        LocalDate manufactureDate = LocalDate.now();
        LocalDate expirationDate = LocalDate.now().minusMonths(1);

        // Act & Assert
        assertThatThrownBy(() -> vaccineInventoryService.addInventory(
                testVaccine.getId(),
                "BATCH-INVALID-001",
                100,
                manufactureDate,
                expirationDate,
                "Storage A",
                testUser.getId()
        ))
        .isInstanceOf(BusinessException.class)
        .hasMessageContaining("Expiration date must be after manufacture date");
    }

    @Test
    @DisplayName("Should throw exception when adding expired vaccine")
    void addInventory_WithExpiredVaccine_ShouldThrowException() {
        // Arrange
        LocalDate manufactureDate = LocalDate.now().minusYears(2);
        LocalDate expirationDate = LocalDate.now().minusDays(1);

        // Act & Assert
        assertThatThrownBy(() -> vaccineInventoryService.addInventory(
                testVaccine.getId(),
                "BATCH-EXPIRED-001",
                100,
                manufactureDate,
                expirationDate,
                "Storage A",
                testUser.getId()
        ))
        .isInstanceOf(BusinessException.class)
        .hasMessageContaining("Cannot add expired vaccine to inventory");
    }

    @Test
    @DisplayName("Should throw exception when vaccine does not exist")
    void addInventory_WithNonExistentVaccine_ShouldThrowException() {
        // Arrange
        Long nonExistentVaccineId = 99999L;

        // Act & Assert
        assertThatThrownBy(() -> vaccineInventoryService.addInventory(
                nonExistentVaccineId,
                "BATCH-001",
                100,
                LocalDate.now().minusMonths(1),
                LocalDate.now().plusYears(2),
                "Storage A",
                testUser.getId()
        ))
        .isInstanceOf(ResourceNotFoundException.class)
        .hasMessageContaining("Vaccine");
    }

    @Test
    @DisplayName("Should update quantity successfully")
    void updateQuantity_WithValidData_ShouldSucceed() {
        // Arrange
        VaccineInventory inventory = vaccineInventoryService.addInventory(
                testVaccine.getId(),
                "BATCH-UPDATE-001",
                100,
                LocalDate.now().minusMonths(1),
                LocalDate.now().plusYears(2),
                "Storage A",
                testUser.getId()
        );

        Integer newQuantity = 150;

        // Act
        VaccineInventory result = vaccineInventoryService.updateQuantity(
                inventory.getId(),
                newQuantity
        );

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(inventory.getId());
        assertThat(result.getQuantity()).isEqualTo(newQuantity);

        // Verify it's persisted in database
        VaccineInventory updatedInventory = inventoryRepository.findById(inventory.getId()).orElse(null);
        assertThat(updatedInventory).isNotNull();
        assertThat(updatedInventory.getQuantity()).isEqualTo(newQuantity);
    }

    @Test
    @DisplayName("Should decrease quantity when new quantity is lower")
    void updateQuantity_WithLowerQuantity_ShouldDecrease() {
        // Arrange
        VaccineInventory inventory = vaccineInventoryService.addInventory(
                testVaccine.getId(),
                "BATCH-DECREASE-001",
                100,
                LocalDate.now().minusMonths(1),
                LocalDate.now().plusYears(2),
                "Storage A",
                testUser.getId()
        );

        Integer newQuantity = 50;

        // Act
        VaccineInventory result = vaccineInventoryService.updateQuantity(
                inventory.getId(),
                newQuantity
        );

        // Assert
        assertThat(result.getQuantity()).isEqualTo(newQuantity);
    }

    @Test
    @DisplayName("Should throw exception when updating to negative quantity")
    void updateQuantity_WithNegativeQuantity_ShouldThrowException() {
        // Arrange
        VaccineInventory inventory = vaccineInventoryService.addInventory(
                testVaccine.getId(),
                "BATCH-NEG-001",
                100,
                LocalDate.now().minusMonths(1),
                LocalDate.now().plusYears(2),
                "Storage A",
                testUser.getId()
        );

        // Act & Assert
        assertThatThrownBy(() -> vaccineInventoryService.updateQuantity(
                inventory.getId(),
                -10
        ))
        .isInstanceOf(BusinessException.class)
        .hasMessageContaining("Quantity cannot be negative");
    }

    @Test
    @DisplayName("Should retrieve inventory expiring soon")
    void getExpiringSoonInventory_WithMultipleInventories_ShouldReturnExpiringSoon() {
        // Arrange - Create inventories with different expiration dates
        // Expiring in 15 days (should be included)
        vaccineInventoryService.addInventory(
                testVaccine.getId(),
                "BATCH-EXP-SOON-1",
                100,
                LocalDate.now().minusMonths(1),
                LocalDate.now().plusDays(15),
                "Storage A",
                testUser.getId()
        );

        // Expiring in 25 days (should be included)
        vaccineInventoryService.addInventory(
                testVaccine.getId(),
                "BATCH-EXP-SOON-2",
                50,
                LocalDate.now().minusMonths(1),
                LocalDate.now().plusDays(25),
                "Storage B",
                testUser.getId()
        );

        // Expiring in 2 years (should NOT be included)
        vaccineInventoryService.addInventory(
                testVaccine.getId(),
                "BATCH-EXP-LATER",
                200,
                LocalDate.now().minusMonths(1),
                LocalDate.now().plusYears(2),
                "Storage C",
                testUser.getId()
        );

        // Act
        List<VaccineInventory> result = vaccineInventoryService.getExpiringSoonInventory();

        // Assert
        assertThat(result).isNotEmpty();
        assertThat(result).hasSize(2);
        assertThat(result).allMatch(inventory -> {
            LocalDate expDate = inventory.getExpirationDate();
            return !expDate.isBefore(LocalDate.now()) &&
                   !expDate.isAfter(LocalDate.now().plusDays(30));
        });
    }

    @Test
    @DisplayName("Should return empty list when no inventory is expiring soon")
    void getExpiringSoonInventory_WithNoExpiringSoon_ShouldReturnEmptyList() {
        // Arrange - Only create inventory expiring far in the future
        vaccineInventoryService.addInventory(
                testVaccine.getId(),
                "BATCH-FUTURE",
                100,
                LocalDate.now().minusMonths(1),
                LocalDate.now().plusYears(3),
                "Storage A",
                testUser.getId()
        );

        // Act
        List<VaccineInventory> result = vaccineInventoryService.getExpiringSoonInventory();

        // Assert
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Should retrieve low stock inventory")
    void getLowStockInventory_WithMultipleInventories_ShouldReturnLowStock() {
        // Arrange - Create inventories with different quantities
        // Low stock (5 doses)
        vaccineInventoryService.addInventory(
                testVaccine.getId(),
                "BATCH-LOW-1",
                5,
                LocalDate.now().minusMonths(1),
                LocalDate.now().plusYears(1),
                "Storage A",
                testUser.getId()
        );

        // Low stock (8 doses)
        vaccineInventoryService.addInventory(
                testVaccine.getId(),
                "BATCH-LOW-2",
                8,
                LocalDate.now().minusMonths(1),
                LocalDate.now().plusYears(1),
                "Storage B",
                testUser.getId()
        );

        // Normal stock (100 doses)
        vaccineInventoryService.addInventory(
                testVaccine.getId(),
                "BATCH-NORMAL",
                100,
                LocalDate.now().minusMonths(1),
                LocalDate.now().plusYears(1),
                "Storage C",
                testUser.getId()
        );

        // Act
        List<VaccineInventory> result = vaccineInventoryService.getLowStockInventory();

        // Assert
        assertThat(result).isNotEmpty();
        assertThat(result).hasSize(2);
        assertThat(result).allMatch(inventory ->
                inventory.getQuantity() < 10 &&
                inventory.getStatus() == VaccineInventory.InventoryStatus.AVAILABLE
        );
    }

    @Test
    @DisplayName("Should return empty list when all inventory has sufficient stock")
    void getLowStockInventory_WithSufficientStock_ShouldReturnEmptyList() {
        // Arrange - Only create high stock inventory
        vaccineInventoryService.addInventory(
                testVaccine.getId(),
                "BATCH-HIGH-1",
                100,
                LocalDate.now().minusMonths(1),
                LocalDate.now().plusYears(1),
                "Storage A",
                testUser.getId()
        );

        vaccineInventoryService.addInventory(
                testVaccine.getId(),
                "BATCH-HIGH-2",
                200,
                LocalDate.now().minusMonths(1),
                LocalDate.now().plusYears(1),
                "Storage B",
                testUser.getId()
        );

        // Act
        List<VaccineInventory> result = vaccineInventoryService.getLowStockInventory();

        // Assert
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Should not include unavailable inventory in low stock list")
    void getLowStockInventory_WithUnavailableInventory_ShouldExcludeThem() {
        // Arrange - Create low stock but unavailable inventory
        VaccineInventory inventory = vaccineInventoryService.addInventory(
                testVaccine.getId(),
                "BATCH-UNAVAIL",
                5,
                LocalDate.now().minusMonths(1),
                LocalDate.now().plusYears(1),
                "Storage A",
                testUser.getId()
        );

        // Mark as quarantined
        vaccineInventoryService.updateStatus(
                inventory.getId(),
                VaccineInventory.InventoryStatus.QUARANTINED
        );

        // Act
        List<VaccineInventory> result = vaccineInventoryService.getLowStockInventory();

        // Assert
        assertThat(result).isEmpty();
    }
}
