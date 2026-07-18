package com.repopulse.config;

import com.repopulse.repository.UserRepository;
import com.repopulse.security.JwtAuthenticationEntryPoint;
import com.repopulse.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Complete Spring Security configuration for RepoPulse.
 *
 * <ul>
 *   <li>Stateless session (JWT only, no HTTP sessions)</li>
 *   <li>CSRF disabled (REST API with JWT bearer tokens)</li>
 *   <li>BCrypt password encoding</li>
 *   <li>{@link DaoAuthenticationProvider} wired to the {@link UserDetailsService}</li>
 *   <li>Public routes: {@code /api/v1/auth/**}, Swagger UI, OpenAPI docs</li>
 *   <li>Every other route requires a valid JWT access token</li>
 *   <li>Custom 401 JSON response via {@link JwtAuthenticationEntryPoint}</li>
 * </ul>
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final JwtAuthenticationEntryPoint jwtAuthEntryPoint;
    private final UserRepository userRepository;

    // ── Security Filter Chain ─────────────────────────────────────────────────

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // ── Disable CSRF – we are stateless (JWT bearer tokens) ──────────
            .csrf(AbstractHttpConfigurer::disable)

            // ── Custom 401 handler ───────────────────────────────────────────
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint(jwtAuthEntryPoint)
            )

            // ── No HTTP sessions ─────────────────────────────────────────────
            .sessionManagement(sm -> sm
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )

            // ── Route authorisation ──────────────────────────────────────────
            .authorizeHttpRequests(auth -> auth

                // Auth endpoints – fully public
                .requestMatchers(HttpMethod.POST,
                    "/api/v1/auth/register",
                    "/api/v1/auth/login",
                    "/api/v1/auth/refresh"
                ).permitAll()

                // OpenAPI / Swagger UI – public
                .requestMatchers(
                    "/v3/api-docs/**",
                    "/swagger-ui/**",
                    "/swagger-ui.html",
                    "/swagger-resources/**",
                    "/webjars/**"
                ).permitAll()

                // Everything else needs a valid JWT
                .anyRequest().authenticated()
            )

            // ── Authentication provider ──────────────────────────────────────
            .authenticationProvider(authenticationProvider())

            // ── Add JWT filter before Spring's username/password filter ──────
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // ── Beans ─────────────────────────────────────────────────────────────────

    /**
     * Loads a {@link com.repopulse.entity.User} by username for authentication.
     * The {@link com.repopulse.entity.User} entity already implements
     * {@link org.springframework.security.core.userdetails.UserDetails}.
     */
    @Bean
    public UserDetailsService userDetailsService() {
        return username -> userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "User not found with username: " + username));
    }

    /**
     * {@link DaoAuthenticationProvider} that delegates to our
     * {@link UserDetailsService} and uses BCrypt for password verification.
     */
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService());
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    /**
     * Exposes the {@link AuthenticationManager} so that
     * {@link com.repopulse.service.impl.AuthServiceImpl} can trigger
     * username/password authentication programmatically.
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config)
            throws Exception {
        return config.getAuthenticationManager();
    }

    /** BCrypt with default strength (10 rounds). */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
