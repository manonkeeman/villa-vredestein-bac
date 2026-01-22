package com.villavredestein.repository;

import com.villavredestein.model.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {

    List<Room> findAllByOrderByIdAsc();

    Optional<Room> findByNameIgnoreCase(String name);

    Optional<Room> findByOccupant_Id(Long occupantId);
}