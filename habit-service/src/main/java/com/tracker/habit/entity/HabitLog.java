package com.tracker.habit.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "habit_logs", uniqueConstraints = @UniqueConstraint(columnNames = {"habitId", "completedDate"}))
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HabitLog {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false)
    private UUID habitId;

    @Column(nullable = false)
    private UUID userId;

    @Column(nullable = false)
    private LocalDate completedDate;
}
