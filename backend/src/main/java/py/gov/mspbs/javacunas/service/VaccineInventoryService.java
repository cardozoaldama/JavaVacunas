package py.gov.mspbs.javacunas.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import py.gov.mspbs.javacunas.entity.Vaccine;
import py.gov.mspbs.javacunas.entity.VaccineInventory;
import py.gov.mspbs.javacunas.entity.User;
import py.gov.mspbs.javacunas.exception.BusinessException;
import py.gov.mspbs.javacunas.exception.DuplicateResourceException;
import py.gov.mspbs.javacunas.exception.ResourceNotFoundException;
import py.gov.mspbs.javacunas.repository.VaccineInventoryRepository;
import py.gov.mspbs.javacunas.repository.VaccineRepository;
import py.gov.mspbs.javacunas.repository.UserRepository;

import java.time.LocalDate;
import java.util.List;

/**
 * Service for managing vaccine inventory.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class VaccineInventoryService {

    private final VaccineInventoryRepository inventoryRepository;
    private final VaccineRepository vaccineRepository;
    private final UserRepository userRepository;

    private static final int LOW_STOCK_THRESHOLD = 10;
    private static final int EXPIRATION_WARNING_DAYS = 30;

    /**
     * Add new vaccine inventory.
     */
    @Transactional
    public VaccineInventory addInventory(Long vaccineId, String batchNumber, Integer quantity,
                                        LocalDate manufactureDate, LocalDate expirationDate,
                                        String storageLocation, Long receivedByUserId) {
        log.info("Adding inventory for vaccine {} with batch {}", vaccineId, batchNumber);

        // Validate vaccine exists
        Vaccine vaccine = vaccineRepository.findById(vaccineId)
                .orElseThrow(() -> new ResourceNotFoundException("Vaccine", "id", vaccineId));

        // Validate user exists
        User user = userRepository.findById(receivedByUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", receivedByUserId));

        // Check if batch already exists for this vaccine
        if (inventoryRepository.findByVaccineIdAndBatchNumber(vaccineId, batchNumber).isPresent()) {
            throw new DuplicateResourceException("VaccineInventory", "batchNumber", batchNumber);
        }

        // Validate dates
        if (expirationDate.isBefore(manufactureDate)) {
            throw new BusinessException("Expiration date must be after manufacture date");
        }

        if (expirationDate.isBefore(LocalDate.now())) {
            throw new BusinessException("Cannot add expired vaccine to inventory");
        }

        VaccineInventory inventory = VaccineInventory.builder()
                .vaccine(vaccine)
                .batchNumber(batchNumber)
                .quantity(quantity)
                .manufactureDate(manufactureDate)
                .expirationDate(expirationDate)
                .storageLocation(storageLocation)
                .status(VaccineInventory.InventoryStatus.AVAILABLE)
                .receivedBy(user)
                .build();

        VaccineInventory saved = inventoryRepository.save(inventory);
        log.info("Inventory added successfully with ID: {}", saved.getId());

        return saved;
    }

    /**
     * Get inventory by ID.
     */
    @Transactional(readOnly = true)
    public VaccineInventory getInventoryById(Long id) {
        log.debug("Retrieving inventory with ID: {}", id);
        return inventoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("VaccineInventory", "id", id));
    }

    /**
     * Get available inventory for a vaccine.
     */
    @Transactional(readOnly = true)
    public List<VaccineInventory> getAvailableInventoryForVaccine(Long vaccineId) {
        log.debug("Retrieving available inventory for vaccine: {}", vaccineId);
        return inventoryRepository.findAvailableByVaccineId(vaccineId);
    }

    /**
     * Get inventory expiring soon.
     */
    @Transactional(readOnly = true)
    public List<VaccineInventory> getExpiringSoonInventory() {
        log.debug("Retrieving inventory expiring in next {} days", EXPIRATION_WARNING_DAYS);
        LocalDate today = LocalDate.now();
        LocalDate endDate = today.plusDays(EXPIRATION_WARNING_DAYS);
        return inventoryRepository.findExpiringSoon(today, endDate);
    }

    /**
     * Get low stock inventory.
     */
    @Transactional(readOnly = true)
    public List<VaccineInventory> getLowStockInventory() {
        log.debug("Retrieving inventory with stock below {}", LOW_STOCK_THRESHOLD);
        return inventoryRepository.findLowStock(LOW_STOCK_THRESHOLD);
    }

    /**
     * Get total available quantity for a vaccine.
     */
    @Transactional(readOnly = true)
    public Integer getTotalAvailableQuantity(Long vaccineId) {
        log.debug("Calculating total available quantity for vaccine: {}", vaccineId);
        return inventoryRepository.calculateTotalAvailableQuantity(vaccineId);
    }

    /**
     * Update inventory quantity.
     */
    @Transactional
    public VaccineInventory updateQuantity(Long inventoryId, Integer newQuantity) {
        log.info("Updating inventory {} quantity to {}", inventoryId, newQuantity);

        VaccineInventory inventory = inventoryRepository.findById(inventoryId)
                .orElseThrow(() -> new ResourceNotFoundException("VaccineInventory", "id", inventoryId));

        if (newQuantity < 0) {
            throw new BusinessException("Quantity cannot be negative");
        }

        int difference = newQuantity - inventory.getQuantity();
        if (difference > 0) {
            inventory.increaseQuantity(difference);
        } else if (difference < 0) {
            inventory.decreaseQuantity(Math.abs(difference));
        }

        VaccineInventory updated = inventoryRepository.save(inventory);
        log.info("Inventory quantity updated successfully");

        return updated;
    }

    /**
     * Update inventory status.
     */
    @Transactional
    public VaccineInventory updateStatus(Long inventoryId, VaccineInventory.InventoryStatus status) {
        log.info("Updating inventory {} status to {}", inventoryId, status);

        VaccineInventory inventory = inventoryRepository.findById(inventoryId)
                .orElseThrow(() -> new ResourceNotFoundException("VaccineInventory", "id", inventoryId));

        inventory.setStatus(status);

        VaccineInventory updated = inventoryRepository.save(inventory);
        log.info("Inventory status updated successfully");

        return updated;
    }

    /**
     * Get all inventory.
     */
    @Transactional(readOnly = true)
    public List<VaccineInventory> getAllInventory() {
        log.debug("Retrieving all inventory");
        return inventoryRepository.findAll();
    }

}
