package com.tracker.auth.service;

import com.tracker.auth.dto.AuthDtos.*;
import com.tracker.auth.entity.ApiKeyRecord;
import com.tracker.auth.entity.User;
import com.tracker.auth.exception.ApiExceptions;
import com.tracker.auth.repository.ApiKeyRepository;
import com.tracker.auth.repository.UserRepository;
import com.tracker.auth.util.CurrentUser;
import com.tracker.auth.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ApiKeyService {

    private static final long VALIDITY_DAYS = 365; // 1 year - reissue annually
    private static final DateTimeFormatter ISO = DateTimeFormatter.ISO_INSTANT;

    private final ApiKeyRepository apiKeyRepository;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    public ApiKeyCreatedResponse create(CreateApiKeyRequest request) {
        UUID userId = CurrentUser.id();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiExceptions.InvalidCredentialsException());

        String token = jwtUtil.generateApiKey(userId, user.getEmail(), VALIDITY_DAYS);
        Instant issuedAt = Instant.now();
        Instant expiresAt = issuedAt.plusSeconds(VALIDITY_DAYS * 24 * 60 * 60);

        ApiKeyRecord record = ApiKeyRecord.builder()
                .userId(userId)
                .label(request.label())
                .tokenSuffix(token.substring(token.length() - 6))
                .issuedAt(issuedAt)
                .expiresAt(expiresAt)
                .build();
        record = apiKeyRepository.save(record);

        return new ApiKeyCreatedResponse(
                record.getId().toString(), record.getLabel(), token,
                ISO.format(issuedAt.atZone(ZoneOffset.UTC)), ISO.format(expiresAt.atZone(ZoneOffset.UTC))
        );
    }

    public List<ApiKeySummaryResponse> list() {
        return apiKeyRepository.findByUserId(CurrentUser.id()).stream()
                .map(r -> new ApiKeySummaryResponse(
                        r.getId().toString(), r.getLabel(), "…" + r.getTokenSuffix(),
                        ISO.format(r.getIssuedAt().atZone(ZoneOffset.UTC)),
                        r.getExpiresAt() != null ? ISO.format(r.getExpiresAt().atZone(ZoneOffset.UTC)) : null
                ))
                .toList();
    }

    public void delete(UUID id) {
        ApiKeyRecord record = apiKeyRepository.findByIdAndUserId(id, CurrentUser.id())
                .orElseThrow(() -> new ApiExceptions.InvalidCredentialsException());
        apiKeyRepository.delete(record);
        // NOTE: this removes the record from view but does NOT invalidate
        // the token itself - see ApiKeyRecord's class-level javadoc.
    }
}
