package com.villavredestein.service;

import com.villavredestein.model.Room;
import com.villavredestein.model.User;
import com.villavredestein.repository.RoomRepository;
import com.villavredestein.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class RoomService {

    private final RoomRepository roomRepository;
    private final UserRepository userRepository;

    public RoomService(RoomRepository roomRepository, UserRepository userRepository) {
        this.roomRepository = roomRepository;
        this.userRepository = userRepository;
    }

    public List<Room> getAllRooms() {
        return roomRepository.findAll().stream()
                .peek(room -> {
                    if (room.getOccupant() != null)
                        room.setName(room.getName() + " (" + room.getOccupant().getUsername() + ")");
                })
                .toList();
    }

    public Optional<Room> getRoomById(Long id) {
        return roomRepository.findById(id)
                .map(room -> {
                    if (room.getOccupant() != null)
                        room.setName(room.getName() + " (" + room.getOccupant().getUsername() + ")");
                    return room;
                });
    }

    public Room createRoom(Room room) {
        return roomRepository.save(room);
    }

    public Room assignOccupant(Long roomId, Long userId) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Room not found"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        room.setOccupant(user);
        return roomRepository.save(room);
    }

    public Room removeOccupant(Long roomId) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Room not found"));
        room.setOccupant(null);
        return roomRepository.save(room);
    }

    public void deleteRoom(Long id) {
        roomRepository.deleteById(id);
    }
}