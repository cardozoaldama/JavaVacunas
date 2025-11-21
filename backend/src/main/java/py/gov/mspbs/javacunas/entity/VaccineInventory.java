package py.gov.mspbs.javacunas.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entity representing vaccine inventory with batch tracking.
 */
@Entity
@Table(name = "vaccine_inventory", uniqueConstraints = {
    @UniqueConstraint(name = "uq_vi_vaccine_batch", columnNames = {"vaccine_id", "batch_number"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VaccineInventory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vaccine_id", nullable = false)
    private Vaccine vaccine;

    @Column(name = "batch_number", nullable = false, length = 50)
    private String batchNumber;

    @Column(nullable = false)
    private Integer quantity;

    @Column(name = "manufacture_date", nullable = false)
    private LocalDate manufactureDate;

    @Column(name = "expiration_date", nullable = false)
    private LocalDate expirationDate;

    @Column(name = "storage_location", length = 100)
    private String storageLocation;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private InventoryStatus status = InventoryStatus.AVAILABLE;

    @Column(name = "received_date", nullable = false)
    private LocalDate receivedDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "received_by", nullable = false)
    private User receivedBy;

    @Column(length = 500)
    private String notes;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (receivedDate == null) {
            receivedDate = LocalDate.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * Inventory status enum.
     */
    public enum InventoryStatus {
        AVAILABLE,
        RESERVED,
        DEPLETED,
        EXPIRED,
        RECALLED
    }

    /**
     * Decrease quantity by the specified amount.
     */
    public void decreaseQuantity(int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }
        if (this.quantity < amount) {
            throw new IllegalStateException("Insufficient quantity");
        }
        this.quantity -= amount;
        if (this.quantity == 0) {
            this.status = InventoryStatus.DEPLETED;
        }
    }

    /**
     * Increase quantity by the specified amount.
     */
    public void increaseQuantity(int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }
        this.quantity += amount;
        if (this.status == InventoryStatus.DEPLETED && this.quantity > 0) {
            this.status = InventoryStatus.AVAILABLE;
        }
    }

}
