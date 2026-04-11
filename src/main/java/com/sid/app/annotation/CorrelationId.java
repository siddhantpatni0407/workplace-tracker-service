package com.sid.app.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks controller/service methods for which a correlation id should be generated
 * (or propagated from the incoming X-Correlation-Id header) and placed into MDC.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface CorrelationId {
}

