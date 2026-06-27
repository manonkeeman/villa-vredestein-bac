package com.villavredestein.controller;

import com.villavredestein.model.Shift;
import com.villavredestein.model.User;
import com.villavredestein.repository.ShiftRepository;
import com.villavredestein.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(value = "/api/shifts", produces = MediaType.APPLICATION_JSON_VALUE)
public class ShiftsController {

    private final ShiftRepository shiftRepository;
    private final UserRepository userRepository;

    public ShiftsController(ShiftRepository shiftRepository, UserRepository userRepository) {
        this.shiftRepository = shiftRepository;
        this.userRepository = userRepository;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Shift>> getAll() {
        return ResponseEntity.ok(shiftRepository.findAllByOrderByShiftDateDescCheckInAtDesc());
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('CLEANER')")
    public ResponseEntity<List<Shift>> getMine(Authentication auth) {
        User cleaner = resolveUser(auth.getName());
        return ResponseEntity.ok(shiftRepository.findByCleanerOrderByShiftDateDescCheckInAtDesc(cleaner));
    }

    @PostMapping("/checkin")
    @PreAuthorize("hasRole('CLEANER')")
    public ResponseEntity<Shift> checkIn(@RequestBody(required = false) Map<String, String> body,
                                         Authentication auth) {
        User cleaner = resolveUser(auth.getName());
        LocalDate today = LocalDate.now();

        Shift shift = new Shift();
        shift.setCleaner(cleaner);
        shift.setShiftDate(today);
        shift.setCheckInAt(Instant.now());
        if (body != null && body.get("notes") != null) shift.setNotes(body.get("notes"));

        return ResponseEntity.ok(shiftRepository.save(shift));
    }

    @PostMapping("/checkout")
    @PreAuthorize("hasRole('CLEANER')")
    public ResponseEntity<Shift> checkOut(@RequestBody(required = false) Map<String, String> body,
                                          Authentication auth) {
        User cleaner = resolveUser(auth.getName());
        Shift shift = shiftRepository.findFirstByCleanerAndCheckOutAtIsNullOrderByCheckInAtDesc(cleaner)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Geen actieve shift gevonden. Check eerst in."));

        shift.setCheckOutAt(Instant.now());
        if (body != null && body.get("notes") != null) shift.setNotes(body.get("notes"));

        return ResponseEntity.ok(shiftRepository.save(shift));
    }

    private User resolveUser(String email) {
        return userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Gebruiker niet gevonden."));
    }
}
