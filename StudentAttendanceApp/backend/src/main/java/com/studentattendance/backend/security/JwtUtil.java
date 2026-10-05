package com.studentattendance.backend.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * Issues and validates HS256 JWTs used as Bearer tokens by every protected
 * endpoint. The secret and expiry are externalised to application.properties
 * so they can differ between dev/staging/prod without a code change.
 */
@Component
public class JwtUtil {

    private final SecretKey key;
    private final long expirationMillis;

    public JwtUtil(@Value("${app.jwt.secret}") String secret,
                    @Value("${app.jwt.expiration-ms:86400000}") long expirationMillis) {
        // The secret must be >= 256 bits for HS256; pad defensively so a short
        // value in application.properties.example never crashes startup.
        byte[] bytes = secret.getBytes(StandardCharsets.UTF_8);
        if (bytes.length < 32) {
            byte[] padded = new byte[32];
            System.arraycopy(bytes, 0, padded, 0, bytes.length);
            bytes = padded;
        }
        this.key = Keys.hmacShaKeyFor(bytes);
        this.expirationMillis = expirationMillis;
    }

    public String generateToken(String userId, String email) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expirationMillis);
        return Jwts.builder()
                .setSubject(userId)
                .claim("email", email)
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public long getExpirationMillis(String token) {
        try {
            Claims claims = Jwts.parserBuilder().setSigningKey(key).build()
                    .parseClaimsJws(token).getBody();
            Date exp = claims.getExpiration();
            return exp != null ? exp.getTime() : System.currentTimeMillis() + expirationMillis;
        } catch (JwtException | IllegalArgumentException e) {
            return System.currentTimeMillis();
        }
    }

    /** Returns the user id (subject) if the token is valid, otherwise null. */
    public String validateAndGetUserId(String token) {
        try {
            Claims claims = Jwts.parserBuilder().setSigningKey(key).build()
                    .parseClaimsJws(token).getBody();
            return claims.getSubject();
        } catch (ExpiredJwtException | JwtException | IllegalArgumentException e) {
            return null;
        }
    }
}
