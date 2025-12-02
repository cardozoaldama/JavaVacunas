package py.gov.mspbs.javacunas.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import py.gov.mspbs.javacunas.AbstractOracleIntegrationTest;
import py.gov.mspbs.javacunas.entity.Child;
import py.gov.mspbs.javacunas.entity.User;
import py.gov.mspbs.javacunas.entity.Vaccine;
import py.gov.mspbs.javacunas.entity.VaccinationRecord;
import py.gov.mspbs.javacunas.entity.VaccinationSchedule;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * Integration tests for VaccinationRecordRepository using Oracle 23c Free database.
 *
 * These tests verify:
 * - Basic CRUD operations with timestamp tracking
 * - Custom query methods (findByChildId, findByVaccineId, etc.)
 * - Database constraints (not null, foreign keys)
 * - Relationships with Child, Vaccine, User, and VaccinationSchedule
 * - Query ordering (DESC by administrationDate, ASC by doseNumber)
 * - Batch number tracking for vaccine inventory
 * - Next dose date scheduling
 * - JPA lifecycle callbacks (@PrePersist, @PreUpdate)
 */
@DisplayName("VaccinationRecordRepository Integration Tests")
class VaccinationRecordRepositoryIT extends AbstractOracleIntegrationTest {

    @Autowired
    private VaccinationRecordRepository vaccinationRecordRepository;

    @Autowired
    private ChildRepository childRepository;

    @Autowired
    private VaccineRepository vaccineRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private VaccinationScheduleRepository vaccinationScheduleRepository;

    private Child testChild;
    private Vaccine testVaccine;
    private User testDoctor;
    private User testNurse;

    @BeforeEach
    void setUp() {
        // Create test child
        testChild = Child.builder()
                .firstName("Sofia")
                .lastName("Gonzalez")
                .documentNumber("1234567890")
                .dateOfBirth(LocalDate.of(2023, 6, 15))
                .gender(Child.Gender.F)
                .build();
        testChild = childRepository.save(testChild);

        // Create test vaccine
        testVaccine = Vaccine.builder()
                .name("BCG")
                .diseasePrevented("Tuberculosis")
                .doseCount(1)
                .isActive('Y')
                .build();
        testVaccine = vaccineRepository.save(testVaccine);

        // Create test doctor
        testDoctor = User.builder()
                .username("doctor_test")
                .passwordHash("$2a$10$hashedpassword")
                .firstName("Dr. Carlos")
                .lastName("Rodriguez")
                .email("carlos.rodriguez@test.com")
                .role(User.UserRole.DOCTOR)
                .isActive('Y')
                .build();
        testDoctor = userRepository.save(testDoctor);

        // Create test nurse
        testNurse = User.builder()
                .username("nurse_test")
                .passwordHash("$2a$10$hashedpassword")
                .firstName("Maria")
                .lastName("Lopez")
                .email("maria.lopez@test.com")
                .role(User.UserRole.NURSE)
                .isActive('Y')
                .build();
        testNurse = userRepository.save(testNurse);
    }

    @Test
    @DisplayName("Should save and retrieve vaccination record with all required fields")
    void testSaveAndRetrieveVaccinationRecord() {
        // Given
        VaccinationRecord record = VaccinationRecord.builder()
                .child(testChild)
                .vaccine(testVaccine)
                .doseNumber(1)
                .administrationDate(LocalDate.of(2023, 6, 16))
                .batchNumber("BATCH001")
                .expirationDate(LocalDate.of(2025, 12, 31))
                .administeredBy(testDoctor)
                .administrationSite("Left arm")
                .notes("First dose administered without complications")
                .nextDoseDate(LocalDate.of(2023, 8, 16))
                .build();

        // When
        VaccinationRecord savedRecord = vaccinationRecordRepository.save(record);
        vaccinationRecordRepository.flush();

        // Then
        assertThat(savedRecord.getId()).isNotNull();
        assertThat(savedRecord.getCreatedAt()).isNotNull();
        assertThat(savedRecord.getUpdatedAt()).isNotNull();
        assertThat(savedRecord.getDoseNumber()).isEqualTo(1);
        assertThat(savedRecord.getBatchNumber()).isEqualTo("BATCH001");
        assertThat(savedRecord.getAdministrationSite()).isEqualTo("Left arm");
        assertThat(savedRecord.getNotes()).isEqualTo("First dose administered without complications");
        assertThat(savedRecord.getNextDoseDate()).isEqualTo(LocalDate.of(2023, 8, 16));

        // Verify database retrieval
        VaccinationRecord retrievedRecord = vaccinationRecordRepository.findById(savedRecord.getId()).orElseThrow();
        assertThat(retrievedRecord.getDoseNumber()).isEqualTo(1);
        assertThat(retrievedRecord.getBatchNumber()).isEqualTo("BATCH001");
    }

