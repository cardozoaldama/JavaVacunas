package py.gov.mspbs.javacunas.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import py.gov.mspbs.javacunas.BaseUnitTest;
import py.gov.mspbs.javacunas.dto.ChildDto;
import py.gov.mspbs.javacunas.dto.CreateChildRequest;
import py.gov.mspbs.javacunas.entity.Child;
import py.gov.mspbs.javacunas.entity.Child.Gender;
import py.gov.mspbs.javacunas.exception.DuplicateResourceException;
import py.gov.mspbs.javacunas.exception.ResourceNotFoundException;
import py.gov.mspbs.javacunas.repository.ChildRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@DisplayName("ChildService Tests")
class ChildServiceTest extends BaseUnitTest {

    @Mock
    private ChildRepository childRepository;

    @InjectMocks
    private ChildService childService;

    private Child testChild;
    private CreateChildRequest createRequest;

    @BeforeEach
    void setUp() {
        testChild = Child.builder()
                .id(1L)
                .firstName("Juan")
                .lastName("Pérez")
                .documentNumber("123456789")
                .dateOfBirth(LocalDate.of(2023, 1, 15))
                .gender(Gender.M)
                .bloodType("O+")
                .birthWeight(new BigDecimal("3.5"))
                .birthHeight(new BigDecimal("50.0"))
                .createdAt(LocalDateTime.now().minusMonths(10))
                .updatedAt(LocalDateTime.now().minusMonths(10))
                .build();

        createRequest = new CreateChildRequest();
        createRequest.setFirstName("María");
        createRequest.setLastName("González");
        createRequest.setDocumentNumber("987654321");
        createRequest.setDateOfBirth(LocalDate.of(2024, 6, 10));
        createRequest.setGender(Gender.F);
        createRequest.setBloodType("A+");
        createRequest.setBirthWeight(new BigDecimal("3.2"));
        createRequest.setBirthHeight(new BigDecimal("48.5"));
    }

    @Nested
    @DisplayName("Create Child Tests")
    class CreateChildTests {

        @Test
        @DisplayName("Should create child successfully")
        void createChildSuccess() {
            // Given
            when(childRepository.findByDocumentNumber("987654321")).thenReturn(Optional.empty());
            when(childRepository.save(any(Child.class))).thenAnswer(invocation -> {
                Child child = invocation.getArgument(0);
                child.setId(2L);
                child.setCreatedAt(LocalDateTime.now());
                child.setUpdatedAt(LocalDateTime.now());
                return child;
            });

            // When
            ChildDto result = childService.createChild(createRequest);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(2L);
            assertThat(result.getFirstName()).isEqualTo("María");
            assertThat(result.getLastName()).isEqualTo("González");
            assertThat(result.getDocumentNumber()).isEqualTo("987654321");
            assertThat(result.getDateOfBirth()).isEqualTo(LocalDate.of(2024, 6, 10));
            assertThat(result.getGender()).isEqualTo(Gender.F);
            assertThat(result.getBloodType()).isEqualTo("A+");

            verify(childRepository).findByDocumentNumber("987654321");
            verify(childRepository).save(any(Child.class));
        }

        @Test
        @DisplayName("Should throw exception when document number exists")
        void shouldThrowExceptionWhenDocumentNumberExists() {
            // Given
            when(childRepository.findByDocumentNumber("987654321")).thenReturn(Optional.of(testChild));

            // When / Then
            assertThatThrownBy(() -> childService.createChild(createRequest))
                    .isInstanceOf(DuplicateResourceException.class)
                    .hasMessageContaining("documentNumber")
                    .hasMessageContaining("987654321");

            verify(childRepository).findByDocumentNumber("987654321");
            verify(childRepository, never()).save(any(Child.class));
        }

        @Test
        @DisplayName("Should calculate age in months correctly")
        void shouldCalculateAgeInMonths() {
            // Given
            LocalDate birthDate = LocalDate.now().minusMonths(6);
            createRequest.setDateOfBirth(birthDate);

            when(childRepository.findByDocumentNumber(anyString())).thenReturn(Optional.empty());
            when(childRepository.save(any(Child.class))).thenAnswer(invocation -> {
                Child child = invocation.getArgument(0);
                child.setId(2L);
                child.setCreatedAt(LocalDateTime.now());
                child.setUpdatedAt(LocalDateTime.now());
                return child;
            });

            // When
            ChildDto result = childService.createChild(createRequest);

            // Then
            assertThat(result.getAgeInMonths()).isEqualTo(6);
        }

