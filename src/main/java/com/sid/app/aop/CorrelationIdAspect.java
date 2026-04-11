package com.sid.app.aop;

import com.sid.app.annotation.CorrelationId;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.slf4j.MDC;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.UUID;

/**
 * Aspect that sets a correlation id into MDC for methods annotated with @CorrelationId.
 * - reads incoming header X-Correlation-Id if present
 * - otherwise generates a UUID
 * - puts it into MDC under key "correlationId" for the duration of the method call
 * - writes the header to the response if a HttpServletResponse is available
 */
@Aspect
@Component
public class CorrelationIdAspect {

    private static final String HEADER = "X-Correlation-Id";
    private static final String MDC_KEY = "correlationId";

    @Around("@annotation(correlationId) || @within(correlationId)")
    public Object around(ProceedingJoinPoint pjp, CorrelationId correlationId) throws Throwable {
        String cid = null;

        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        if (requestAttributes instanceof ServletRequestAttributes) {
            ServletRequestAttributes sra = (ServletRequestAttributes) requestAttributes;
            HttpServletRequest request = sra.getRequest();
            if (request != null) {
                String header = request.getHeader(HEADER);
                if (header != null && !header.isBlank()) {
                    cid = header;
                }
            }
        }

        if (cid == null || cid.isBlank()) {
            cid = UUID.randomUUID().toString();
        }

        // put into MDC
        MDC.put(MDC_KEY, cid);

        // try to set on response header if available
        if (requestAttributes instanceof ServletRequestAttributes) {
            ServletRequestAttributes sra = (ServletRequestAttributes) requestAttributes;
            HttpServletResponse response = sra.getResponse();
            if (response != null) {
                response.setHeader(HEADER, cid);
            }
        }

        try {
            return pjp.proceed();
        } finally {
            MDC.remove(MDC_KEY);
        }
    }
}

