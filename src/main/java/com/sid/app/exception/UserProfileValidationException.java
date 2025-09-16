package com.sid.app.exception;

import java.util.Collections;
import java.util.Map;

public class UserProfileValidationException extends RuntimeException {
    private final Map<String, String> fieldErrors;

    public UserProfileValidationException(Map<String, String> fieldErrors) {
        super("Validation failed");
        this.fieldErrors = Collections.unmodifiableMap(fieldErrors);
    }

    public Map<String, String> getFieldErrors() {
        return fieldErrors;
    }
}
