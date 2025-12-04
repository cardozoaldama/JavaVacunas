package py.gov.mspbs.javacunas.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entity representing vaccination schedules by country (PAI for Paraguay).
 */
@Entity
@Table(name = "vaccination_schedules")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VaccinationSchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vaccine_id", nullable = false)
    private Vaccine vaccine;

    @Column(name = "country_code", nullable = false, length = 2)
    private String countryCode = "PY";

    @Column(name = "dose_number", nullable = false)
    private Integer doseNumber;

    @Column(name = "recommended_age_months", nullable = false)
    private Integer recommendedAgeMonths;

    @Column(name = "age_range_start_months")
    private Integer ageRangeStartMonths;

    @Column(name = "age_range_end_months")
    private Integer ageRangeEndMonths;

    @Column(name = "is_mandatory", nullable = false)
    private Boolean isMandatory = true;

    @Column(length = 500)
    private String notes;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

}
