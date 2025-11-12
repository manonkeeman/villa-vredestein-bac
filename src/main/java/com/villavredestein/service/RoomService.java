package com.villavredestein.service;

import com.villavredestein.model.Room;
import com.villavredestein.model.User;
import com.villavredestein.repository.RoomRepository;
import com.villavredestein.repository.UserRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

/**
 * {@code RoomService} beheert de logica rondom kamers binnen het Villa Vredestein-systeem.
 *
 * <p>Deze service is verantwoordelijk voor het aanmaken, ophalen, bijwerken en verwijderen van kamers,
 * en voor het toewijzen of verwijderen van bewoners (occupants). De klasse vormt de schakel
 * tussen de database-repositories {@link RoomRepository} en {@link UserRepository}.</p>
 *
 * <p>Elke kamer kan één bewoner (student) bevatten. De naam van de kamer wordt dynamisch
 * aangevuld met de naam van de bewoner wanneer deze aanwezig is, zodat lijsten
 * direct inzicht geven in de bezetting.</p>
 *
 * <p>De {@link RoomService} wordt gebruikt door de {@code RoomController} en door
 * beheerdersfuncties in de applicatie.</p>
 */
@Service
public class RoomService {

    private final RoomRepository roomRepository;
    private final UserRepository userRepository;

    /**
     * Constructor voor {@link RoomService}.
     *
     * @param roomRepository repository voor kamerbeheer
     * @param userRepository repository voor gebruikersbeheer
     */
    public RoomService(RoomRepository roomRepository, UserRepository userRepository) {
        this.roomRepository = roomRepository;
        this.userRepository = userRepository;
    }

    /**
     * Haalt alle kamers op en voegt — indien aanwezig — de naam van de bewoner toe aan de kamernaam.
     *
     * @return lijst van alle {@link Room}-objecten, inclusief occupantinformatie
     */
    public List<Room> getAllRooms() {
        return roomRepository.findAll().stream()
                .peek(room -> {
                    if (room.getOccupant() != null)
                        room.setName(room.getName() + " (" + room.getOccupant().getUsername() + ")");
                })
                .toList();
    }

    /**
     * Haalt één kamer op aan de hand van het unieke ID.
     * Als de kamer een bewoner heeft, wordt de naam uitgebreid met de gebruikersnaam.
     *
     * @param id het unieke ID van de kamer
     * @return optioneel {@link Room}-object
     */
    public Optional<Room> getRoomById(Long id) {
        return roomRepository.findById(id)
                .map(room -> {
                    if (room.getOccupant() != null)
                        room.setName(room.getName() + " (" + room.getOccupant().getUsername() + ")");
                    return room;
                });
    }

    /**
     * Maakt een nieuwe kamer aan in de database.
     *
     * @param room het {@link Room}-object dat moet worden aangemaakt
     * @return de opgeslagen {@link Room}
     */
    public Room createRoom(Room room) {
        return roomRepository.save(room);
    }

    /**
     * Wijs een bestaande gebruiker toe als bewoner van een kamer.
     *
     * @param roomId het unieke ID van de kamer
     * @param userId het unieke ID van de gebruiker
     * @return de bijgewerkte {@link Room} met toegewezen bewoner
     * @throws RuntimeException als kamer of gebruiker niet wordt gevonden
     */
    public Room assignOccupant(Long roomId, Long userId) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Room not found"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        room.setOccupant(user);
        return roomRepository.save(room);
    }

    /**
     * Verwijdert de bewoner (occupant) uit een kamer zonder de kamer zelf te verwijderen.
     *
     * @param roomId het unieke ID van de kamer
     * @return de bijgewerkte {@link Room} zonder occupant
     * @throws RuntimeException als de kamer niet wordt gevonden
     */
    public Room removeOccupant(Long roomId) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Room not found"));
        room.setOccupant(null);
        return roomRepository.save(room);
    }

    /**
     * Verwijdert een kamer uit de database op basis van ID.
     *
     * @param id het unieke ID van de kamer die moet worden verwijderd
     */
    public void deleteRoom(Long id) {
        roomRepository.deleteById(id);
    }
}