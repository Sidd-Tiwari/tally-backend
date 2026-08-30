package com.tracker.habit.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "habits")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Habit {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false)
    private UUID userId;

    @Column(nullable = false)
    private String name;

    @Builder.Default
    @Column(nullable = false)
    private String frequency = "DAILY"; // DAILY or WEEKLY

    @Builder.Default
    @Column(nullable = false)
    private Integer currentStreak = 0;

    @Builder.Default
    @Column(nullable = false)
    private Integer longestStreak = 0;

    private LocalDate lastCompletedDate;

    @Builder.Default
    @Column(nullable = false)
    private Instant createdAt = Instant.now();
}
