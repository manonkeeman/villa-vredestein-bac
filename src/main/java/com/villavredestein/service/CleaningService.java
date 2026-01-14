package com.villavredestein.service;

import com.villavredestein.dto.CleaningRequestDTO;
import com.villavredestein.dto.CleaningResponseDTO;
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
import java.util.Locale;

/**
 * Service-laag voor het beheren van schoonmaaktaken.
 */
@Service
@Transactional
public class CleaningService {

    private final CleaningTaskRepository taskRepository;
    private final UserRepository userRepository;

    public CleaningService(CleaningTaskRepository taskRepository, UserRepository userRepository) {
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
    }

    /**
     * Berekent de huidige rotatie-week (1 t/m 4).
     */
    private int getCurrentRotationWeek() {
        int currentWeekOfYear = LocalDate.now().get(WeekFields.of(Locale.getDefault()).weekOfYear());
        return ((currentWeekOfYear - 1) % 4) + 1;
    }

    public List<CleaningResponseDTO> getAllTasks() {
        return taskRepository.findAllByOrderByWeekNumberAscIdAsc()
                .stream()
                .map(this::toResponseDTO)
                .toList();
    }

    public List<CleaningResponseDTO> getCurrentWeekTasks() {
        int rotationWeek = getCurrentRotationWeek();
        return taskRepository.findByWeekNumberOrderByIdAsc(rotationWeek)
                .stream()
                .map(this::toResponseDTO)
                .toList();
    }

    public List<CleaningResponseDTO> getTasksByWeek(int weekNumber) {
        requireValidWeekNumber(weekNumber);
        return taskRepository.findByWeekNumberOrderByIdAsc(weekNumber)
                .stream()
                .map(this::toResponseDTO)
                .toList();
    }

    public CleaningResponseDTO addTask(CleaningRequestDTO dto) {
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

    public CleaningResponseDTO updateTask(Long id, CleaningRequestDTO dto) {
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

    public CleaningResponseDTO toggleTask(Long id) {
        CleaningTask task = findTaskOrThrow(id);
        task.setCompleted(!task.isCompleted());
        return toResponseDTO(task);
    }

    public CleaningResponseDTO addComment(Long id, String comment) {
        CleaningTask task = findTaskOrThrow(id);
        String safeComment = requireNonBlank(comment, "Comment mag niet leeg zijn");

        task.setComment(safeComment);
        return toResponseDTO(task);
    }

    public CleaningResponseDTO addIncident(Long id, String incidentReport) {
        CleaningTask task = findTaskOrThrow(id);
        String safeIncident = requireNonBlank(incidentReport, "Incidentbeschrijving mag niet leeg zijn");

        task.setIncidentReport(safeIncident);
        return toResponseDTO(task);
    }

    public void deleteTask(Long id) {
        CleaningTask task = findTaskOrThrow(id);
        taskRepository.delete(task);
    }

    // =========================
    // Helpers
    // =========================

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

    private CleaningResponseDTO toResponseDTO(CleaningTask task) {
        String assignedTo = task.getAssignedTo() != null ? task.getAssignedTo().getUsername() : null;

        return new CleaningResponseDTO(
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