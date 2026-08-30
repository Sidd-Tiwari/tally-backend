package com.tracker.habit.service;

import com.tracker.habit.dto.HabitDtos.TaskRequest;
import com.tracker.habit.dto.HabitDtos.TaskResponse;
import com.tracker.habit.entity.Task;
import com.tracker.habit.exception.ApiExceptions;
import com.tracker.habit.repository.TaskRepository;
import com.tracker.habit.security.CurrentUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;

    public List<TaskResponse> getAll() {
        return taskRepository.findByUserIdOrderByDueDateAsc(CurrentUser.id()).stream()
                .map(this::toResponse)
                .toList();
    }

    public TaskResponse create(TaskRequest request) {
        Task task = Task.builder()
                .userId(CurrentUser.id())
                .title(request.title())
                .description(request.description())
                .dueDate(request.dueDate())
                .priority(request.priority())
                .build();
        return toResponse(taskRepository.save(task));
    }

    public TaskResponse update(UUID id, TaskRequest request) {
        Task task = getOwnedOrThrow(id);
        task.setTitle(request.title());
        task.setDescription(request.description());
        task.setDueDate(request.dueDate());
        task.setPriority(request.priority());
        return toResponse(taskRepository.save(task));
    }

    public TaskResponse toggleComplete(UUID id) {
        Task task = getOwnedOrThrow(id);
        task.setCompleted(!task.isCompleted());
        return toResponse(taskRepository.save(task));
    }

    public void delete(UUID id) {
        taskRepository.delete(getOwnedOrThrow(id));
    }

    private Task getOwnedOrThrow(UUID id) {
        return taskRepository.findByIdAndUserId(id, CurrentUser.id())
                .orElseThrow(() -> new ApiExceptions.ResourceNotFoundException("Task not found: " + id));
    }

    private TaskResponse toResponse(Task t) {
        return new TaskResponse(t.getId(), t.getTitle(), t.getDescription(),
                t.getDueDate(), t.getPriority(), t.isCompleted());
    }
}
