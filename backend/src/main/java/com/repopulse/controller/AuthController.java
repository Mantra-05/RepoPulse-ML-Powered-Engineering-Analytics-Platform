package com.repopulse.controller;

import com.repopulse.dto.request.LoginRequest;
import com.repopulse.dto.request.RefreshTokenRequest;
import com.repopulse.dto.request.RegisterRequest;
import com.repopulse.dto.response.ApiResponse;
import com.repopulse.dto.response.AuthResponse;
import com.repopulse.dto.response.UserResponse;
import com.repopulse.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for JWT-based authentication.
 *
 * <pre>
 * POST /api/v1/auth/register  – register a new account
 * POST /api/v1/auth/login     – authenticate with credentials
 * POST /api/v1/auth/refresh   – exchange a refresh token for a new access token
 * GET  /api/v1/auth/me        – return the currently authenticated user's profile
 * </pre>
 *
 * All endpoints except {@code /me} are publicly accessible (no JWT required).
 */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    // ── Register ──────────────────────────────────────────────────────────────

    /**
     * Create a new user account.
     *
     * <p>Request body: {@link RegisterRequest}<br>
     * Response: access token + refresh token
     *
     * @return 201 Created with {@link AuthResponse}
     */
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(
            @Valid @RequestBody RegisterRequest request) {

        AuthResponse response = authService.register(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("User registered successfully", response));
    }

    // ── Login ─────────────────────────────────────────────────────────────────

    /**
     * Authenticate with username and password.
     *
     * <p>Request body: {@link LoginRequest}<br>
     * Response: access token + refresh token
     *
     * @return 200 OK with {@link AuthResponse}
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request) {

        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success("Login successful", response));
    }

    // ── Refresh ───────────────────────────────────────────────────────────────

    /**
     * Exchange a valid refresh token for a new short-lived access token.
     *
     * <p>The refresh token is sent in the request body (not a custom header)
     * for consistent REST semantics and to allow standard HTTP clients to use it.
     *
     * <p>Request body: {@link RefreshTokenRequest}<br>
     * Response: new access token (same refresh token)
     *
     * @return 200 OK with {@link AuthResponse}
     */
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthResponse>> refresh(
            @Valid @RequestBody RefreshTokenRequest request) {

        AuthResponse response = authService.refreshToken(request);
        return ResponseEntity.ok(ApiResponse.success("Token refreshed successfully", response));
    }

    // ── Current user ──────────────────────────────────────────────────────────

    /**
     * Return the profile of the currently authenticated user.
     * Requires a valid Bearer access token.
     *
     * @return 200 OK with {@link UserResponse}
     */
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> getCurrentUser(
            @AuthenticationPrincipal UserDetails userDetails) {

        UserResponse response = authService.getCurrentUser(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
