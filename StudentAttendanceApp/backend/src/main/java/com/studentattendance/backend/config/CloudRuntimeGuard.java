package com.studentattendance.backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * Fails fast in cloud deployments if the process is still on H2 or a
 * placeholder JWT secret. Local {@code mvn spring-boot:run} without
 * {@code DATABASE_URL} is unchanged.
 */
@Component
public class CloudRuntimeGuard implements ApplicationRunner {

    private final String jwtSecret;
    private final String datasourceUrl;

    public CloudRuntimeGuard(
            @Value("${app.jwt.secret}") String jwtSecret,
            @Value("${spring.datasource.url}") String datasourceUrl) {
        this.jwtSecret = jwtSecret;
        this.datasourceUrl = datasourceUrl;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (!StringUtils.hasText(System.getenv("DATABASE_URL"))) {
            return;
        }
        if (datasourceUrl != null && datasourceUrl.toLowerCase().startsWith("jdbc:h2:")) {
            throw new IllegalStateException(
                    "DATABASE_URL is set but the datasource is still H2. Use a postgres:// cloud URL.");
        }
        if (!StringUtils.hasText(jwtSecret)
                || jwtSecret.contains("local-dev-only")
                || jwtSecret.contains("CHANGE_ME")
                || jwtSecret.length() < 32) {
            throw new IllegalStateException(
                    "Set JWT_SECRET to a random value of at least 32 characters for cloud deployment.");
        }
    }
}
