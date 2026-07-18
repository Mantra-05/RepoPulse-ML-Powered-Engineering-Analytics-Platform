package com.repopulse.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Returned to the client upon successful authentication (register / login / refresh).
 *
 * <p>Fields:
 * <ul>
 *   <li>{@code accessToken}  – Bearer token to include in the {@code Authorization} header.</li>
 *   <li>{@code refreshToken} – Long-lived token sent to {@code POST /api/v1/auth/refresh}.</li>
 *   <li>{@code tokenType}    – Always {@code "Bearer"}.</li>
 *   <li>{@code expiresIn}    – Access-token lifetime in seconds (for the UI countdown).</li>
 * </ul>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AuthResponse {

    private String accessToken;
    private String refreshToken;

    @Builder.Default
    private String tokenType = "Bearer";

    /** Access-token lifetime in seconds. */
    private Long expiresIn;
}
