package py.gov.mspbs.javacunas.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import py.gov.mspbs.javacunas.entity.User;

import java.time.LocalDateTime;

/**
 * DTO for User entity (excluding sensitive data).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {

    private Long id;
    private String username;
    private String firstName;
    private String lastName;
    private String email;
    private User.UserRole role;
    private String licenseNumber;
    private Character isActive;
    private LocalDateTime lastLogin;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

}
