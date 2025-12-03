package py.gov.mspbs.javacunas.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for VaccinationSchedule entity.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VaccinationScheduleDto {

    private Long id;
    private VaccineDto vaccine;
    private String countryCode;
    private Integer doseNumber;
    private Integer recommendedAgeMonths;
    private Integer ageRangeStartMonths;
    private Integer ageRangeEndMonths;
    private Boolean isMandatory;
    private String notes;
    private LocalDateTime createdAt;

}
