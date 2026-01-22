package com.villavredestein.service;

import com.villavredestein.dto.CleaningTaskRequestDTO;
import com.villavredestein.dto.CleaningTaskResponseDTO;
import com.villavredestein.model.CleaningTask;
import com.villavredestein.model.User;
import com.villavredestein.repository.CleaningTaskRepository;
import com.villavredestein.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.WeekFields;
import java.util.List;

// ==============================================================
// CleaningTaskService
// ==============================================================
@Service
@Transactional
public class CleaningTaskService {

    private final CleaningTaskRepository taskRepository;
    private final UserRepository userRepository;

    public CleaningTaskService(CleaningTaskRepository taskRepository, UserRepository userRepository) {
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
    }

    // --------------------------------------------------------------
    // Rotation week helper (1..4)
    // --------------------------------------------------------------
    private int getCurrentRotationWeek() {
        int currentWeekOfYear = LocalDate.now().get(WeekFields.ISO.weekOfWeekBasedYear());
        return ((currentWeekOfYear - 1) % 4) + 1;
    }

    // ==============================================================
    // Role-aware methods
    // ==============================================================

    public List<CleaningTaskResponseDTO> getAllTasksForRole(String role) {
        String callerRole = normalizeCallerRole(role);

        List<CleaningTask> tasks = "ADMIN".equals(callerRole)
                ? taskRepository.findAllByOrderByWeekNumberAscIdAsc()
                : taskRepository.findAccessibleForRole(callerRole);

        return tasks.stream().map(this::toResponseDTO).toList();
    }

    public List<CleaningTaskResponseDTO> getTasksByWeekForRole(String role, int weekNumber) {
        int safeWeek = requireValidWeekNumber(weekNumber);
        String callerRole = normalizeCallerRole(role);

        List<CleaningTask> tasks = "ADMIN".equals(callerRole)
                ? taskRepository.findByWeekNumberOrderByIdAsc(safeWeek)
                : taskRepository.findAccessibleForRoleByWeek(callerRole, safeWeek);

        return tasks.stream().map(this::toResponseDTO).toList();
    }

    public List<CleaningTaskResponseDTO> getCurrentWeekTasksForRole(String role) {
        int rotationWeek = getCurrentRotationWeek();
        return getTasksByWeekForRole(role, rotationWeek);
    }

    // ==============================================================
    // Backwards-compatible READ methods
    // ==============================================================

    public List<CleaningTaskResponseDTO> getAllTasks() {
        return getAllTasksForRole("ADMIN");
    }

    public List<CleaningTaskResponseDTO> getCurrentWeekTasks() {
        return getCurrentWeekTasksForRole("STUDENT");
    }

    public List<CleaningTaskResponseDTO> getTasksByWeek(int weekNumber) {
        return getTasksByWeekForRole("STUDENT", weekNumber);
    }

    // ==============================================================
    // WRITE methods
    // ==============================================================

    public CleaningTaskResponseDTO addTask(CleaningTaskRequestDTO dto) {
        int weekNumber = requireValidWeekNumber(dto.getWeekNumber());

        CleaningTask task = new CleaningTask(
                weekNumber,
                dto.getName(),
                dto.getDescription(),
                null
        );

        task.setCompleted(dto.isCompleted());
        task.setComment(dto.getComment());
        task.setIncidentReport(dto.getIncidentReport());
        task.setAssignedTo(resolveAssignee(dto.getAssignedTo()));

        return toResponseDTO(taskRepository.save(task));
    }

    public CleaningTaskResponseDTO updateTask(Long id, CleaningTaskRequestDTO dto) {
        CleaningTask task = findTaskOrThrow(id);

        task.setWeekNumber(requireValidWeekNumber(dto.getWeekNumber()));
        task.setName(dto.getName());
        task.setDescription(dto.getDescription());
        task.setComment(dto.getComment());
        task.setIncidentReport(dto.getIncidentReport());

        if (dto.getAssignedTo() != null && !dto.getAssignedTo().isBlank()) {
            task.setAssignedTo(resolveAssignee(dto.getAssignedTo()));
        }

        return toResponseDTO(task);
    }

    public CleaningTaskResponseDTO toggleTask(Long id) {
        CleaningTask task = findTaskOrThrow(id);
        task.setCompleted(!task.isCompleted());
        return toResponseDTO(task);
    }

    public CleaningTaskResponseDTO addComment(Long id, String comment) {
        CleaningTask task = findTaskOrThrow(id);
        String safeComment = requireNonBlank(comment, "Comment mag niet leeg zijn");

        task.setComment(safeComment);
        return toResponseDTO(task);
    }

    public CleaningTaskResponseDTO addIncident(Long id, String incidentReport) {
        CleaningTask task = findTaskOrThrow(id);
        String safeIncident = requireNonBlank(incidentReport, "Incidentbeschrijving mag niet leeg zijn");

        task.setIncidentReport(safeIncident);
        return toResponseDTO(task);
    }

    public void deleteTask(Long id) {
        CleaningTask task = findTaskOrThrow(id);
        taskRepository.delete(task);
    }

    // ==============================================================
    // Helpers
    // ==============================================================

    private String normalizeCallerRole(String role) {
        if (role == null || role.isBlank()) {
            return "STUDENT";
        }

        String normalized = role.trim().toUpperCase();
        if (normalized.startsWith("ROLE_")) {
            normalized = normalized.substring("ROLE_".length());
        }
        return normalized;
    }

    private int requireValidWeekNumber(Integer weekNumber) {
        if (weekNumber == null) {
            throw new IllegalArgumentException("Weeknummer is verplicht");
        }
        if (weekNumber < 1 || weekNumber > 4) {
            throw new IllegalArgumentException("Weeknummer moet tussen 1 en 4 liggen");
        }
        return weekNumber;
    }

    private CleaningTask findTaskOrThrow(Long id) {
        return taskRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Taak niet gevonden: " + id));
    }

    private User resolveAssignee(String emailOrNull) {
        if (emailOrNull == null || emailOrNull.isBlank()) {
            return null;
        }
        String email = emailOrNull.trim();
        return userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new EntityNotFoundException("Gebruiker niet gevonden: " + email));
    }

    private String requireNonBlank(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(message);
        }
        return value.trim();
    }

    private CleaningTaskResponseDTO toResponseDTO(CleaningTask task) {
        String assignedTo = task.getAssignedTo() != null ? task.getAssignedTo().getUsername() : null;

        return new CleaningTaskResponseDTO(
                task.getId(),
                task.getWeekNumber(),
                task.getName(),
                task.getDescription(),
                task.isCompleted(),
                assignedTo,
                task.getComment(),
                task.getIncidentReport()
        );
    }
}