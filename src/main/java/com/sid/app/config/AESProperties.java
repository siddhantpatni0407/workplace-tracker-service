package com.sid.app.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "encryption.aes")
public class AESProperties {

    private String algorithm;
    private String secretKey;

}