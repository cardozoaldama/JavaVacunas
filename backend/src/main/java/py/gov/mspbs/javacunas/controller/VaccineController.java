package py.gov.mspbs.javacunas.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import py.gov.mspbs.javacunas.dto.VaccineDto;
import py.gov.mspbs.javacunas.service.VaccineService;

import java.util.List;

/**
 * REST controller for vaccine catalog.
 */
@RestController
@RequestMapping("/api/v1/vaccines")
@RequiredArgsConstructor
@Tag(name = "Vaccines", description = "Vaccine catalog operations")
@SecurityRequirement(name = "Bearer Authentication")
public class VaccineController {

    private final VaccineService vaccineService;

    /**
     * Get all active vaccines.
     */
    @GetMapping
    @Operation(summary = "Get all vaccines", description = "Retrieve all active vaccines")
    public ResponseEntity<List<VaccineDto>> getAllVaccines() {
        List<VaccineDto> vaccines = vaccineService.getAllActiveVaccines();
        return ResponseEntity.ok(vaccines);
    }

    /**
     * Get vaccine by ID.
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get vaccine by ID", description = "Retrieve vaccine details by ID")
    public ResponseEntity<VaccineDto> getVaccineById(@PathVariable Long id) {
        VaccineDto vaccine = vaccineService.getVaccineById(id);
        return ResponseEntity.ok(vaccine);
    }

    /**
     * Get vaccine by name.
     */
    @GetMapping("/name/{name}")
    @Operation(summary = "Get vaccine by name", description = "Retrieve vaccine by name")
    public ResponseEntity<VaccineDto> getVaccineByName(@PathVariable String name) {
        VaccineDto vaccine = vaccineService.getVaccineByName(name);
        return ResponseEntity.ok(vaccine);
    }

    /**
     * Search vaccines by disease.
     */
    @GetMapping("/search")
    @Operation(summary = "Search vaccines by disease", description = "Search vaccines by disease prevented")
    public ResponseEntity<List<VaccineDto>> searchByDisease(@RequestParam String disease) {
        List<VaccineDto> vaccines = vaccineService.searchByDisease(disease);
        return ResponseEntity.ok(vaccines);
    }

}
