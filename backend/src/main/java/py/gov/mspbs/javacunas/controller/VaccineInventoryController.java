package py.gov.mspbs.javacunas.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import py.gov.mspbs.javacunas.dto.VaccineInventoryDto;
import py.gov.mspbs.javacunas.entity.VaccineInventory;
import py.gov.mspbs.javacunas.security.UserPrincipal;
import py.gov.mspbs.javacunas.service.VaccineInventoryService;

import java.time.LocalDate;
import java.util.List;

/**
 * REST controller for vaccine inventory management.
 */
@RestController
@RequestMapping("/api/v1/inventory")
@RequiredArgsConstructor
@Tag(name = "Vaccine Inventory", description = "Vaccine inventory management operations")
@SecurityRequirement(name = "Bearer Authentication")
@PreAuthorize("hasAnyRole('DOCTOR', 'NURSE')")
public class VaccineInventoryController {

    private final VaccineInventoryService inventoryService;

    /**
     * Add new vaccine inventory.
     */
    @PostMapping
    @Operation(summary = "Add inventory", description = "Add new vaccine batch to inventory")
    public ResponseEntity<VaccineInventoryDto> addInventory(
            @RequestParam Long vaccineId,
            @RequestParam String batchNumber,
            @RequestParam Integer quantity,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate manufactureDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate expirationDate,
            @RequestParam(required = false) String storageLocation,
            Authentication authentication) {

        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        VaccineInventoryDto inventory = inventoryService.addInventory(
                vaccineId, batchNumber, quantity, manufactureDate, expirationDate,
                storageLocation, userPrincipal.getId());

        return ResponseEntity.status(HttpStatus.CREATED).body(inventory);
    }

    /**
     * Get all inventory.
     */
    @GetMapping
    @Operation(summary = "Get all inventory", description = "Retrieve all vaccine inventory")
    public ResponseEntity<List<VaccineInventoryDto>> getAllInventory() {
        List<VaccineInventoryDto> inventory = inventoryService.getAllInventory();
        return ResponseEntity.ok(inventory);
    }

    /**
     * Get inventory by ID.
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get inventory by ID", description = "Retrieve inventory record by ID")
    public ResponseEntity<VaccineInventoryDto> getInventoryById(@PathVariable Long id) {
        VaccineInventoryDto inventory = inventoryService.getInventoryById(id);
        return ResponseEntity.ok(inventory);
    }

    /**
     * Get available inventory for a vaccine.
     */
    @GetMapping("/vaccine/{vaccineId}")
    @Operation(summary = "Get available inventory", description = "Retrieve available inventory for a vaccine")
    public ResponseEntity<List<VaccineInventoryDto>> getAvailableInventoryForVaccine(@PathVariable Long vaccineId) {
        List<VaccineInventoryDto> inventory = inventoryService.getAvailableInventoryForVaccine(vaccineId);
        return ResponseEntity.ok(inventory);
    }

    /**
     * Get inventory expiring soon.
     */
    @GetMapping("/expiring-soon")
    @Operation(summary = "Get expiring inventory", description = "Retrieve inventory expiring soon")
    public ResponseEntity<List<VaccineInventoryDto>> getExpiringSoonInventory() {
        List<VaccineInventoryDto> inventory = inventoryService.getExpiringSoonInventory();
        return ResponseEntity.ok(inventory);
    }

    /**
     * Get low stock inventory.
     */
    @GetMapping("/low-stock")
    @Operation(summary = "Get low stock inventory", description = "Retrieve inventory with low stock")
    public ResponseEntity<List<VaccineInventoryDto>> getLowStockInventory() {
        List<VaccineInventoryDto> inventory = inventoryService.getLowStockInventory();
        return ResponseEntity.ok(inventory);
    }

    /**
     * Get total available quantity for a vaccine.
     */
    @GetMapping("/vaccine/{vaccineId}/quantity")
    @Operation(summary = "Get total quantity", description = "Get total available quantity for a vaccine")
    public ResponseEntity<Integer> getTotalAvailableQuantity(@PathVariable Long vaccineId) {
        Integer quantity = inventoryService.getTotalAvailableQuantity(vaccineId);
        return ResponseEntity.ok(quantity);
    }

    /**
     * Update inventory quantity.
     */
    @PutMapping("/{id}/quantity")
    @Operation(summary = "Update quantity", description = "Update inventory quantity")
    public ResponseEntity<VaccineInventoryDto> updateQuantity(
            @PathVariable Long id,
            @RequestParam Integer quantity) {
        VaccineInventoryDto inventory = inventoryService.updateQuantity(id, quantity);
        return ResponseEntity.ok(inventory);
    }

    /**
     * Update inventory status.
     */
    @PutMapping("/{id}/status")
    @Operation(summary = "Update status", description = "Update inventory status")
    public ResponseEntity<VaccineInventoryDto> updateStatus(
            @PathVariable Long id,
            @RequestParam VaccineInventory.InventoryStatus status) {
        VaccineInventoryDto inventory = inventoryService.updateStatus(id, status);
        return ResponseEntity.ok(inventory);
    }

}
