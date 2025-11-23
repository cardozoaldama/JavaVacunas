package py.gov.mspbs.javacunas.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import py.gov.mspbs.javacunas.dto.ChildDto;
import py.gov.mspbs.javacunas.dto.CreateChildRequest;
import py.gov.mspbs.javacunas.service.ChildService;

import java.util.List;

/**
 * REST controller for child/infant management.
 */
@RestController
@RequestMapping("/api/v1/children")
@RequiredArgsConstructor
@Tag(name = "Children", description = "Child management operations")
@SecurityRequirement(name = "Bearer Authentication")
public class ChildController {

    private final ChildService childService;

    /**
     * Create a new child.
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('DOCTOR', 'NURSE')")
    @Operation(summary = "Create new child", description = "Register a new child in the system")
    public ResponseEntity<ChildDto> createChild(@Valid @RequestBody CreateChildRequest request) {
        ChildDto child = childService.createChild(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(child);
    }

    /**
     * Get child by ID.
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('DOCTOR', 'NURSE', 'PARENT')")
    @Operation(summary = "Get child by ID", description = "Retrieve child details by ID")
    public ResponseEntity<ChildDto> getChildById(@PathVariable Long id) {
        ChildDto child = childService.getChildById(id);
        return ResponseEntity.ok(child);
    }

    /**
     * Get child by document number.
     */
    @GetMapping("/document/{documentNumber}")
    @PreAuthorize("hasAnyRole('DOCTOR', 'NURSE', 'PARENT')")
    @Operation(summary = "Get child by document number", description = "Retrieve child by document number")
    public ResponseEntity<ChildDto> getChildByDocumentNumber(@PathVariable String documentNumber) {
        ChildDto child = childService.getChildByDocumentNumber(documentNumber);
        return ResponseEntity.ok(child);
    }

    /**
     * Get all active children.
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('DOCTOR', 'NURSE', 'PARENT')")
    @Operation(summary = "Get all children", description = "Retrieve all active children")
    public ResponseEntity<List<ChildDto>> getAllChildren() {
        List<ChildDto> children = childService.getAllActiveChildren();
        return ResponseEntity.ok(children);
    }

    /**
     * Search children by name.
     */
    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('DOCTOR', 'NURSE', 'PARENT')")
    @Operation(summary = "Search children", description = "Search children by name")
    public ResponseEntity<List<ChildDto>> searchChildren(
            @RequestParam
            @NotBlank(message = "Search query cannot be blank")
            @Size(min = 2, max = 100, message = "Search query must be between 2 and 100 characters") String query) {
        List<ChildDto> children = childService.searchChildren(query);
        return ResponseEntity.ok(children);
    }

    /**
     * Update child information.
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('DOCTOR', 'NURSE')")
    @Operation(summary = "Update child", description = "Update child information")
    public ResponseEntity<ChildDto> updateChild(@PathVariable Long id,
                                                @Valid @RequestBody CreateChildRequest request) {
        ChildDto child = childService.updateChild(id, request);
        return ResponseEntity.ok(child);
    }

    /**
     * Delete child (soft delete).
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('DOCTOR')")
    @Operation(summary = "Delete child", description = "Soft delete a child record")
    public ResponseEntity<Void> deleteChild(@PathVariable Long id) {
        childService.deleteChild(id);
        return ResponseEntity.noContent().build();
    }

}
