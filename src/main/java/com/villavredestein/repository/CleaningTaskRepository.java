package com.villavredestein.repository;

import com.villavredestein.model.CleaningTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CleaningTaskRepository extends JpaRepository<CleaningTask, Long> {
    List<CleaningTask> findByWeekNumberOrderByDueDateAsc(int weekNumber);
    List<CleaningTask> findByCompletedFalseOrderByWeekNumberAsc();
    List<CleaningTask> findByCompletedTrueOrderByWeekNumberAsc();
}