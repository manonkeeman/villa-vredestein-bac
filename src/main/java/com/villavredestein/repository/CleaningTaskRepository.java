package com.villavredestein.repository;

import com.villavredestein.model.CleaningTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CleaningTaskRepository extends JpaRepository<CleaningTask, Long> {

    List<CleaningTask> findByWeekNumberOrderByIdAsc(int weekNumber);

    List<CleaningTask> findAllByOrderByWeekNumberAscIdAsc();

    List<CleaningTask> findAllByOrderByIdAsc();

    @Query("""
            SELECT t
            FROM CleaningTask t
            WHERE UPPER(t.roleAccess) = UPPER(:role)
               OR UPPER(t.roleAccess) = 'ROLE_ALL'
               OR UPPER(t.roleAccess) = 'ALL'
            ORDER BY t.weekNumber ASC, t.id ASC
            """)
    List<CleaningTask> findAccessibleForRole(@Param("role") String role);

    @Query("""
            SELECT t
            FROM CleaningTask t
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
}