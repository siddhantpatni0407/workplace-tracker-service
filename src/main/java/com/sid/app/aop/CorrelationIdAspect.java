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
        // If a correlationId already exists in MDC (set by an outer aspect invocation),
        // reuse it rather than generating a new one. This prevents nested @CorrelationId
        // annotated methods (e.g. controller -> service) from overriding the correlation id.
        String existing = MDC.get(MDC_KEY);
        boolean createdByThis = false;
        String cid = existing;

        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();

        if (cid == null || cid.isBlank()) {
            // try to read from incoming request header
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

            // generate if still absent
            if (cid == null || cid.isBlank()) {
                cid = UUID.randomUUID().toString();
            }

            // put into MDC and mark that this invocation created it so we can remove it later
            MDC.put(MDC_KEY, cid);
            createdByThis = true;

            // write header to response only when we created the id (top-level)
            if (requestAttributes instanceof ServletRequestAttributes) {
                ServletRequestAttributes sra = (ServletRequestAttributes) requestAttributes;
                HttpServletResponse response = sra.getResponse();
                if (response != null) {
                    response.setHeader(HEADER, cid);
                }
            }
        }

        try {
            return pjp.proceed();
        } finally {
            // only remove if we put it here; don't remove an id set by an outer aspect
            if (createdByThis) {
                MDC.remove(MDC_KEY);
            }
        }
    }
}

