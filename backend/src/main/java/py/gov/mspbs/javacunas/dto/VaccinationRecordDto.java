package py.gov.mspbs.javacunas.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO for VaccinationRecord entity.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VaccinationRecordDto {

    private Long id;
    private Long childId;
    private String childName;
    private Long vaccineId;
    private String vaccineName;
    private Integer doseNumber;
    private LocalDate administrationDate;
    private String batchNumber;
    private LocalDate expirationDate;
    private Long administeredById;
    private String administeredByName;
    private String administrationSite;
    private String notes;
    private LocalDate nextDoseDate;
    private LocalDateTime createdAt;

}
