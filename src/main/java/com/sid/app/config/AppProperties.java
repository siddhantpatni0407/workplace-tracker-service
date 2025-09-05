package com.sid.app.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Data
public class AppProperties {

    @Value("${ui.host}")
    private String uiHost;

    @Value("${ui.port}")
    private String uiPort;

}