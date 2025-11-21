package py.gov.mspbs.javacunas.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entity representing vaccine master data.
 */
@Entity
@Table(name = "vaccines")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Vaccine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 500)
    private String description;

    @Column(length = 100)
    private String manufacturer;

    @Column(name = "disease_prevented", nullable = false, length = 255)
    private String diseasePrevented;

    @Column(name = "dose_count", nullable = false)
    private Integer doseCount = 1;

    @Column(name = "minimum_age_months")
    private Integer minimumAgeMonths;

    @Column(name = "storage_temperature_min", precision = 4, scale = 1)
    private BigDecimal storageTemperatureMin;

    @Column(name = "storage_temperature_max", precision = 4, scale = 1)
    private BigDecimal storageTemperatureMax;

    @Column(name = "is_active", nullable = false, length = 1)
    private Character isActive = 'Y';

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

}
