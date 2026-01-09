package com.villavredestein.dto;

/**
 * Response DTO voor een kamer.
 *
 * <p>Wordt gebruikt in alle API-responses voor rooms.
 * Deze DTO vervangt het direct terugsturen van de Room-entity.</p>
 *
 * <ul>
 *   <li><b>occupantId</b> en <b>occupantUsername</b> zijn {@code null} als de kamer leeg is</li>
 * </ul>
 */
public class RoomResponseDTO {

    private final Long id;
    private final String name;
    private final Long occupantId;
    private final String occupantUsername;

    public RoomResponseDTO(
            Long id,
            String name,
            Long occupantId,
            String occupantUsername
    ) {
        this.id = id;
        this.name = name;
        this.occupantId = occupantId;
        this.occupantUsername = occupantUsername;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Long getOccupantId() {
        return occupantId;
    }

    public String getOccupantUsername() {
        return occupantUsername;
    }
}