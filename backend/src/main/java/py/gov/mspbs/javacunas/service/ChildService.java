package py.gov.mspbs.javacunas.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import py.gov.mspbs.javacunas.dto.ChildDto;
import py.gov.mspbs.javacunas.dto.CreateChildRequest;
import py.gov.mspbs.javacunas.entity.Child;
import py.gov.mspbs.javacunas.exception.DuplicateResourceException;
import py.gov.mspbs.javacunas.exception.ResourceNotFoundException;
import py.gov.mspbs.javacunas.repository.ChildRepository;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for managing children/infants.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ChildService {

    private final ChildRepository childRepository;

    /**
     * Create a new child.
     */
    @Transactional
    public ChildDto createChild(CreateChildRequest request) {
        log.info("Creating new child with document number: {}", request.getDocumentNumber());

        // Check if child with document number already exists
        if (childRepository.findByDocumentNumber(request.getDocumentNumber()).isPresent()) {
            throw new DuplicateResourceException("Child", "documentNumber", request.getDocumentNumber());
        }

        Child child = Child.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .documentNumber(request.getDocumentNumber())
                .dateOfBirth(request.getDateOfBirth())
                .gender(request.getGender())
                .bloodType(request.getBloodType())
                .birthWeight(request.getBirthWeight())
                .birthHeight(request.getBirthHeight())
                .build();

        Child savedChild = childRepository.save(child);
        log.info("Child created successfully with ID: {}", savedChild.getId());

        return mapToDto(savedChild);
    }

    /**
     * Get child by ID.
     */
    @Transactional(readOnly = true)
    public ChildDto getChildById(Long id) {
        log.debug("Retrieving child with ID: {}", id);

        Child child = childRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Child", "id", id));

        if (child.isDeleted()) {
            throw new ResourceNotFoundException("Child", "id", id);
        }

        return mapToDto(child);
    }

    /**
     * Get child by document number.
     */
    @Transactional(readOnly = true)
    public ChildDto getChildByDocumentNumber(String documentNumber) {
        log.debug("Retrieving child with document number: {}", documentNumber);

        Child child = childRepository.findByDocumentNumber(documentNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Child", "documentNumber", documentNumber));

        if (child.isDeleted()) {
            throw new ResourceNotFoundException("Child", "documentNumber", documentNumber);
        }

        return mapToDto(child);
    }

    /**
     * Get all active children.
     */
    @Transactional(readOnly = true)
    public List<ChildDto> getAllActiveChildren() {
        log.debug("Retrieving all active children");

        return childRepository.findAllActive().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    /**
     * Search children by name.
     */
    @Transactional(readOnly = true)
    public List<ChildDto> searchChildren(String searchTerm) {
        log.debug("Searching children with term: {}", searchTerm);

        return childRepository.searchByName(searchTerm).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    /**
     * Get children by guardian ID.
     */
    @Transactional(readOnly = true)
    public List<ChildDto> getChildrenByGuardianId(Long guardianId) {
        log.debug("Retrieving children for guardian ID: {}", guardianId);

        return childRepository.findByGuardianId(guardianId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    /**
     * Get children by user ID (through guardians).
     */
    @Transactional(readOnly = true)
    public List<ChildDto> getChildrenByUserId(Long userId) {
        log.debug("Retrieving children for user ID: {}", userId);

        return childRepository.findByUserId(userId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    /**
     * Update child information.
     */
    @Transactional
    public ChildDto updateChild(Long id, CreateChildRequest request) {
        log.info("Updating child with ID: {}", id);

        Child child = childRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Child", "id", id));

        if (child.isDeleted()) {
            throw new ResourceNotFoundException("Child", "id", id);
        }

        // Check if new document number conflicts with another child
        if (!child.getDocumentNumber().equals(request.getDocumentNumber())) {
            childRepository.findByDocumentNumber(request.getDocumentNumber())
                    .ifPresent(existing -> {
                        throw new DuplicateResourceException("Child", "documentNumber", request.getDocumentNumber());
                    });
        }

        child.setFirstName(request.getFirstName());
        child.setLastName(request.getLastName());
        child.setDocumentNumber(request.getDocumentNumber());
        child.setDateOfBirth(request.getDateOfBirth());
        child.setGender(request.getGender());
        child.setBloodType(request.getBloodType());
        child.setBirthWeight(request.getBirthWeight());
        child.setBirthHeight(request.getBirthHeight());

        Child updatedChild = childRepository.save(child);
        log.info("Child updated successfully with ID: {}", updatedChild.getId());

        return mapToDto(updatedChild);
    }

    /**
     * Soft delete a child.
     */
    @Transactional
    public void deleteChild(Long id) {
        log.info("Deleting child with ID: {}", id);

        Child child = childRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Child", "id", id));

        if (child.isDeleted()) {
            throw new ResourceNotFoundException("Child", "id", id);
        }

        child.softDelete();
        childRepository.save(child);

        log.info("Child soft deleted successfully with ID: {}", id);
    }

    /**
     * Calculate age in months from date of birth.
     */
    private Integer calculateAgeInMonths(LocalDate dateOfBirth) {
        if (dateOfBirth == null) {
            return null;
        }
        Period period = Period.between(dateOfBirth, LocalDate.now());
        return period.getYears() * 12 + period.getMonths();
    }

    /**
     * Map Child entity to DTO.
     */
    private ChildDto mapToDto(Child child) {
        return ChildDto.builder()
                .id(child.getId())
                .firstName(child.getFirstName())
                .lastName(child.getLastName())
                .documentNumber(child.getDocumentNumber())
                .dateOfBirth(child.getDateOfBirth())
                .gender(child.getGender())
                .bloodType(child.getBloodType())
                .birthWeight(child.getBirthWeight())
                .birthHeight(child.getBirthHeight())
                .createdAt(child.getCreatedAt())
                .updatedAt(child.getUpdatedAt())
                .ageInMonths(calculateAgeInMonths(child.getDateOfBirth()))
                .build();
    }

}
