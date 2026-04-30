package com.villavredestein.config;

import com.villavredestein.model.CleaningTask;
import com.villavredestein.model.User;
import com.villavredestein.repository.CleaningTaskRepository;
import com.villavredestein.repository.UserRepository;
import com.villavredestein.service.CleaningScheduleService;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
@Order(20) // runs after ContractSeeder
public class CleaningTaskSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(CleaningTaskSeeder.class);

    private final CleaningTaskRepository taskRepository;
    private final UserRepository userRepository;
    private final CleaningScheduleService scheduleService;

    public CleaningTaskSeeder(CleaningTaskRepository taskRepository,
                              UserRepository userRepository,
                              CleaningScheduleService scheduleService) {
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
        this.scheduleService = scheduleService;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (needsReseed()) {
            log.info("CleaningTaskSeeder: stale or missing tasks detected — reseeding…");
            scheduleService.reseedNow();
            log.info("CleaningTaskSeeder: reseed complete");
        } else {
            log.debug("CleaningTaskSeeder: taken OK, overslaan");
        }
    }

    private boolean needsReseed() {
        long count = taskRepository.count();
        long expected = scheduleService.expectedTaskCount();

        if (count != expected) {
            log.info("CleaningTaskSeeder: task count={} (expected {})", count, expected);
            return true;
        }

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
            log.info("CleaningTaskSeeder: verouderde taaknamen gevonden");
            return true;
        }

        Set<Long> studentIds = new HashSet<>();
        userRepository.findByRole(User.Role.STUDENT).forEach(u -> studentIds.add(u.getId()));

        boolean hasUnknownAssignee = all.stream().anyMatch(t -> {
            if (t.getAssignedTo() == null) return false;
            return !studentIds.contains(t.getAssignedTo().getId());
        });
        if (hasUnknownAssignee) {
            log.info("CleaningTaskSeeder: taken toegewezen aan verwijderde/onbekende studenten");
            return true;
        }

        return false;
    }
}