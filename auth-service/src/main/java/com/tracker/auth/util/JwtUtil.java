package com.tracker.auth.util;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration-ms}")
    private long expirationMs;

    public String generateToken(UUID userId, String email) {
        SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expirationMs);

        return Jwts.builder()
                .setSubject(userId.toString())
                .claim("email", email)
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(key)
                .compact();
    }

    /**
     * Issues a long-lived token for third-party developers to use as an
     * "API key". It's validated by the exact same filters as a normal
     * login token (same secret, same claims shape) - the only difference
     * is the expiry and a "type" claim so it's distinguishable in logs.
     * See ApiKeyRecord for the tracking/revocation caveat.
     */
    public String generateApiKey(UUID userId, String email, long validityDays) {
        SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        Date now = new Date();
        Date expiry = new Date(now.getTime() + validityDays * 24L * 60 * 60 * 1000);

        return Jwts.builder()
                .setSubject(userId.toString())
                .claim("email", email)
                .claim("type", "api_key")
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(key)
                .compact();
    }
}
