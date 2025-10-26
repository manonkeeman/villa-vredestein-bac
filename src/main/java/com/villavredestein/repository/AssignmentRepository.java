package com.villavredestein.repository;
import com.villavredestein.model.Assignment;
import com.villavredestein.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface AssignmentRepository extends JpaRepository<Assignment,Long> {
    List<Assignment> findByAssignee(User user);
    List<Assignment> findByDueDateBetween(LocalDate from, LocalDate to);
}