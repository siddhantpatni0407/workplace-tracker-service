package com.sid.app.auth;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Custom annotation to specify required roles for accessing an endpoint.
 * Can be applied to controller methods to enforce role-based access control.
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface RequiredRole {
    String[] value() default {}; // Array of required roles
}
