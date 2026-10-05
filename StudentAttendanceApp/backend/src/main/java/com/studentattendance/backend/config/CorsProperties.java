package com.studentattendance.backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/** Externalises the CORS allow-list to application.properties (app.cors.allowed-origins). */
@Component
public class CorsProperties {

    @Value("${app.cors.allowed-origins:*}")
    private String allowedOrigins;

    public String getAllowedOrigins() {
        return allowedOrigins;
    }
}
