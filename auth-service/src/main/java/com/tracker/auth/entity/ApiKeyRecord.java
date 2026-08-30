package com.tracker.auth.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

/**
 * Records that a developer generated a long-lived "API key" (which is
 * really just a JWT with a long expiry - see JwtUtil.generateApiKey).
 *
 * IMPORTANT LIMITATION: because the key itself is a stateless JWT, this
 * table is for the user's own visibility (a dashboard of "keys I've
 * issued") - deleting a row here does NOT invalidate the token, since
 * no service checks this table on every request. True revocation would
 * require either a shared denylist every service checks (adds a DB or
 * cache dependency to every request) or switching to opaque tokens
 * validated against this table directly. Documented in the API docs so
 * developers aren't surprised - fine for a v1, worth upgrading before
 * this handles anything sensitive.
 */
@Entity
@Table(name = "api_keys")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiKeyRecord {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false)
    private UUID userId;

    @Column(nullable = false)
    private String label;

    /** Last 6 chars of the issued token, so the user can recognize it later without it being stored in full. */
    @Column(nullable = false)
    private String tokenSuffix;

    @Builder.Default
    @Column(nullable = false)
    private Instant issuedAt = Instant.now();

    private Instant expiresAt;
}
