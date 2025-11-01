package com.villavredestein.repository;

import com.villavredestein.model.CleaningSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CleaningScheduleRepository extends JpaRepository<CleaningSchedule, Long> {
    CleaningSchedule findByWeekNumber(int weekNumber);
}