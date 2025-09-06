package com.sid.app.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
@Data
public class AppProperties {

    @Value("${ui.host}")
    private String uiHost;

    @Value("${ui.port}")
    private String uiPort;

    // JWT properties (from application.yml / env)
    @Value("${app.jwt.secret:my-super-secret-key-which-is-at-least-32-characters-long!}")
    private String jwtSecret;

    /**
     * Token lifetime in milliseconds (default 3600000 = 1 hour)
     */
    @Value("${app.jwt.expiration-ms:3600000}")
    private long jwtExpirationMs;

    /**
     * Allowed clock skew in seconds (default 10 seconds)
     */
    @Value("${app.jwt.allowed-clock-skew-sec:10}")
    private long jwtAllowedClockSkewSec;

    public List<String> getAllowedOrigins() {
        return Arrays.stream(uiHost.split(","))
                .map(String::trim)
                .filter(h -> !h.isEmpty())
                .map(h -> h + ":" + uiPort)
                .toList();
    }

}
