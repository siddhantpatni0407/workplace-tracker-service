package com.sid.app.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.sid.app.repository.TenantRepository;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Service for generating unique codes for platform users, tenant users, and tenants
 */
@Service
public class CodeGenerationService {

    private static final String PLATFORM_USER_PREFIX = "PU";
    private static final String TENANT_USER_PREFIX = "TU";
    private static final String TENANT_PREFIX = "T";
    private static final String ALPHANUMERIC = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private final SecureRandom random = new SecureRandom();

    @Autowired
    private TenantRepository tenantRepository;

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
     * Generate unique tenant code based on tenant name
     * Format: T + extracted chars from name + YYMMDD + random chars
     * Example: For "ACME Corporation" -> TACME241016A1B2
     */
    public String generateTenantCode(String tenantName) {
        String extractedChars = extractCharsFromName(tenantName);
        String dateStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyMMdd"));
        String randomStr = generateRandomString(4);

        String baseCode = TENANT_PREFIX + extractedChars + dateStr + randomStr;

        // Ensure uniqueness by checking database and adding suffix if needed
        String finalCode = baseCode;
        int counter = 1;
        while (tenantRepository.existsByTenantCode(finalCode)) {
            finalCode = baseCode + String.format("%02d", counter);
            counter++;
        }

        return finalCode;
    }

    /**
     * Extract meaningful characters from tenant name for code generation
     * Takes first 3-4 chars from significant words, converts to uppercase
     */
    private String extractCharsFromName(String tenantName) {
        if (tenantName == null || tenantName.trim().isEmpty()) {
            return generateRandomString(4);
        }

        // Clean the name: remove special characters, keep only letters and spaces
        String cleanName = tenantName.replaceAll("[^a-zA-Z\\s]", "").trim();

        if (cleanName.isEmpty()) {
            return generateRandomString(4);
        }

        StringBuilder codeBuilder = new StringBuilder();
        String[] words = cleanName.split("\\s+");

        if (words.length == 1) {
            // Single word: take first 4 characters
            String word = words[0].toUpperCase();
            codeBuilder.append(word.substring(0, Math.min(4, word.length())));
        } else {
            // Multiple words: take first 2 chars from first 2 significant words
            int wordsAdded = 0;
            for (String word : words) {
                if (wordsAdded >= 2) break;

                // Skip common words
                if (isSignificantWord(word)) {
                    String upperWord = word.toUpperCase();
                    codeBuilder.append(upperWord.substring(0, Math.min(2, upperWord.length())));
                    wordsAdded++;
                }
            }

            // If we didn't get enough significant words, add from any remaining words
            if (codeBuilder.length() < 3) {
                for (String word : words) {
                    if (codeBuilder.length() >= 4) break;
                    if (!isSignificantWord(word)) {
                        String upperWord = word.toUpperCase();
                        codeBuilder.append(upperWord.substring(0, Math.min(2, upperWord.length())));
                    }
                }
            }
        }

        // Ensure we have at least 3 characters, pad with random if needed
        while (codeBuilder.length() < 3) {
            codeBuilder.append(generateRandomString(1));
        }

        // Limit to maximum 4 characters
        if (codeBuilder.length() > 4) {
            codeBuilder.setLength(4);
        }

        return codeBuilder.toString();
    }

    /**
     * Check if a word is significant (not a common word to skip)
     */
    private boolean isSignificantWord(String word) {
        if (word == null || word.length() < 2) {
            return false;
        }

        String lowerWord = word.toLowerCase();
        // Skip common business words
        return !lowerWord.matches("(ltd|llc|inc|corp|corporation|company|co|pvt|private|limited|group|enterprises|solutions|services|systems|technologies|tech|international|intl|global|and|the|of|for)");
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
