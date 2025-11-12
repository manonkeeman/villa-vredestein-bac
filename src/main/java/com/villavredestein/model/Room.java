package com.villavredestein.model;

import jakarta.persistence.*;

@Entity
@Table(name = "rooms")
public class Room {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @OneToOne
    @JoinColumn(name = "occupant_id")
    private User occupant;

    public Room() {}

    public Room(String name, User occupant) {
        this.name = name;
        this.occupant = occupant;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public User getOccupant() { return occupant; }
    public void setOccupant(User occupant) { this.occupant = occupant; }
}