        @Test
        @DisplayName("Should save child with all fields")
        void shouldSaveChildWithAllFields() {
            // Given
            when(childRepository.findByDocumentNumber(anyString())).thenReturn(Optional.empty());
            when(childRepository.save(any(Child.class))).thenAnswer(invocation -> {
                Child child = invocation.getArgument(0);
                child.setId(2L);
                return child;
            });

            // When
            childService.createChild(createRequest);

            // Then
            ArgumentCaptor<Child> captor = ArgumentCaptor.forClass(Child.class);
            verify(childRepository).save(captor.capture());
            Child savedChild = captor.getValue();

            assertThat(savedChild.getFirstName()).isEqualTo("María");
            assertThat(savedChild.getLastName()).isEqualTo("González");
            assertThat(savedChild.getDocumentNumber()).isEqualTo("987654321");
            assertThat(savedChild.getDateOfBirth()).isEqualTo(LocalDate.of(2024, 6, 10));
            assertThat(savedChild.getGender()).isEqualTo(Gender.F);
            assertThat(savedChild.getBloodType()).isEqualTo("A+");
            assertThat(savedChild.getBirthWeight()).isEqualByComparingTo(new BigDecimal("3.2"));
            assertThat(savedChild.getBirthHeight()).isEqualByComparingTo(new BigDecimal("48.5"));
        }
    }

    @Nested
    @DisplayName("Get Child Tests")
    class GetChildTests {

        @Test
        @DisplayName("Should get child by ID successfully")
        void getChildByIdSuccess() {
            // Given
            when(childRepository.findById(1L)).thenReturn(Optional.of(testChild));

            // When
            ChildDto result = childService.getChildById(1L);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1L);
            assertThat(result.getFirstName()).isEqualTo("Juan");
            assertThat(result.getLastName()).isEqualTo("Pérez");

            verify(childRepository).findById(1L);
        }

