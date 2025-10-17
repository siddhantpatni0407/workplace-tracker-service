package com.sid.app.auth;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import com.sid.app.repository.UserRepository;
import com.sid.app.entity.User;
import java.util.Optional;

/**
 * Utility class to extract user information from JWT tokens in controllers.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationContext {

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

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
     * Extract the current authenticated user's tenant user ID from the database.
     */
    public Long getCurrentUserTenantUserId() {
        Long currentUserId = getCurrentUserId();
        log.info("getCurrentUserTenantUserId() : Current user ID from JWT: {}", currentUserId);

        if (currentUserId == null) {
            log.warn("getCurrentUserTenantUserId() : Current user ID is null");
            return null;
        }

        try {
            // First, try to find by user_id in users table
            Optional<User> userOptional = userRepository.findById(currentUserId);
            log.info("getCurrentUserTenantUserId() : User found in users table: {}", userOptional.isPresent());

            if (userOptional.isPresent()) {
                User user = userOptional.get();
                Long tenantUserId = user.getTenantUserId();
                log.info("getCurrentUserTenantUserId() : User tenant_user_id from users table: {}", tenantUserId);
                return tenantUserId;
            } else {
                // If not found in users table, the JWT userId might directly be the tenant_user_id
                log.info("getCurrentUserTenantUserId() : User not found in users table, treating JWT userId as tenant_user_id: {}", currentUserId);
                return currentUserId;
            }
        } catch (Exception e) {
            log.error("getCurrentUserTenantUserId() : Error fetching user from database: {}", e.getMessage(), e);
            return null;
        }
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
