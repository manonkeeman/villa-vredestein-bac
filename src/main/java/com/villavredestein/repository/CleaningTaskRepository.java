package com.villavredestein.repository;

import com.villavredestein.model.CleaningTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CleaningTaskRepository extends JpaRepository<CleaningTask, Long> {
}