    @Test
    @DisplayName("Should enforce not null constraints on required fields")
    void testNotNullConstraints() {
        // Given - record without required 'doseNumber' field
        VaccinationRecord record = VaccinationRecord.builder()
                .child(testChild)
                .vaccine(testVaccine)
                .administrationDate(LocalDate.of(2023, 6, 16))
                .batchNumber("BATCH001")
                .expirationDate(LocalDate.of(2025, 12, 31))
                .administeredBy(testDoctor)
                .build();

        // When/Then - should fail due to null doseNumber
        assertThatThrownBy(() -> {
            vaccinationRecordRepository.save(record);
            vaccinationRecordRepository.flush();
        }).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("Should find vaccination records by child id with vaccine JOIN FETCH")
    void testFindByChildId() {
        // Given - create 3 records with different dates
        VaccinationRecord record1 = createRecord(testChild, testVaccine, 1, LocalDate.of(2023, 6, 16), "BATCH001");
        VaccinationRecord record2 = createRecord(testChild, testVaccine, 2, LocalDate.of(2023, 8, 16), "BATCH002");
        VaccinationRecord record3 = createRecord(testChild, testVaccine, 3, LocalDate.of(2023, 10, 16), "BATCH003");

        vaccinationRecordRepository.saveAll(List.of(record1, record2, record3));
        vaccinationRecordRepository.flush();

        // When
        List<VaccinationRecord> records = vaccinationRecordRepository.findByChildId(testChild.getId());

        // Then - should be ordered DESC by administrationDate
        assertThat(records).hasSize(3);
        assertThat(records.get(0).getAdministrationDate()).isEqualTo(LocalDate.of(2023, 10, 16));
        assertThat(records.get(1).getAdministrationDate()).isEqualTo(LocalDate.of(2023, 8, 16));
        assertThat(records.get(2).getAdministrationDate()).isEqualTo(LocalDate.of(2023, 6, 16));

        // Verify JOIN FETCH loaded vaccine (no lazy loading exception)
        assertThat(records.get(0).getVaccine().getName()).isEqualTo("BCG");
    }

    @Test
    @DisplayName("Should find vaccination records by vaccine id")
    void testFindByVaccineId() {
        // Given - create 3 records for same vaccine
        Child child1 = createChild("1111111111", LocalDate.of(2023, 1, 1));
        Child child2 = createChild("2222222222", LocalDate.of(2023, 2, 1));
        Child child3 = createChild("3333333333", LocalDate.of(2023, 3, 1));

        VaccinationRecord record1 = createRecord(child1, testVaccine, 1, LocalDate.of(2023, 6, 1), "BATCH001");
        VaccinationRecord record2 = createRecord(child2, testVaccine, 1, LocalDate.of(2023, 7, 1), "BATCH002");
        VaccinationRecord record3 = createRecord(child3, testVaccine, 1, LocalDate.of(2023, 8, 1), "BATCH003");

        vaccinationRecordRepository.saveAll(List.of(record1, record2, record3));
        vaccinationRecordRepository.flush();

        // When
        List<VaccinationRecord> records = vaccinationRecordRepository.findByVaccineId(testVaccine.getId());

        // Then - should be ordered DESC by administrationDate
        assertThat(records).hasSize(3);
        assertThat(records.get(0).getAdministrationDate()).isEqualTo(LocalDate.of(2023, 8, 1));
        assertThat(records.get(1).getAdministrationDate()).isEqualTo(LocalDate.of(2023, 7, 1));
        assertThat(records.get(2).getAdministrationDate()).isEqualTo(LocalDate.of(2023, 6, 1));
    }

    @Test
    @DisplayName("Should find vaccination records by batch number")
    void testFindByBatchNumber() {
        // Given - create 2 records with same batch number
        Child child1 = createChild("4444444444", LocalDate.of(2023, 4, 1));
        Child child2 = createChild("5555555555", LocalDate.of(2023, 5, 1));

        VaccinationRecord record1 = createRecord(child1, testVaccine, 1, LocalDate.of(2023, 6, 1), "BATCH_SAME");
        VaccinationRecord record2 = createRecord(child2, testVaccine, 1, LocalDate.of(2023, 6, 2), "BATCH_SAME");

        vaccinationRecordRepository.saveAll(List.of(record1, record2));
        vaccinationRecordRepository.flush();

        // When
        List<VaccinationRecord> records = vaccinationRecordRepository.findByBatchNumber("BATCH_SAME");

        // Then
        assertThat(records).hasSize(2);
        assertThat(records).allMatch(r -> r.getBatchNumber().equals("BATCH_SAME"));
    }

    @Test
    @DisplayName("Should find records by next dose date between range")
    void testFindByNextDoseDateBetween() {
        // Given - create 3 records, 2 in range
        VaccinationRecord record1 = createRecordWithNextDose(testChild, testVaccine, 1, LocalDate.of(2023, 6, 1),
                                                             "BATCH001", LocalDate.of(2023, 8, 1));

        Child child2 = createChild("6666666666", LocalDate.of(2023, 1, 1));
        VaccinationRecord record2 = createRecordWithNextDose(child2, testVaccine, 1, LocalDate.of(2023, 6, 2),
                                                             "BATCH002", LocalDate.of(2023, 8, 15));

        Child child3 = createChild("7777777777", LocalDate.of(2023, 2, 1));
        VaccinationRecord record3 = createRecordWithNextDose(child3, testVaccine, 1, LocalDate.of(2023, 6, 3),
                                                             "BATCH003", LocalDate.of(2023, 9, 1));

        vaccinationRecordRepository.saveAll(List.of(record1, record2, record3));
        vaccinationRecordRepository.flush();

        // When
        List<VaccinationRecord> records = vaccinationRecordRepository.findByNextDoseDateBetween(
                LocalDate.of(2023, 8, 1),
                LocalDate.of(2023, 8, 31)
        );

        // Then - should find 2 records ordered by nextDoseDate
        assertThat(records).hasSize(2);
        assertThat(records.get(0).getNextDoseDate()).isEqualTo(LocalDate.of(2023, 8, 1));
        assertThat(records.get(1).getNextDoseDate()).isEqualTo(LocalDate.of(2023, 8, 15));
    }

    @Test
    @DisplayName("Should find records by child id and vaccine id ordered by dose number")
    void testFindByChildIdAndVaccineId() {
        // Given - create 3 doses for same child and vaccine
        VaccinationRecord record1 = createRecord(testChild, testVaccine, 1, LocalDate.of(2023, 6, 16), "BATCH001");
        VaccinationRecord record2 = createRecord(testChild, testVaccine, 2, LocalDate.of(2023, 8, 16), "BATCH002");
        VaccinationRecord record3 = createRecord(testChild, testVaccine, 3, LocalDate.of(2023, 10, 16), "BATCH003");

        vaccinationRecordRepository.saveAll(List.of(record1, record2, record3));
        vaccinationRecordRepository.flush();

        // When
        List<VaccinationRecord> records = vaccinationRecordRepository.findByChildIdAndVaccineId(
                testChild.getId(),
                testVaccine.getId()
        );

        // Then - should be ordered ASC by doseNumber
        assertThat(records).hasSize(3);
        assertThat(records.get(0).getDoseNumber()).isEqualTo(1);
        assertThat(records.get(1).getDoseNumber()).isEqualTo(2);
        assertThat(records.get(2).getDoseNumber()).isEqualTo(3);
    }

    @Test
    @DisplayName("Should count administered doses for child and vaccine")
    void testCountByChildIdAndVaccineId() {
        // Given - create 3 doses
        VaccinationRecord record1 = createRecord(testChild, testVaccine, 1, LocalDate.of(2023, 6, 16), "BATCH001");
        VaccinationRecord record2 = createRecord(testChild, testVaccine, 2, LocalDate.of(2023, 8, 16), "BATCH002");
        VaccinationRecord record3 = createRecord(testChild, testVaccine, 3, LocalDate.of(2023, 10, 16), "BATCH003");

        vaccinationRecordRepository.saveAll(List.of(record1, record2, record3));
        vaccinationRecordRepository.flush();

        // When
        Long count = vaccinationRecordRepository.countByChildIdAndVaccineId(
                testChild.getId(),
                testVaccine.getId()
        );

        // Then
        assertThat(count).isEqualTo(3L);
    }

    @Test
    @DisplayName("Should verify vaccination record-child relationship")
    void testVaccinationRecordChildRelationship() {
        // Given
        VaccinationRecord record = createRecord(testChild, testVaccine, 1, LocalDate.of(2023, 6, 16), "BATCH001");
        VaccinationRecord savedRecord = vaccinationRecordRepository.save(record);
        vaccinationRecordRepository.flush();

        // When
        VaccinationRecord retrievedRecord = vaccinationRecordRepository.findById(savedRecord.getId()).orElseThrow();

        // Then - lazy loading should work
        assertThat(retrievedRecord.getChild()).isNotNull();
        assertThat(retrievedRecord.getChild().getFirstName()).isEqualTo("Sofia");
        assertThat(retrievedRecord.getChild().getLastName()).isEqualTo("Gonzalez");

        // Test not null constraint
        VaccinationRecord recordWithoutChild = VaccinationRecord.builder()
                .vaccine(testVaccine)
                .doseNumber(1)
                .administrationDate(LocalDate.now())
                .batchNumber("BATCH001")
                .expirationDate(LocalDate.now().plusYears(2))
                .administeredBy(testDoctor)
                .build();

        assertThatThrownBy(() -> {
            vaccinationRecordRepository.save(recordWithoutChild);
            vaccinationRecordRepository.flush();
        }).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("Should verify vaccination record-vaccine relationship")
    void testVaccinationRecordVaccineRelationship() {
        // Given
        VaccinationRecord record = createRecord(testChild, testVaccine, 1, LocalDate.of(2023, 6, 16), "BATCH001");
        VaccinationRecord savedRecord = vaccinationRecordRepository.save(record);
        vaccinationRecordRepository.flush();

        // When
        VaccinationRecord retrievedRecord = vaccinationRecordRepository.findById(savedRecord.getId()).orElseThrow();

        // Then - lazy loading should work
        assertThat(retrievedRecord.getVaccine()).isNotNull();
        assertThat(retrievedRecord.getVaccine().getName()).isEqualTo("BCG");
        assertThat(retrievedRecord.getVaccine().getDiseasePrevented()).isEqualTo("Tuberculosis");

        // Test not null constraint
        VaccinationRecord recordWithoutVaccine = VaccinationRecord.builder()
                .child(testChild)
                .doseNumber(1)
                .administrationDate(LocalDate.now())
                .batchNumber("BATCH001")
                .expirationDate(LocalDate.now().plusYears(2))
                .administeredBy(testDoctor)
                .build();

        assertThatThrownBy(() -> {
            vaccinationRecordRepository.save(recordWithoutVaccine);
            vaccinationRecordRepository.flush();
        }).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("Should verify vaccination record-schedule optional relationship")
    void testVaccinationRecordScheduleOptionalRelationship() {
        // Given - record without schedule (schedule is optional)
        VaccinationRecord recordWithoutSchedule = createRecord(testChild, testVaccine, 1,
                                                               LocalDate.of(2023, 6, 16), "BATCH001");

        // When
        VaccinationRecord savedRecord = vaccinationRecordRepository.save(recordWithoutSchedule);
        vaccinationRecordRepository.flush();

        // Then - should save successfully with null schedule
        assertThat(savedRecord.getId()).isNotNull();
        assertThat(savedRecord.getSchedule()).isNull();

        // Verify retrieval
        VaccinationRecord retrievedRecord = vaccinationRecordRepository.findById(savedRecord.getId()).orElseThrow();
        assertThat(retrievedRecord.getSchedule()).isNull();
    }

    @Test
    @DisplayName("Should verify vaccination record-user administered by relationship")
    void testVaccinationRecordUserRelationship() {
        // Given - create records with DOCTOR and NURSE
        VaccinationRecord recordByDoctor = createRecord(testChild, testVaccine, 1,
                                                        LocalDate.of(2023, 6, 16), "BATCH001");
        recordByDoctor.setAdministeredBy(testDoctor);

        Child child2 = createChild("8888888888", LocalDate.of(2023, 1, 1));
        VaccinationRecord recordByNurse = createRecord(child2, testVaccine, 1,
                                                       LocalDate.of(2023, 6, 17), "BATCH002");
        recordByNurse.setAdministeredBy(testNurse);

        vaccinationRecordRepository.saveAll(List.of(recordByDoctor, recordByNurse));
        vaccinationRecordRepository.flush();

        // When
        VaccinationRecord retrievedDoctor = vaccinationRecordRepository.findById(recordByDoctor.getId()).orElseThrow();
        VaccinationRecord retrievedNurse = vaccinationRecordRepository.findById(recordByNurse.getId()).orElseThrow();

        // Then
        assertThat(retrievedDoctor.getAdministeredBy()).isNotNull();
        assertThat(retrievedDoctor.getAdministeredBy().getRole()).isEqualTo(User.UserRole.DOCTOR);
        assertThat(retrievedDoctor.getAdministeredBy().getFirstName()).isEqualTo("Dr. Carlos");

        assertThat(retrievedNurse.getAdministeredBy()).isNotNull();
        assertThat(retrievedNurse.getAdministeredBy().getRole()).isEqualTo(User.UserRole.NURSE);
        assertThat(retrievedNurse.getAdministeredBy().getFirstName()).isEqualTo("Maria");
    }

    @Test
    @DisplayName("Should verify administration date ordering")
    void testAdministrationDateOrdering() {
        // Given - save records in random order
        VaccinationRecord record2 = createRecord(testChild, testVaccine, 2, LocalDate.of(2023, 8, 16), "BATCH002");
        VaccinationRecord record1 = createRecord(testChild, testVaccine, 1, LocalDate.of(2023, 6, 16), "BATCH001");
        VaccinationRecord record3 = createRecord(testChild, testVaccine, 3, LocalDate.of(2023, 10, 16), "BATCH003");

        vaccinationRecordRepository.saveAll(List.of(record2, record1, record3));
        vaccinationRecordRepository.flush();

        // When - retrieve by child id (ordered DESC by administrationDate)
        List<VaccinationRecord> records = vaccinationRecordRepository.findByChildId(testChild.getId());

        // Then - verify DESC ordering
        assertThat(records).hasSize(3);
        assertThat(records.get(0).getAdministrationDate()).isAfter(records.get(1).getAdministrationDate());
        assertThat(records.get(1).getAdministrationDate()).isAfter(records.get(2).getAdministrationDate());
        assertThat(records.get(0).getBatchNumber()).isEqualTo("BATCH003");
        assertThat(records.get(1).getBatchNumber()).isEqualTo("BATCH002");
        assertThat(records.get(2).getBatchNumber()).isEqualTo("BATCH001");
    }

    @Test
    @DisplayName("Should update vaccination record and modify updatedAt timestamp")
    void testUpdateVaccinationRecord() throws InterruptedException {
        // Given
        VaccinationRecord record = createRecord(testChild, testVaccine, 1, LocalDate.of(2023, 6, 16), "BATCH001");
        record.setNotes("Original note");

        VaccinationRecord savedRecord = vaccinationRecordRepository.save(record);
        vaccinationRecordRepository.flush();

        var originalCreatedAt = savedRecord.getCreatedAt();
        var originalUpdatedAt = savedRecord.getUpdatedAt();

        // Small delay to ensure updatedAt will be different
        Thread.sleep(10);

        // When
        savedRecord.setNotes("Updated note with additional information");
        savedRecord.setAdministrationSite("Right thigh");
        VaccinationRecord updatedRecord = vaccinationRecordRepository.save(savedRecord);
        vaccinationRecordRepository.flush();

        // Then
        assertThat(updatedRecord.getNotes()).isEqualTo("Updated note with additional information");
        assertThat(updatedRecord.getAdministrationSite()).isEqualTo("Right thigh");
        assertThat(updatedRecord.getCreatedAt()).isEqualTo(originalCreatedAt);
        assertThat(updatedRecord.getUpdatedAt()).isAfter(originalUpdatedAt);
    }

    @Test
    @DisplayName("Should handle multiple doses for same child and vaccine")
    void testMultipleDosesForSameChildAndVaccine() {
        // Given - create 3 dose records
        Vaccine multiDoseVaccine = vaccineRepository.save(Vaccine.builder()
                .name("Pentavalent")
                .diseasePrevented("Multiple diseases")
                .doseCount(3)
                .isActive('Y')
                .build());

        VaccinationRecord dose1 = createRecord(testChild, multiDoseVaccine, 1, LocalDate.of(2023, 6, 16), "BATCH001");
        VaccinationRecord dose2 = createRecord(testChild, multiDoseVaccine, 2, LocalDate.of(2023, 8, 16), "BATCH002");
        VaccinationRecord dose3 = createRecord(testChild, multiDoseVaccine, 3, LocalDate.of(2023, 10, 16), "BATCH003");

        vaccinationRecordRepository.saveAll(List.of(dose1, dose2, dose3));
        vaccinationRecordRepository.flush();

        Long multiDoseVaccineId = multiDoseVaccine.getId();

        // When
        Long count = vaccinationRecordRepository.countByChildIdAndVaccineId(
                testChild.getId(),
                multiDoseVaccineId
        );
        List<VaccinationRecord> records = vaccinationRecordRepository.findByChildIdAndVaccineId(
                testChild.getId(),
                multiDoseVaccineId
        );

        // Then
        assertThat(count).isEqualTo(3L);
        assertThat(records).hasSize(3);
        assertThat(records.get(0).getDoseNumber()).isEqualTo(1);
        assertThat(records.get(1).getDoseNumber()).isEqualTo(2);
        assertThat(records.get(2).getDoseNumber()).isEqualTo(3);
        assertThat(records).allMatch(r -> r.getChild().getId().equals(testChild.getId()));
        assertThat(records).allMatch(r -> r.getVaccine().getId().equals(multiDoseVaccineId));
    }

    @Test
    @DisplayName("Should verify Oracle database connection and schema")
    void testOracleDatabaseConnection() {
        // Given/When
        String oracleVersion = getOracleVersion();
        boolean vaccinationRecordsTableExists = tableExists("vaccination_records");

        // Then
        assertThat(oracleVersion).containsIgnoringCase("Oracle");
        assertThat(vaccinationRecordsTableExists).isTrue();
    }

    // Helper methods

    private VaccinationRecord createRecord(Child child, Vaccine vaccine, Integer doseNumber,
                                          LocalDate administrationDate, String batchNumber) {
        return VaccinationRecord.builder()
                .child(child)
                .vaccine(vaccine)
                .doseNumber(doseNumber)
                .administrationDate(administrationDate)
                .batchNumber(batchNumber)
                .expirationDate(administrationDate.plusYears(2))
                .administeredBy(testDoctor)
                .administrationSite("Left arm")
                .build();
    }

    private VaccinationRecord createRecordWithNextDose(Child child, Vaccine vaccine, Integer doseNumber,
                                                       LocalDate administrationDate, String batchNumber,
                                                       LocalDate nextDoseDate) {
        VaccinationRecord record = createRecord(child, vaccine, doseNumber, administrationDate, batchNumber);
        record.setNextDoseDate(nextDoseDate);
        return record;
    }

    private Child createChild(String documentNumber, LocalDate dateOfBirth) {
        Child child = Child.builder()
                .firstName("Child")
                .lastName("Test")
                .documentNumber(documentNumber)
                .dateOfBirth(dateOfBirth)
                .gender(Child.Gender.M)
                .build();
        return childRepository.save(child);
    }
}
