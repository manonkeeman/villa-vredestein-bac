package com.villavredestein.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Entity
@Table(
        name = "rooms",
        indexes = {
                @Index(name = "idx_room_name", columnList = "name"),
                @Index(name = "idx_room_occupant", columnList = "occupant_id")
        }
)
public class Room {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Room name is required")
    @Size(max = 50, message = "Room name may contain at most 50 characters")
    @Column(nullable = false, unique = true, length = 50)
    private String name;

    @OneToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "occupant_id", unique = true)
    private User occupant;

    protected Room() {
    }

    public Room(String name) {
        this.name = name;
        normalize();
        validateName();
    }

    @PrePersist
    private void onCreate() {
        normalize();
        validateName();
    }

    @PreUpdate
    private void onUpdate() {
        normalize();
        validateName();
    }

    private void normalize() {
        if (name != null) {
            name = name.trim();
        }
    }

    private void validateName() {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Room name must not be blank");
        }
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public User getOccupant() {
        return occupant;
    }

    public void rename(String name) {
        this.name = name;
        normalize();
        validateName();
    }

    public void assignOccupant(User user) {
        if (user == null) {
            throw new IllegalArgumentException("Occupant is required");
        }
        this.occupant = user;
    }

    public void removeOccupant() {
        this.occupant = null;
    }

    public boolean isOccupied() {
        return occupant != null;
    }

    @Override
    public String toString() {
        return "Room{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", occupantId=" + (occupant != null ? occupant.getId() : null) +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Room)) return false;
        Room other = (Room) o;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return 31;
    }
}