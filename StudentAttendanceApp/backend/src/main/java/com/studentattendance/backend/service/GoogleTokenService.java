package com.studentattendance.backend.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.studentattendance.backend.exception.BadRequestException;
import com.studentattendance.backend.exception.UnauthorizedException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 * Verifies a Google ID token with Google's tokeninfo endpoint and checks
 * that the audience matches this app's OAuth client IDs.
 */
@Service
public class GoogleTokenService {

    private static final Set<String> TRUSTED_ISSUERS = Set.of(
            "accounts.google.com",
            "https://accounts.google.com");

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final List<String> acceptedAudiences;

    public GoogleTokenService(
            ObjectMapper objectMapper,
            @Value("${app.google.client-ids:}") String clientIds) {
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        this.acceptedAudiences = Arrays.stream(clientIds.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
    }

    public GoogleProfile verify(String idToken) {
        if (acceptedAudiences.isEmpty()) {
            throw new BadRequestException("Google Sign-In is not configured on the server.");
        }
        if (idToken == null || idToken.isBlank()) {
            throw new UnauthorizedException("Google sign-in failed. Please try again.");
        }
        try {
            String url = "https://oauth2.googleapis.com/tokeninfo?id_token="
                    + URLEncoder.encode(idToken.trim(), StandardCharsets.UTF_8);
            HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                    .timeout(Duration.ofSeconds(15))
                    .GET()
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                throw new UnauthorizedException("Google sign-in could not be verified. Please try again.");
            }
            JsonNode body = objectMapper.readTree(response.body());
            String issuer = text(body, "iss");
            if (!TRUSTED_ISSUERS.contains(issuer)) {
                throw new UnauthorizedException("Google sign-in could not be verified. Please try again.");
            }
            String audience = text(body, "aud");
            if (!acceptedAudiences.contains(audience)) {
                throw new UnauthorizedException("This Google account is not authorised for this app.");
            }
            if (!"true".equalsIgnoreCase(text(body, "email_verified"))) {
                throw new UnauthorizedException("Please use a Google account with a verified email address.");
            }
            String email = text(body, "email").trim().toLowerCase();
            if (email.isEmpty() || !email.contains("@")) {
                throw new UnauthorizedException("Google did not provide a valid email address.");
            }
            String name = text(body, "name");
            if (name.isBlank()) {
                name = email.substring(0, email.indexOf('@'));
            }
            String subject = text(body, "sub");
            if (subject.isBlank()) {
                throw new UnauthorizedException("Google sign-in could not be verified. Please try again.");
            }
            return new GoogleProfile(subject, email, name);
        } catch (UnauthorizedException | BadRequestException e) {
            throw e;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new UnauthorizedException("Google sign-in timed out. Please try again.");
        } catch (Exception e) {
            throw new UnauthorizedException("Google sign-in could not be verified. Please try again.");
        }
    }

    private static String text(JsonNode node, String field) {
        JsonNode value = node.get(field);
        return value == null || value.isNull() ? "" : value.asText("");
    }

    public record GoogleProfile(String subject, String email, String name) {
    }
}
