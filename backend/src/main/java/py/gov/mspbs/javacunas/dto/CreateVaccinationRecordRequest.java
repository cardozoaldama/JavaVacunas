package py.gov.mspbs.javacunas.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Request DTO for creating a vaccination record.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateVaccinationRecordRequest {

    @NotNull(message = "Child ID is required")
    private Long childId;

    @NotNull(message = "Vaccine ID is required")
    private Long vaccineId;

    private Long scheduleId;

    @NotNull(message = "Dose number is required")
    @Min(value = 1, message = "Dose number must be at least 1")
    private Integer doseNumber;

    @NotNull(message = "Administration date is required")
    @PastOrPresent(message = "Administration date cannot be in the future")
    private LocalDate administrationDate;

    @NotBlank(message = "Batch number is required")
    @Size(max = 50, message = "Batch number must be maximum 50 characters")
    private String batchNumber;

    @NotNull(message = "Expiration date is required")
    @Future(message = "Expiration date must be in the future")
    private LocalDate expirationDate;

    @Size(max = 50, message = "Administration site must be maximum 50 characters")
    private String administrationSite;

    @Size(max = 500, message = "Notes must be maximum 500 characters")
    private String notes;

    @Future(message = "Next dose date must be in the future")
    private LocalDate nextDoseDate;

}
