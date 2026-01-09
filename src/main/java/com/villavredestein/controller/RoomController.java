package com.villavredestein.controller;

import com.villavredestein.dto.RoomResponseDTO;
import com.villavredestein.service.RoomService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * {@code RoomController} beheert alle API-endpoints rondom kamers.
 *
 * <ul>
 *   <li><b>STUDENT</b>: mag kamers inzien</li>
 *   <li><b>ADMIN</b>: mag kamers aanmaken, bezetting beheren en kamers verwijderen</li>
 * </ul>
 *
 * <p>Responses zijn altijd DTO's (geen entities).</p>
 */
@RestController
@RequestMapping(value = "/api/rooms", produces = MediaType.APPLICATION_JSON_VALUE)
@CrossOrigin
public class RoomController {

    private final RoomService roomService;

    public RoomController(RoomService roomService) {
        this.roomService = roomService;
    }

    // =====================================================================
    // REQUEST DTO
    // =====================================================================

    /**
     * Request body voor het aanmaken van een kamer.
     */
    public record CreateRoomRequest(
            @NotBlank(message = "room name is verplicht")
            String name
    ) {}

    // =====================================================================
    // READ
    // =====================================================================

    /**
     * Haalt alle kamers op.
     */
    @PreAuthorize("hasAnyRole('ADMIN','STUDENT')")
    @GetMapping
    public ResponseEntity<List<RoomResponseDTO>> getAllRooms() {
        return ResponseEntity.ok(roomService.getAllRoomsDTO());
    }

    /**
     * Haalt een kamer op via id.
     */
    @PreAuthorize("hasAnyRole('ADMIN','STUDENT')")
    @GetMapping("/{id}")
    public ResponseEntity<RoomResponseDTO> getRoomById(@PathVariable Long id) {
        return roomService.getRoomByIdDTO(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // =====================================================================
    // CREATE
    // =====================================================================

    /**
     * ADMIN maakt een nieuwe kamer aan.
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<RoomResponseDTO> createRoom(@Valid @RequestBody CreateRoomRequest request) {
        RoomResponseDTO created = roomService.createRoomDTO(request.name());
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    // =====================================================================
    // UPDATE
    // =====================================================================

    /**
     * ADMIN wijst een bewoner toe aan een kamer.
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{roomId}/assign/{userId}")
    public ResponseEntity<RoomResponseDTO> assignOccupant(@PathVariable Long roomId, @PathVariable Long userId) {
        return ResponseEntity.ok(roomService.assignOccupantDTO(roomId, userId));
    }

    /**
     * ADMIN verwijdert de bewoner uit een kamer.
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{roomId}/remove")
    public ResponseEntity<RoomResponseDTO> removeOccupant(@PathVariable Long roomId) {
        return ResponseEntity.ok(roomService.removeOccupantDTO(roomId));
    }

    // =====================================================================
    // DELETE
    // =====================================================================

    /**
     * ADMIN verwijdert een kamer.
     */
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRoom(@PathVariable Long id) {
        roomService.deleteRoom(id);
        return ResponseEntity.noContent().build();
    }
}