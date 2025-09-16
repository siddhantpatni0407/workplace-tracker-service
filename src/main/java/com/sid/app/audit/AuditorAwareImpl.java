package com.sid.app.audit;

import com.sid.app.constants.AppConstants;
import org.springframework.data.domain.AuditorAware;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * @author Siddhant Patni
 */
@Component("auditorAwareImpl") // explicit name for clarity
public class AuditorAwareImpl implements AuditorAware<String> {

    @Override
    public Optional<String> getCurrentAuditor() {
        // Ideally return logged-in user from SecurityContext
        return Optional.of(AppConstants.DEFAULT_USER);
    }
}
