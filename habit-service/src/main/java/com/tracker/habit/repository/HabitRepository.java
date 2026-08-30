package com.tracker.habit.repository;

import com.tracker.habit.entity.Habit;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface HabitRepository extends JpaRepository<Habit, UUID> {
    List<Habit> findByUserId(UUID userId);
    Optional<Habit> findByIdAndUserId(UUID id, UUID userId);
}
