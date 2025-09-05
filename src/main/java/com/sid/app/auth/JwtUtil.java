package com.sid.app.auth;

import com.sid.app.config.AppProperties;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;
import java.util.Optional;

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
            log.warn("JWT secret appears weak/short (length {}). It's recommended to provide a secure secret of at least 32 characters via configuration (app.jwt.secret).", secret == null ? 0 : secret.length());
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
    public String extractUsername(String token) {
        try {
            Jws<Claims> parsed = Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .setAllowedClockSkewSeconds(appProperties.getJwtAllowedClockSkewSec())
                    .build()
                    .parseClaimsJws(token);
            return parsed.getBody().getSubject();
        } catch (ExpiredJwtException eje) {
            log.debug("extractUsername: token expired: {}", eje.getMessage());
            // rethrow so filters can catch and respond with a proper 401/refresh flow
            throw eje;
        } catch (JwtException | IllegalArgumentException ex) {
            log.warn("extractUsername: invalid token: {}", ex.getMessage());
            throw ex;
        }
    }

    /**
     * Validate token belongs to username and not expired.
     * Returns false on invalid/expired tokens (does not throw).
     */
    public boolean validateToken(String token, String username) {
        try {
            Optional<Claims> claimsOpt = parseClaims(token);
            if (claimsOpt.isEmpty()) return false;
            Claims claims = claimsOpt.get();
            String subj = claims.getSubject();
            return subj != null && subj.equals(username) && !isTokenExpired(claims);
        } catch (Exception ex) {
            log.warn("validateToken exception: {}", ex.getMessage());
            return false;
        }
    }

    /** Check expiration using Claims (already parsed with allowed clock skew). */
    private boolean isTokenExpired(Claims claims) {
        Date expiration = claims.getExpiration();
        if (expiration == null) return true;
        return expiration.before(new Date());
    }

    /** Convenience: parse and check expiration (returns true if expired or invalid) */
    private boolean isTokenExpired(String token) {
        Optional<Claims> claimsOpt = parseClaims(token);
        return claimsOpt.map(this::isTokenExpired).orElse(true);
    }

    /**
     * Returns remaining validity in milliseconds or 0 if expired/invalid.
     * Useful to decide when to trigger refresh on client side.
     */
    public long getRemainingValidityMillis(String token) {
        try {
            Optional<Claims> claimsOpt = parseClaims(token);
            if (claimsOpt.isEmpty()) return 0L;
            Date exp = claimsOpt.get().getExpiration();
            if (exp == null) return 0L;
            long diff = exp.getTime() - System.currentTimeMillis();
            return Math.max(0L, diff);
        } catch (Exception ex) {
            log.debug("getRemainingValidityMillis: {}", ex.getMessage());
            return 0L;
        }
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

        // copy claims except standard ones (sub/iat/exp)
        claims.remove(Claims.SUBJECT);
        claims.remove(Claims.ISSUED_AT);
        claims.remove(Claims.EXPIRATION);

        return generateToken(subject, claims, newTtlMillis);
    }
}
