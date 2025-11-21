package py.gov.mspbs.javacunas.security;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import py.gov.mspbs.javacunas.entity.User;

import java.util.Collection;
import java.util.Collections;

/**
 * Spring Security UserDetails implementation.
 */
@AllArgsConstructor
@Getter
public class UserPrincipal implements UserDetails {

    private Long id;
    private String username;
    private String password;
    private User.UserRole role;
    private boolean isActive;

    /**
     * Create UserPrincipal from User entity.
     */
    public static UserPrincipal create(User user) {
        return new UserPrincipal(
                user.getId(),
                user.getUsername(),
                user.getPasswordHash(),
                user.getRole(),
                user.getIsActive() == 'Y'
        );
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(
                new SimpleGrantedAuthority("ROLE_" + role.name())
        );
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return isActive;
    }

}
