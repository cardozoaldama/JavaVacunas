package py.gov.mspbs.javacunas.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import py.gov.mspbs.javacunas.dto.AuthResponse;
import py.gov.mspbs.javacunas.dto.LoginRequest;
import py.gov.mspbs.javacunas.dto.RegisterRequest;
import py.gov.mspbs.javacunas.entity.User;
import py.gov.mspbs.javacunas.exception.DuplicateResourceException;
import py.gov.mspbs.javacunas.repository.UserRepository;
import py.gov.mspbs.javacunas.security.JwtTokenProvider;

/**
 * Service for authentication operations (login, register).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;

    /**
     * Authenticate user and generate JWT token.
     */
    @Transactional
    public AuthResponse login(LoginRequest request) {
        log.info("User login attempt: {}", request.getUsername());

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        String jwt = tokenProvider.generateToken(authentication);

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Update last login timestamp
        user.setLastLogin(java.time.LocalDateTime.now());
        userRepository.save(user);

        log.info("User logged in successfully: {}", request.getUsername());

        return AuthResponse.builder()
                .token(jwt)
                .tokenType("Bearer")
                .userId(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .build();
    }

    /**
     * Register a new user.
     */
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        log.info("New user registration: {}", request.getUsername());

        // Check if username already exists
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new DuplicateResourceException("User", "username", request.getUsername());
        }

        // Check if email already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("User", "email", request.getEmail());
        }

        // Create new user
        User user = User.builder()
                .username(request.getUsername())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .role(request.getRole())
                .licenseNumber(request.getLicenseNumber())
                .isActive('Y')
                .build();

        User savedUser = userRepository.save(user);

        // Generate JWT token
        String jwt = tokenProvider.generateTokenFromUserId(
                savedUser.getId(),
                savedUser.getUsername(),
                savedUser.getRole().name()
        );

        log.info("User registered successfully: {}", request.getUsername());

        return AuthResponse.builder()
                .token(jwt)
                .tokenType("Bearer")
                .userId(savedUser.getId())
                .username(savedUser.getUsername())
                .email(savedUser.getEmail())
                .role(savedUser.getRole())
                .firstName(savedUser.getFirstName())
                .lastName(savedUser.getLastName())
                .build();
    }

}
