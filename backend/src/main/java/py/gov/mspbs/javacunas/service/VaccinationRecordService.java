package py.gov.mspbs.javacunas.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import py.gov.mspbs.javacunas.dto.CreateVaccinationRecordRequest;
import py.gov.mspbs.javacunas.dto.VaccinationRecordDto;
import py.gov.mspbs.javacunas.entity.*;
import py.gov.mspbs.javacunas.exception.BusinessException;
import py.gov.mspbs.javacunas.exception.ResourceNotFoundException;
import py.gov.mspbs.javacunas.repository.*;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for managing vaccination records.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class VaccinationRecordService {

    private final VaccinationRecordRepository vaccinationRecordRepository;
    private final ChildRepository childRepository;
    private final VaccineRepository vaccineRepository;
    private final VaccinationScheduleRepository scheduleRepository;
    private final VaccineInventoryRepository inventoryRepository;
    private final UserRepository userRepository;

    /**
     * Create a new vaccination record.
     */
    @Transactional
    public VaccinationRecordDto createVaccinationRecord(CreateVaccinationRecordRequest request, Long userId) {
        log.info("Creating vaccination record for child {} and vaccine {}", request.getChildId(), request.getVaccineId());

        // Validate child exists and is active
        Child child = childRepository.findById(request.getChildId())
                .orElseThrow(() -> new ResourceNotFoundException("Child", "id", request.getChildId()));

        if (child.isDeleted()) {
            throw new BusinessException("Cannot create vaccination record for deleted child");
        }

        // Validate vaccine exists and is active
        Vaccine vaccine = vaccineRepository.findById(request.getVaccineId())
                .orElseThrow(() -> new ResourceNotFoundException("Vaccine", "id", request.getVaccineId()));

        if (vaccine.getIsActive() != 'Y') {
            throw new BusinessException("Cannot administer inactive vaccine");
        }

        // Validate user exists
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        // Validate expiration date is after administration date
        if (request.getExpirationDate().isBefore(request.getAdministrationDate())) {
            throw new BusinessException("Expiration date must be after administration date");
        }

        // Check if vaccine batch exists in inventory
        VaccineInventory inventory = inventoryRepository
                .findByVaccineIdAndBatchNumber(request.getVaccineId(), request.getBatchNumber())
                .orElse(null);

        if (inventory != null) {
            // Validate inventory has available doses
            if (inventory.getQuantity() < 1 || inventory.getStatus() != VaccineInventory.InventoryStatus.AVAILABLE) {
                throw new BusinessException("Vaccine batch is not available or has insufficient quantity");
            }

            // Decrease inventory quantity
            inventory.decreaseQuantity(1);
            inventoryRepository.save(inventory);
            log.info("Decreased inventory for batch {} by 1 dose", request.getBatchNumber());
        } else {
            log.warn("Vaccine batch {} not found in inventory, proceeding anyway", request.getBatchNumber());
        }

        // Get schedule if provided
        VaccinationSchedule schedule = null;
        if (request.getScheduleId() != null) {
            schedule = scheduleRepository.findById(request.getScheduleId())
                    .orElseThrow(() -> new ResourceNotFoundException("VaccinationSchedule", "id", request.getScheduleId()));
        }

        // Create vaccination record
        VaccinationRecord record = VaccinationRecord.builder()
                .child(child)
                .vaccine(vaccine)
                .schedule(schedule)
                .doseNumber(request.getDoseNumber())
                .administrationDate(request.getAdministrationDate())
                .batchNumber(request.getBatchNumber())
                .expirationDate(request.getExpirationDate())
                .administeredBy(user)
                .administrationSite(request.getAdministrationSite())
                .notes(request.getNotes())
                .nextDoseDate(request.getNextDoseDate())
                .build();

        VaccinationRecord savedRecord = vaccinationRecordRepository.save(record);
        log.info("Vaccination record created successfully with ID: {}", savedRecord.getId());

        return mapToDto(savedRecord);
    }

    /**
     * Get vaccination records by child ID.
     */
    @Transactional(readOnly = true)
    public List<VaccinationRecordDto> getVaccinationRecordsByChildId(Long childId) {
        log.debug("Retrieving vaccination records for child: {}", childId);

        // Verify child exists
        Child child = childRepository.findById(childId)
                .orElseThrow(() -> new ResourceNotFoundException("Child", "id", childId));

        if (child.isDeleted()) {
            throw new ResourceNotFoundException("Child", "id", childId);
        }

        return vaccinationRecordRepository.findByChildId(childId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    /**
     * Get vaccination record by ID.
     */
    @Transactional(readOnly = true)
    public VaccinationRecordDto getVaccinationRecordById(Long id) {
        log.debug("Retrieving vaccination record with ID: {}", id);

        VaccinationRecord record = vaccinationRecordRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("VaccinationRecord", "id", id));

        return mapToDto(record);
    }

    /**
     * Get records by vaccine ID.
     */
    @Transactional(readOnly = true)
    public List<VaccinationRecordDto> getVaccinationRecordsByVaccineId(Long vaccineId) {
        log.debug("Retrieving vaccination records for vaccine: {}", vaccineId);

        return vaccinationRecordRepository.findByVaccineId(vaccineId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    /**
     * Get records by batch number.
     */
    @Transactional(readOnly = true)
    public List<VaccinationRecordDto> getVaccinationRecordsByBatchNumber(String batchNumber) {
        log.debug("Retrieving vaccination records for batch: {}", batchNumber);

        return vaccinationRecordRepository.findByBatchNumber(batchNumber).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    /**
     * Get upcoming vaccinations (records with next dose dates).
     */
    @Transactional(readOnly = true)
    public List<VaccinationRecordDto> getUpcomingVaccinations(int daysAhead) {
        log.debug("Retrieving upcoming vaccinations for next {} days", daysAhead);

        LocalDate today = LocalDate.now();
        LocalDate endDate = today.plusDays(daysAhead);

        return vaccinationRecordRepository.findByNextDoseDateBetween(today, endDate).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    /**
     * Map VaccinationRecord entity to DTO.
     */
    private VaccinationRecordDto mapToDto(VaccinationRecord record) {
        return VaccinationRecordDto.builder()
                .id(record.getId())
                .childId(record.getChild().getId())
                .childName(record.getChild().getFirstName() + " " + record.getChild().getLastName())
                .vaccineId(record.getVaccine().getId())
                .vaccineName(record.getVaccine().getName())
                .doseNumber(record.getDoseNumber())
                .administrationDate(record.getAdministrationDate())
                .batchNumber(record.getBatchNumber())
                .expirationDate(record.getExpirationDate())
                .administeredById(record.getAdministeredBy().getId())
                .administeredByName(record.getAdministeredBy().getFirstName() + " " +
                                   record.getAdministeredBy().getLastName())
                .administrationSite(record.getAdministrationSite())
                .notes(record.getNotes())
                .nextDoseDate(record.getNextDoseDate())
                .createdAt(record.getCreatedAt())
                .build();
    }

}
