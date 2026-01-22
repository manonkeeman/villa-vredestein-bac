package com.villavredestein.service;

import com.villavredestein.dto.RoomResponseDTO;
import com.villavredestein.model.Room;
import com.villavredestein.model.User;
import com.villavredestein.repository.RoomRepository;
import com.villavredestein.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class RoomService {

    private final RoomRepository roomRepository;
    private final UserRepository userRepository;

    public RoomService(RoomRepository roomRepository, UserRepository userRepository) {
        this.roomRepository = roomRepository;
        this.userRepository = userRepository;
    }

    // =====================================================================
    // # READ
    // =====================================================================

    @Transactional(readOnly = true)
    public List<RoomResponseDTO> getAllRoomsDTO() {
        return roomRepository.findAllByOrderByIdAsc()
                .stream()
                .map(this::toDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public Optional<RoomResponseDTO> getRoomByIdDTO(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Room id is required");
        }
        return roomRepository.findById(id).map(this::toDTO);
    }

    // =====================================================================
    // # UPDATE
    // =====================================================================

    public RoomResponseDTO assignOccupantDTO(Long roomId, Long userId) {
        Room room = roomRepository.findById(requireId(roomId, "roomId"))
                .orElseThrow(() -> new EntityNotFoundException("Room not found: " + roomId));

        User user = userRepository.findById(requireId(userId, "userId"))
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + userId));

        if (room.getOccupant() != null && !room.getOccupant().getId().equals(user.getId())) {
            throw new IllegalStateException("Room is already occupied");
        }

        roomRepository.findByOccupant_Id(user.getId()).ifPresent(existingRoom -> {
            if (!existingRoom.getId().equals(room.getId())) {
                throw new IllegalStateException("User is already assigned to another room");
            }
        });

        room.assignOccupant(user);
        return toDTO(room);
    }

    public RoomResponseDTO removeOccupantDTO(Long roomId) {
        Room room = roomRepository.findById(requireId(roomId, "roomId"))
                .orElseThrow(() -> new EntityNotFoundException("Room not found: " + roomId));

        room.removeOccupant();
        return toDTO(room);
    }

    // =====================================================================
    // # DELETE (optional)
    // =====================================================================

    public void deleteRoom(Long id) {
        Room room = roomRepository.findById(requireId(id, "id"))
                .orElseThrow(() -> new EntityNotFoundException("Room not found: " + id));
        roomRepository.delete(room);
    }

    // =====================================================================
    // # MAPPER
    // =====================================================================

    private RoomResponseDTO toDTO(Room room) {
        Long occupantId = room.getOccupant() != null ? room.getOccupant().getId() : null;
        String occupantUsername = room.getOccupant() != null ? room.getOccupant().getUsername() : null;

        return new RoomResponseDTO(
                room.getId(),
                room.getName(),
                occupantId,
                occupantUsername
        );
    }

    // =====================================================================
    // # HELPERS
    // =====================================================================

    private Long requireId(Long value, String fieldName) {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException(fieldName + " is required");
        }
        return value;
    }
}