        @Test
        @DisplayName("Should throw exception when child not found by ID")
        void shouldThrowExceptionWhenNotFoundById() {
            // Given
            when(childRepository.findById(999L)).thenReturn(Optional.empty());

            // When / Then
            assertThatThrownBy(() -> childService.getChildById(999L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Child")
                    .hasMessageContaining("999");
        }

        @Test
        @DisplayName("Should throw exception when child is deleted (by ID)")
        void shouldThrowExceptionWhenDeletedById() {
            // Given
            testChild.softDelete();
            when(childRepository.findById(1L)).thenReturn(Optional.of(testChild));

            // When / Then
            assertThatThrownBy(() -> childService.getChildById(1L))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        @DisplayName("Should get child by document number successfully")
        void getChildByDocumentNumberSuccess() {
            // Given
            when(childRepository.findByDocumentNumber("123456789")).thenReturn(Optional.of(testChild));

            // When
            ChildDto result = childService.getChildByDocumentNumber("123456789");

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getDocumentNumber()).isEqualTo("123456789");

            verify(childRepository).findByDocumentNumber("123456789");
        }

        @Test
        @DisplayName("Should throw exception when child not found by document number")
        void shouldThrowExceptionWhenNotFoundByDocumentNumber() {
            // Given
            when(childRepository.findByDocumentNumber("999999999")).thenReturn(Optional.empty());

            // When / Then
            assertThatThrownBy(() -> childService.getChildByDocumentNumber("999999999"))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("documentNumber");
        }

        @Test
        @DisplayName("Should throw exception when child is deleted (by document number)")
        void shouldThrowExceptionWhenDeletedByDocumentNumber() {
            // Given
            testChild.softDelete();
            when(childRepository.findByDocumentNumber("123456789")).thenReturn(Optional.of(testChild));

            // When / Then
            assertThatThrownBy(() -> childService.getChildByDocumentNumber("123456789"))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("Get All Children Tests")
    class GetAllChildrenTests {

        @Test
        @DisplayName("Should get all active children")
        void getAllActiveChildrenSuccess() {
            // Given
            Child child2 = Child.builder()
                    .id(2L)
                    .firstName("María")
                    .lastName("López")
                    .documentNumber("111222333")
                    .dateOfBirth(LocalDate.of(2024, 3, 20))
                    .gender(Gender.F)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();

            when(childRepository.findAllActive()).thenReturn(Arrays.asList(testChild, child2));

            // When
            List<ChildDto> result = childService.getAllActiveChildren();

            // Then
            assertThat(result).hasSize(2);
            assertThat(result.get(0).getId()).isEqualTo(1L);
            assertThat(result.get(1).getId()).isEqualTo(2L);

            verify(childRepository).findAllActive();
        }

        @Test
        @DisplayName("Should return empty list when no active children")
        void shouldReturnEmptyListWhenNoActiveChildren() {
            // Given
            when(childRepository.findAllActive()).thenReturn(List.of());

            // When
            List<ChildDto> result = childService.getAllActiveChildren();

            // Then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("Search Children Tests")
    class SearchChildrenTests {

        @Test
        @DisplayName("Should search children by name")
        void searchChildrenSuccess() {
            // Given
            when(childRepository.searchByName("Juan")).thenReturn(List.of(testChild));

            // When
            List<ChildDto> result = childService.searchChildren("Juan");

            // Then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getFirstName()).isEqualTo("Juan");

            verify(childRepository).searchByName("Juan");
        }

        @Test
        @DisplayName("Should return empty list when no matches found")
        void shouldReturnEmptyListWhenNoMatches() {
            // Given
            when(childRepository.searchByName("NonExistent")).thenReturn(List.of());

            // When
            List<ChildDto> result = childService.searchChildren("NonExistent");

            // Then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("Update Child Tests")
    class UpdateChildTests {

        @Test
        @DisplayName("Should update child successfully")
        void updateChildSuccess() {
            // Given
            when(childRepository.findById(1L)).thenReturn(Optional.of(testChild));
            when(childRepository.save(any(Child.class))).thenReturn(testChild);

            createRequest.setDocumentNumber("123456789"); // Same document number

            // When
            ChildDto result = childService.updateChild(1L, createRequest);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getFirstName()).isEqualTo("María");
            assertThat(result.getLastName()).isEqualTo("González");

            verify(childRepository).findById(1L);
            verify(childRepository).save(testChild);
        }

        @Test
        @DisplayName("Should throw exception when updating non-existent child")
        void shouldThrowExceptionWhenUpdatingNonExistentChild() {
            // Given
            when(childRepository.findById(999L)).thenReturn(Optional.empty());

            // When / Then
            assertThatThrownBy(() -> childService.updateChild(999L, createRequest))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        @DisplayName("Should throw exception when updating deleted child")
        void shouldThrowExceptionWhenUpdatingDeletedChild() {
            // Given
            testChild.softDelete();
            when(childRepository.findById(1L)).thenReturn(Optional.of(testChild));

            // When / Then
            assertThatThrownBy(() -> childService.updateChild(1L, createRequest))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        @DisplayName("Should throw exception when new document number conflicts")
        void shouldThrowExceptionWhenDocumentNumberConflicts() {
            // Given
            Child otherChild = Child.builder()
                    .id(2L)
                    .documentNumber("987654321")
                    .build();

            when(childRepository.findById(1L)).thenReturn(Optional.of(testChild));
            when(childRepository.findByDocumentNumber("987654321")).thenReturn(Optional.of(otherChild));

            // When / Then
            assertThatThrownBy(() -> childService.updateChild(1L, createRequest))
                    .isInstanceOf(DuplicateResourceException.class)
                    .hasMessageContaining("documentNumber");

            verify(childRepository, never()).save(any(Child.class));
        }

        @Test
        @DisplayName("Should allow updating with same document number")
        void shouldAllowUpdatingWithSameDocumentNumber() {
            // Given
            createRequest.setDocumentNumber("123456789"); // Same as testChild

            when(childRepository.findById(1L)).thenReturn(Optional.of(testChild));
            when(childRepository.save(any(Child.class))).thenReturn(testChild);

            // When
            childService.updateChild(1L, createRequest);

            // Then
            verify(childRepository).save(testChild);
            verify(childRepository, never()).findByDocumentNumber(anyString());
        }
    }

    @Nested
    @DisplayName("Delete Child Tests")
    class DeleteChildTests {

        @Test
        @DisplayName("Should soft delete child successfully")
        void deleteChildSuccess() {
            // Given
            when(childRepository.findById(1L)).thenReturn(Optional.of(testChild));
            when(childRepository.save(any(Child.class))).thenReturn(testChild);

            // When
            childService.deleteChild(1L);

            // Then
            assertThat(testChild.isDeleted()).isTrue();
            assertThat(testChild.getDeletedAt()).isNotNull();

            verify(childRepository).findById(1L);
            verify(childRepository).save(testChild);
        }

        @Test
        @DisplayName("Should throw exception when deleting non-existent child")
        void shouldThrowExceptionWhenDeletingNonExistentChild() {
            // Given
            when(childRepository.findById(999L)).thenReturn(Optional.empty());

            // When / Then
            assertThatThrownBy(() -> childService.deleteChild(999L))
                    .isInstanceOf(ResourceNotFoundException.class);

            verify(childRepository, never()).save(any(Child.class));
        }

        @Test
        @DisplayName("Should throw exception when deleting already deleted child")
        void shouldThrowExceptionWhenDeletingAlreadyDeletedChild() {
            // Given
            testChild.softDelete();
            when(childRepository.findById(1L)).thenReturn(Optional.of(testChild));

            // When / Then
            assertThatThrownBy(() -> childService.deleteChild(1L))
                    .isInstanceOf(ResourceNotFoundException.class);

            verify(childRepository, times(1)).findById(1L);
            verify(childRepository, never()).save(any(Child.class));
        }
    }

    @Nested
    @DisplayName("Age Calculation Tests")
    class AgeCalculationTests {

        @Test
        @DisplayName("Should calculate age for newborn (0 months)")
        void shouldCalculateAgeForNewborn() {
            // Given
            createRequest.setDateOfBirth(LocalDate.now());
            when(childRepository.findByDocumentNumber(anyString())).thenReturn(Optional.empty());
            when(childRepository.save(any(Child.class))).thenAnswer(invocation -> {
                Child child = invocation.getArgument(0);
                child.setId(2L);
                child.setCreatedAt(LocalDateTime.now());
                child.setUpdatedAt(LocalDateTime.now());
                return child;
            });

            // When
            ChildDto result = childService.createChild(createRequest);

            // Then
            assertThat(result.getAgeInMonths()).isEqualTo(0);
        }

        @Test
        @DisplayName("Should calculate age for 1 year old (12 months)")
        void shouldCalculateAgeForOneYearOld() {
            // Given
            createRequest.setDateOfBirth(LocalDate.now().minusYears(1));
            when(childRepository.findByDocumentNumber(anyString())).thenReturn(Optional.empty());
            when(childRepository.save(any(Child.class))).thenAnswer(invocation -> {
                Child child = invocation.getArgument(0);
                child.setId(2L);
                child.setCreatedAt(LocalDateTime.now());
                child.setUpdatedAt(LocalDateTime.now());
                return child;
            });

            // When
            ChildDto result = childService.createChild(createRequest);

            // Then
            assertThat(result.getAgeInMonths()).isEqualTo(12);
        }

        @Test
        @DisplayName("Should calculate age for 2 years 3 months old (27 months)")
        void shouldCalculateAgeForTwoYearsThreeMonths() {
            // Given
            createRequest.setDateOfBirth(LocalDate.now().minusYears(2).minusMonths(3));
            when(childRepository.findByDocumentNumber(anyString())).thenReturn(Optional.empty());
            when(childRepository.save(any(Child.class))).thenAnswer(invocation -> {
                Child child = invocation.getArgument(0);
                child.setId(2L);
                child.setCreatedAt(LocalDateTime.now());
                child.setUpdatedAt(LocalDateTime.now());
                return child;
            });

            // When
            ChildDto result = childService.createChild(createRequest);

            // Then
            assertThat(result.getAgeInMonths()).isEqualTo(27);
        }
    }
}
