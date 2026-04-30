package com.villavredestein.service;

import com.villavredestein.dto.RoomResponseDTO;
import com.villavredestein.model.Room;
import com.villavredestein.model.User;
import com.villavredestein.repository.RoomRepository;
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
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RoomServiceTest {

    @Mock RoomRepository roomRepository;
    @Mock UserRepository userRepository;
    @InjectMocks RoomService roomService;


    @Test
    void getAllRoomsDTO_returnsListOfDTOs() {
        Room r1 = makeRoom(1L, "Kamer 1", null);
        Room r2 = makeRoom(2L, "Kamer 2", null);
        when(roomRepository.findAllByOrderByIdAsc()).thenReturn(List.of(r1, r2));

        List<RoomResponseDTO> result = roomService.getAllRoomsDTO();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).name()).isEqualTo("Kamer 1");
        assertThat(result.get(1).name()).isEqualTo("Kamer 2");
    }

    @Test
    void getAllRoomsDTO_empty_returnsEmptyList() {
        when(roomRepository.findAllByOrderByIdAsc()).thenReturn(List.of());

        List<RoomResponseDTO> result = roomService.getAllRoomsDTO();

        assertThat(result).isEmpty();
    }


    @Test
    void getRoomByIdDTO_found_returnsDto() {
        Room room = makeRoom(1L, "Kamer 1", null);
        when(roomRepository.findById(1L)).thenReturn(Optional.of(room));

        Optional<RoomResponseDTO> result = roomService.getRoomByIdDTO(1L);

        assertThat(result).isPresent();
        assertThat(result.get().name()).isEqualTo("Kamer 1");
        assertThat(result.get().occupantId()).isNull();
    }

    @Test
    void getRoomByIdDTO_notFound_returnsEmptyOptional() {
        when(roomRepository.findById(99L)).thenReturn(Optional.empty());

        Optional<RoomResponseDTO> result = roomService.getRoomByIdDTO(99L);

        assertThat(result).isEmpty();
    }

    @Test
    void getRoomByIdDTO_nullId_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> roomService.getRoomByIdDTO(null));
        verify(roomRepository, never()).findById(any());
    }

    @Test
    void getRoomByIdDTO_zeroId_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> roomService.getRoomByIdDTO(0L));
        verify(roomRepository, never()).findById(any());
    }


    @Test
    void assignOccupantDTO_success_assignsOccupantAndReturnsDto() {
        User user = makeUser(1L, "student", "s@test.com");
        Room room = makeRoom(1L, "Kamer 1", null);
        when(roomRepository.findById(1L)).thenReturn(Optional.of(room));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(roomRepository.findByOccupant_Id(1L)).thenReturn(Optional.empty());

        RoomResponseDTO result = roomService.assignOccupantDTO(1L, 1L);

        assertThat(result.name()).isEqualTo("Kamer 1");
        assertThat(result.occupantUsername()).isEqualTo("student");
    }

    @Test
    void assignOccupantDTO_roomNotFound_throwsEntityNotFoundException() {
        when(roomRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> roomService.assignOccupantDTO(99L, 1L));
        verify(userRepository, never()).findById(any());
    }

    @Test
    void assignOccupantDTO_userNotFound_throwsEntityNotFoundException() {
        Room room = makeRoom(1L, "Kamer 1", null);
        when(roomRepository.findById(1L)).thenReturn(Optional.of(room));
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> roomService.assignOccupantDTO(1L, 99L));
    }

    @Test
    void assignOccupantDTO_roomOccupiedByDifferentUser_throwsIllegalStateException() {
        User existingOccupant = makeUser(2L, "other", "other@test.com");
        User newUser = makeUser(1L, "student", "s@test.com");
        Room room = makeRoom(1L, "Kamer 1", existingOccupant);
        when(roomRepository.findById(1L)).thenReturn(Optional.of(room));
        when(userRepository.findById(1L)).thenReturn(Optional.of(newUser));

        assertThrows(IllegalStateException.class, () -> roomService.assignOccupantDTO(1L, 1L));
    }

    @Test
    void assignOccupantDTO_userAlreadyInDifferentRoom_throwsIllegalStateException() {
        User user = makeUser(1L, "student", "s@test.com");
        Room targetRoom = makeRoom(1L, "Kamer 1", null);
        Room otherRoom = makeRoom(2L, "Kamer 2", user);
        when(roomRepository.findById(1L)).thenReturn(Optional.of(targetRoom));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(roomRepository.findByOccupant_Id(1L)).thenReturn(Optional.of(otherRoom));

        assertThrows(IllegalStateException.class, () -> roomService.assignOccupantDTO(1L, 1L));
    }

    @Test
    void assignOccupantDTO_reassignSameUserToSameRoom_succeeds() {
        User user = makeUser(1L, "student", "s@test.com");
        Room room = makeRoom(1L, "Kamer 1", user);
        when(roomRepository.findById(1L)).thenReturn(Optional.of(room));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(roomRepository.findByOccupant_Id(1L)).thenReturn(Optional.of(room));

        RoomResponseDTO result = roomService.assignOccupantDTO(1L, 1L);

        assertThat(result.name()).isEqualTo("Kamer 1");
        assertThat(result.occupantUsername()).isEqualTo("student");
    }


    @Test
    void removeOccupantDTO_success_removesOccupantAndReturnsDto() {
        User user = makeUser(1L, "student", "s@test.com");
        Room room = makeRoom(1L, "Kamer 1", user);
        when(roomRepository.findById(1L)).thenReturn(Optional.of(room));

        RoomResponseDTO result = roomService.removeOccupantDTO(1L);

        assertThat(result.name()).isEqualTo("Kamer 1");
        assertThat(result.occupantId()).isNull();
    }

    @Test
    void removeOccupantDTO_roomNotFound_throwsEntityNotFoundException() {
        when(roomRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> roomService.removeOccupantDTO(99L));
    }


    @Test
    void deleteRoom_success_deletesRoom() {
        Room room = makeRoom(1L, "Kamer 1", null);
        when(roomRepository.findById(1L)).thenReturn(Optional.of(room));

        assertDoesNotThrow(() -> roomService.deleteRoom(1L));
        verify(roomRepository).delete(room);
    }

    @Test
    void deleteRoom_notFound_throwsEntityNotFoundException() {
        when(roomRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> roomService.deleteRoom(99L));
        verify(roomRepository, never()).delete(any());
    }

    @Test
    void deleteRoom_nullId_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> roomService.deleteRoom(null));
        verify(roomRepository, never()).findById(any());
    }

    @Test
    void deleteRoom_zeroId_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> roomService.deleteRoom(0L));
        verify(roomRepository, never()).findById(any());
    }


    private User makeUser(long id, String username, String email) {
        User user = new User(username, email, "hash", User.Role.STUDENT);
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }

    private Room makeRoom(long id, String name, User occupant) {
        Room room = new Room(name);
        ReflectionTestUtils.setField(room, "id", id);
        if (occupant != null) {
            room.assignOccupant(occupant);
        }
        return room;
    }
}
