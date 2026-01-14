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
    // CREATE
    // =====================================================================

    public RoomResponseDTO createRoomDTO(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Room name is verplicht");
        }
        String normalizedName = name.trim();

        if (roomRepository.existsByNameIgnoreCase(normalizedName)) {
            throw new IllegalArgumentException("Room name already exists");
        }

        Room room = roomRepository.save(new Room(normalizedName));
        return toDTO(room);
    }

    // =====================================================================
    // READ
    // =====================================================================

    public List<RoomResponseDTO> getAllRoomsDTO() {
        return roomRepository.findAllByOrderByIdAsc()
                .stream()
                .map(this::toDTO)
                .toList();
    }

    public Optional<RoomResponseDTO> getRoomByIdDTO(Long id) {
        return roomRepository.findById(id).map(this::toDTO);
    }

    // =====================================================================
    // UPDATE
    // =====================================================================

    public RoomResponseDTO assignOccupantDTO(Long roomId, Long userId) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new EntityNotFoundException("Room niet gevonden: " + roomId));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User niet gevonden: " + userId));

        room.assignOccupant(user);
        return toDTO(room);
    }

    public RoomResponseDTO removeOccupantDTO(Long roomId) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new EntityNotFoundException("Room niet gevonden: " + roomId));

        room.removeOccupant();
        return toDTO(room);
    }

    // =====================================================================
    // DELETE
    // =====================================================================

    public void deleteRoom(Long id) {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Room niet gevonden: " + id));
        roomRepository.delete(room);
    }

    // =====================================================================
    // MAPPER
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
}