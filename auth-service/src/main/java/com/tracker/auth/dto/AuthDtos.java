package com.tracker.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class AuthDtos {

    public record RegisterRequest(
            @NotBlank String name,
            @Email @NotBlank String email,
            @Size(min = 8, message = "Password must be at least 8 characters") String password
    ) {}

    public record LoginRequest(
            @Email @NotBlank String email,
            @NotBlank String password
    ) {}

    public record AuthResponse(
            String token,
            String userId,
            String name,
            String email
    ) {}

    public record UserResponse(
            String id,
            String name,
            String email
    ) {}

    public record CreateApiKeyRequest(
            @NotBlank String label
    ) {}

    /** key is only ever returned once, at creation - it's never stored in plaintext. */
    public record ApiKeyCreatedResponse(
            String id,
            String label,
            String key,
            String issuedAt,
            String expiresAt
    ) {}

    public record ApiKeySummaryResponse(
            String id,
            String label,
            String tokenSuffix,
            String issuedAt,
            String expiresAt
    ) {}
}
