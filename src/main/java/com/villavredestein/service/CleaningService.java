package com.villavredestein.service;

import com.villavredestein.dto.CleaningRequestDTO;
import com.villavredestein.dto.CleaningResponseDTO;
import com.villavredestein.model.CleaningSchedule;
import com.villavredestein.model.CleaningTask;
import com.villavredestein.model.User;
import com.villavredestein.repository.CleaningScheduleRepository;
import com.villavredestein.repository.CleaningTaskRepository;
import com.villavredestein.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class CleaningService {

    private final CleaningScheduleRepository scheduleRepo;
    private final CleaningTaskRepository taskRepo;
    private final UserRepository userRepo;

    public CleaningService(CleaningScheduleRepository scheduleRepo,
                           CleaningTaskRepository taskRepo,
                           UserRepository userRepo) {
        this.scheduleRepo = scheduleRepo;
        this.taskRepo = taskRepo;
        this.userRepo = userRepo;
    }

    public List<CleaningResponseDTO> getAllSchedules() {
        return scheduleRepo.findAll().stream()
                .map(this::toScheduleDTO)
                .collect(Collectors.toList());
    }

    public CleaningResponseDTO getByWeek(int week) {
        CleaningSchedule schedule = scheduleRepo.findByWeekNumber(week);
        if (schedule == null) {
            throw new RuntimeException("Geen schoonmaakschema gevonden voor week " + week);
        }
        return toScheduleDTO(schedule);
    }

    public CleaningResponseDTO createSchedule(int week) {
        CleaningSchedule existing = scheduleRepo.findByWeekNumber(week);
        if (existing != null) {
            return toScheduleDTO(existing);
        }
        CleaningSchedule created = new CleaningSchedule(week);
        return toScheduleDTO(scheduleRepo.save(created));
    }

    public CleaningRequestDTO addTask(Long scheduleId, CleaningRequestDTO dto) {
        CleaningSchedule schedule = scheduleRepo.findById(scheduleId)
                .orElseThrow(() -> new RuntimeException("Schoonmaakschema niet gevonden: " + scheduleId));

        User assignee = null;
        if (dto.getAssignedTo() != null && !dto.getAssignedTo().isBlank()) {
            assignee = userRepo.findByEmail(dto.getAssignedTo()).orElse(null);
        }

        CleaningTask task = new CleaningTask();
        task.setName(dto.getName());
        task.setDescription(dto.getDescription());
        task.setDueDate(dto.getDueDate());
        task.setCompleted(dto.isCompleted());
        task.setAssignedTo(assignee);
        task.setCleaningSchedule(schedule);

        CleaningTask saved = taskRepo.save(task);
        schedule.getCleaningTasks().add(saved);

        return toTaskDTO(saved);
    }

    public CleaningRequestDTO toggleTask(Long taskId) {
        CleaningTask task = taskRepo.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Taak niet gevonden: " + taskId));
        task.setCompleted(!task.isCompleted());
        return toTaskDTO(taskRepo.save(task));
    }

    // ✅ Admin-versie
    public CleaningRequestDTO updateTaskNote(Long taskId, String note) {
        CleaningTask task = taskRepo.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Taak niet gevonden: " + taskId));
        task.setDescription(note);
        return toTaskDTO(taskRepo.save(task));
    }

    public void deleteTask(Long taskId) {
        if (taskRepo.existsById(taskId)) {
            taskRepo.deleteById(taskId);
        }
    }

    // ✅ Cleaner-versie
    public CleaningRequestDTO toggleTaskForCleaner(Long taskId, String email) {
        CleaningTask task = taskRepo.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Taak niet gevonden: " + taskId));

        if (task.getAssignedTo() == null ||
                !task.getAssignedTo().getEmail().equalsIgnoreCase(email)) {
            throw new RuntimeException("Geen toestemming om deze taak aan te passen.");
        }

        task.setCompleted(!task.isCompleted());
        return toTaskDTO(taskRepo.save(task));
    }

    public CleaningRequestDTO updateTaskNoteForCleaner(Long taskId, String note, String email) {
        CleaningTask task = taskRepo.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Taak niet gevonden: " + taskId));

        if (task.getAssignedTo() == null ||
                !task.getAssignedTo().getEmail().equalsIgnoreCase(email)) {
            throw new RuntimeException("Geen toestemming om deze taak aan te passen.");
        }

        task.setDescription(note);
        return toTaskDTO(taskRepo.save(task));
    }

    private CleaningResponseDTO toScheduleDTO(CleaningSchedule schedule) {
        List<CleaningRequestDTO> tasks = (schedule.getCleaningTasks() == null) ? List.of() :
                schedule.getCleaningTasks().stream()
                        .map(this::toTaskDTO)
                        .collect(Collectors.toList());
        return new CleaningResponseDTO(schedule.getId(), schedule.getWeekNumber(), tasks);
    }

    private CleaningRequestDTO toTaskDTO(CleaningTask task) {
        String email = (task.getAssignedTo() != null) ? task.getAssignedTo().getEmail() : null;
        return new CleaningRequestDTO(
                task.getId(),
                task.getName(),
                task.getDescription(),
                task.getDueDate(),
                task.isCompleted(),
                email
        );
    }
}