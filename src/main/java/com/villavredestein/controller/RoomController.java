package com.villavredestein.controller;

import com.villavredestein.dto.RoomResponseDTO;
import com.villavredestein.service.RoomService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.constraints.Positive;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// =====================================================================
// # RoomController
// =====================================================================
@Validated
@RestController
@RequestMapping(value = "/api/rooms", produces = MediaType.APPLICATION_JSON_VALUE)
@CrossOrigin
public class RoomController {

    private final RoomService roomService;

    public RoomController(RoomService roomService) {
        this.roomService = roomService;
    }

    // =====================================================================
    // # READ
    // =====================================================================

    @PreAuthorize("hasAnyRole('ADMIN','STUDENT')")
    @GetMapping
    public ResponseEntity<List<RoomResponseDTO>> getAllRooms() {
        return ResponseEntity.ok(roomService.getAllRoomsDTO());
    }

    @PreAuthorize("hasAnyRole('ADMIN','STUDENT')")
    @GetMapping("/{id}")
    public ResponseEntity<RoomResponseDTO> getRoomById(@PathVariable @Positive Long id) {
        return ResponseEntity.ok(
                roomService.getRoomByIdDTO(id)
                        .orElseThrow(() -> new EntityNotFoundException("Room not found: " + id))
        );
    }

    // =====================================================================
    // # UPDATE
    // =====================================================================

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{roomId}/assign/{userId}")
    public ResponseEntity<RoomResponseDTO> assignOccupant(
            @PathVariable @Positive Long roomId,
            @PathVariable @Positive Long userId
    ) {
        return ResponseEntity.ok(roomService.assignOccupantDTO(roomId, userId));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{roomId}/remove")
    public ResponseEntity<RoomResponseDTO> removeOccupant(@PathVariable @Positive Long roomId) {
        return ResponseEntity.ok(roomService.removeOccupantDTO(roomId));
    }

    // =====================================================================
    // # DELETE
    // =====================================================================

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRoom(@PathVariable @Positive Long id) {
        roomService.deleteRoom(id);
        return ResponseEntity.noContent().build();
    }
}