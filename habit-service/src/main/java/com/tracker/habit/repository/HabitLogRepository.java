package com.tracker.habit.repository;

import com.tracker.habit.entity.HabitLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface HabitLogRepository extends JpaRepository<HabitLog, UUID> {
    List<HabitLog> findByHabitIdOrderByCompletedDateDesc(UUID habitId);
    Optional<HabitLog> findByHabitIdAndCompletedDate(UUID habitId, LocalDate date);
    boolean existsByHabitIdAndCompletedDate(UUID habitId, LocalDate date);
}
