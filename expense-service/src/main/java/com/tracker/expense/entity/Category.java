package com.tracker.expense.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "categories")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Category {

    @Id
    @GeneratedValue
    private UUID id;

    // No FK to a "users" table - that table lives in a different service's
    // database. We just store the id extracted from the JWT and trust it.
    @Column(nullable = false)
    private UUID userId;

    @Column(nullable = false)
    private String name;

    @Builder.Default
    @Column(nullable = false)
    private String type = "EXPENSE"; // EXPENSE or INCOME

    @Builder.Default
    private String icon = "💰";
}
