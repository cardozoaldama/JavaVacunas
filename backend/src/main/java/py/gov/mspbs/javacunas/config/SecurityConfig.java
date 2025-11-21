package py.gov.mspbs.javacunas.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import py.gov.mspbs.javacunas.security.CustomUserDetailsService;
import py.gov.mspbs.javacunas.security.JwtAuthenticationFilter;

import java.util.Arrays;
import java.util.List;

/**
 * Spring Security configuration with JWT authentication.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Public endpoints
                        .requestMatchers("/api/v1/auth/**").permitAll()
                        .requestMatchers("/swagger-ui/**", "/api-docs/**", "/swagger-ui.html").permitAll()
                        .requestMatchers("/actuator/health").permitAll()

                        // Vaccines - read-only for all authenticated users
                        .requestMatchers(HttpMethod.GET, "/api/v1/vaccines/**").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/v1/schedules/**").authenticated()

                        // Children - DOCTOR, NURSE, PARENT can read
                        .requestMatchers(HttpMethod.GET, "/api/v1/children/**").hasAnyRole("DOCTOR", "NURSE", "PARENT")
                        .requestMatchers(HttpMethod.POST, "/api/v1/children/**").hasAnyRole("DOCTOR", "NURSE")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/children/**").hasAnyRole("DOCTOR", "NURSE")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/children/**").hasRole("DOCTOR")

                        // Vaccination records - DOCTOR, NURSE can create/update
                        .requestMatchers(HttpMethod.GET, "/api/v1/vaccinations/**").hasAnyRole("DOCTOR", "NURSE", "PARENT")
                        .requestMatchers(HttpMethod.POST, "/api/v1/vaccinations/**").hasAnyRole("DOCTOR", "NURSE")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/vaccinations/**").hasAnyRole("DOCTOR", "NURSE")

                        // Appointments - authenticated users can manage
                        .requestMatchers("/api/v1/appointments/**").authenticated()

                        // Inventory - DOCTOR, NURSE only
                        .requestMatchers("/api/v1/inventory/**").hasAnyRole("DOCTOR", "NURSE")

                        // All other requests require authentication
                        .anyRequest().authenticated()
                )
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("http://localhost:5173", "http://localhost:3000"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "Accept"));
        configuration.setExposedHeaders(List.of("Authorization"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", configuration);
        return source;
    }

}
