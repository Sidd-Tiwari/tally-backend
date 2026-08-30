package com.tracker.auth.repository;

import com.tracker.auth.entity.ApiKeyRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ApiKeyRepository extends JpaRepository<ApiKeyRecord, UUID> {
    List<ApiKeyRecord> findByUserId(UUID userId);
    Optional<ApiKeyRecord> findByIdAndUserId(UUID id, UUID userId);
}
