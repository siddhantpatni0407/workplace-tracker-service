package com.sid.app.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author Siddhant Patni
 */
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "app.backup")
public class DatabaseBackupProperties {

    private String directory;
    private String prefix;
    private String defaultDb;

}