package com.tracker.habit.service;

import com.tracker.habit.dto.HabitDtos.HabitRequest;
import com.tracker.habit.dto.HabitDtos.HabitResponse;
import com.tracker.habit.entity.Habit;
import com.tracker.habit.entity.HabitLog;
import com.tracker.habit.exception.ApiExceptions;
import com.tracker.habit.repository.HabitLogRepository;
import com.tracker.habit.repository.HabitRepository;
import com.tracker.habit.security.CurrentUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class HabitService {

    private final HabitRepository habitRepository;
    private final HabitLogRepository habitLogRepository;

    public List<HabitResponse> getAll() {
        return habitRepository.findByUserId(CurrentUser.id()).stream()
                .map(this::toResponse)
                .toList();
    }

    public HabitResponse create(HabitRequest request) {
        Habit habit = Habit.builder()
                .userId(CurrentUser.id())
                .name(request.name())
                .frequency(request.frequency())
                .build();
        return toResponse(habitRepository.save(habit));
    }

    public void delete(UUID id) {
        habitRepository.delete(getOwnedOrThrow(id));
    }

    /**
     * Marks a habit as completed for today and updates the streak.
     *
     * Streak logic:
     * - If already completed today -> reject (no double counting).
     * - If last completion was yesterday -> streak continues, +1.
     * - Otherwise (first time, or a gap/missed day) -> streak resets to 1.
     * - longestStreak is updated whenever currentStreak exceeds it.
     */
    @Transactional
    public HabitResponse markComplete(UUID id) {
        Habit habit = getOwnedOrThrow(id);
        LocalDate today = LocalDate.now();

        if (habitLogRepository.existsByHabitIdAndCompletedDate(habit.getId(), today)) {
            throw new ApiExceptions.AlreadyCompletedException("Habit already marked complete for today");
        }

        LocalDate lastCompleted = habit.getLastCompletedDate();
        int newStreak = (lastCompleted != null && lastCompleted.equals(today.minusDays(1)))
                ? habit.getCurrentStreak() + 1
                : 1;

        habit.setCurrentStreak(newStreak);
        habit.setLongestStreak(Math.max(habit.getLongestStreak(), newStreak));
        habit.setLastCompletedDate(today);
        habitRepository.save(habit);

        habitLogRepository.save(HabitLog.builder()
                .habitId(habit.getId())
                .userId(CurrentUser.id())
                .completedDate(today)
                .build());

        return toResponse(habit);
    }

    private Habit getOwnedOrThrow(UUID id) {
        return habitRepository.findByIdAndUserId(id, CurrentUser.id())
                .orElseThrow(() -> new ApiExceptions.ResourceNotFoundException("Habit not found: " + id));
    }

    private HabitResponse toResponse(Habit h) {
        return new HabitResponse(h.getId(), h.getName(), h.getFrequency(),
                h.getCurrentStreak(), h.getLongestStreak(), h.getLastCompletedDate());
    }
}
