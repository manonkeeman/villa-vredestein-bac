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

/**
 * {@code RoomService} beheert de businesslogica rondom kamers binnen
 * de Villa Vredestein applicatie.
 *
 * <p>Deze service werkt intern met {@link Room} entities,
 * maar exposeert uitsluitend {@link RoomResponseDTO} richting controllers.</p>
 */
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
        if (roomRepository.existsByName(name)) {
            throw new IllegalArgumentException("Room name already exists");
        }

        Room room = roomRepository.save(new Room(name));
        return toDTO(room);
    }

    // =====================================================================
    // READ
    // =====================================================================

    public List<RoomResponseDTO> getAllRoomsDTO() {
        return roomRepository.findAll()
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
                .orElseThrow(() -> new EntityNotFoundException("Room not found"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        room.assignOccupant(user);
        return toDTO(roomRepository.save(room));
    }

    public RoomResponseDTO removeOccupantDTO(Long roomId) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new EntityNotFoundException("Room not found"));

        room.removeOccupant();
        return toDTO(roomRepository.save(room));
    }

    // =====================================================================
    // DELETE
    // =====================================================================

    public void deleteRoom(Long id) {
        if (!roomRepository.existsById(id)) {
            throw new EntityNotFoundException("Room not found");
        }
        roomRepository.deleteById(id);
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