package com.sid.app.audit;

import com.sid.app.constants.AppConstants;
import org.springframework.data.domain.AuditorAware;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * @author Siddhant Patni
 */
@Component
public class AuditorAwareImpl implements AuditorAware<String> {

    @Override
    public Optional<String> getCurrentAuditor() {
        // Here you can fetch the current logged in user, for this example returning a hardcoded user
        return Optional.of(AppConstants.DEFAULT_USER);
    }

}