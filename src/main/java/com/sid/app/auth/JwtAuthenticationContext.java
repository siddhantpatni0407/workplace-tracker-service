package com.sid.app.auth;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Utility class to extract user information from JWT tokens in controllers.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationContext {

    private final JwtUtil jwtUtil;

    /**
     * Extract the current authenticated user's ID from the JWT token.
     */
    public Long getCurrentUserId() {
        String token = getCurrentToken();
        return token != null ? jwtUtil.extractUserId(token) : null;
    }

    /**
     * Extract the current authenticated user's email from the JWT token.
     */
    public String getCurrentUserEmail() {
        String token = getCurrentToken();
        return token != null ? jwtUtil.extractUsername(token) : null;
    }

    /**
     * Extract the current authenticated user's display name from the JWT token.
     */
    public String getCurrentUserDisplayName() {
        String token = getCurrentToken();
        return token != null ? jwtUtil.extractUserDisplayName(token) : null;
    }

    /**
     * Extract the current authenticated user's role from the JWT token.
     */
    public String getCurrentUserRole() {
        String token = getCurrentToken();
        return token != null ? jwtUtil.extractRole(token) : null;
    }

    /**
     * Check if the current user has a specific role.
     */
    public boolean hasRole(String role) {
        String userRole = getCurrentUserRole();
        return role != null && role.equals(userRole);
    }

    /**
     * Check if the current user has any of the specified roles.
     */
    public boolean hasAnyRole(String... roles) {
        String userRole = getCurrentUserRole();
        if (userRole == null) return false;

        for (String role : roles) {
            if (userRole.equals(role)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check if the current user is the owner (same userId) or has admin privileges.
     */
    public boolean isOwnerOrAdmin(Long resourceUserId) {
        Long currentUserId = getCurrentUserId();
        String currentUserRole = getCurrentUserRole();

        // Check if user is accessing their own resource
        if (currentUserId != null && currentUserId.equals(resourceUserId)) {
            return true;
        }

        // Check if user has admin privileges
        return "ADMIN".equals(currentUserRole) || "SUPER_ADMIN".equals(currentUserRole);
    }

    /**
     * Get the current JWT token from the request.
     */
    private String getCurrentToken() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes == null) return null;

            HttpServletRequest request = attributes.getRequest();
            String authHeader = request.getHeader("Authorization");

            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                return authHeader.substring(7);
            }
        } catch (Exception e) {
            log.warn("Error extracting JWT token: {}", e.getMessage());
        }
        return null;
    }
}
