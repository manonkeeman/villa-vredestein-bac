package com.villavredestein.controller;

import com.villavredestein.model.Huisregel;
import com.villavredestein.repository.HuisregelRepository;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(value = "/api/huisregels", produces = MediaType.APPLICATION_JSON_VALUE)
public class HuisregelsController {

    private final HuisregelRepository huisregelRepository;

    public HuisregelsController(HuisregelRepository huisregelRepository) {
        this.huisregelRepository = huisregelRepository;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','STUDENT','CLEANER')")
    public ResponseEntity<List<Huisregel>> getAll() {
        return ResponseEntity.ok(huisregelRepository.findAllByOrderByOrderIndexAscIdAsc());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','STUDENT','CLEANER')")
    public ResponseEntity<Huisregel> getById(@PathVariable Long id) {
        return huisregelRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Huisregel> create(@Valid @RequestBody Huisregel huisregel) {
        huisregel.setUpdatedAt(Instant.now());
        return ResponseEntity.ok(huisregelRepository.save(huisregel));
    }

    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Huisregel> update(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        return huisregelRepository.findById(id).map(h -> {
            if (body.containsKey("title") && body.get("title") != null)
                h.setTitle(body.get("title").toString());
            if (body.containsKey("content"))
                h.setContent(body.get("content") != null ? body.get("content").toString() : null);
            if (body.containsKey("orderIndex") && body.get("orderIndex") != null)
                h.setOrderIndex(Integer.parseInt(body.get("orderIndex").toString()));
            h.setUpdatedAt(Instant.now());
            return ResponseEntity.ok(huisregelRepository.save(h));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (!huisregelRepository.existsById(id)) return ResponseEntity.notFound().build();
        huisregelRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
