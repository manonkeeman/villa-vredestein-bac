package com.villavredestein.repository;

import com.villavredestein.model.CleaningTask;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface CleaningTaskRepository extends JpaRepository<CleaningTask, Long> {

    @Query("SELECT t FROM CleaningTask t LEFT JOIN FETCH t.assignedTo WHERE t.weekNumber = :weekNumber ORDER BY t.id ASC")
    List<CleaningTask> findByWeekNumberOrderByIdAsc(@Param("weekNumber") int weekNumber);

    @Query("SELECT t FROM CleaningTask t LEFT JOIN FETCH t.assignedTo ORDER BY t.weekNumber ASC, t.id ASC")
    List<CleaningTask> findAllByOrderByWeekNumberAscIdAsc();

    @EntityGraph(attributePaths = "assignedTo")
    List<CleaningTask> findAllByOrderByIdAsc();

    @Query("""
            SELECT t
            FROM CleaningTask t LEFT JOIN FETCH t.assignedTo
            WHERE UPPER(t.roleAccess) = UPPER(:role)
               OR UPPER(t.roleAccess) = 'ROLE_ALL'
               OR UPPER(t.roleAccess) = 'ALL'
            ORDER BY t.weekNumber ASC, t.id ASC
            """)
    List<CleaningTask> findAccessibleForRole(@Param("role") String role);

    @Query("""
            SELECT t
            FROM CleaningTask t LEFT JOIN FETCH t.assignedTo
            WHERE t.weekNumber = :weekNumber
              AND (
                   UPPER(t.roleAccess) = UPPER(:role)
                OR UPPER(t.roleAccess) = 'ROLE_ALL'
                OR UPPER(t.roleAccess) = 'ALL'
              )
            ORDER BY t.id ASC
            """)
    List<CleaningTask> findAccessibleForRoleByWeek(@Param("role") String role,
                                                  @Param("weekNumber") int weekNumber);

    @Query("SELECT t FROM CleaningTask t LEFT JOIN FETCH t.assignedTo WHERE LOWER(t.assignedTo.email) = LOWER(:email) ORDER BY t.weekNumber ASC, t.id ASC")
    List<CleaningTask> findByAssignedTo_EmailIgnoreCaseOrderByWeekNumberAscIdAsc(@Param("email") String email);

    @Query("""
            SELECT t FROM CleaningTask t
            WHERE t.deadline < :today
              AND t.completed = false
              AND t.assignedTo IS NOT NULL
            ORDER BY t.deadline ASC
            """)
    List<CleaningTask> findOverdueTasks(@Param("today") LocalDate today);
}