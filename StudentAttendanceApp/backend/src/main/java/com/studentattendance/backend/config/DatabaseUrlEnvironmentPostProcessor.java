package com.studentattendance.backend.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * Maps cloud {@code DATABASE_URL} values ({@code postgres://...} from
 * Render, Neon, Railway, Heroku) onto Spring datasource properties so the
 * same JAR can run against local H2/MySQL or a hosted PostgreSQL database.
 */
public class DatabaseUrlEnvironmentPostProcessor implements EnvironmentPostProcessor {

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        String databaseUrl = firstNonBlank(
                environment.getProperty("DATABASE_URL"),
                System.getenv("DATABASE_URL"));
        if (databaseUrl == null) {
            return;
        }
        String trimmed = databaseUrl.trim();
        if (trimmed.startsWith("jdbc:postgresql:")) {
            Map<String, Object> jdbcOnly = new HashMap<>();
            jdbcOnly.put("spring.datasource.url", trimmed);
            jdbcOnly.put("spring.datasource.driver-class-name", "org.postgresql.Driver");
            jdbcOnly.put("spring.jpa.properties.hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");
            environment.getPropertySources().addFirst(new MapPropertySource("cloudDatabaseUrl", jdbcOnly));
            return;
        }
        if (!(trimmed.startsWith("postgres://") || trimmed.startsWith("postgresql://"))) {
            return;
        }
        URI uri = URI.create(trimmed);
        String userInfo = uri.getUserInfo();
        if (userInfo == null || !userInfo.contains(":")) {
            return;
        }
        int split = userInfo.indexOf(':');
        String username = URLDecoder.decode(userInfo.substring(0, split), StandardCharsets.UTF_8);
        String password = URLDecoder.decode(userInfo.substring(split + 1), StandardCharsets.UTF_8);
        int port = uri.getPort() > 0 ? uri.getPort() : 5432;
        String path = uri.getPath() == null || uri.getPath().isBlank() ? "/postgres" : uri.getPath();
        String query = uri.getRawQuery();
        String jdbc = "jdbc:postgresql://" + uri.getHost() + ":" + port + path;
        if (query != null && !query.isBlank()) {
            jdbc += "?" + query;
            if (!query.contains("sslmode")) {
                jdbc += "&sslmode=require";
            }
        } else {
            jdbc += "?sslmode=require";
        }

        Map<String, Object> props = new HashMap<>();
        props.put("spring.datasource.url", jdbc);
        props.put("spring.datasource.username", username);
        props.put("spring.datasource.password", password);
        props.put("spring.datasource.driver-class-name", "org.postgresql.Driver");
        props.put("spring.jpa.properties.hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");
        props.put("spring.jpa.hibernate.ddl-auto", firstNonBlank(
                environment.getProperty("JPA_DDL_AUTO"), "update"));
        if (environment.getProperty("SEED_ACADEMIC") == null && System.getenv("SEED_ACADEMIC") == null) {
            props.put("app.seed.academic-data", "false");
        }
        environment.getPropertySources().addFirst(new MapPropertySource("cloudDatabaseUrl", props));
    }

    private static String firstNonBlank(String a, String b) {
        if (a != null && !a.isBlank()) {
            return a;
        }
        if (b != null && !b.isBlank()) {
            return b;
        }
        return null;
    }
}
