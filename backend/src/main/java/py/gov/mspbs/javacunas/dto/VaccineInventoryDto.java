package py.gov.mspbs.javacunas.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import py.gov.mspbs.javacunas.entity.VaccineInventory;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO for VaccineInventory entity.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VaccineInventoryDto {

    private Long id;
    private VaccineDto vaccine;
    private String batchNumber;
    private Integer quantity;
    private LocalDate manufactureDate;
    private LocalDate expirationDate;
    private String storageLocation;
    private VaccineInventory.InventoryStatus status;
    private LocalDate receivedDate;
    private UserDto receivedBy;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

}
