package com.sid.app.auth;

import com.sid.app.config.AppProperties;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * JWT utility class for token generation, validation, and claim extraction.
 * Supports enhanced tokens with user details (userId, username, role, platformId, tenantId).
 */
@Component
public class JwtUtil {
    private static final Logger log = LoggerFactory.getLogger(JwtUtil.class);

    private final AppProperties appProperties;
    private SecretKey secretKey;

    public JwtUtil(AppProperties appProperties) {
        this.appProperties = appProperties;
    }

    @PostConstruct
    public void init() {
        String secret = appProperties.getJwtSecret();
        if (secret == null || secret.length() < 32) {
            log.warn("JWT secret appears weak/short (length {}). It's recommended to provide a secure secret of at least 32 characters via configuration (app.jwt.secret).",
                    secret == null ? 0 : secret.length());
        }
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        log.info("JwtUtil initialized (expirationMs={}, allowedClockSkewSec={})",
                appProperties.getJwtExpirationMs(), appProperties.getJwtAllowedClockSkewSec());
    }

    /**
     * Generate JWT with subject (username/email). Optional extra claims and TTL override.
     *
     * @param subject     subject (usually username or email)
     * @param extraClaims optional extra claims to include
     * @param ttlMillis   optional TTL override (if <=0, default expirationTimeMs is used)
     * @return compact JWT string
     */
    public String generateToken(String subject, Map<String, Object> extraClaims, long ttlMillis) {
        long ttl = ttlMillis > 0 ? ttlMillis : appProperties.getJwtExpirationMs();
        Date now = new Date();
        Date expiry = new Date(now.getTime() + ttl);

        JwtBuilder builder = Jwts.builder();

        // Use explicit claim(...) calls to avoid deprecated ClaimsMutator helpers
        builder.claim(Claims.SUBJECT, subject);
        builder.claim(Claims.ISSUED_AT, now);
        builder.claim(Claims.EXPIRATION, expiry);

        // Use signWith(key) to avoid the deprecated signWith(key, alg) overload
        builder.signWith(secretKey);

        if (extraClaims != null && !extraClaims.isEmpty()) {
            // addClaims(Map) is deprecated — add individual claims instead
            extraClaims.forEach(builder::claim);
        }

        return builder.compact();
    }

    /**
     * Convenience overload with no extra claims and default TTL
     */
    public String generateToken(String subject) {
        return generateToken(subject, null, -1);
    }

    /**
     * Convenience overload with extra claims and default TTL
     */
    public String generateToken(String subject, Map<String, Object> extraClaims) {
        return generateToken(subject, extraClaims, -1);
    }

    /**
     * Generate JWT with additional user claims (role, userId, username, platformId, tenantId)
     *
     * @param subject    subject (email)
     * @param userId     user ID
     * @param username   username/name
     * @param role       user role
     * @param platformId platform ID
     * @param tenantId   tenant ID
     * @param ttlMillis  optional TTL override (if <=0, default expirationTimeMs is used)
     * @return compact JWT string
     */
    public String generateTokenWithUserDetails(String subject, Long userId, String username, String role,
                                               Long platformId, Long tenantId, long ttlMillis) {
        // Use LinkedHashMap to preserve insertion order for JWT claims
        Map<String, Object> extraClaims = new LinkedHashMap<>();
        extraClaims.put("userId", userId);
        extraClaims.put("username", username);
        // Note: "sub" (subject) will be added automatically by JWT builder
        extraClaims.put("role", role);
        extraClaims.put("tenantId", tenantId != null ? tenantId : 0L);
        extraClaims.put("platformId", platformId != null ? platformId : 0L);
        // Note: "iat" and "exp" will be added automatically by JWT builder

        return generateToken(subject, extraClaims, ttlMillis);
    }

    /**
     * Convenience overload with default TTL
     */
    public String generateTokenWithUserDetails(String subject, Long userId, String username, String role,
                                               Long platformId, Long tenantId) {
        return generateTokenWithUserDetails(subject, userId, username, role, platformId, tenantId, -1);
    }

    /**
     * Convenience overload with default TTL for backward compatibility
     */
    public String generateTokenWithUserDetails(String subject, Long userId, String username, String role, long ttlMillis) {
        return generateTokenWithUserDetails(subject, userId, username, role, null, null, ttlMillis);
    }

    /**
     * Convenience overload with default TTL for backward compatibility
     */
    public String generateTokenWithUserDetails(String subject, Long userId, String username, String role) {
        return generateTokenWithUserDetails(subject, userId, username, role, null, null, -1);
    }

