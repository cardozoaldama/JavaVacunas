package py.gov.mspbs.javacunas.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO for Vaccine entity.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VaccineDto {

    private Long id;
    private String name;
    private String description;
    private String manufacturer;
    private String diseasePrevented;
    private Integer doseCount;
    private Integer minimumAgeMonths;
    private BigDecimal storageTemperatureMin;
    private BigDecimal storageTemperatureMax;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

}
