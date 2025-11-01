package com.villavredestein.repository;

import com.villavredestein.model.CleaningTask;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TaskRepository extends JpaRepository<CleaningTask, Long> {
    List<CleaningTask> findByAssignedToEmail(String email);
}