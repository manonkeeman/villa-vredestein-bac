package com.villavredestein.controller;

import com.villavredestein.model.Room;
import com.villavredestein.service.RoomService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rooms")
@CrossOrigin(origins = "*")
public class RoomController {

    private final RoomService roomService;

    public RoomController(RoomService roomService) {
        this.roomService = roomService;
    }

    @PreAuthorize("hasAnyRole('ADMIN','STUDENT')")
    @GetMapping
    public ResponseEntity<List<Room>> getAllRooms() {
        return ResponseEntity.ok(roomService.getAllRooms());
    }

    @PreAuthorize("hasAnyRole('ADMIN','STUDENT')")
    @GetMapping("/{id}")
    public ResponseEntity<Room> getRoomById(@PathVariable Long id) {
        return roomService.getRoomById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<Room> createRoom(@RequestBody Room room) {
        return ResponseEntity.ok(roomService.createRoom(room));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{roomId}/assign/{userId}")
    public ResponseEntity<Room> assignOccupant(@PathVariable Long roomId, @PathVariable Long userId) {
        return ResponseEntity.ok(roomService.assignOccupant(roomId, userId));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{roomId}/remove")
    public ResponseEntity<Room> removeOccupant(@PathVariable Long roomId) {
        return ResponseEntity.ok(roomService.removeOccupant(roomId));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRoom(@PathVariable Long id) {
        roomService.deleteRoom(id);
        return ResponseEntity.noContent().build();
    }
}