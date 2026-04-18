package com.villavredestein.config;

import com.villavredestein.model.CleaningTask;
import com.villavredestein.model.User;
import com.villavredestein.repository.CleaningTaskRepository;
import com.villavredestein.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * Runs at startup to ensure the cleaning task rotation is correct.
 *
 * Deletes all existing tasks and re-inserts the canonical 4-week Dutch rotation
 * whenever the data looks stale (wrong count, English names, or old student references).
 *
 * 4 taken × 4 rotatieweken = 16 rijen
 * Kamers: Argentinië (Simon), Thailand (Desmond), Frankrijk (Medoc), Japan (null)
 * Rotatie: taak[i] → slot[(i + week - 1) % 4]
 */
@Component
@Order(20) // runs after ContractSeeder
public class CleaningTaskSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(CleaningTaskSeeder.class);

    private static final int EXPECTED_TASK_COUNT = 16; // 4 tasks × 4 rotation weeks

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

    // Slot order — matches rotation formula slot[(i + week - 1) % 4]
    private static final String[] SLOT_EMAILS = {
        "simontalsma2@gmail.com",   // slot 0 — Argentinië
        "desmondstaal@gmail.com",   // slot 1 — Thailand
        "medocstaal@gmail.com",     // slot 2 — Frankrijk
        null                        // slot 3 — Japan (leeg)
    };

    private final CleaningTaskRepository taskRepository;
    private final UserRepository userRepository;

    public CleaningTaskSeeder(CleaningTaskRepository taskRepository, UserRepository userRepository) {
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (needsReseed()) {
            log.info("CleaningTaskSeeder: stale or missing tasks detected — reseeding…");
            taskRepository.deleteAll();
            insertRotation();
            log.info("CleaningTaskSeeder: 16 tasks inserted (4 taken × 4 rotatieweken)");
        } else {
            log.debug("CleaningTaskSeeder: tasks OK, skipping reseed");
        }
    }

    // ── Condition check ──────────────────────────────────────────────

    private boolean needsReseed() {
        long count = taskRepository.count();
        if (count != EXPECTED_TASK_COUNT) {
            log.info("CleaningTaskSeeder: task count={} (expected {})", count, EXPECTED_TASK_COUNT);
            return true;
        }

        // Check for English task names (old data)
        List<CleaningTask> all = taskRepository.findAllByOrderByIdAsc();
        boolean hasEnglish = all.stream().anyMatch(t ->
            t.getName() != null && (
                t.getName().toLowerCase().contains("kitchen") ||
                t.getName().toLowerCase().contains("bathroom") ||
                t.getName().toLowerCase().contains("trash") ||
                t.getName().toLowerCase().contains("living room") ||
                t.getName().toLowerCase().contains("de was")
            )
        );
        if (hasEnglish) {
            log.info("CleaningTaskSeeder: English or outdated task names detected");
            return true;
        }

        // Check for removed students still assigned
        boolean hasOldStudents = all.stream().anyMatch(t ->
            t.getAssignedTo() != null && (
                "arwenleonor@gmail.com".equalsIgnoreCase(t.getAssignedTo().getEmail()) ||
                "ikheetalvar@gmail.com".equalsIgnoreCase(t.getAssignedTo().getEmail())
            )
        );
        if (hasOldStudents) {
            log.info("CleaningTaskSeeder: tasks still assigned to removed students");
            return true;
        }

        return false;
    }

    // ── Insert 4 × 4 rotation ────────────────────────────────────────

    private void insertRotation() {
        User[] slots = resolveSlots();

        for (int week = 1; week <= 4; week++) {
            for (int taskIdx = 0; taskIdx < 4; taskIdx++) {
                int slotIdx = (taskIdx + week - 1) % 4;
                User assignee = slots[slotIdx];

                CleaningTask task = new CleaningTask(
                    week,
                    TASK_NAMES[taskIdx],
                    TASK_DESCS[taskIdx],
                    null
                );
                task.setAssignedTo(assignee);
                task.setCompleted(false);
                taskRepository.save(task);
            }
        }
    }

    private User[] resolveSlots() {
        User[] slots = new User[4];
        for (int i = 0; i < SLOT_EMAILS.length; i++) {
            if (SLOT_EMAILS[i] != null) {
                Optional<User> u = userRepository.findByEmailIgnoreCase(SLOT_EMAILS[i]);
                if (u.isPresent()) {
                    slots[i] = u.get();
                } else {
                    log.warn("CleaningTaskSeeder: user not found for slot {}: {}", i, SLOT_EMAILS[i]);
                    slots[i] = null;
                }
            }
        }
        return slots;
    }
}