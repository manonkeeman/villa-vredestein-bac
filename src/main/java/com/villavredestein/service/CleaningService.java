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

/**
 * {@code CleaningService} bevat de businesslogica voor het beheren van schoonmaaktaken
 * binnen de Villa Vredestein webapplicatie.
 *
 * <p>De service zorgt voor het aanmaken, bijwerken, verwijderen en ophalen van schoonmaaktaken.
 * Daarnaast ondersteunt het de rotatie van taken op weekbasis, commentaar en incidentmeldingen.</p>
 *
 * <p>Deze klasse vormt de brug tussen de {@link com.villavredestein.controller.CleaningController}
 * en de onderliggende {@link CleaningTaskRepository} en {@link UserRepository}.</p>
 *
 * <p>Alle methoden zijn getransactioneerd en werken met {@link CleaningRequestDTO}
 * om domeinobjecten om te zetten naar overdraagbare data-objecten.</p>
 *
 */
@Service
@Transactional
public class CleaningService {

    private final CleaningTaskRepository taskRepo;
    private final UserRepository userRepo;

    /**
     * Constructor voor {@link CleaningService}.
     *
     * @param taskRepo repository voor het beheren van {@link CleaningTask}-entiteiten
     * @param userRepo repository voor het beheren van {@link User}-entiteiten
     */
    public CleaningService(CleaningTaskRepository taskRepo, UserRepository userRepo) {
        this.taskRepo = taskRepo;
        this.userRepo = userRepo;
    }

    /**
     * Berekent de huidige rotatieweek (1–4) op basis van de huidige datum.
     *
     * @return weeknummer binnen de rotatiecyclus
     */
    private int getCurrentRotationWeek() {
        int currentWeek = LocalDate.now().get(WeekFields.of(Locale.getDefault()).weekOfYear());
        return ((currentWeek - 1) % 4) + 1;
    }

    /**
     * Haalt alle schoonmaaktaken op uit de database.
     *
     * @return lijst van {@link CleaningRequestDTO}-objecten
     */
    public List<CleaningRequestDTO> getAllTasks() {
        return taskRepo.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Haalt schoonmaaktaken op voor de huidige rotatieweek.
     *
     * @return lijst van {@link CleaningRequestDTO}-objecten van deze week
     */
    public List<CleaningRequestDTO> getCurrentWeekTasks() {
        int rotationWeek = getCurrentRotationWeek();
        return taskRepo.findByWeekNumberOrderByIdAsc(rotationWeek)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Haalt schoonmaaktaken op voor een opgegeven weeknummer.
     *
     * @param weekNumber weeknummer waarop gefilterd wordt
     * @return lijst van {@link CleaningRequestDTO}-objecten
     */
    public List<CleaningRequestDTO> getTasksByWeek(int weekNumber) {
        return taskRepo.findByWeekNumberOrderByIdAsc(weekNumber)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Voegt een nieuwe schoonmaaktaak toe aan het systeem.
     *
     * @param dto de data van de taak die moet worden toegevoegd
     * @return de aangemaakte {@link CleaningRequestDTO}
     */
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

    /**
     * Wijzigt een bestaande schoonmaaktaak.
     *
     * @param id het unieke ID van de taak
     * @param dto de nieuwe gegevens van de taak
     * @return de bijgewerkte {@link CleaningRequestDTO}
     * @throws RuntimeException als de taak niet wordt gevonden
     */
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

    /**
     * Wisselt de status van een taak tussen ‘voltooid’ en ‘niet voltooid’.
     *
     * @param id het unieke ID van de taak
     * @return bijgewerkte {@link CleaningRequestDTO}
     * @throws RuntimeException als de taak niet wordt gevonden
     */
    public CleaningRequestDTO toggleTask(Long id) {
        CleaningTask task = taskRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Taak niet gevonden: " + id));
        task.setCompleted(!task.isCompleted());
        return toDTO(taskRepo.save(task));
    }

    /**
     * Voegt een opmerking toe aan een taak.
     *
     * @param id het unieke ID van de taak
     * @param comment de opmerking die toegevoegd moet worden
     * @return bijgewerkte {@link CleaningRequestDTO}
     * @throws IllegalArgumentException als de opmerking leeg is
     */
    public CleaningRequestDTO addComment(Long id, String comment) {
        CleaningTask task = taskRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Taak niet gevonden: " + id));

        if (comment == null || comment.isBlank()) {
            throw new IllegalArgumentException("Comment mag niet leeg zijn");
        }

        task.setComment(comment.trim());
        return toDTO(taskRepo.save(task));
    }

    /**
     * Voegt een incidentrapport toe aan een taak.
     *
     * @param id het unieke ID van de taak
     * @param incidentReport beschrijving van het incident
     * @return bijgewerkte {@link CleaningRequestDTO}
     * @throws IllegalArgumentException als de beschrijving leeg is
     */
    public CleaningRequestDTO addIncident(Long id, String incidentReport) {
        CleaningTask task = taskRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Taak niet gevonden: " + id));

        if (incidentReport == null || incidentReport.isBlank()) {
            throw new IllegalArgumentException("Incidentbeschrijving mag niet leeg zijn");
        }

        task.setIncidentReport(incidentReport.trim());
        return toDTO(taskRepo.save(task));
    }

    /**
     * Verwijdert een taak uit de database op basis van ID.
     *
     * @param id het unieke ID van de taak
     * @throws RuntimeException als de taak niet bestaat
     */
    public void deleteTask(Long id) {
        if (!taskRepo.existsById(id)) {
            throw new RuntimeException("Taak niet gevonden: " + id);
        }
        taskRepo.deleteById(id);
    }

    /**
     * Zet een {@link CleaningTask} om naar een {@link CleaningRequestDTO}.
     *
     * @param task het taakobject dat moet worden omgezet
     * @return overeenkomstige {@link CleaningRequestDTO}
     */
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