    /**
     * Parse token and return Claims wrapped in Optional.
     * Recognizes ExpiredJwtException (returns empty but logs at debug) and logs other JwtExceptions.
     */
    private Optional<Claims> parseClaims(String token) {
        try {
            Jws<Claims> parsed = Jwts.parser()
                    .verifyWith(secretKey)
                    .clockSkewSeconds(appProperties.getJwtAllowedClockSkewSec())
                    .build()
                    .parseSignedClaims(token);
            return Optional.of(parsed.getPayload());
        } catch (ExpiredJwtException eje) {
            // token expired - caller may want to treat specially
            log.debug("JWT expired when parsing token: {}", eje.getMessage());
            return Optional.empty();
        } catch (JwtException | IllegalArgumentException ex) {
            // malformed / signature invalid / other issues
            log.warn("Failed to parse/validate JWT: {}", ex.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Extract username (subject) from token.
     * Throws ExpiredJwtException if token is expired so callers can detect it specifically.
     */
    public String extractUsername(String token) throws ExpiredJwtException {
        try {
            Jws<Claims> parsed = Jwts.parser()
                    .verifyWith(secretKey)
                    .clockSkewSeconds(appProperties.getJwtAllowedClockSkewSec())
                    .build()
                    .parseSignedClaims(token);
            return parsed.getPayload().getSubject();
        } catch (ExpiredJwtException eje) {
            // Re-throw ExpiredJwtException so callers can handle it specifically
            throw eje;
        } catch (JwtException | IllegalArgumentException ex) {
            log.warn("Failed to extract username from JWT: {}", ex.getMessage());
            return null;
        }
    }

    /**
     * Extract username from token, returning null if expired or invalid.
     */
    public String extractUsernameIfValid(String token) {
        return parseClaims(token).map(Claims::getSubject).orElse(null);
    }

    /**
     * Check if token is valid for the given user.
     */
    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsernameIfValid(token);
        return (username != null && username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    /**
     * Check if token is expired.
     */
    public boolean isTokenExpired(String token) {
        return parseClaims(token)
                .map(claims -> claims.getExpiration().before(new Date()))
                .orElse(true); // Consider invalid tokens as expired
    }

    /**
     * Extract expiration date from token.
     */
    public Date extractExpiration(String token) {
        return parseClaims(token).map(Claims::getExpiration).orElse(null);
    }

    /**
     * Extract a specific claim from token.
     */
    public Object extractClaim(String token, String claimName) {
        return parseClaims(token).map(claims -> claims.get(claimName)).orElse(null);
    }

    /**
     * Validate token without checking user details.
     */
    public boolean isTokenValid(String token) {
        return parseClaims(token).isPresent();
    }

    /**
     * Extract user ID from token.
     */
    public Long extractUserId(String token) {
        Object userIdObj = extractClaim(token, "userId");
        if (userIdObj instanceof Number) {
            return ((Number) userIdObj).longValue();
        }
        return null;
    }

    /**
     * Extract display name from token.
     */
    public String extractUserDisplayName(String token) {
        Object usernameObj = extractClaim(token, "username");
        return usernameObj != null ? usernameObj.toString() : null;
    }

    /**
     * Extract role from token.
     */
    public String extractRole(String token) {
        Object roleObj = extractClaim(token, "role");
        return roleObj != null ? roleObj.toString() : null;
    }

    /**
     * Extract platform ID from token.
     */
    public Long extractPlatformId(String token) {
        Object platformIdObj = extractClaim(token, "platformId");
        if (platformIdObj instanceof Number) {
            Long platformId = ((Number) platformIdObj).longValue();
            return platformId.equals(0L) ? null : platformId;
        }
        return null;
    }

    /**
     * Extract tenant ID from token.
     */
    public Long extractTenantId(String token) {
        Object tenantIdObj = extractClaim(token, "tenantId");
        if (tenantIdObj instanceof Number) {
            Long tenantId = ((Number) tenantIdObj).longValue();
            return tenantId.equals(0L) ? null : tenantId;
        }
        return null;
    }

    /**
     * Utility to refresh token by issuing a new token with same subject and optional new TTL.
     * Caller should verify refresh policy (e.g., only when token is near expiry or a valid refresh token exists).
     * <p>
     * NOTE: This method expects the provided token to be valid (not expired). If token is expired, parseClaims will return empty.
     * Ideally refresh should be driven by a refresh-token (HTTP-only cookie) rather than by supplying an expired access token.
     */
    public String refreshToken(String token, long newTtlMillis) {
        Optional<Claims> claimsOpt = parseClaims(token);
        if (claimsOpt.isEmpty()) {
            throw new JwtException("Cannot refresh invalid/expired token");
        }
        Claims claims = claimsOpt.get();
        String subject = claims.getSubject();

        // Copy claims with proper ordering using LinkedHashMap
        Map<String, Object> extraClaims = new LinkedHashMap<>();
        extraClaims.put("userId", claims.get("userId"));
        extraClaims.put("username", claims.get("username"));
        // Note: "sub" (subject) will be added automatically by JWT builder
        extraClaims.put("role", claims.get("role"));
        extraClaims.put("tenantId", claims.get("tenantId") != null ? claims.get("tenantId") : 0L);
        extraClaims.put("platformId", claims.get("platformId") != null ? claims.get("platformId") : 0L);
        // Note: "iat" and "exp" will be added automatically by JWT builder

        return generateToken(subject, extraClaims, newTtlMillis);
    }

    /**
     * Check if token contains all required user details (userId, username, role).
     */
    public boolean hasUserDetails(String token) {
        return extractUserId(token) != null &&
                extractUserDisplayName(token) != null &&
                extractRole(token) != null;
    }

    /**
     * Get token expiration time in milliseconds.
     */
    public long getTokenExpirationTime(String token) {
        Date expiration = extractExpiration(token);
        return expiration != null ? expiration.getTime() : 0;
    }

    /**
     * Check if token will expire within the given time frame (in milliseconds).
     */
    public boolean isTokenExpiringWithin(String token, long timeFrameMs) {
        Date expiration = extractExpiration(token);
        if (expiration == null) {
            return true; // Consider invalid tokens as expiring
        }

        long currentTime = System.currentTimeMillis();
        long expirationTime = expiration.getTime();

        return (expirationTime - currentTime) <= timeFrameMs;
    }
}
