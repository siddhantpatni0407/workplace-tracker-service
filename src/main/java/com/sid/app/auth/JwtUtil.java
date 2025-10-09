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
import java.util.Map;
import java.util.Optional;

/**
 * JWT utility class for token generation, validation, and claim extraction.
 * Supports enhanced tokens with user details (userId, username, role).
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

        JwtBuilder builder = Jwts.builder()
                .setSubject(subject)
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(secretKey, SignatureAlgorithm.HS256);

        if (extraClaims != null && !extraClaims.isEmpty()) {
            builder.addClaims(extraClaims);
        }

        return builder.compact();
    }

    /** Convenience overload with no extra claims and default TTL */
    public String generateToken(String subject) {
        return generateToken(subject, null, -1);
    }

    /** Convenience overload with extra claims and default TTL */
    public String generateToken(String subject, Map<String, Object> extraClaims) {
        return generateToken(subject, extraClaims, -1);
    }

    /**
     * Generate JWT with additional user claims (role, userId, username)
     *
     * @param subject     subject (email)
     * @param userId      user ID
     * @param username    username/name
     * @param role        user role
     * @param ttlMillis   optional TTL override (if <=0, default expirationTimeMs is used)
     * @return compact JWT string
     */
    public String generateTokenWithUserDetails(String subject, Long userId, String username, String role, long ttlMillis) {
        Map<String, Object> extraClaims = Map.of(
                "userId", userId,
                "username", username,
                "role", role
        );
        return generateToken(subject, extraClaims, ttlMillis);
    }

    /** Convenience overload with default TTL */
    public String generateTokenWithUserDetails(String subject, Long userId, String username, String role) {
        return generateTokenWithUserDetails(subject, userId, username, role, -1);
    }

    /**
     * Parse token and return Claims wrapped in Optional.
     * Recognizes ExpiredJwtException (returns empty but logs at debug) and logs other JwtExceptions.
     */
    private Optional<Claims> parseClaims(String token) {
        try {
            Jws<Claims> parsed = Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .setAllowedClockSkewSeconds(appProperties.getJwtAllowedClockSkewSec())
                    .build()
                    .parseClaimsJws(token);
            return Optional.of(parsed.getBody());
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
            Jws<Claims> parsed = Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .setAllowedClockSkewSeconds(appProperties.getJwtAllowedClockSkewSec())
                    .build()
                    .parseClaimsJws(token);
            return parsed.getBody().getSubject();
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
     * Utility to refresh token by issuing a new token with same subject and optional new TTL.
     * Caller should verify refresh policy (e.g., only when token is near expiry or a valid refresh token exists).
     *
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

        // Copy claims except standard ones (sub/iat/exp)
        Map<String, Object> extraClaims = Map.of(
                "userId", claims.get("userId"),
                "username", claims.get("username"),
                "role", claims.get("role")
        );

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
