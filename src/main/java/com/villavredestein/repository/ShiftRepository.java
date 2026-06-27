package com.villavredestein.repository;

import com.villavredestein.model.Shift;
import com.villavredestein.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ShiftRepository extends JpaRepository<Shift, Long> {
    List<Shift> findByCleanerOrderByShiftDateDescCheckInAtDesc(User cleaner);
    List<Shift> findAllByOrderByShiftDateDescCheckInAtDesc();
    Optional<Shift> findFirstByCleanerAndCheckOutAtIsNullOrderByCheckInAtDesc(User cleaner);
    boolean existsByCleanerAndShiftDate(User cleaner, LocalDate date);
}
