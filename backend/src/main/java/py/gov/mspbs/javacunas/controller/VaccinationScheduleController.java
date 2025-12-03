package py.gov.mspbs.javacunas.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import py.gov.mspbs.javacunas.dto.VaccinationScheduleDto;
import py.gov.mspbs.javacunas.service.VaccinationScheduleService;

import java.util.List;

/**
 * REST controller for vaccination schedules (PAI).
 */
@RestController
@RequestMapping("/api/v1/schedules")
@RequiredArgsConstructor
@Tag(name = "Vaccination Schedules", description = "Vaccination schedule operations (PAI)")
@SecurityRequirement(name = "Bearer Authentication")
public class VaccinationScheduleController {

    private final VaccinationScheduleService scheduleService;

    /**
     * Get Paraguay vaccination schedule.
     */
    @GetMapping("/paraguay")
    @Operation(summary = "Get Paraguay schedule", description = "Retrieve complete PAI vaccination schedule for Paraguay")
    public ResponseEntity<List<VaccinationScheduleDto>> getParaguaySchedule() {
        List<VaccinationScheduleDto> schedules = scheduleService.getParaguaySchedule();
        return ResponseEntity.ok(schedules);
    }

    /**
     * Get vaccination schedule by vaccine ID.
     */
    @GetMapping("/vaccine/{vaccineId}")
    @Operation(summary = "Get schedule by vaccine", description = "Retrieve vaccination schedule for a specific vaccine")
    public ResponseEntity<List<VaccinationScheduleDto>> getScheduleByVaccineId(@PathVariable Long vaccineId) {
        List<VaccinationScheduleDto> schedules = scheduleService.getScheduleByVaccineId(vaccineId);
        return ResponseEntity.ok(schedules);
    }

    /**
     * Get mandatory schedules up to a specific age.
     */
    @GetMapping("/mandatory")
    @Operation(summary = "Get mandatory schedules", description = "Retrieve mandatory vaccination schedules up to specified age")
    public ResponseEntity<List<VaccinationScheduleDto>> getMandatorySchedulesUpToAge(
            @RequestParam Integer ageInMonths) {
        List<VaccinationScheduleDto> schedules = scheduleService.getMandatorySchedulesUpToAge(ageInMonths);
        return ResponseEntity.ok(schedules);
    }

    /**
     * Get all vaccination schedules.
     */
    @GetMapping
    @Operation(summary = "Get all schedules", description = "Retrieve all vaccination schedules")
    public ResponseEntity<List<VaccinationScheduleDto>> getAllSchedules() {
        List<VaccinationScheduleDto> schedules = scheduleService.getAllSchedules();
        return ResponseEntity.ok(schedules);
    }

}
