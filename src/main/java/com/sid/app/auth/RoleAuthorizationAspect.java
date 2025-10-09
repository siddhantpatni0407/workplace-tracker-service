package com.sid.app.auth;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

/**
 * Aspect to handle role-based authorization using @RequiredRole annotation.
 * Intercepts method calls and validates if the current user has the required roles.
 */
@Aspect
@Component
@Slf4j
@RequiredArgsConstructor
public class RoleAuthorizationAspect {

    private final JwtAuthenticationContext authContext;

    /**
     * Intercepts methods annotated with @RequiredRole and validates user authorization.
     */
    @Around("@annotation(requiredRole)")
    public Object checkRoleAuthorization(ProceedingJoinPoint joinPoint, RequiredRole requiredRole) throws Throwable {
        String methodName = joinPoint.getSignature().getName();
        String className = joinPoint.getTarget().getClass().getSimpleName();

        log.debug("RoleAuthorizationAspect: Checking authorization for {}.{}", className, methodName);

        // Get required roles from annotation
        String[] requiredRoles = requiredRole.value();
        if (requiredRoles.length == 0) {
            log.warn("RoleAuthorizationAspect: No roles specified in @RequiredRole for {}.{}", className, methodName);
            return joinPoint.proceed(); // No roles specified, allow access
        }

        // Get current user's role from JWT context
        String currentUserRole = authContext.getCurrentUserRole();
        if (currentUserRole == null || currentUserRole.trim().isEmpty()) {
            log.warn("RoleAuthorizationAspect: No role found for current user in {}.{}", className, methodName);
            return createForbiddenResponse("Access denied: No role found for current user");
        }

        // Check if current user's role is in the required roles list
        List<String> requiredRolesList = Arrays.asList(requiredRoles);
        if (!requiredRolesList.contains(currentUserRole)) {
            log.warn("RoleAuthorizationAspect: Access denied for {}.{} - User role: {}, Required roles: {}",
                    className, methodName, currentUserRole, Arrays.toString(requiredRoles));
            return createForbiddenResponse("Access denied: Insufficient privileges");
        }

        log.debug("RoleAuthorizationAspect: Access granted for {}.{} - User role: {}",
                className, methodName, currentUserRole);

        // User has required role, proceed with method execution
        return joinPoint.proceed();
    }

    /**
     * Creates a standardized forbidden response for unauthorized access attempts.
     */
    @SuppressWarnings("unchecked")
    private <T> ResponseEntity<T> createForbiddenResponse(String message) {
        return (ResponseEntity<T>) ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new com.sid.app.model.ResponseDTO<>(
                        com.sid.app.constants.AppConstants.STATUS_FAILED,
                        message,
                        null
                ));
    }
}
