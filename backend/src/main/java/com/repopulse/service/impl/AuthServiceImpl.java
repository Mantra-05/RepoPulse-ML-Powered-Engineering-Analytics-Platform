package com.repopulse.service.impl;

import com.repopulse.dto.request.LoginRequest;
import com.repopulse.dto.request.RefreshTokenRequest;
import com.repopulse.dto.request.RegisterRequest;
import com.repopulse.dto.response.AuthResponse;
import com.repopulse.dto.response.UserResponse;
import com.repopulse.entity.User;
import com.repopulse.exception.DuplicateResourceException;
import com.repopulse.exception.ResourceNotFoundException;
import com.repopulse.mapper.UserMapper;
import com.repopulse.repository.UserRepository;
import com.repopulse.security.JwtService;
import com.repopulse.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Complete JWT authentication service.
 *
 * <p>Flow:
 * <ol>
 *   <li><b>Register</b> – validates uniqueness, BCrypt-hashes the password,
 *       persists the user, and returns a fresh access + refresh token pair.</li>
 *   <li><b>Login</b> – delegates to {@link AuthenticationManager}, which calls
 *       {@link org.springframework.security.core.userdetails.UserDetailsService}
 *       and verifies the BCrypt hash. On success, returns a fresh token pair.</li>
 *   <li><b>Refresh</b> – verifies the supplied refresh token is genuine and
 *       not expired, then issues a new access token (refresh token is kept).</li>
 * </ol>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final UserRepository       userRepository;
    private final PasswordEncoder      passwordEncoder;
    private final JwtService           jwtService;
    private final AuthenticationManager authenticationManager;
    private final UserMapper           userMapper;

    @Value("${application.security.jwt.expiration}")
    private long jwtExpiration;          // ms

    // ── Register ──────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {

        // ── 1. Uniqueness guards ────────────────────────────────────────────
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new DuplicateResourceException(
                    "Username is already taken: " + request.getUsername());
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException(
                    "Email is already registered: " + request.getEmail());
        }

        // ── 2. Persist new user ─────────────────────────────────────────────
        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))   // BCrypt hash
                .role(User.Role.ROLE_USER)
                .build();

        userRepository.save(user);
        log.info("New user registered: username={}, email={}", user.getUsername(), user.getEmail());

        // ── 3. Issue token pair ─────────────────────────────────────────────
        return buildAuthResponse(user);
    }

    // ── Login ─────────────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {

        // Throws BadCredentialsException if auth fails – caught by GlobalExceptionHandler
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User", "username", request.getUsername()));

        log.info("User logged in: username={}", user.getUsername());
        return buildAuthResponse(user);
    }

    // ── Refresh ───────────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public AuthResponse refreshToken(RefreshTokenRequest request) {

        final String token    = request.getRefreshToken();
        final String username = jwtService.extractUsername(token);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User", "username", username));

        // Guard 1: token must still be valid (signature + expiry)
        if (!jwtService.isTokenValid(token, user)) {
            throw new IllegalArgumentException("Refresh token is invalid or has expired.");
        }

        // Guard 2: token must carry the refresh-type claim
        if (!jwtService.isRefreshToken(token)) {
            throw new IllegalArgumentException(
                    "Provided token is not a refresh token. Use the token issued at login.");
        }

        String newAccessToken = jwtService.generateAccessToken(user);
        log.info("Access token refreshed for username={}", username);

        return AuthResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(token)              // keep the same refresh token
                .expiresIn(jwtExpiration / 1000L) // convert ms → seconds
                .build();
    }

    // ── Current user ──────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public UserResponse getCurrentUser(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User", "username", username));
        return userMapper.toResponse(user);
    }

    // ── Helper ────────────────────────────────────────────────────────────────

    private AuthResponse buildAuthResponse(User user) {
        String accessToken  = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .expiresIn(jwtExpiration / 1000L)  // convert ms → seconds
                .build();
    }
}
