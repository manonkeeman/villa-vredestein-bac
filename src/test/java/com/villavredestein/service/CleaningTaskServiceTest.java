package com.villavredestein.service;

import com.villavredestein.dto.CleaningTaskRequestDTO;
import com.villavredestein.dto.CleaningTaskResponseDTO;
import com.villavredestein.model.CleaningTask;
import com.villavredestein.model.User;
import com.villavredestein.repository.CleaningTaskRepository;
import com.villavredestein.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CleaningTaskServiceTest {

    @Mock CleaningTaskRepository taskRepository;
    @Mock UserRepository userRepository;
    @Mock CleaningScheduleService scheduleService;
    @InjectMocks CleaningTaskService cleaningTaskService;

    private User makeStudent(String username, String email) {
        return new User(username, email, "hash", User.Role.STUDENT);
    }

    private CleaningTask makeTask(Long id, int week, String name, User assignee) {
        CleaningTask task = new CleaningTask(week, name, "Beschrijving", null);
        task.setAssignedTo(assignee);
        task.setCompleted(false);
        ReflectionTestUtils.setField(task, "id", id);
        return task;
    }


    @Test
    void getAllTasksForRole_admin_returnsAllTasks() {
        User student = makeStudent("simon", "simon@vv.com");
        CleaningTask t1 = makeTask(1L, 1, "Keuken", student);
        CleaningTask t2 = makeTask(2L, 2, "Badkamer", null);

        when(taskRepository.findAllByOrderByWeekNumberAscIdAsc()).thenReturn(List.of(t1, t2));

        List<CleaningTaskResponseDTO> result = cleaningTaskService.getAllTasksForRole("ADMIN");

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getName()).isEqualTo("Keuken");
        verify(taskRepository).findAllByOrderByWeekNumberAscIdAsc();
        verify(taskRepository, never()).findAccessibleForRole(any());
    }

    @Test
    void getAllTasksForRole_student_returnsAccessibleTasks() {
        User student = makeStudent("simon", "simon@vv.com");
        CleaningTask t1 = makeTask(1L, 1, "Keuken", student);

        when(taskRepository.findAccessibleForRole("STUDENT")).thenReturn(List.of(t1));

        List<CleaningTaskResponseDTO> result = cleaningTaskService.getAllTasksForRole("STUDENT");

        assertThat(result).hasSize(1);
        verify(taskRepository).findAccessibleForRole("STUDENT");
        verify(taskRepository, never()).findAllByOrderByWeekNumberAscIdAsc();
    }

    @Test
    void getAllTasksForRole_roleWithPrefix_stripsPrefix() {
        when(taskRepository.findAccessibleForRole("STUDENT")).thenReturn(List.of());

        cleaningTaskService.getAllTasksForRole("ROLE_STUDENT");

        verify(taskRepository).findAccessibleForRole("STUDENT");
    }

    @Test
    void getAllTasksForRole_nullRole_defaultsToStudent() {
        when(taskRepository.findAccessibleForRole("STUDENT")).thenReturn(List.of());

        cleaningTaskService.getAllTasksForRole(null);

        verify(taskRepository).findAccessibleForRole("STUDENT");
    }


    @Test
    void getTasksByWeekForRole_admin_usesAdminQuery() {
        User student = makeStudent("simon", "simon@vv.com");
        CleaningTask t = makeTask(1L, 1, "Keuken", student);

        when(taskRepository.findByWeekNumberOrderByIdAsc(1)).thenReturn(List.of(t));

        List<CleaningTaskResponseDTO> result = cleaningTaskService.getTasksByWeekForRole("ADMIN", 1);

        assertThat(result).hasSize(1);
        verify(taskRepository).findByWeekNumberOrderByIdAsc(1);
    }

    @Test
    void getTasksByWeekForRole_student_usesRoleQuery() {
        CleaningTask t = makeTask(1L, 1, "Keuken", null);

        when(taskRepository.findAccessibleForRoleByWeek("STUDENT", 1)).thenReturn(List.of(t));

        List<CleaningTaskResponseDTO> result = cleaningTaskService.getTasksByWeekForRole("STUDENT", 1);

        assertThat(result).hasSize(1);
        verify(taskRepository).findAccessibleForRoleByWeek("STUDENT", 1);
    }

    @Test
    void getTasksByWeekForRole_weekZero_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
                () -> cleaningTaskService.getTasksByWeekForRole("ADMIN", 0));
    }

    @Test
    void getTasksByWeekForRole_negativeWeek_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
                () -> cleaningTaskService.getTasksByWeekForRole("ADMIN", -5));
    }


    @Test
    void getCurrentWeekTasksForRole_callsRotationWeek() {
        when(scheduleService.rotationLength()).thenReturn(5);
        when(taskRepository.findByWeekNumberOrderByIdAsc(anyInt())).thenReturn(List.of());

        cleaningTaskService.getCurrentWeekTasksForRole("ADMIN");

        verify(scheduleService).rotationLength();
    }


    @Test
    void addTask_withValidDto_savesAndReturnsDto() {
        CleaningTaskRequestDTO dto = new CleaningTaskRequestDTO();
        dto.setWeekNumber(1);
        dto.setName("Keuken");
        dto.setDescription("Aanrecht schoonmaken");

        CleaningTask saved = makeTask(10L, 1, "Keuken", null);
        when(taskRepository.save(any(CleaningTask.class))).thenReturn(saved);

        CleaningTaskResponseDTO result = cleaningTaskService.addTask(dto);

        assertThat(result.getId()).isEqualTo(10L);
        assertThat(result.getName()).isEqualTo("Keuken");
        verify(taskRepository).save(any(CleaningTask.class));
    }

    @Test
    void addTask_withAssignedToEmail_resolvesUser() {
        CleaningTaskRequestDTO dto = new CleaningTaskRequestDTO();
        dto.setWeekNumber(2);
        dto.setName("Badkamer");
        dto.setAssignedTo("simon@vv.com");

        User student = makeStudent("simon", "simon@vv.com");
        when(userRepository.findByEmailIgnoreCase("simon@vv.com")).thenReturn(Optional.of(student));

        CleaningTask saved = makeTask(11L, 2, "Badkamer", student);
        when(taskRepository.save(any(CleaningTask.class))).thenReturn(saved);

        CleaningTaskResponseDTO result = cleaningTaskService.addTask(dto);

        assertThat(result.getAssignedTo()).isEqualTo("simon");
        assertThat(result.getAssignedToEmail()).isEqualTo("simon@vv.com");
        verify(userRepository).findByEmailIgnoreCase("simon@vv.com");
    }

    @Test
    void addTask_withUnknownAssignee_throwsEntityNotFoundException() {
        CleaningTaskRequestDTO dto = new CleaningTaskRequestDTO();
        dto.setWeekNumber(1);
        dto.setName("Keuken");
        dto.setAssignedTo("onbekend@vv.com");

        when(userRepository.findByEmailIgnoreCase("onbekend@vv.com")).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> cleaningTaskService.addTask(dto));
        verify(taskRepository, never()).save(any());
    }

    @Test
    void addTask_withNullWeekNumber_throwsIllegalArgumentException() {
        CleaningTaskRequestDTO dto = new CleaningTaskRequestDTO();
        dto.setWeekNumber(0);
        dto.setName("Keuken");

        assertThrows(IllegalArgumentException.class, () -> cleaningTaskService.addTask(dto));
    }


    @Test
    void updateTask_existing_updatesFields() {
        User student = makeStudent("simon", "simon@vv.com");
        CleaningTask task = makeTask(1L, 1, "OudNaam", student);
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));

        CleaningTaskRequestDTO dto = new CleaningTaskRequestDTO();
        dto.setWeekNumber(2);
        dto.setName("NieuwNaam");
        dto.setDescription("Nieuwe beschrijving");

        CleaningTaskResponseDTO result = cleaningTaskService.updateTask(1L, dto);

        assertThat(result.getName()).isEqualTo("NieuwNaam");
        assertThat(result.getWeekNumber()).isEqualTo(2);
    }

    @Test
    void updateTask_notExisting_throwsEntityNotFoundException() {
        when(taskRepository.findById(99L)).thenReturn(Optional.empty());

        CleaningTaskRequestDTO dto = new CleaningTaskRequestDTO();
        dto.setWeekNumber(1);
        dto.setName("Test");

        assertThrows(EntityNotFoundException.class, () -> cleaningTaskService.updateTask(99L, dto));
    }


    @Test
    void toggleTask_completedTrue_setsToFalse() {
        CleaningTask task = makeTask(1L, 1, "Keuken", null);
        task.setCompleted(true);
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));

        CleaningTaskResponseDTO result = cleaningTaskService.toggleTask(1L);

        assertThat(result.isCompleted()).isFalse();
    }

    @Test
    void toggleTask_completedFalse_setsToTrue() {
        CleaningTask task = makeTask(1L, 1, "Keuken", null);
        task.setCompleted(false);
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));

        CleaningTaskResponseDTO result = cleaningTaskService.toggleTask(1L);

        assertThat(result.isCompleted()).isTrue();
    }

    @Test
    void toggleTask_notExisting_throwsEntityNotFoundException() {
        when(taskRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> cleaningTaskService.toggleTask(99L));
    }


    @Test
    void addComment_validComment_setsComment() {
        CleaningTask task = makeTask(1L, 1, "Keuken", null);
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));

        CleaningTaskResponseDTO result = cleaningTaskService.addComment(1L, "Goed gedaan");

        assertThat(result.getComment()).isEqualTo("Goed gedaan");
    }

    @Test
    void addComment_blankComment_throwsIllegalArgumentException() {
        CleaningTask task = makeTask(1L, 1, "Keuken", null);
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));

        assertThrows(IllegalArgumentException.class, () -> cleaningTaskService.addComment(1L, "   "));
    }

    @Test
    void addComment_nullComment_throwsIllegalArgumentException() {
        CleaningTask task = makeTask(1L, 1, "Keuken", null);
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));

        assertThrows(IllegalArgumentException.class, () -> cleaningTaskService.addComment(1L, null));
    }


    @Test
    void addIncident_validReport_setsIncidentReport() {
        CleaningTask task = makeTask(1L, 1, "Keuken", null);
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));

        CleaningTaskResponseDTO result = cleaningTaskService.addIncident(1L, "Waterlekkage ontdekt");

        assertThat(result.getIncidentReport()).isEqualTo("Waterlekkage ontdekt");
    }

    @Test
    void addIncident_blankReport_throwsIllegalArgumentException() {
        CleaningTask task = makeTask(1L, 1, "Keuken", null);
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));

        assertThrows(IllegalArgumentException.class, () -> cleaningTaskService.addIncident(1L, ""));
    }


    @Test
    void deleteTask_existing_callsDelete() {
        CleaningTask task = makeTask(1L, 1, "Keuken", null);
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));

        cleaningTaskService.deleteTask(1L);

        verify(taskRepository).delete(task);
    }

    @Test
    void deleteTask_notExisting_throwsEntityNotFoundException() {
        when(taskRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> cleaningTaskService.deleteTask(99L));
        verify(taskRepository, never()).delete(any());
    }


    @Test
    void getTasksForCaller_validEmail_returnsTasks() {
        User student = makeStudent("simon", "simon@vv.com");
        CleaningTask task = makeTask(1L, 1, "Keuken", student);

        when(taskRepository.findByAssignedTo_EmailIgnoreCaseOrderByWeekNumberAscIdAsc("simon@vv.com"))
                .thenReturn(List.of(task));

        List<CleaningTaskResponseDTO> result = cleaningTaskService.getTasksForCaller("simon@vv.com");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getAssignedTo()).isEqualTo("simon");
    }

    @Test
    void getTasksForCaller_nullEmail_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> cleaningTaskService.getTasksForCaller(null));
    }

    @Test
    void getTasksForCaller_blankEmail_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> cleaningTaskService.getTasksForCaller("   "));
    }


    @Test
    void getRotationLength_delegatesToScheduleService() {
        when(scheduleService.rotationLength()).thenReturn(5);

        int result = cleaningTaskService.getRotationLength();

        assertThat(result).isEqualTo(5);
        verify(scheduleService).rotationLength();
    }

    @Test
    void getCurrentRotationWeek_usesRotationLength() {
        when(scheduleService.rotationLength()).thenReturn(5);

        int rotW = cleaningTaskService.getCurrentRotationWeek();

        assertThat(rotW).isBetween(1, 5);
    }
}
