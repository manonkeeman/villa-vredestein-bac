package com.villavredestein.service;

import com.villavredestein.model.CleaningTask;
import com.villavredestein.model.User;
import com.villavredestein.repository.CleaningTaskRepository;
import com.villavredestein.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CleaningScheduleServiceTest {

    @Mock UserRepository userRepository;
    @Mock CleaningTaskRepository cleaningTaskRepository;
    @InjectMocks CleaningScheduleService cleaningScheduleService;

    private User makeStudent(long id, String username, String email) {
        User user = new User(username, email, "hash", User.Role.STUDENT);
        org.springframework.test.util.ReflectionTestUtils.setField(user, "id", id);
        return user;
    }


    @Test
    void rotationLength_withThreeStudents_returnsFour() {
        when(userRepository.findByRole(User.Role.STUDENT))
                .thenReturn(List.of(makeStudent(1, "a", "a@vv.com"),
                                    makeStudent(2, "b", "b@vv.com"),
                                    makeStudent(3, "c", "c@vv.com")));

        int result = cleaningScheduleService.rotationLength();

        assertThat(result).isEqualTo(4); // 3 studenten + 1 vrije week
    }

    @Test
    void rotationLength_withNoStudents_returnsOne() {
        when(userRepository.findByRole(User.Role.STUDENT)).thenReturn(List.of());

        int result = cleaningScheduleService.rotationLength();

        assertThat(result).isEqualTo(1);
    }


    @Test
    void expectedTaskCount_withFourStudents_returnsTwenty() {
        when(userRepository.findByRole(User.Role.STUDENT))
                .thenReturn(List.of(makeStudent(1, "a", "a@vv.com"),
                                    makeStudent(2, "b", "b@vv.com"),
                                    makeStudent(3, "c", "c@vv.com"),
                                    makeStudent(4, "d", "d@vv.com")));

        int result = cleaningScheduleService.expectedTaskCount();

        assertThat(result).isEqualTo(20); // (4 + 1) * 4
    }

    @Test
    void expectedTaskCount_withNoStudents_returnsFour() {
        when(userRepository.findByRole(User.Role.STUDENT)).thenReturn(List.of());

        int result = cleaningScheduleService.expectedTaskCount();

        assertThat(result).isEqualTo(4); // (0 + 1) * 4
    }


    @Test
    void reseedNow_withTwoStudents_createsThreeWeeksTwelveTasksTotal() {
        User s1 = makeStudent(1, "simon", "simon@vv.com");
        User s2 = makeStudent(2, "desmond", "desmond@vv.com");
        when(userRepository.findByRole(User.Role.STUDENT)).thenReturn(List.of(s1, s2));

        cleaningScheduleService.reseedNow();

        verify(cleaningTaskRepository, times(12)).save(any(CleaningTask.class));
        verify(cleaningTaskRepository).deleteAll();
        verify(cleaningTaskRepository).flush();
    }

    @Test
    void reseedNow_withNoStudents_createsOneWeekFourTasks() {
        when(userRepository.findByRole(User.Role.STUDENT)).thenReturn(List.of());

        cleaningScheduleService.reseedNow();

        verify(cleaningTaskRepository, times(4)).save(any(CleaningTask.class));
    }

    @Test
    void reseedNow_deletesAllExistingTasksFirst() {
        when(userRepository.findByRole(User.Role.STUDENT)).thenReturn(List.of());

        cleaningScheduleService.reseedNow();

        var inOrder = inOrder(cleaningTaskRepository);
        inOrder.verify(cleaningTaskRepository).deleteAll();
        inOrder.verify(cleaningTaskRepository).flush();
        inOrder.verify(cleaningTaskRepository, atLeastOnce()).save(any(CleaningTask.class));
    }

    @Test
    void reseedNow_withOneStudent_createsEightTasksTwoWeeks() {
        User s1 = makeStudent(1, "simon", "simon@vv.com");
        when(userRepository.findByRole(User.Role.STUDENT)).thenReturn(List.of(s1));

        cleaningScheduleService.reseedNow();

        verify(cleaningTaskRepository, times(8)).save(any(CleaningTask.class));
    }

    @Test
    void reseedNow_sortsByStudentId() {
        User s1 = makeStudent(1, "simon", "simon@vv.com");
        User s2 = makeStudent(2, "desmond", "desmond@vv.com");
        when(userRepository.findByRole(User.Role.STUDENT)).thenReturn(List.of(s2, s1));

        ArgumentCaptor<CleaningTask> captor = ArgumentCaptor.forClass(CleaningTask.class);

        cleaningScheduleService.reseedNow();

        verify(cleaningTaskRepository, times(12)).save(captor.capture());

        CleaningTask firstTask = captor.getAllValues().get(0);
        assertThat(firstTask.getWeekNumber()).isEqualTo(1);
        assertThat(firstTask.getAssignedTo()).isEqualTo(s1); // gesorteerd op id → simon eerst
    }

    @Test
    void reseedNow_taskNamesAreInDutch() {
        User s1 = makeStudent(1, "simon", "simon@vv.com");
        when(userRepository.findByRole(User.Role.STUDENT)).thenReturn(List.of(s1));

        ArgumentCaptor<CleaningTask> captor = ArgumentCaptor.forClass(CleaningTask.class);

        cleaningScheduleService.reseedNow();

        verify(cleaningTaskRepository, atLeastOnce()).save(captor.capture());

        List<String> names = captor.getAllValues().stream()
                .map(CleaningTask::getName)
                .toList();

        assertThat(names).anyMatch(n -> n.toLowerCase().contains("keuken"));
        assertThat(names).anyMatch(n -> n.toLowerCase().contains("badkamer"));
        assertThat(names).anyMatch(n -> n.toLowerCase().contains("vuilnis"));
        assertThat(names).anyMatch(n -> n.toLowerCase().contains("woonkamer"));
    }

    @Test
    void reseedNow_freeWeekHasNullAssignee() {
        User s1 = makeStudent(1, "simon", "simon@vv.com");
        when(userRepository.findByRole(User.Role.STUDENT)).thenReturn(List.of(s1));

        ArgumentCaptor<CleaningTask> captor = ArgumentCaptor.forClass(CleaningTask.class);

        cleaningScheduleService.reseedNow();

        verify(cleaningTaskRepository, times(8)).save(captor.capture());

        assertThat(captor.getAllValues())
                .anyMatch(t -> t.getAssignedTo() == null);
    }
}