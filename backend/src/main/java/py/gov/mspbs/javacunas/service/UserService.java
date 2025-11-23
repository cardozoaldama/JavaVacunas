package py.gov.mspbs.javacunas.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import py.gov.mspbs.javacunas.dto.UserDto;
import py.gov.mspbs.javacunas.entity.User;
import py.gov.mspbs.javacunas.exception.ResourceNotFoundException;
import py.gov.mspbs.javacunas.repository.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for managing users.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;

    /**
     * Get user by ID.
     */
    @Transactional(readOnly = true)
    public UserDto getUserById(Long id) {
        log.debug("Retrieving user with ID: {}", id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
        return mapToDto(user);
    }

    /**
     * Get all users.
     */
    @Transactional(readOnly = true)
    public List<UserDto> getAllUsers() {
        log.debug("Retrieving all users");
        return userRepository.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    /**
     * Get users by role.
     */
    @Transactional(readOnly = true)
    public List<UserDto> getUsersByRole(User.UserRole role) {
        log.debug("Retrieving users with role: {}", role);
        return userRepository.findByRole(role).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    /**
     * Get active users by role.
     */
    @Transactional(readOnly = true)
    public List<UserDto> getActiveUsersByRole(User.UserRole role) {
        log.debug("Retrieving active users with role: {}", role);
        return userRepository.findByRoleAndIsActive(role, 'Y').stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    /**
     * Get all active doctors.
     */
    @Transactional(readOnly = true)
    public List<UserDto> getAllActiveDoctors() {
        return getActiveUsersByRole(User.UserRole.DOCTOR);
    }

    /**
     * Get all active nurses.
     */
    @Transactional(readOnly = true)
    public List<UserDto> getAllActiveNurses() {
        return getActiveUsersByRole(User.UserRole.NURSE);
    }

    /**
     * Get all active medical staff (doctors and nurses).
     */
    @Transactional(readOnly = true)
    public List<UserDto> getAllActiveMedicalStaff() {
        log.debug("Retrieving all active medical staff");
        List<UserDto> doctors = getAllActiveDoctors();
        List<UserDto> nurses = getAllActiveNurses();
        doctors.addAll(nurses);
        return doctors;
    }

    /**
     * Map User entity to DTO.
     */
    private UserDto mapToDto(User user) {
        return UserDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .role(user.getRole())
                .licenseNumber(user.getLicenseNumber())
                .isActive(user.getIsActive())
                .lastLogin(user.getLastLogin())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }

}
