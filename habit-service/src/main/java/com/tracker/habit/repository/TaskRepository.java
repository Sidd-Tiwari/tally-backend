package com.tracker.habit.repository;

import com.tracker.habit.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TaskRepository extends JpaRepository<Task, UUID> {
    List<Task> findByUserIdOrderByDueDateAsc(UUID userId);
    Optional<Task> findByIdAndUserId(UUID id, UUID userId);
}
