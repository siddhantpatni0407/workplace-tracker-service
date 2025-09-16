package com.sid.app.audit;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * @author Siddhant Patni
 */
@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorAwareImpl")
public class AuditConfig {
    // No duplicate @Bean needed, AuditorAwareImpl is already a @Component
}
