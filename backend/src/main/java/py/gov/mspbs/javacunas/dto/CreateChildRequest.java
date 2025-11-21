package py.gov.mspbs.javacunas.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import py.gov.mspbs.javacunas.entity.Child;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Request DTO for creating a new child.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateChildRequest {

    @NotBlank(message = "First name is required")
    @Size(min = 2, max = 100, message = "First name must be between 2 and 100 characters")
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(min = 2, max = 100, message = "Last name must be between 2 and 100 characters")
    private String lastName;

    @NotBlank(message = "Document number is required")
    @Pattern(regexp = "^\\d{6,8}$", message = "Document number must be 6-8 digits")
    private String documentNumber;

    @NotNull(message = "Date of birth is required")
    @Past(message = "Date of birth must be in the past")
    private LocalDate dateOfBirth;

    @NotNull(message = "Gender is required")
    private Child.Gender gender;

    @Size(max = 10, message = "Blood type must be maximum 10 characters")
    private String bloodType;

    @DecimalMin(value = "0.0", message = "Birth weight must be positive")
    @DecimalMax(value = "99.99", message = "Birth weight must be less than 100kg")
    private BigDecimal birthWeight;

    @DecimalMin(value = "0.0", message = "Birth height must be positive")
    @DecimalMax(value = "99.99", message = "Birth height must be less than 100cm")
    private BigDecimal birthHeight;

}
