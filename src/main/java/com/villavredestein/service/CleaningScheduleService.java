package com.villavredestein.service;

import com.villavredestein.model.CleaningTask;
import com.villavredestein.model.User;
import com.villavredestein.repository.CleaningTaskRepository;
import com.villavredestein.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Generates / regenerates the cleaning schedule based on current STUDENT users.
 *
 * Slots  = students sorted by ID  +  one null slot (Japan / vrije week).
 * Weeks  = number of slots.
 * Tasks  = 4 per week (Keuken, Badkamer, Vuilnis, Woonkamer).
 * Formula: task[i] → slot[(i + week - 1) % slots]
 *
 * Call reseedNow() after any student is added or removed.
 */
@Service
public class CleaningScheduleService {

    private static final Logger log = LoggerFactory.getLogger(CleaningScheduleService.class);

    private static final String[] TASK_NAMES = {
        "Keuken & vaatwasser",
        "Badkamer & toilet",
        "Vuilnis & was",
        "Woonkamer & gang"
    };

    private static final String[] TASK_DESCS = {
        "Vaatwasser leegmaken, aanrecht, oven en inductieplaat schoonmaken.",
        "Wastafel, douche, spiegel en toilet grondig schoonmaken en droogvegen.",
        "Afval scheiden: gft, plastic/blik, papier, restafval, statiegeld. Was draaien en opvouwen.",
        "Woonkamer stofzuigen en dweilen. Gang en trap schoonmaken. Eettafel opruimen."
    };

    private final UserRepository userRepository;
    private final CleaningTaskRepository cleaningTaskRepository;

    public CleaningScheduleService(UserRepository userRepository,
                                   CleaningTaskRepository cleaningTaskRepository) {
        this.userRepository = userRepository;
        this.cleaningTaskRepository = cleaningTaskRepository;
    }

    @Transactional
    public void reseedNow() {
        List<User> students = userRepository.findByRole(User.Role.STUDENT)
                .stream()
                .sorted((a, b) -> Long.compare(a.getId(), b.getId()))
                .toList();

        int slots = students.size() + 1; // +1 voor de vrije week (Japan-slot)

        cleaningTaskRepository.deleteAll();
        cleaningTaskRepository.flush();

        for (int week = 1; week <= slots; week++) {
            for (int taskIdx = 0; taskIdx < 4; taskIdx++) {
                int slotIdx = (taskIdx + week - 1) % slots;
                User assignee = slotIdx < students.size() ? students.get(slotIdx) : null;

                CleaningTask task = new CleaningTask(
                        week, TASK_NAMES[taskIdx], TASK_DESCS[taskIdx], null);
                task.setAssignedTo(assignee);
                task.setCompleted(false);
                cleaningTaskRepository.save(task);
            }
        }

        log.info("CleaningScheduleService: {} taken aangemaakt ({} studenten, {} weken)",
                slots * 4, students.size(), slots);
    }

    /** Verwacht aantal taken op basis van huidige studenten. */
    public int expectedTaskCount() {
        int n = userRepository.findByRole(User.Role.STUDENT).size();
        return (n + 1) * 4;
    }

    /** Rotatiefactor (aantal weken in de cyclus). */
    public int rotationLength() {
        return userRepository.findByRole(User.Role.STUDENT).size() + 1;
    }
}