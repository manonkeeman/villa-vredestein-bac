package com.villavredestein.controller;

import com.villavredestein.model.Room;
import com.villavredestein.service.RoomService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * {@code RoomController} beheert alle API-endpoints voor kamerverhuur binnen
 * de Villa Vredestein webapplicatie.
 *
 * <p>De controller maakt het mogelijk om kamers te bekijken, aan te maken,
 * bewoners toe te wijzen of te verwijderen en kamers te verwijderen uit
 * de database. Toegang tot de endpoints is afhankelijk van de gebruikersrol:
 * STUDENT kan kamers inzien, ADMIN kan kamers beheren.</p>
 *
 * <p>De controller werkt samen met {@link RoomService} voor de verwerking
 * van de businesslogica en database-operaties.</p>
 */
@RestController
@RequestMapping("/api/rooms")
@CrossOrigin
public class RoomController {

    private final RoomService roomService;

    /**
     * Constructor voor {@link RoomController}.
     *
     * @param roomService service die kamerverwerking verzorgt
     */
    public RoomController(RoomService roomService) {
        this.roomService = roomService;
    }

    /**
     * Haalt een lijst op van alle beschikbare kamers.
     *
     * <p>Beschikbaar voor gebruikers met de rollen ADMIN en STUDENT.</p>
     *
     * @return lijst van {@link Room} objecten
     */
    @PreAuthorize("hasAnyRole('ADMIN','STUDENT')")
    @GetMapping
    public ResponseEntity<List<Room>> getAllRooms() {
        return ResponseEntity.ok(roomService.getAllRooms());
    }

    /**
     * Haalt details op van een specifieke kamer op basis van ID.
     *
     * <p>Beschikbaar voor gebruikers met de rollen ADMIN en STUDENT.</p>
     *
     * @param id het unieke ID van de kamer
     * @return {@link Room} object bij succes of 404 Not Found als niet gevonden
     */
    @PreAuthorize("hasAnyRole('ADMIN','STUDENT')")
    @GetMapping("/{id}")
    public ResponseEntity<Room> getRoomById(@PathVariable Long id) {
        return roomService.getRoomById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Maakt een nieuwe kamer aan in het systeem.
     *
     * <p>Alleen toegankelijk voor gebruikers met de rol ADMIN.</p>
     *
     * @param room kamerobject dat moet worden opgeslagen
     * @return aangemaakte {@link Room}
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<Room> createRoom(@RequestBody Room room) {
        return ResponseEntity.ok(roomService.createRoom(room));
    }

    /**
     * Wijs een bewoner toe aan een kamer.
     *
     * <p>Alleen toegankelijk voor gebruikers met de rol ADMIN.</p>
     *
     * @param roomId ID van de kamer
     * @param userId ID van de gebruiker die wordt toegewezen
     * @return bijgewerkte {@link Room} met toegewezen bewoner
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{roomId}/assign/{userId}")
    public ResponseEntity<Room> assignOccupant(@PathVariable Long roomId, @PathVariable Long userId) {
        return ResponseEntity.ok(roomService.assignOccupant(roomId, userId));
    }

    /**
     * Verwijdert de huidige bewoner uit een kamer.
     *
     * <p>Alleen toegankelijk voor gebruikers met de rol ADMIN.</p>
     *
     * @param roomId ID van de kamer
     * @return bijgewerkte {@link Room} zonder bewoner
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{roomId}/remove")
    public ResponseEntity<Room> removeOccupant(@PathVariable Long roomId) {
        return ResponseEntity.ok(roomService.removeOccupant(roomId));
    }

    /**
     * Verwijdert een kamer uit de database.
     *
     * <p>Alleen toegankelijk voor gebruikers met de rol ADMIN.</p>
     *
     * @param id het unieke ID van de kamer
     * @return HTTP 204 No Content bij succes
     */
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRoom(@PathVariable Long id) {
        roomService.deleteRoom(id);
        return ResponseEntity.noContent().build();
    }
}