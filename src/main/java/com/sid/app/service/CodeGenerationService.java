package com.sid.app.service;

import org.springframework.stereotype.Service;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Service for generating unique codes for platform users and tenant users
 */
@Service
public class CodeGenerationService {

    private static final String PLATFORM_USER_PREFIX = "PU";
    private static final String TENANT_USER_PREFIX = "TU";
    private static final String ALPHANUMERIC = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private final SecureRandom random = new SecureRandom();

    /**
     * Generate unique platform user code
     * Format: PU + YYYYMMDD + 6 random alphanumeric characters
     * Example: PU202410141A2B3C
     */
    public String generatePlatformUserCode() {
        String dateStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String randomStr = generateRandomString(6);
        return PLATFORM_USER_PREFIX + dateStr + randomStr;
    }

    /**
     * Generate unique tenant user code
     * Format: TU + YYYYMMDD + 6 random alphanumeric characters
     * Example: TU202410141A2B3C
     */
    public String generateTenantUserCode() {
        String dateStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String randomStr = generateRandomString(6);
        return TENANT_USER_PREFIX + dateStr + randomStr;
    }

    /**
     * Generate random alphanumeric string of specified length
     */
    private String generateRandomString(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(ALPHANUMERIC.charAt(random.nextInt(ALPHANUMERIC.length())));
        }
        return sb.toString();
    }
}
