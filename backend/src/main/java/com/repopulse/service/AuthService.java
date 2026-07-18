package com.repopulse.service;

import com.repopulse.dto.request.LoginRequest;
import com.repopulse.dto.request.RefreshTokenRequest;
import com.repopulse.dto.request.RegisterRequest;
import com.repopulse.dto.response.AuthResponse;
import com.repopulse.dto.response.UserResponse;

public interface AuthService {

    /** Register a new user and return a fresh token pair. */
    AuthResponse register(RegisterRequest request);

    /** Authenticate with username + password and return a fresh token pair. */
    AuthResponse login(LoginRequest request);

    /**
     * Exchange a valid refresh token for a new access token.
     * The same refresh token is returned (no rotation required by default).
     */
    AuthResponse refreshToken(RefreshTokenRequest request);

    /** Return the profile of the currently authenticated user. */
    UserResponse getCurrentUser(String username);
}
