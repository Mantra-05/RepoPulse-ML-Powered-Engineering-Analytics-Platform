package com.repopulse.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Utility service for generating, validating, and parsing JWT tokens.
 *
 * <p>Uses JJWT 0.12.x fluent API (no deprecated {@code SignatureAlgorithm} enum).
 * Two token types are issued:
 * <ul>
 *   <li><b>Access token</b> – short-lived (default 1 day), carries role claim.</li>
 *   <li><b>Refresh token</b> – long-lived (default 7 days), carries a
 *       {@code token_type=refresh} claim so it can be distinguished from access tokens.</li>
 * </ul>
 */
@Service
public class JwtService {

    /** Claim key used to distinguish refresh tokens from access tokens. */
    public static final String CLAIM_TOKEN_TYPE = "token_type";
    public static final String TOKEN_TYPE_REFRESH = "refresh";
    public static final String TOKEN_TYPE_ACCESS  = "access";

    @Value("${application.security.jwt.secret-key}")
    private String secretKey;

    @Value("${application.security.jwt.expiration}")
    private long jwtExpiration;

    @Value("${application.security.jwt.refresh-token.expiration}")
    private long refreshExpiration;

    // ── Token generation ──────────────────────────────────────────────────────

    /**
     * Generates a short-lived access token for the given user.
     * Extra claims include the user's role.
     */
    public String generateAccessToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        claims.put(CLAIM_TOKEN_TYPE, TOKEN_TYPE_ACCESS);
        // Embed roles so downstream code doesn't need another DB call
        claims.put("roles", userDetails.getAuthorities().stream()
                .map(a -> a.getAuthority())
                .toList());
        return buildToken(claims, userDetails, jwtExpiration);
    }

    /**
     * Generates a long-lived refresh token.
     * Carries a {@code token_type=refresh} claim.
     */
    public String generateRefreshToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        claims.put(CLAIM_TOKEN_TYPE, TOKEN_TYPE_REFRESH);
        return buildToken(claims, userDetails, refreshExpiration);
    }

    private String buildToken(Map<String, Object> extraClaims,
                              UserDetails userDetails,
                              long expiration) {
        return Jwts.builder()
                .claims(extraClaims)                                         // JJWT 0.12 API
                .subject(userDetails.getUsername())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSignInKey())                                     // algorithm inferred from key
                .compact();
    }

    // ── Token validation ──────────────────────────────────────────────────────

    /**
     * Returns {@code true} if the token's subject matches {@code userDetails}
     * and the token has not expired.
     */
    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
    }

    /**
     * Returns {@code true} only when the token carries the {@code token_type=refresh} claim.
     * Used by the refresh endpoint to reject access tokens being passed as refresh tokens.
     */
    public boolean isRefreshToken(String token) {
        String type = extractClaim(token, claims -> claims.get(CLAIM_TOKEN_TYPE, String.class));
        return TOKEN_TYPE_REFRESH.equals(type);
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    // ── Claims extraction ─────────────────────────────────────────────────────

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        return claimsResolver.apply(extractAllClaims(token));
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()                       // JJWT 0.12: parserBuilder() is gone
                .verifyWith(getSignInKey())
                .build()
                .parseSignedClaims(token)          // replaces parseClaimsJws()
                .getPayload();
    }

    // ── Key ───────────────────────────────────────────────────────────────────

    private SecretKey getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
