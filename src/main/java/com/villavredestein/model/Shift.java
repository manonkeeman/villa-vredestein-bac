package com.villavredestein.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "shifts",
        indexes = @Index(name = "idx_shifts_cleaner_date", columnList = "cleaner_id, shift_date"))
public class Shift {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cleaner_id", nullable = false)
    @JsonIgnoreProperties({"password", "invoices", "hibernateLazyInitializer"})
    private User cleaner;

    @Column(name = "shift_date", nullable = false)
    private LocalDate shiftDate;

    @Column(name = "check_in_at")
    private Instant checkInAt;

    @Column(name = "check_out_at")
    private Instant checkOutAt;

    @Column(length = 500)
    private String notes;

    public Long getId() { return id; }
    public User getCleaner() { return cleaner; }
    public void setCleaner(User cleaner) { this.cleaner = cleaner; }
    public LocalDate getShiftDate() { return shiftDate; }
    public void setShiftDate(LocalDate shiftDate) { this.shiftDate = shiftDate; }
    public Instant getCheckInAt() { return checkInAt; }
    public void setCheckInAt(Instant checkInAt) { this.checkInAt = checkInAt; }
    public Instant getCheckOutAt() { return checkOutAt; }
    public void setCheckOutAt(Instant checkOutAt) { this.checkOutAt = checkOutAt; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
