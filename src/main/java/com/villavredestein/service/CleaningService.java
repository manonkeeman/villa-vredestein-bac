package com.villavredestein.service;

import com.villavredestein.dto.CleaningRequestDTO;
import com.villavredestein.dto.CleaningResponseDTO;
import com.villavredestein.model.CleaningTask;
import com.villavredestein.model.User;
import com.villavredestein.repository.CleaningTaskRepository;
import com.villavredestein.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.WeekFields;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * Service-laag voor het beheren van schoonmaaktaken.
 *
 * <p>De {@code CleaningService} bevat de businesslogica voor het aanmaken, ophalen,
 * bijwerken en verwijderen van schoonmaaktaken. Daarnaast ondersteunt deze service
 * eenvoudige week-rotatie en het registreren van opmerkingen en incidenten.</p>
 *
 * <p>Deze service wordt aangeroepen vanuit de controllerlaag en praat met de database
 * via {@link CleaningTaskRepository} en {@link UserRepository}.</p>
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
     * Berekent de huidige rotatie-week (1 t/m 4) op basis van de ISO-week van het jaar.
     */
    private int getCurrentRotationWeek() {
        int currentWeekOfYear = LocalDate.now().get(WeekFields.of(Locale.getDefault()).weekOfYear());
        return ((currentWeekOfYear - 1) % 4) + 1;
    }

    public List<CleaningResponseDTO> getAllTasks() {
        return taskRepository.findAll()
                .stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    public List<CleaningResponseDTO> getCurrentWeekTasks() {
        int rotationWeek = getCurrentRotationWeek();
        return taskRepository.findByWeekNumberOrderByIdAsc(rotationWeek)
                .stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    public List<CleaningResponseDTO> getTasksByWeek(int weekNumber) {
        return taskRepository.findByWeekNumberOrderByIdAsc(weekNumber)
                .stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    public CleaningResponseDTO addTask(CleaningRequestDTO dto) {
        CleaningTask task = new CleaningTask();
        task.setWeekNumber(dto.getWeekNumber());
        task.setName(dto.getName());
        task.setDescription(dto.getDescription());
        task.setCompleted(dto.isCompleted());
        task.setComment(dto.getComment());
        task.setIncidentReport(dto.getIncidentReport());
        task.setAssignedTo(resolveAssignee(dto.getAssignedTo()));

        return toResponseDTO(taskRepository.save(task));
    }

    public CleaningResponseDTO updateTask(Long id, CleaningRequestDTO dto) {
        CleaningTask task = findTaskOrThrow(id);

        task.setWeekNumber(dto.getWeekNumber());
        task.setName(dto.getName());
        task.setDescription(dto.getDescription());
        task.setComment(dto.getComment());
        task.setIncidentReport(dto.getIncidentReport());

        if (dto.getAssignedTo() != null && !dto.getAssignedTo().isBlank()) {
            User assignee = resolveAssignee(dto.getAssignedTo());
            if (assignee != null) {
                task.setAssignedTo(assignee);
            }
        }

        return toResponseDTO(taskRepository.save(task));
    }

    public CleaningResponseDTO toggleTask(Long id) {
        CleaningTask task = findTaskOrThrow(id);
        task.setCompleted(!task.isCompleted());
        return toResponseDTO(taskRepository.save(task));
    }

    public CleaningResponseDTO addComment(Long id, String comment) {
        CleaningTask task = findTaskOrThrow(id);
        String safeComment = requireNonBlank(comment, "Comment mag niet leeg zijn");

        task.setComment(safeComment);
        return toResponseDTO(taskRepository.save(task));
    }

    public CleaningResponseDTO addIncident(Long id, String incidentReport) {
        CleaningTask task = findTaskOrThrow(id);
        String safeIncident = requireNonBlank(incidentReport, "Incidentbeschrijving mag niet leeg zijn");

        task.setIncidentReport(safeIncident);
        return toResponseDTO(taskRepository.save(task));
    }

    public void deleteTask(Long id) {
        if (!taskRepository.existsById(id)) {
            throw new RuntimeException("Taak niet gevonden: " + id);
        }
        taskRepository.deleteById(id);
    }

    // =========================
    // Helpers
    // =========================

    private CleaningTask findTaskOrThrow(Long id) {
        return taskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Taak niet gevonden: " + id));
    }

    private User resolveAssignee(String emailOrNull) {
        if (emailOrNull == null || emailOrNull.isBlank()) {
            return null;
        }
        return userRepository.findByEmail(emailOrNull.trim()).orElse(null);
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