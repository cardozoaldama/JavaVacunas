package py.gov.mspbs.javacunas.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import py.gov.mspbs.javacunas.entity.Child;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO for Child entity.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChildDto {

    private Long id;
    private String firstName;
    private String lastName;
    private String documentNumber;
    private LocalDate dateOfBirth;
    private Child.Gender gender;
    private String bloodType;
    private BigDecimal birthWeight;
    private BigDecimal birthHeight;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Integer ageInMonths;

}
