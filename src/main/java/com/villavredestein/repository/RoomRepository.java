package com.villavredestein.repository;

import com.villavredestein.model.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {

    boolean existsByNameIgnoreCase(String name);

    Optional<Room> findByNameIgnoreCase(String name);

    List<Room> findAllByOrderByIdAsc();
}