package com.villavredestein.service;

import com.villavredestein.dto.CleaningRequestDTO;
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

@Service
@Transactional
public class CleaningService {

    private final CleaningTaskRepository taskRepo;
    private final UserRepository userRepo;

    public CleaningService(CleaningTaskRepository taskRepo, UserRepository userRepo) {
        this.taskRepo = taskRepo;
        this.userRepo = userRepo;
    }

    private int getCurrentRotationWeek() {
        int currentWeek = LocalDate.now().get(WeekFields.of(Locale.getDefault()).weekOfYear());
        return ((currentWeek - 1) % 4) + 1; // cyclisch 1–4
    }

    public List<CleaningRequestDTO> getAllTasks() {
        return taskRepo.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public List<CleaningRequestDTO> getCurrentWeekTasks() {
        int rotationWeek = getCurrentRotationWeek();
        return taskRepo.findByWeekNumberOrderByIdAsc(rotationWeek)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public List<CleaningRequestDTO> getTasksByWeek(int weekNumber) {
        return taskRepo.findByWeekNumberOrderByIdAsc(weekNumber)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public CleaningRequestDTO addTask(CleaningRequestDTO dto) {
        User assignee = dto.getAssignedTo() != null
                ? userRepo.findByEmail(dto.getAssignedTo()).orElse(null)
                : null;

        CleaningTask task = new CleaningTask();
        task.setWeekNumber(dto.getWeekNumber());
        task.setName(dto.getName());
        task.setDescription(dto.getDescription());
        task.setCompleted(dto.isCompleted());
        task.setComment(dto.getComment());
        task.setIncidentReport(dto.getIncidentReport());
        task.setAssignedTo(assignee);

        return toDTO(taskRepo.save(task));
    }

    public CleaningRequestDTO updateTask(Long id, CleaningRequestDTO dto) {
        CleaningTask task = taskRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Taak niet gevonden: " + id));

        task.setWeekNumber(dto.getWeekNumber());
        task.setName(dto.getName());
        task.setDescription(dto.getDescription());
        task.setComment(dto.getComment());
        task.setIncidentReport(dto.getIncidentReport());

        if (dto.getAssignedTo() != null) {
            userRepo.findByEmail(dto.getAssignedTo()).ifPresent(task::setAssignedTo);
        }

        return toDTO(taskRepo.save(task));
    }

    public CleaningRequestDTO toggleTask(Long id) {
        CleaningTask task = taskRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Taak niet gevonden: " + id));
        task.setCompleted(!task.isCompleted());
        return toDTO(taskRepo.save(task));
    }

    public CleaningRequestDTO addComment(Long id, String comment) {
        CleaningTask task = taskRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Taak niet gevonden: " + id));

        if (comment == null || comment.isBlank()) {
            throw new IllegalArgumentException("Comment mag niet leeg zijn");
        }

        task.setComment(comment.trim());
        return toDTO(taskRepo.save(task));
    }

    public CleaningRequestDTO addIncident(Long id, String incidentReport) {
        CleaningTask task = taskRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Taak niet gevonden: " + id));

        if (incidentReport == null || incidentReport.isBlank()) {
            throw new IllegalArgumentException("Incidentbeschrijving mag niet leeg zijn");
        }

        task.setIncidentReport(incidentReport.trim());
        return toDTO(taskRepo.save(task));
    }

    public void deleteTask(Long id) {
        if (!taskRepo.existsById(id)) {
            throw new RuntimeException("Taak niet gevonden: " + id);
        }
        taskRepo.deleteById(id);
    }

    private CleaningRequestDTO toDTO(CleaningTask task) {
        String assigned = task.getAssignedTo() != null ? task.getAssignedTo().getUsername() : null;

        return new CleaningRequestDTO(
                task.getId(),
                task.getWeekNumber(),
                task.getName(),
                task.getDescription(),
                null, // dueDate niet gebruikt
                task.isCompleted(),
                assigned,
                task.getComment(),
                task.getIncidentReport()
        );
    }
}