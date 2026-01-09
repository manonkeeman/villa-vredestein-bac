package com.villavredestein.model;

import jakarta.persistence.*;

@Entity
@Table(name = "rooms")
public class Room {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @OneToOne(optional = true)
    @JoinColumn(name = "occupant_id", unique = true)
    private User occupant;

    protected Room() {
        // JPA only
    }

    public Room(String name) {
        this.name = name;
    }

    // =====================================================================
    // GETTERS
    // =====================================================================

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public User getOccupant() {
        return occupant;
    }

    // =====================================================================
    // DOMAIN LOGIC
    // =====================================================================

    /**
     * Wijzigt de naam van de kamer.
     */
    public void rename(String name) {
        this.name = name;
    }

    /**
     * Wijs een bewoner toe aan deze kamer.
     */
    public void assignOccupant(User user) {
        this.occupant = user;
    }

    /**
     * Verwijdert de bewoner uit deze kamer.
     */
    public void removeOccupant() {
        this.occupant = null;
    }
}