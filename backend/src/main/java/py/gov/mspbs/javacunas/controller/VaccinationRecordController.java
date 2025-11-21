package py.gov.mspbs.javacunas.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import py.gov.mspbs.javacunas.dto.CreateVaccinationRecordRequest;
import py.gov.mspbs.javacunas.dto.VaccinationRecordDto;
import py.gov.mspbs.javacunas.security.UserPrincipal;
import py.gov.mspbs.javacunas.service.VaccinationRecordService;

import java.util.List;

/**
 * REST controller for vaccination records.
 */
@RestController
@RequestMapping("/api/v1/vaccinations")
@RequiredArgsConstructor
@Tag(name = "Vaccination Records", description = "Vaccination record management")
@SecurityRequirement(name = "Bearer Authentication")
public class VaccinationRecordController {

    private final VaccinationRecordService vaccinationRecordService;

    /**
     * Create a new vaccination record.
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('DOCTOR', 'NURSE')")
    @Operation(summary = "Create vaccination record", description = "Record a new vaccine administration")
    public ResponseEntity<VaccinationRecordDto> createVaccinationRecord(
            @Valid @RequestBody CreateVaccinationRecordRequest request,
            Authentication authentication) {

        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        VaccinationRecordDto record = vaccinationRecordService.createVaccinationRecord(
                request, userPrincipal.getId());

        return ResponseEntity.status(HttpStatus.CREATED).body(record);
    }

    /**
     * Get vaccination record by ID.
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('DOCTOR', 'NURSE', 'PARENT')")
    @Operation(summary = "Get vaccination record", description = "Retrieve vaccination record by ID")
    public ResponseEntity<VaccinationRecordDto> getVaccinationRecordById(@PathVariable Long id) {
        VaccinationRecordDto record = vaccinationRecordService.getVaccinationRecordById(id);
        return ResponseEntity.ok(record);
    }

    /**
     * Get vaccination records by child ID.
     */
    @GetMapping("/child/{childId}")
    @PreAuthorize("hasAnyRole('DOCTOR', 'NURSE', 'PARENT')")
    @Operation(summary = "Get child vaccination history", description = "Retrieve all vaccination records for a child")
    public ResponseEntity<List<VaccinationRecordDto>> getVaccinationRecordsByChildId(@PathVariable Long childId) {
        List<VaccinationRecordDto> records = vaccinationRecordService.getVaccinationRecordsByChildId(childId);
        return ResponseEntity.ok(records);
    }

    /**
     * Get vaccination records by vaccine ID.
     */
    @GetMapping("/vaccine/{vaccineId}")
    @PreAuthorize("hasAnyRole('DOCTOR', 'NURSE')")
    @Operation(summary = "Get records by vaccine", description = "Retrieve all records for a specific vaccine")
    public ResponseEntity<List<VaccinationRecordDto>> getVaccinationRecordsByVaccineId(@PathVariable Long vaccineId) {
        List<VaccinationRecordDto> records = vaccinationRecordService.getVaccinationRecordsByVaccineId(vaccineId);
        return ResponseEntity.ok(records);
    }

    /**
     * Get vaccination records by batch number.
     */
    @GetMapping("/batch/{batchNumber}")
    @PreAuthorize("hasAnyRole('DOCTOR', 'NURSE')")
    @Operation(summary = "Get records by batch", description = "Retrieve all records for a vaccine batch")
    public ResponseEntity<List<VaccinationRecordDto>> getVaccinationRecordsByBatchNumber(
            @PathVariable String batchNumber) {
        List<VaccinationRecordDto> records = vaccinationRecordService.getVaccinationRecordsByBatchNumber(batchNumber);
        return ResponseEntity.ok(records);
    }

    /**
     * Get upcoming vaccinations.
     */
    @GetMapping("/upcoming")
    @PreAuthorize("hasAnyRole('DOCTOR', 'NURSE')")
    @Operation(summary = "Get upcoming vaccinations", description = "Retrieve upcoming vaccine doses")
    public ResponseEntity<List<VaccinationRecordDto>> getUpcomingVaccinations(
            @RequestParam(defaultValue = "30") int daysAhead) {
        List<VaccinationRecordDto> records = vaccinationRecordService.getUpcomingVaccinations(daysAhead);
        return ResponseEntity.ok(records);
    }

}
