package py.gov.mspbs.javacunas.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import py.gov.mspbs.javacunas.entity.User;

/**
 * Response DTO for authentication (login/register).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthResponse {

    private String token;
    private String tokenType = "Bearer";
    private Long userId;
    private String username;
    private String email;
    private User.UserRole role;
    private String firstName;
    private String lastName;

}
