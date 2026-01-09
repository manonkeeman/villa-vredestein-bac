package com.villavredestein.dto;

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