package py.gov.mspbs.javacunas.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import py.gov.mspbs.javacunas.BaseUnitTest;
import py.gov.mspbs.javacunas.dto.AuthResponse;
import py.gov.mspbs.javacunas.dto.LoginRequest;
import py.gov.mspbs.javacunas.dto.RegisterRequest;
import py.gov.mspbs.javacunas.entity.User;
import py.gov.mspbs.javacunas.entity.User.UserRole;
import py.gov.mspbs.javacunas.exception.DuplicateResourceException;
import py.gov.mspbs.javacunas.repository.UserRepository;
import py.gov.mspbs.javacunas.security.JwtTokenProvider;
import py.gov.mspbs.javacunas.security.UserPrincipal;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@DisplayName("AuthService Tests")
class AuthServiceTest extends BaseUnitTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider tokenProvider;

    @InjectMocks
    private AuthService authService;

    private User testUser;
    private LoginRequest loginRequest;
    private RegisterRequest registerRequest;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .username("testdoctor")
                .passwordHash("$2a$10$hashedPassword")
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@test.com")
                .role(UserRole.DOCTOR)
                .licenseNumber("DOC-001")
                .isActive('Y')
                .lastLogin(LocalDateTime.now().minusDays(1))
                .createdAt(LocalDateTime.now().minusDays(30))
                .updatedAt(LocalDateTime.now().minusDays(1))
                .build();

        loginRequest = new LoginRequest();
        loginRequest.setUsername("testdoctor");
        loginRequest.setPassword("password123");

        registerRequest = new RegisterRequest();
        registerRequest.setUsername("newdoctor");
        registerRequest.setPassword("newpassword123");
        registerRequest.setFirstName("Jane");
        registerRequest.setLastName("Smith");
        registerRequest.setEmail("jane.smith@test.com");
        registerRequest.setRole(UserRole.DOCTOR);
        registerRequest.setLicenseNumber("DOC-002");
    }

    @Nested
    @DisplayName("Login Tests")
    class LoginTests {

        @Test
        @DisplayName("Should login successfully with valid credentials")
        void loginSuccess() {
            // Given
            Authentication authentication = mock(Authentication.class);

            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .thenReturn(authentication);
            when(userRepository.findByUsername("testdoctor"))
                    .thenReturn(Optional.of(testUser));
            when(tokenProvider.generateToken(authentication))
                    .thenReturn("jwt-token-12345");

            // When
            AuthResponse response = authService.login(loginRequest);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getToken()).isEqualTo("jwt-token-12345");
            assertThat(response.getTokenType()).isEqualTo("Bearer");
            assertThat(response.getUserId()).isEqualTo(1L);
            assertThat(response.getUsername()).isEqualTo("testdoctor");
            assertThat(response.getEmail()).isEqualTo("john.doe@test.com");
            assertThat(response.getRole()).isEqualTo(UserRole.DOCTOR);
            assertThat(response.getFirstName()).isEqualTo("John");
            assertThat(response.getLastName()).isEqualTo("Doe");

            verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
            verify(userRepository).findByUsername("testdoctor");
            verify(userRepository).save(testUser);
            verify(tokenProvider).generateToken(authentication);
        }

        @Test
        @DisplayName("Should update last login timestamp on successful login")
        void shouldUpdateLastLoginTimestamp() {
            // Given
            LocalDateTime beforeLogin = LocalDateTime.now().minusSeconds(1);
            Authentication authentication = mock(Authentication.class);

            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .thenReturn(authentication);
            when(userRepository.findByUsername("testdoctor"))
                    .thenReturn(Optional.of(testUser));
            when(tokenProvider.generateToken(authentication))
                    .thenReturn("jwt-token-12345");

            // When
            authService.login(loginRequest);

            // Then
            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
            verify(userRepository).save(userCaptor.capture());
            User savedUser = userCaptor.getValue();

            assertThat(savedUser.getLastLogin()).isAfter(beforeLogin);
            assertThat(savedUser.getLastLogin()).isBeforeOrEqualTo(LocalDateTime.now());
        }

        @Test
        @DisplayName("Should throw exception when user not found after authentication")
        void shouldThrowExceptionWhenUserNotFoundAfterAuth() {
            // Given
            Authentication authentication = mock(Authentication.class);
            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .thenReturn(authentication);
            when(userRepository.findByUsername("testdoctor"))
                    .thenReturn(Optional.empty());

            // When / Then
            assertThatThrownBy(() -> authService.login(loginRequest))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("User not found");
        }

        @Test
        @DisplayName("Should throw exception when credentials are invalid")
        void shouldThrowExceptionWhenCredentialsInvalid() {
            // Given
            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .thenThrow(new BadCredentialsException("Bad credentials"));

            // When / Then
            assertThatThrownBy(() -> authService.login(loginRequest))
                    .isInstanceOf(BadCredentialsException.class)
                    .hasMessage("Bad credentials");

            verify(userRepository, never()).save(any(User.class));
        }

        @Test
        @DisplayName("Should create correct authentication token")
        void shouldCreateCorrectAuthenticationToken() {
            // Given
            Authentication authentication = mock(Authentication.class);

            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .thenReturn(authentication);
            when(userRepository.findByUsername("testdoctor"))
                    .thenReturn(Optional.of(testUser));
            when(tokenProvider.generateToken(authentication))
                    .thenReturn("jwt-token-12345");

            // When
            authService.login(loginRequest);

            // Then
            ArgumentCaptor<UsernamePasswordAuthenticationToken> tokenCaptor =
                    ArgumentCaptor.forClass(UsernamePasswordAuthenticationToken.class);
            verify(authenticationManager).authenticate(tokenCaptor.capture());

            UsernamePasswordAuthenticationToken capturedToken = tokenCaptor.getValue();
            assertThat(capturedToken.getPrincipal()).isEqualTo("testdoctor");
            assertThat(capturedToken.getCredentials()).isEqualTo("password123");
        }
    }

    @Nested
    @DisplayName("Register Tests")
    class RegisterTests {

        @Test
        @DisplayName("Should register new user successfully")
        void registerSuccess() {
            // Given
            User newUser = User.builder()
                    .id(2L)
                    .username("newdoctor")
                    .passwordHash("encodedPassword")
                    .firstName("Jane")
                    .lastName("Smith")
                    .email("jane.smith@test.com")
                    .role(UserRole.DOCTOR)
                    .licenseNumber("DOC-002")
                    .isActive('Y')
                    .build();

            when(userRepository.existsByUsername("newdoctor")).thenReturn(false);
            when(userRepository.existsByEmail("jane.smith@test.com")).thenReturn(false);
            when(passwordEncoder.encode("newpassword123")).thenReturn("encodedPassword");
            when(userRepository.save(any(User.class))).thenReturn(newUser);
            when(tokenProvider.generateTokenFromUserId(2L, "newdoctor", "DOCTOR"))
                    .thenReturn("jwt-token-67890");

            // When
            AuthResponse response = authService.register(registerRequest);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getToken()).isEqualTo("jwt-token-67890");
            assertThat(response.getTokenType()).isEqualTo("Bearer");
            assertThat(response.getUserId()).isEqualTo(2L);
            assertThat(response.getUsername()).isEqualTo("newdoctor");
            assertThat(response.getEmail()).isEqualTo("jane.smith@test.com");
            assertThat(response.getRole()).isEqualTo(UserRole.DOCTOR);
            assertThat(response.getFirstName()).isEqualTo("Jane");
            assertThat(response.getLastName()).isEqualTo("Smith");

            verify(userRepository).existsByUsername("newdoctor");
            verify(userRepository).existsByEmail("jane.smith@test.com");
            verify(passwordEncoder).encode("newpassword123");
            verify(userRepository).save(any(User.class));
            verify(tokenProvider).generateTokenFromUserId(2L, "newdoctor", "DOCTOR");
        }

        @Test
        @DisplayName("Should encode password before saving user")
        void shouldEncodePassword() {
            // Given
            when(userRepository.existsByUsername(anyString())).thenReturn(false);
            when(userRepository.existsByEmail(anyString())).thenReturn(false);
            when(passwordEncoder.encode("newpassword123")).thenReturn("encodedPassword");
            when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
                User user = invocation.getArgument(0);
                user.setId(2L);
                return user;
            });
            when(tokenProvider.generateTokenFromUserId(anyLong(), anyString(), anyString()))
                    .thenReturn("jwt-token");

            // When
            authService.register(registerRequest);

            // Then
            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
            verify(userRepository).save(userCaptor.capture());
            User savedUser = userCaptor.getValue();

            assertThat(savedUser.getPasswordHash()).isEqualTo("encodedPassword");
            assertThat(savedUser.getPasswordHash()).isNotEqualTo("newpassword123");
        }

        @Test
        @DisplayName("Should set user as active by default")
        void shouldSetUserAsActiveByDefault() {
            // Given
            when(userRepository.existsByUsername(anyString())).thenReturn(false);
            when(userRepository.existsByEmail(anyString())).thenReturn(false);
            when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
            when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
                User user = invocation.getArgument(0);
                user.setId(2L);
                return user;
            });
            when(tokenProvider.generateTokenFromUserId(anyLong(), anyString(), anyString()))
                    .thenReturn("jwt-token");

            // When
            authService.register(registerRequest);

            // Then
            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
            verify(userRepository).save(userCaptor.capture());
            User savedUser = userCaptor.getValue();

            assertThat(savedUser.getIsActive()).isEqualTo('Y');
        }

        @Test
        @DisplayName("Should throw exception when username already exists")
        void shouldThrowExceptionWhenUsernameExists() {
            // Given
            when(userRepository.existsByUsername("newdoctor")).thenReturn(true);

            // When / Then
            assertThatThrownBy(() -> authService.register(registerRequest))
                    .isInstanceOf(DuplicateResourceException.class)
                    .hasMessageContaining("username")
                    .hasMessageContaining("newdoctor");

            verify(userRepository).existsByUsername("newdoctor");
            verify(userRepository, never()).existsByEmail(anyString());
            verify(userRepository, never()).save(any(User.class));
        }

        @Test
        @DisplayName("Should throw exception when email already exists")
        void shouldThrowExceptionWhenEmailExists() {
            // Given
            when(userRepository.existsByUsername("newdoctor")).thenReturn(false);
            when(userRepository.existsByEmail("jane.smith@test.com")).thenReturn(true);

            // When / Then
            assertThatThrownBy(() -> authService.register(registerRequest))
                    .isInstanceOf(DuplicateResourceException.class)
                    .hasMessageContaining("email")
                    .hasMessageContaining("jane.smith@test.com");

            verify(userRepository).existsByUsername("newdoctor");
            verify(userRepository).existsByEmail("jane.smith@test.com");
            verify(userRepository, never()).save(any(User.class));
        }

        @Test
        @DisplayName("Should save user with all fields correctly")
        void shouldSaveUserWithAllFields() {
            // Given
            when(userRepository.existsByUsername(anyString())).thenReturn(false);
            when(userRepository.existsByEmail(anyString())).thenReturn(false);
            when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
            when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
                User user = invocation.getArgument(0);
                user.setId(2L);
                return user;
            });
            when(tokenProvider.generateTokenFromUserId(anyLong(), anyString(), anyString()))
                    .thenReturn("jwt-token");

            // When
            authService.register(registerRequest);

            // Then
            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
            verify(userRepository).save(userCaptor.capture());
            User savedUser = userCaptor.getValue();

            assertThat(savedUser.getUsername()).isEqualTo("newdoctor");
            assertThat(savedUser.getFirstName()).isEqualTo("Jane");
            assertThat(savedUser.getLastName()).isEqualTo("Smith");
            assertThat(savedUser.getEmail()).isEqualTo("jane.smith@test.com");
            assertThat(savedUser.getRole()).isEqualTo(UserRole.DOCTOR);
            assertThat(savedUser.getLicenseNumber()).isEqualTo("DOC-002");
            assertThat(savedUser.getIsActive()).isEqualTo('Y');
        }

        @Test
        @DisplayName("Should register nurse with correct role")
        void shouldRegisterNurse() {
            // Given
            registerRequest.setRole(UserRole.NURSE);
            registerRequest.setUsername("newnurse");
            registerRequest.setEmail("nurse@test.com");

            when(userRepository.existsByUsername(anyString())).thenReturn(false);
            when(userRepository.existsByEmail(anyString())).thenReturn(false);
            when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
            when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
                User user = invocation.getArgument(0);
                user.setId(3L);
                return user;
            });
            when(tokenProvider.generateTokenFromUserId(anyLong(), anyString(), anyString()))
                    .thenReturn("jwt-token");

            // When
            AuthResponse response = authService.register(registerRequest);

            // Then
            assertThat(response.getRole()).isEqualTo(UserRole.NURSE);

            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
            verify(userRepository).save(userCaptor.capture());
            assertThat(userCaptor.getValue().getRole()).isEqualTo(UserRole.NURSE);
        }

        @Test
        @DisplayName("Should register parent with correct role")
        void shouldRegisterParent() {
            // Given
            registerRequest.setRole(UserRole.PARENT);
            registerRequest.setUsername("newparent");
            registerRequest.setEmail("parent@test.com");
            registerRequest.setLicenseNumber(null); // Parents don't have license numbers

            when(userRepository.existsByUsername(anyString())).thenReturn(false);
            when(userRepository.existsByEmail(anyString())).thenReturn(false);
            when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
            when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
                User user = invocation.getArgument(0);
                user.setId(4L);
                return user;
            });
            when(tokenProvider.generateTokenFromUserId(anyLong(), anyString(), anyString()))
                    .thenReturn("jwt-token");

            // When
            AuthResponse response = authService.register(registerRequest);

            // Then
            assertThat(response.getRole()).isEqualTo(UserRole.PARENT);

            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
            verify(userRepository).save(userCaptor.capture());
            User savedUser = userCaptor.getValue();
            assertThat(savedUser.getRole()).isEqualTo(UserRole.PARENT);
            assertThat(savedUser.getLicenseNumber()).isNull();
        }

        @Test
        @DisplayName("Should generate JWT token with correct user data")
        void shouldGenerateTokenWithCorrectData() {
            // Given
            when(userRepository.existsByUsername(anyString())).thenReturn(false);
            when(userRepository.existsByEmail(anyString())).thenReturn(false);
            when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
            when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
                User user = invocation.getArgument(0);
                user.setId(2L);
                return user;
            });
            when(tokenProvider.generateTokenFromUserId(2L, "newdoctor", "DOCTOR"))
                    .thenReturn("jwt-token-67890");

            // When
            authService.register(registerRequest);

            // Then
            verify(tokenProvider).generateTokenFromUserId(2L, "newdoctor", "DOCTOR");
        }
    }
}
