package com.tracker.habit.controller;

import com.tracker.habit.dto.HabitDtos.HabitRequest;
import com.tracker.habit.dto.HabitDtos.HabitResponse;
import com.tracker.habit.service.HabitService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/habits")
@RequiredArgsConstructor
public class HabitController {

    private final HabitService habitService;

    @GetMapping
    public List<HabitResponse> getAll() {
        return habitService.getAll();
    }

    @PostMapping
    public ResponseEntity<HabitResponse> create(@Valid @RequestBody HabitRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(habitService.create(request));
    }

    @PostMapping("/{id}/complete")
    public HabitResponse markComplete(@PathVariable UUID id) {
        return habitService.markComplete(id);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        habitService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
