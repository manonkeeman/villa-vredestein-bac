package com.villavredestein.model;

import jakarta.persistence.*;

/**
 * Entity die een kamer binnen Villa Vredestein representeert.
 *
 * <ul>
 *   <li>Een kamer heeft altijd een unieke naam</li>
 *   <li>Een kamer kan maximaal één bewoner (User) hebben</li>
 *   <li>Een gebruiker kan maximaal één kamer bewonen</li>
 * </ul>
 *
 * <p>Deze entity wordt nooit direct naar de client gestuurd;
 * controllers werken uitsluitend met DTO's.</p>
 */
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

    /**
     * Protected no-args constructor voor JPA.
     */
    protected Room() {
        // JPA only
    }

    /**
     * Maakt een nieuwe kamer aan met een verplichte naam.
     */
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