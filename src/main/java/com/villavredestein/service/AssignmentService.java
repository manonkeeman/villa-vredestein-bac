package com.villavredestein.service;

import com.villavredestein.model.Assignment;
import com.villavredestein.model.Task;
import com.villavredestein.model.User;
import com.villavredestein.repository.TaskRepository;
import com.villavredestein.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class AssignmentService {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

    public AssignmentService(TaskRepository taskRepository, UserRepository userRepository) {
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
    }

    public Assignment create(Long taskId, Long assigneeId, LocalDate dueDate) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Task not found with id: " + taskId));

        User assignee = userRepository.findById(assigneeId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + assigneeId));

        Assignment assignment = new Assignment();
        assignment.setTask(task);
        assignment.setAssignee(assignee);
        assignment.setDueDate(dueDate);
        assignment.setStatus(Assignment.Status.OPEN);

        return assignment;
    }

    public void markDone(Assignment assignment) {
        assignment.setStatus(Assignment.Status.DONE);
    }
}